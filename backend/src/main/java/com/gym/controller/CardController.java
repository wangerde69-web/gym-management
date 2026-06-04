package com.gym.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gym.config.AuthHelper;
import com.gym.entity.Card;
import com.gym.entity.CardTypeEntity;
import com.gym.service.CardService;
import com.gym.service.CardTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/* 会员卡控制器，处理购卡、支付确认、审批、退款及导出等业务流程 */
@RestController
@RequestMapping("/api/card")
public class CardController {

    @Autowired private CardService cardService;
    @Autowired private CardTypeService cardTypeService;
    @Autowired private AuthHelper auth;

    // 会员购卡：根据卡种创建待审核的会员卡记录
    @PostMapping("/buy")
    public Map<String, Object> buy(@RequestBody Map<String, Object> params, @RequestHeader(value = "Authorization", required = false) String token) {
        Integer userId = auth.extractUserId(token);
        if (userId == null) return auth.unauthorized();
        if (params.get("cardType") == null || params.get("cardType").toString().trim().isEmpty()) {
            return auth.resp(400, "请选择卡种");
        }
        Card card = cardService.buyCard(userId, params.get("cardType").toString());
        Map<String, Object> result = auth.ok();
        result.put("cardId", card.getId());
        result.put("msg", "请完成支付");
        return result;
    }

    // 会员确认已支付，提交管理员审核
    @PostMapping("/confirm-payment/{id}")
    public Map<String, Object> confirmPayment(@PathVariable Integer id, @RequestHeader(value = "Authorization", required = false) String token) {
        Integer userId = auth.extractUserId(token);
        if (userId == null) return auth.unauthorized();
        cardService.confirmPayment(id, userId);
        return auth.resp(200, "已提交，等待管理员审核");
    }

    // 会员取消支付（标记为未支付状态）
    @PostMapping("/reject-payment/{id}")
    public Map<String, Object> rejectPayment(@PathVariable Integer id, @RequestHeader(value = "Authorization", required = false) String token) {
        Integer userId = auth.extractUserId(token);
        if (userId == null) return auth.unauthorized();
        cardService.rejectPayment(id, userId);
        return auth.resp(200, "支付已取消");
    }

    // 管理员审批通过会员卡，激活卡并计算有效期
    @PutMapping("/approve/{id}")
    public Map<String, Object> approve(@PathVariable Integer id) {
        cardService.approveCard(id);
        return auth.resp(200, "已审批通过");
    }

    // 管理员确认会员卡未支付状态
    @PutMapping("/confirm-unpaid/{id}")
    public Map<String, Object> confirmUnpaid(@PathVariable Integer id) {
        cardService.confirmUnpaid(id);
        return auth.resp(200, "已确认未支付");
    }

    // 查询当前会员的会员卡列表（排除已取消和未支付已删除的记录）
    @GetMapping("/my")
    public Map<String, Object> myList(@RequestHeader("Authorization") String token) {
        Integer userId = auth.extractUserId(token);
        QueryWrapper<Card> wrapper = new QueryWrapper<>();
        wrapper.eq("member_id", userId).notIn("status", 4, 5).orderByDesc("create_time");
        return auth.ok(cardService.list(wrapper));
    }

    // 会员申请退款
    @PostMapping("/refund/{id}")
    public Map<String, Object> refund(@PathVariable Integer id, @RequestHeader(value = "Authorization", required = false) String token) {
        Integer userId = auth.extractUserId(token);
        if (userId == null) return auth.unauthorized();
        cardService.refundCard(id, userId);
        return auth.ok();
    }

    // 会员确认退款完成
    @PostMapping("/confirm-refund/{id}")
    public Map<String, Object> confirmRefund(@PathVariable Integer id, @RequestHeader(value = "Authorization", required = false) String token) {
        Integer userId = auth.extractUserId(token);
        if (userId == null) return auth.unauthorized();
        cardService.confirmRefund(id, userId);
        return auth.ok();
    }

    // 查询全部会员卡记录并联表获取会员姓名（后台管理）
    @GetMapping("/list")
    public Map<String, Object> list() {
        List<Card> cards = cardService.selectListWithDetail();
        Map<String, Object> result = auth.ok(cards);
        result.put("total", cards.size());
        return result;
    }

    // 删除会员卡记录并重置自增 ID
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Integer id) {
        cardService.deleteAndResetId(id);
        return auth.ok();
    }

    // 导出会员卡记录为 CSV 文件（含 BOM 标记以兼容 Excel 中文显示）
    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        List<Card> cards = cardService.selectListWithDetail();
        List<CardTypeEntity> cardTypes = cardTypeService.list();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {

            writer.write('\ufeff'); // Excel UTF-8 的 BOM 标记
            writer.write("卡ID,会员ID,卡种,价格,状态,过期时间,创建时间\n");

            for (Card c : cards) {
                String cardName = cardTypes.stream()
                        .filter(ct -> ct.getCardKey().equals(c.getCardTypeKey()))
                        .findFirst().map(CardTypeEntity::getCardName).orElse(c.getCardTypeKey());
                String status = "";
                switch (c.getStatus()) {
                    case 0: status = "待审核"; break;
                    case 1: status = "生效中"; break;
                    case 2: status = "已过期"; break;
                    case 3: status = "已退款"; break;
                    case 4: status = "未支付"; break;
                    case 5: status = "未支付待审"; break;
                }
                writer.write(String.format("%d,%d,%s,%.2f,%s,%s,%s\n",
                        c.getId(), c.getMemberId(), cardName, c.getPrice(), status,
                        c.getEndDate() != null ? c.getEndDate().toString() : "",
                        c.getCreateTime() != null ? c.getCreateTime().toString() : ""));
            }
            writer.flush();

            byte[] bytes = baos.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
            headers.setContentDispositionFormData("attachment", "card_records.csv");
            headers.setContentLength(bytes.length);
            return ResponseEntity.ok().headers(headers).body(bytes);

        } catch (Exception e) {
            throw new RuntimeException("导出失败", e);
        }
    }
}

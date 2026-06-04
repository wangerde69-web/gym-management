package com.gym.controller;

import com.gym.config.AuthHelper;
import com.gym.service.CardTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

/* 卡种管理控制器，通过原生 SQL 操作卡种配置的增删改查 */
@RestController
@RequestMapping("/api/cardtype")
public class CardTypeController {

    private static final String SELECT_COLS = "SELECT id, card_name AS cardName, card_key AS cardKey, price, validity_days AS validityDays, validity_unit AS validityUnit, cover_image AS coverImage, status, sort_order AS sortOrder FROM card_type";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CardTypeService cardTypeService;

    @Autowired
    private AuthHelper auth;

    // 查询全部卡种列表（按排序值升序，含禁用）
    @GetMapping("/list")
    public Map<String, Object> list() {
        return auth.ok(jdbcTemplate.queryForList(SELECT_COLS + " ORDER BY sort_order ASC"));
    }

    // 查询所有启用状态的卡种（前台展示用）
    @GetMapping("/all")
    public Map<String, Object> all() {
        return auth.ok(jdbcTemplate.queryForList(SELECT_COLS + " WHERE status = 1 ORDER BY sort_order ASC"));
    }

    // 根据 ID 查询单个卡种详情
    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Integer id) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(SELECT_COLS + " WHERE id = ?", id);
        return auth.ok(rows.isEmpty() ? null : rows.get(0));
    }

    // 新增卡种配置
    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Map<String, Object> body) {
        try {
            jdbcTemplate.update("INSERT INTO card_type (card_name, card_key, price, validity_days, validity_unit, cover_image, status, sort_order) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                body.get("cardName"), body.get("cardKey"),
                body.get("price") != null ? new BigDecimal(body.get("price").toString()) : BigDecimal.ZERO,
                body.get("validityDays"), body.get("validityUnit") != null ? body.get("validityUnit") : "DAY",
                body.get("coverImage") != null ? body.get("coverImage") : "",
                body.get("status") != null ? body.get("status") : 1,
                body.get("sortOrder") != null ? body.get("sortOrder") : 0);
            return auth.resp(200, "添加成功");
        } catch (Exception e) {
            return auth.resp(500, "添加失败: " + e.getMessage());
        }
    }

    // 更新卡种配置信息
    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody Map<String, Object> body) {
        try {
            jdbcTemplate.update("UPDATE card_type SET card_name=?, card_key=?, price=?, validity_days=?, validity_unit=?, cover_image=?, status=?, sort_order=? WHERE id=?",
                body.get("cardName"), body.get("cardKey"),
                body.get("price") != null ? new BigDecimal(body.get("price").toString()) : BigDecimal.ZERO,
                body.get("validityDays"), body.get("validityUnit") != null ? body.get("validityUnit") : "DAY",
                body.get("coverImage") != null ? body.get("coverImage") : "",
                body.get("status"), body.get("sortOrder"), body.get("id"));
            return auth.resp(200, "更新成功");
        } catch (Exception e) {
            return auth.resp(500, "更新失败: " + e.getMessage());
        }
    }

    // 删除卡种并重置自增 ID
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Integer id) {
        try {
            cardTypeService.deleteAndResetId(id);
            return auth.resp(200, "删除成功");
        } catch (Exception e) {
            return auth.resp(500, "删除失败: " + e.getMessage());
        }
    }
}

package com.gym.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gym.config.AuthHelper;
import com.gym.entity.CardTypeEntity;
import com.gym.service.CardTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Set;

/* 卡种管理控制器，使用 MyBatis-Plus ORM 替代 JdbcTemplate 原生 SQL */
@RestController
@RequestMapping("/api/cardtype")
public class CardTypeController {

    private static final Set<String> VALID_UNITS = Set.of("DAY", "MONTH", "YEAR");

    @Autowired
    private CardTypeService cardTypeService;

    @Autowired
    private AuthHelper auth;

    // 查询全部卡种列表（按排序值升序，含禁用）
    @GetMapping("/list")
    public Map<String, Object> list() {
        return auth.ok(cardTypeService.list(new QueryWrapper<CardTypeEntity>().orderByAsc("sort_order")));
    }

    // 查询所有启用状态的卡种（前台展示用）
    @GetMapping("/all")
    public Map<String, Object> all() {
        return auth.ok(cardTypeService.list(new QueryWrapper<CardTypeEntity>().eq("status", 1).orderByAsc("sort_order")));
    }

    // 根据 ID 查询单个卡种详情
    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Integer id) {
        return auth.ok(cardTypeService.getById(id));
    }

    // 新增卡种配置
    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody CardTypeEntity cardType) {
        try {
            if (cardType.getValidityUnit() != null && !VALID_UNITS.contains(cardType.getValidityUnit())) {
                return auth.resp(400, "有效期单位仅支持: DAY(天), MONTH(月), YEAR(年)");
            }
            cardTypeService.save(cardType);
            return auth.resp(200, "添加成功");
        } catch (Exception e) {
            return auth.resp(500, "添加失败: " + e.getMessage());
        }
    }

    // 更新卡种配置信息
    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody CardTypeEntity cardType) {
        try {
            if (cardType.getValidityUnit() != null && !VALID_UNITS.contains(cardType.getValidityUnit())) {
                return auth.resp(400, "有效期单位仅支持: DAY(天), MONTH(月), YEAR(年)");
            }
            cardTypeService.updateById(cardType);
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

package com.gym.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gym.config.AuthHelper;
import com.gym.entity.Equipment;
import com.gym.service.EquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/* 健身器材管理控制器，提供器材的增删改查及分类查询接口 */
@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private AuthHelper auth;

    // 查询全部器材列表（按创建时间倒序，仅启用状态）
    @GetMapping("/list")
    public Map<String, Object> list() {
        QueryWrapper<Equipment> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).orderByDesc("create_time");
        return auth.ok(equipmentService.list(wrapper));
    }

    // 查询全部器材（含禁用）
    @GetMapping("/all")
    public Map<String, Object> all() {
        return auth.ok(equipmentService.list());
    }

    // 根据 ID 查询器材详情
    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Integer id) {
        return auth.ok(equipmentService.getById(id));
    }

    // 新增器材
    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Equipment equipment) {
        equipmentService.save(equipment);
        return auth.resp(200, "添加成功");
    }

    // 更新器材信息
    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody Equipment equipment) {
        equipmentService.updateById(equipment);
        return auth.resp(200, "更新成功");
    }

    // 删除器材并重置自增 ID
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Integer id) {
        equipmentService.deleteAndResetId(id);
        return auth.resp(200, "删除成功");
    }

    // 查询所有启用器材的分类列表（去重）
    @GetMapping("/categories")
    public Map<String, Object> categories() {
        QueryWrapper<Equipment> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).isNotNull("category").ne("category", "");
        wrapper.select("DISTINCT category");
        return auth.ok(equipmentService.listObjs(wrapper));
    }
}

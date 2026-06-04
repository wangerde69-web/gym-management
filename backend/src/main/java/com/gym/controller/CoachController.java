package com.gym.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gym.config.AuthHelper;
import com.gym.entity.Coach;
import com.gym.service.CoachService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/* 教练管理控制器，提供教练信息的增删改查接口 */
@RestController
@RequestMapping("/api/coach")
public class CoachController {

    @Autowired
    private CoachService coachService;

    @Autowired
    private AuthHelper auth;

    // 查询启用状态的教练列表（按创建时间倒序）
    @GetMapping("/list")
    public Map<String, Object> list() {
        QueryWrapper<Coach> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).orderByDesc("create_time");
        return auth.ok(coachService.list(wrapper));
    }

    // 查询全部教练（含禁用）
    @GetMapping("/all")
    public Map<String, Object> all() {
        return auth.ok(coachService.list());
    }

    // 根据 ID 查询教练详情
    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Integer id) {
        return auth.ok(coachService.getById(id));
    }

    // 新增教练信息
    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Coach coach) {
        coachService.save(coach);
        return auth.resp(200, "添加成功");
    }

    // 更新教练信息
    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody Coach coach) {
        boolean ok = coachService.updateById(coach);
        return auth.resp(200, ok ? "更新成功" : "更新失败");
    }

    // 删除教练并重置自增 ID
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Integer id) {
        coachService.deleteAndResetId(id);
        return auth.resp(200, "删除成功");
    }
}

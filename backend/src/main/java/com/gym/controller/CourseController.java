package com.gym.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gym.config.AuthHelper;
import com.gym.entity.Course;
import com.gym.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/* 课程管理控制器，提供课程的增删改查接口 */
@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private AuthHelper auth;

    // 查询启用状态的课程列表（按创建时间倒序）
    @GetMapping("/list")
    public Map<String, Object> list() {
        QueryWrapper<Course> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).orderByDesc("create_time");
        return auth.ok(courseService.list(wrapper));
    }

    // 查询全部课程（含禁用）
    @GetMapping("/all")
    public Map<String, Object> all() {
        return auth.ok(courseService.list());
    }

    // 根据 ID 查询课程详情
    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Integer id) {
        return auth.ok(courseService.getById(id));
    }

    // 新增课程
    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Course course) {
        courseService.save(course);
        return auth.resp(200, "添加成功");
    }

    // 更新课程信息
    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody Course course) {
        courseService.updateById(course);
        return auth.resp(200, "更新成功");
    }

    // 删除课程并重置自增 ID
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Integer id) {
        courseService.deleteAndResetId(id);
        return auth.resp(200, "删除成功");
    }
}

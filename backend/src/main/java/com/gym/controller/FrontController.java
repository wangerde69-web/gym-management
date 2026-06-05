package com.gym.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gym.config.AuthHelper;
import com.gym.entity.*;
import com.gym.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/* 前台公开接口控制器，提供首页聚合数据与详情查询（无需登录） */
@RestController
@RequestMapping("/api/front")
public class FrontController {

    @Autowired
    private BannerService bannerService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CoachService coachService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private GymConfigService gymConfigService;

    @Autowired
    private AuthHelper auth;

    // 首页聚合接口：一次返回轮播图、课程、教练、器材、统计数据及系统配置
    @GetMapping("/index")
    public Map<String, Object> index() {
        QueryWrapper<Banner> bannerWrapper = new QueryWrapper<>();
        bannerWrapper.eq("status", 1).orderByAsc("sort");

        QueryWrapper<Course> courseWrapper = new QueryWrapper<>();
        courseWrapper.eq("status", 1).orderByDesc("create_time");

        QueryWrapper<Coach> coachWrapper = new QueryWrapper<>();
        coachWrapper.eq("status", 1).orderByDesc("create_time");

        QueryWrapper<Equipment> equipmentWrapper = new QueryWrapper<>();
        equipmentWrapper.eq("status", 1).orderByDesc("create_time");

        Map<String, Object> result = auth.ok();
        result.put("memberCount", memberService.count());
        result.put("courseCount", courseService.count(new QueryWrapper<Course>().eq("status", 1)));
        result.put("coachCount", coachService.count(new QueryWrapper<Coach>().eq("status", 1)));
        result.put("banners", bannerService.list(bannerWrapper));
        result.put("courses", courseService.list(courseWrapper));
        result.put("coaches", coachService.list(coachWrapper));
        result.put("equipments", equipmentService.list(equipmentWrapper));
        result.put("config", gymConfigService.getConfigMap());
        return result;
    }

    // 查询课程详情，并联表查询关联教练信息
    @GetMapping("/course/{id}")
    public Map<String, Object> courseDetail(@PathVariable Integer id) {
        Course course = courseService.getById(id);
        Map<String, Object> data = new HashMap<>();
        data.put("course", course);
        if (course != null && course.getCoachId() != null) {
            data.put("coach", coachService.getById(course.getCoachId()));
        }
        return auth.ok(data);
    }

    // 查询教练详情
    @GetMapping("/coach/{id}")
    public Map<String, Object> coachDetail(@PathVariable Integer id) {
        return auth.ok(coachService.getById(id));
    }

    // 公开获取收款码 URL（支付弹窗使用，无需认证）
    @GetMapping("/pay-qr")
    public Map<String, Object> payQr() {
        Map<String, String> config = gymConfigService.getConfigMap();
        String qrUrl = config.getOrDefault("pay_qr", "");
        Map<String, Object> data = new HashMap<>();
        data.put("payQr", qrUrl);
        return auth.ok(data);
    }
}

package com.gym.controller;

import com.gym.config.AuthHelper;
import com.gym.entity.Admin;
import com.gym.service.AdminService;
import com.gym.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/* 管理员控制器，处理管理员登录与信息获取 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthHelper auth;

    // 管理员登录：校验用户名密码并返回 JWT 令牌
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        try {
            String token = adminService.login(params.get("username"), params.get("password"));
            Map<String, Object> result = auth.ok();
            result.put("token", token);
            result.put("role", "admin");
            return result;
        } catch (Exception e) {
            return auth.resp(500, e.getMessage());
        }
    }

    // 获取当前登录管理员信息
    @GetMapping("/info")
    public Map<String, Object> info(@RequestHeader("Authorization") String token) {
        try {
            token = token.substring(7);
            Integer userId = jwtUtil.getUserId(token);
            Admin admin = adminService.getInfo(userId);
            return auth.ok(admin);
        } catch (Exception e) {
            return auth.resp(500, e.getMessage());
        }
    }
}

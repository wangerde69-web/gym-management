package com.gym.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

/* 认证辅助工具类，提供 JWT 令牌解析及统一响应构造 */
@Component
public class AuthHelper {

    @Autowired
    private JwtUtil jwtUtil;

    // 从 Authorization 请求头中提取并验证 JWT，返回用户 ID
    public Integer extractUserId(String token) {
        if (token == null || !token.startsWith("Bearer ")) return null;
        String raw = token.substring(7);
        if (!jwtUtil.validateToken(raw)) return null;
        return jwtUtil.getUserId(raw);
    }

    // 统一响应构造方法 —— 401 未登录
    public Map<String, Object> unauthorized() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("msg", "未登录");
        return result;
    }

    // 统一响应构造方法 —— 403 无权操作
    public Map<String, Object> forbidden() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 403);
        result.put("msg", "无权操作");
        return result;
    }

    // 统一响应构造方法 —— 200 操作成功（无数据体）
    public Map<String, Object> ok() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "操作成功");
        return result;
    }

    // 统一响应构造方法 —— 200 操作成功（携带数据体）
    public Map<String, Object> ok(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "操作成功");
        result.put("data", data);
        return result;
    }

    // 统一响应构造方法 —— 自定义 code + msg
    public Map<String, Object> resp(int code, String msg) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("msg", msg);
        return result;
    }
}

package com.gym.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.Admin;

/* 管理员服务接口，定义管理员登录 */
public interface AdminService extends IService<Admin> {
    String login(String username, String password);
}

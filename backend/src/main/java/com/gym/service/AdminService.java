package com.gym.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.Admin;

/* 管理员服务接口，定义管理员登录、信息查询与删除操作 */
public interface AdminService extends IService<Admin> {
    String login(String username, String password);
    Admin getInfo(Integer id);
    void deleteAndResetId(Integer id);
}

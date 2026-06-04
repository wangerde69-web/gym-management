package com.gym.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.Member;

/* 会员服务接口，定义会员登录、信息查询与删除操作 */
public interface MemberService extends IService<Member> {
    Member login(String username, String password);
    Member getInfo(Integer id);
    void deleteAndResetId(Integer id);
}

package com.gym.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.Member;
import java.util.Map;

/* 会员服务接口，定义会员登录、信息查询与删除操作 */
public interface MemberService extends IService<Member> {
    Map<String, Object> login(String username, String password);
    void deleteAndResetId(Integer id);
}

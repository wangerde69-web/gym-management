package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.Member;
import com.gym.mapper.MemberMapper;
import com.gym.service.MemberService;
import com.gym.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/* 会员服务实现类，处理会员登录认证、信息查询与删除 */
@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ResetIdUtil resetIdUtil;

    @Autowired
    private JwtUtil jwtUtil;

    // 会员登录：根据用户名查询并使用 BCrypt 校验密码，成功后返回 JWT 令牌及会员基本信息
    @Override
    public Map<String, Object> login(String username, String password) {
        QueryWrapper<Member> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        Member member = this.getOne(wrapper);
        if (member == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        String token = jwtUtil.generateToken(member.getId(), "member");
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("memberId", member.getId());
        result.put("username", member.getUsername());
        result.put("name", member.getName());
        result.put("imageUrl", member.getAvatar());
        result.put("role", "member");
        return result;
    }

    // 删除会员并重置自增 ID（不级联删除关联的卡和预约记录）
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteAndResetId(Integer id) {
        resetIdUtil.deleteAndReset(id, "member");
    }
}
package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.Card;
import com.gym.entity.Member;
import com.gym.mapper.MemberMapper;
import com.gym.service.CardService;
import com.gym.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/* 会员服务实现类，处理会员登录认证、信息查询与删除 */
@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ResetIdUtil resetIdUtil;

    @Autowired
    private CardService cardService;

    // 会员登录：根据用户名查询并使用 BCrypt 校验密码
    @Override
    public Member login(String username, String password) {
        QueryWrapper<Member> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        Member member = this.getOne(wrapper);
        if (member == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        return member;
    }

    @Override
    public Member getInfo(Integer id) {
        return this.getById(id);
    }

    // 删除会员并级联清理关联的卡片记录，最后重置自增 ID
    @Override
    public void deleteAndResetId(Integer id) {
        cardService.remove(new QueryWrapper<Card>().eq("member_id", id));
        resetIdUtil.deleteAndReset(id, "member");
    }
}
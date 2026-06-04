package com.gym.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gym.config.AuthHelper;
import com.gym.config.ExcelUtil;
import com.gym.entity.Member;
import com.gym.service.MemberService;
import com.gym.config.JwtUtil;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/* 会员控制器，处理会员注册、登录、信息管理、列表查询及数据导出 */
@RestController
@RequestMapping("/api/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthHelper auth;

    // 会员登录：校验用户名密码并返回 JWT 令牌及会员基本信息
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        try {
            Member member = memberService.login(params.get("username"), params.get("password"));
            String token = jwtUtil.generateToken(member.getId(), "member");
            Map<String, Object> result = auth.ok();
            result.put("token", token);
            result.put("memberId", member.getId());
            result.put("username", member.getUsername());
            result.put("name", member.getName());
            result.put("imageUrl", member.getAvatar());
            result.put("role", "member");
            return result;
        } catch (Exception e) {
            return auth.resp(500, e.getMessage());
        }
    }

    // 会员注册：校验用户名唯一性，密码加密后保存
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, Object> params) {
        String username = (String) params.get("username");
        String password = (String) params.get("password");
        String name = (String) params.get("name");
        String phone = (String) params.get("phone");
        String avatar = (String) params.get("avatar");
        Object genderObj = params.get("gender");
        Object ageObj = params.get("age");

        QueryWrapper<Member> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        if (memberService.getOne(wrapper) != null) return auth.resp(500, "用户名已存在");

        Member member = new Member();
        member.setUsername(username);
        member.setPassword(passwordEncoder.encode(password));
        member.setName(name);
        member.setPhone(phone);
        member.setAvatar(avatar);
        if (genderObj instanceof Number) member.setGender(((Number) genderObj).intValue());
        if (ageObj instanceof Number) member.setAge(((Number) ageObj).intValue());
        memberService.save(member);
        return auth.resp(200, "注册成功");
    }

    // 获取当前登录会员的详细信息
    @GetMapping("/info")
    public Map<String, Object> info(@RequestHeader("Authorization") String token) {
        try {
            token = token.substring(7);
            Integer userId = jwtUtil.getUserId(token);
            return auth.ok(memberService.getInfo(userId));
        } catch (Exception e) {
            return auth.resp(500, e.getMessage());
        }
    }

    // 查询全部会员列表（后台管理）
    @GetMapping("/all")
    public Map<String, Object> all() {
        return auth.ok(memberService.list());
    }

    // 更新会员信息：若传入密码则加密，否则保留原密码
    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody Member member) {
        if (member.getPassword() != null && !member.getPassword().isEmpty()) {
            member.setPassword(passwordEncoder.encode(member.getPassword()));
        } else {
            Member old = memberService.getById(member.getId());
            if (old != null) member.setPassword(old.getPassword());
        }
        memberService.updateById(member);
        return auth.resp(200, "更新成功");
    }

    // 删除会员并重置自增 ID
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Integer id) {
        memberService.deleteAndResetId(id);
        return auth.resp(200, "删除成功");
    }

    // 导出会员列表为 Excel 文件
    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        try {
            List<Member> list = memberService.list();
            String[] headers = {"编号", "姓名", "用户名", "手机号", "性别", "年龄", "状态", "注册时间"};
            Workbook workbook = ExcelUtil.start(response, "会员信息", "会员信息", headers);
            Sheet sheet = workbook.getSheetAt(0);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < list.size(); i++) {
                Member m = list.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(m.getId());
                row.createCell(1).setCellValue(m.getName() != null ? m.getName() : "");
                row.createCell(2).setCellValue(m.getUsername() != null ? m.getUsername() : "");
                row.createCell(3).setCellValue(m.getPhone() != null ? m.getPhone() : "");
                row.createCell(4).setCellValue(m.getGender() != null && m.getGender() == 1 ? "男" : "女");
                row.createCell(5).setCellValue(m.getAge() != null ? m.getAge().doubleValue() : 0);
                row.createCell(6).setCellValue(m.getStatus() != null && m.getStatus() == 1 ? "正常" : "停用");
                row.createCell(7).setCellValue(m.getCreateTime() != null ? m.getCreateTime().format(formatter) : "");
            }
            ExcelUtil.finish(workbook, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

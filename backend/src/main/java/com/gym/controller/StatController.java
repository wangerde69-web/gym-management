package com.gym.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gym.config.AuthHelper;
import com.gym.entity.*;
import com.gym.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/* 统计报表控制器，提供仪表盘概览、课程预约统计及年度收入分析 */
@RestController
@RequestMapping("/api/stat")
public class StatController {

    @Autowired
    private CourseService courseService;
    @Autowired
    private CoachService coachService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private EquipmentService equipmentService;
    @Autowired
    private CardService cardService;
    @Autowired
    private AuthHelper auth;

    // 仪表盘概览数据：办卡人数、教练数、预约数、器材数、已确认预约数
    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        Map<String, Object> data = new HashMap<>();
        // 办卡人数：统计会员卡表中去重的 memberId
        long cardMemberCount = cardService.lambdaQuery()
                .select(Card::getMemberId)
                .notIn(Card::getStatus, 3, 4, 5)
                .list().stream().map(Card::getMemberId).distinct().count();     
        data.put("cardMemberCount", cardMemberCount);
        data.put("courseCount", courseService.count());
        data.put("coachCount", coachService.count());
        data.put("bookingCount", bookingService.count());
        data.put("equipmentCount", equipmentService.count());
        data.put("confirmedBooking", bookingService.lambdaQuery().eq(Booking::getStatus, 1).count());
        return auth.ok(data);
    }

    // 各课程预约数量统计，使用 GROUP BY 一次查询替代 N+1 循环
    @GetMapping("/courseBooking")
    public Map<String, Object> courseBooking() {
        List<Map<String, Object>> data = new ArrayList<>();
        List<Course> courses = courseService.list();
        // 一次查询获取各课程的预约数
        Map<Integer, Long> countMap = bookingService.listMaps(
                new QueryWrapper<Booking>().select("course_id", "COUNT(*) AS cnt").groupBy("course_id")
        ).stream().collect(Collectors.toMap(
                m -> ((Number) m.get("course_id")).intValue(),
                m -> ((Number) m.get("cnt")).longValue(),
                (a, b) -> a
        ));
        for (Course c : courses) {
            Map<String, Object> item = new HashMap<>();
            item.put("courseName", c.getName());
            item.put("count", countMap.getOrDefault(c.getId(), 0L));
            data.add(item);
        }
        return auth.ok(data);
    }

    // 年度收入统计：按月汇总会员卡销售额，排除退款与未支付记录
    @GetMapping("/income")
    public Map<String, Object> income(@RequestParam(required = false) Integer year) {
        try {
            final int y = (year != null) ? year : LocalDate.now().getYear();    
            List<String> months = new ArrayList<>(Arrays.asList("1月","2月","3月","4月","5月","6月","7月","8月","9月","10月","11月","12月"));
            List<Integer> incomeList = new ArrayList<>();
            LocalDateTime yearStart = LocalDateTime.of(y, 1, 1, 0, 0, 0);       
            LocalDateTime yearEnd = LocalDateTime.of(y, 12, 31, 23, 59, 59);    
            // 查询该年度内有效会员卡记录（排除已退款、未支付、未支付待审状态） 
            QueryWrapper<Card> incomeWrapper = new QueryWrapper<>();
            incomeWrapper.isNotNull("create_time")
                .between("create_time", yearStart, yearEnd)
                .notIn("status", 3, 4, 5);
            List<Card> cards = cardService.list(incomeWrapper);
            // 按月份汇总收入金额
            Map<Integer, BigDecimal> monthlyIncome = new HashMap<>();
            for (Card card : cards) {
                if (card.getPrice() != null && card.getCreateTime() != null) {  
                    int month = card.getCreateTime().getMonthValue();
                    monthlyIncome.merge(month, card.getPrice(), BigDecimal::add);
                }
            }
            // 构造 1-12 月的收入列表，无数据的月份补零
            for (int m = 1; m <= 12; m++) {
                incomeList.add(monthlyIncome.containsKey(m) ? monthlyIncome.get(m).intValue() : 0);
            }
            return auth.ok(Map.of("months", months, "income", incomeList));
        } catch (Exception e) {
            return auth.resp(500, e.getClass().getName() + ": " + e.getMessage());
        }
    }
}

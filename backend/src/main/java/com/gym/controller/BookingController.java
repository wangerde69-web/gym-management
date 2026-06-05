package com.gym.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.config.AuthHelper;
import com.gym.config.ExcelUtil;
import com.gym.entity.Booking;
import com.gym.service.BookingService;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/* 预约管理控制器，处理课程预约的增删改查、状态流转及数据导出 */
@RestController
@RequestMapping("/api/booking")
public class BookingController {

    @Autowired private BookingService bookingService;
    @Autowired private AuthHelper auth;

    // 分页查询预约列表（联表获取关联详情，替代 N+1 查询）
    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Booking> pager = new Page<>(page, pageSize);
        Page<Booking> pageResult = bookingService.pageWithDetail(pager);
        Map<String, Object> result = auth.ok(pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    // 查询全部预约记录（含联表详情），用于后台管理
    @GetMapping("/all")
    public Map<String, Object> all() {
        return auth.ok(bookingService.listWithDetail());
    }

    // 查询当前登录会员自己的预约列表
    @GetMapping("/my")
    public Map<String, Object> myList(@RequestHeader(value = "Authorization", required = false) String token) {
        Integer userId = auth.extractUserId(token);
        if (userId == null) return auth.unauthorized();
        return auth.ok(bookingService.listByMemberIdWithDetail(userId));
    }

    // 新增预约：校验课程、日期、时间等必填字段后保存
    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Map<String, Object> body, @RequestHeader(value = "Authorization", required = false) String token) {
        Integer userId = auth.extractUserId(token);
        if (userId == null) return auth.unauthorized();
        Object courseIdObj = body.get("courseId");
        if (courseIdObj == null) return auth.resp(400, "请选择课程");
        String bookingTime = body.get("bookingTime") == null ? null : body.get("bookingTime").toString();
        String bookingDate = body.get("bookingDate") == null ? null : body.get("bookingDate").toString();
        if (bookingTime == null || bookingTime.trim().isEmpty()) return auth.resp(400, "请选择预约时间");
        if (bookingTime.contains("-")) {
            try {
                String endTimeStr = bookingTime.split("-")[1].trim();
                LocalTime endTime = LocalTime.parse(endTimeStr);
                if (LocalTime.now().isAfter(endTime)) return auth.resp(400, "该时间段已过，请选择其他时间");
            } catch (Exception ignored) {}
        }
        Booking booking = new Booking();
        booking.setMemberId(userId);
        if (courseIdObj instanceof Number) booking.setCourseId(((Number) courseIdObj).intValue());
        // 若同时传了 bookingDate，则与 bookingTime 拼接为完整时间字符串供后端持久化
        booking.setBookingTime(bookingDate != null && !bookingDate.isEmpty() ? bookingDate + " " + bookingTime : bookingTime);
        booking.setStatus(0);
        bookingService.save(booking);
        return auth.ok();
    }

    // 会员修改自己的预约信息
    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody Booking booking, @RequestHeader(value = "Authorization", required = false) String token) {
        Integer userId = auth.extractUserId(token);
        if (userId == null) return auth.unauthorized();
        Booking existing = bookingService.getById(booking.getId());
        if (existing == null || !existing.getMemberId().equals(userId)) return auth.forbidden();
        bookingService.updateById(booking);
        return auth.ok();
    }

    // 会员确认预约（将状态改为已确认）
    @PutMapping("/confirm/{id}")
    public Map<String, Object> confirm(@PathVariable Integer id, @RequestHeader(value = "Authorization", required = false) String token) {
        Integer userId = auth.extractUserId(token);
        if (userId == null) return auth.unauthorized();
        Booking existing = bookingService.getById(id);
        if (existing == null || !existing.getMemberId().equals(userId)) return auth.forbidden();
        existing.setStatus(1);
        bookingService.updateById(existing);
        return auth.ok();
    }

    // 管理员审批预约（后台操作，无需校验归属）
    @PutMapping("/approve/{id}")
    public Map<String, Object> approve(@PathVariable Integer id) {
        Booking existing = bookingService.getById(id);
        if (existing == null) return auth.resp(500, "预约不存在");
        existing.setStatus(1);
        bookingService.updateById(existing);
        return auth.ok();
    }

    // 会员取消预约（状态置为已取消）
    @PutMapping("/cancel/{id}")
    public Map<String, Object> cancel(@PathVariable Integer id, @RequestHeader(value = "Authorization", required = false) String token) {
        Integer userId = auth.extractUserId(token);
        if (userId == null) return auth.unauthorized();
        Booking existing = bookingService.getById(id);
        if (existing == null || !existing.getMemberId().equals(userId)) return auth.forbidden();
        existing.setStatus(2);
        bookingService.updateById(existing);
        return auth.ok();
    }

    // 会员确认退款（复用取消逻辑，状态置为已取消）
    @PostMapping("/confirm-refund/{id}")
    public Map<String, Object> confirmRefund(@PathVariable Integer id, @RequestHeader(value = "Authorization", required = false) String token) {
        return cancel(id, token);
    }

    // 删除预约记录并重置自增 ID
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Integer id) {
        bookingService.deleteAndResetId(id);
        return auth.ok();
    }

    // 导出预约记录为 Excel 文件
    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        try {
            List<Booking> list = bookingService.listWithDetail();
            String[] headers = {"编号", "会员姓名", "课程名称", "教练", "预约时间", "状态", "备注"};
            Workbook workbook = ExcelUtil.start(response, "预约记录", "预约记录", headers);
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 0; i < list.size(); i++) {
                Booking b = list.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(b.getId());
                row.createCell(1).setCellValue(b.getMemberName());
                row.createCell(2).setCellValue(b.getCourseName());
                row.createCell(3).setCellValue(b.getCoachName() != null ? b.getCoachName() : "");
                row.createCell(4).setCellValue(b.getBookingTime());
                row.createCell(5).setCellValue(switch (b.getStatus()) {
                    case 0 -> "待确认"; case 1 -> "已确认"; case 2 -> "已取消";
                    case 3 -> "未支付"; case 4 -> "已过期"; default -> "未知";
                });
                row.createCell(6).setCellValue(b.getRemark() != null ? b.getRemark() : "");
            }
            ExcelUtil.finish(workbook, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

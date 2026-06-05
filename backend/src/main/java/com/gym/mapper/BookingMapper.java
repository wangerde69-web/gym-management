package com.gym.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.entity.Booking;

import org.apache.ibatis.annotations.Update;
import java.util.List;

/* 预约 Mapper 接口，提供联表查询预约详情的自定义 SQL 方法及过期批量更新 */

public interface BookingMapper extends BaseMapper<Booking> {
    // 联表查询所有预约记录（关联会员、课程、教练表）
    List<Booking> selectListWithDetail();
    // 按会员 ID 联表查询预约记录
    List<Booking> selectListByMemberIdWithDetail(Integer memberId);
    // 分页联表查询预约记录
    Page<Booking> selectPageWithDetail(Page<Booking> page);
    // 批量将已过时间段的待确认/已确认预约标记为已过期
    // 新格式 booking_time="2026-06-10 14:00-15:00"（含日期前缀），旧格式="14:00-15:00"（用 create_time 兜底）
    @Update("UPDATE booking SET status = 4 WHERE status IN (0, 1) AND ("
            + "(booking_time REGEXP '^[0-9]{4}-[0-9]{2}-[0-9]{2}' "
            + " AND STR_TO_DATE(CONCAT(SUBSTRING(booking_time, 1, 10), ' ', "
            + "   SUBSTRING_INDEX(booking_time, '-', -1)), '%Y-%m-%d %H:%i') < NOW())"
            + " OR "
            + "(booking_time NOT REGEXP '^[0-9]{4}-[0-9]{2}-[0-9]{2}' "
            + " AND DATE(create_time) <= CURDATE()"
            + " AND (DATE(create_time) < CURDATE() OR "
            + "   STR_TO_DATE(SUBSTRING_INDEX(booking_time, '-', -1), '%H:%i') <= CURTIME())))")
    void updateExpiredBookings();
}

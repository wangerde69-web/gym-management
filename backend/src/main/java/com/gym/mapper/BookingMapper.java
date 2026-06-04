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
    // 批量将已过时间段的待确认/已确认预约标记为已过期（仅处理今天及之前的预约）
    @Update("UPDATE booking SET status = 4 WHERE status IN (0, 1) AND " +
            "DATE(create_time) <= CURDATE() AND " +
            "(DATE(create_time) < CURDATE() OR " +
            "STR_TO_DATE(SUBSTRING_INDEX(booking_time, '-', -1), '%H:%i') <= CURTIME())")
    void updateExpiredBookings();
}

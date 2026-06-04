package com.gym.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.Booking;
import java.util.List;

/* 预约服务接口，支持联表详情查询与按会员查询 */
public interface BookingService extends IService<Booking> {
    List<Booking> listWithDetail();
    List<Booking> listByMemberIdWithDetail(Integer memberId);
    Page<Booking> pageWithDetail(Page<Booking> page);
    void deleteAndResetId(Integer id);
}

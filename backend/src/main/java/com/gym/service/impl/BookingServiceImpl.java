package com.gym.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.Booking;
import com.gym.mapper.BookingMapper;
import com.gym.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

/* 预约服务实现类，通过自定义 Mapper SQL 实现联表详情查询，包含预约自动过期定时任务 */
@Service
public class BookingServiceImpl extends ServiceImpl<BookingMapper, Booking> implements BookingService {

    @Autowired
    private ResetIdUtil resetIdUtil;

    /* 预约自动过期定时任务：每小时检查一次，将已过时间段的预约标记为已过期 */  
    @Scheduled(cron = "0 0 * * * ?")
    public void autoExpireBookings() {
        baseMapper.updateExpiredBookings();
    }

    @Override
    public List<Booking> listWithDetail() {
        return baseMapper.selectListWithDetail();
    }

    @Override
    public List<Booking> listByMemberIdWithDetail(Integer memberId) {
        return baseMapper.selectListByMemberIdWithDetail(memberId);
    }

    @Override
    public Page<Booking> pageWithDetail(Page<Booking> page) {
        return baseMapper.selectPageWithDetail(page);
    }

    @Override
    public void deleteAndResetId(Integer id) {
        resetIdUtil.deleteAndReset(id, "booking");
    }
}

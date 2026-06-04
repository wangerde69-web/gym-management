package com.gym.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.Coach;
import com.gym.mapper.CoachMapper;
import com.gym.service.CoachService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/* 教练服务实现类 */
@Service
public class CoachServiceImpl extends ServiceImpl<CoachMapper, Coach> implements CoachService {
    @Autowired
    private ResetIdUtil resetIdUtil;

    @Override
    public void deleteAndResetId(Integer id) {
        resetIdUtil.deleteAndReset(id, "coach");
    }
}
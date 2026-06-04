package com.gym.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.CardTypeEntity;
import com.gym.mapper.CardTypeMapper;
import com.gym.service.CardTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/* 卡种配置服务实现类 */
@Service
public class CardTypeServiceImpl extends ServiceImpl<CardTypeMapper, CardTypeEntity> implements CardTypeService {
    @Autowired
    private ResetIdUtil resetIdUtil;

    @Override
    public void deleteAndResetId(Integer id) {
        resetIdUtil.deleteAndReset(id, "card_type");
    }
}
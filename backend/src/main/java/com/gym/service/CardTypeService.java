package com.gym.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.CardTypeEntity;

/* 卡种配置服务接口 */
public interface CardTypeService extends IService<CardTypeEntity> {
    void deleteAndResetId(Integer id);
}

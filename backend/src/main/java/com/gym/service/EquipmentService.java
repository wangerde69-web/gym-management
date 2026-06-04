package com.gym.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.Equipment;

/* 健身器材服务接口 */
public interface EquipmentService extends IService<Equipment> {
    void deleteAndResetId(Integer id);
}

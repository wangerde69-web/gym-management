package com.gym.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.Equipment;
import com.gym.mapper.EquipmentMapper;
import com.gym.service.EquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/* 健身器材服务实现类 */
@Service
public class EquipmentServiceImpl extends ServiceImpl<EquipmentMapper, Equipment> implements EquipmentService {
    @Autowired
    private ResetIdUtil resetIdUtil;

    @Override
    public void deleteAndResetId(Integer id) {
        resetIdUtil.deleteAndReset(id, "equipment");
    }
}
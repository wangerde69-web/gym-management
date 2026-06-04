package com.gym.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.Coach;

/* 教练服务接口 */
public interface CoachService extends IService<Coach> {
    void deleteAndResetId(Integer id);
}

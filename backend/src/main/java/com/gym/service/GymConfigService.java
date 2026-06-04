package com.gym.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.GymConfig;
import java.util.Map;

/* 系统配置服务接口 */
public interface GymConfigService extends IService<GymConfig> {
    Map<String, String> getConfigMap();
}

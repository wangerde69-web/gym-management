package com.gym.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.GymConfig;
import com.gym.mapper.GymConfigMapper;
import com.gym.service.GymConfigService;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

/* 系统配置服务实现类 */
@Service
public class GymConfigServiceImpl extends ServiceImpl<GymConfigMapper, GymConfig> implements GymConfigService {

    @Override
    public Map<String, String> getConfigMap() {
        Map<String, String> map = new HashMap<>();
        for (GymConfig c : this.list()) {
            map.put(c.getConfigKey(), c.getConfigValue());
        }
        return map;
    }
}

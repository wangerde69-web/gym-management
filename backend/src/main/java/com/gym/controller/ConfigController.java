package com.gym.controller;

import com.gym.config.AuthHelper;
import com.gym.entity.GymConfig;
import com.gym.service.GymConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/* 系统配置控制器，管理健身房动态配置项的读取与保存 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Autowired
    private GymConfigService configService;

    @Autowired
    private AuthHelper auth;

    // 查询所有配置项，以键值对 Map 形式返回
    @GetMapping("/all")
    public Map<String, Object> all() {
        return auth.ok(configService.getConfigMap());
    }

    // 批量保存配置项：一次查询已有配置，再批量更新或新增（减少数据库往返次数）
    @Transactional(rollbackFor = Exception.class)
    @PutMapping("/save")
    public Map<String, Object> save(@RequestBody Map<String, String> params) {
        // 一次查出所有已有配置项，避免循环内逐条查询
        Map<String, GymConfig> existing = new HashMap<>();
        configService.list().forEach(c -> existing.put(c.getConfigKey(), c));

        List<GymConfig> toUpdate = new ArrayList<>();
        List<GymConfig> toInsert = new ArrayList<>();

        for (Map.Entry<String, String> e : params.entrySet()) {
            GymConfig c = existing.get(e.getKey());
            if (c != null) {
                c.setConfigValue(e.getValue());
                toUpdate.add(c);
            } else {
                GymConfig nc = new GymConfig();
                nc.setConfigKey(e.getKey());
                nc.setConfigValue(e.getValue());
                toInsert.add(nc);
            }
        }

        if (!toUpdate.isEmpty()) configService.updateBatchById(toUpdate);
        if (!toInsert.isEmpty()) configService.saveBatch(toInsert);
        return auth.resp(200, "保存成功");
    }
}

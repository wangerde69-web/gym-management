package com.gym.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gym.config.AuthHelper;
import com.gym.entity.Banner;
import com.gym.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/* 轮播图控制器，提供轮播图的增删改查与批量排序功能 */
@RestController
@RequestMapping("/api/banner")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @Autowired
    private AuthHelper auth;

    // 查询启用状态的轮播图列表（按排序值升序）
    @GetMapping("/list")
    public Map<String, Object> list() {
        QueryWrapper<Banner> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).orderByAsc("sort");
        return auth.ok(bannerService.list(wrapper));
    }

    // 查询全部轮播图（含禁用状态）
    @GetMapping("/all")
    public Map<String, Object> all() {
        QueryWrapper<Banner> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("sort");
        return auth.ok(bannerService.list(wrapper));
    }

    // 批量保存轮播图：先清空再按传入顺序重新插入
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/saveBatch")
    public Map<String, Object> saveBatch(@RequestBody List<Banner> banners) {
        bannerService.remove(new QueryWrapper<>());
        for (int i = 0; i < banners.size(); i++) {
            Banner b = banners.get(i);
            b.setId(null);
            b.setSort(i + 1);
            b.setCreateTime(null);
            if (b.getStatus() == null) b.setStatus(1);
        }
        bannerService.saveBatch(banners);
        return auth.resp(200, "保存成功");
    }

    // 新增单个轮播图
    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Banner banner) {
        bannerService.save(banner);
        return auth.resp(200, "添加成功");
    }

    // 更新轮播图信息
    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody Banner banner) {
        bannerService.updateById(banner);
        return auth.resp(200, "更新成功");
    }

    // 删除轮播图并重置自增 ID
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Integer id) {
        bannerService.deleteAndResetId(id);
        return auth.resp(200, "删除成功");
    }
}

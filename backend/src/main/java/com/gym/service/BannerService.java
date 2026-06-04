package com.gym.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.Banner;

/* 轮播图服务接口 */
public interface BannerService extends IService<Banner> {
    void deleteAndResetId(Integer id);
}

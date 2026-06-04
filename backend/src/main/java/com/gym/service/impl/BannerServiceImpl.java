package com.gym.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.Banner;
import com.gym.mapper.BannerMapper;
import com.gym.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/* 轮播图服务实现类 */
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {
    @Autowired
    private ResetIdUtil resetIdUtil;

    @Override
    public void deleteAndResetId(Integer id) {
        resetIdUtil.deleteAndReset(id, "banner");
    }
}
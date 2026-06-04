package com.gym.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.Course;
import com.gym.mapper.CourseMapper;
import com.gym.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/* 课程服务实现类 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
    @Autowired
    private ResetIdUtil resetIdUtil;

    @Override
    public void deleteAndResetId(Integer id) {
        resetIdUtil.deleteAndReset(id, "course");
    }
}
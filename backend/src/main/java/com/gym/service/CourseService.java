package com.gym.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.Course;

/* 课程服务接口 */
public interface CourseService extends IService<Course> {
    void deleteAndResetId(Integer id);
}

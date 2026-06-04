package com.gym.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/* 课程实体类，对应 course 表，包含课程名称、教练、容量、价格等 */
@Data
@TableName("course")
public class Course {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private Integer coachId;
    private String description;
    private Integer capacity;
    private BigDecimal price;
    private String imageUrl;
    private Integer status;
    private LocalDateTime createTime;
}

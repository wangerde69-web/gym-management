package com.gym.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/* 教练实体类，对应 coach 表，包含教练基本信息与专长 */
@Data
@TableName("coach")
public class Coach {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String avatar;
    private String specialty;
    private String experience;
    private String phone;
    private String intro;
    private Integer status;
    private LocalDateTime createTime;
}

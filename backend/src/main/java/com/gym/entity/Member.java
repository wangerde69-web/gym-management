package com.gym.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;

/* 会员实体类，对应 member 表，包含会员基本信息与账号数据 */
@Data
@TableName("member")
public class Member {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String username;
    @JsonIgnore
    private String password;
    private String name;
    private String phone;
    private String avatar;
    private Integer gender;
    private Integer age;
    private Integer status;
    private LocalDateTime createTime;
}

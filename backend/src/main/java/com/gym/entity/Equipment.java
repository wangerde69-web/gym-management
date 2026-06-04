package com.gym.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/* 健身器材实体类，对应 equipment 表，包含器材名称、位置、分类等 */
@Data
@TableName("equipment")
public class Equipment {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String location;
    private Integer status;
    private String imageUrl;
    private String category;
    private String remark;
    private LocalDateTime createTime;
}

package com.gym.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/* 轮播图实体类，对应 banner 表 */
@Data
@TableName("banner")
public class Banner {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private String subtitle;
    private String imageUrl;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
}

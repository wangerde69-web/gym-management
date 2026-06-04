package com.gym.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/* 会员卡类型配置实体类，对应 card_type 表，定义卡种名称、价格、有效期等 */
@Data
@TableName("card_type")
public class CardTypeEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String cardName;

    private String cardKey;

    private BigDecimal price;

    private Integer validityDays;

    private String validityUnit;   // 有效期单位：DAY=天, MONTH=月, YEAR=年

    private String coverImage;

    private Integer status;

    private Integer sortOrder;

    private LocalDateTime createTime;
}

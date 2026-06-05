package com.gym.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/* 会员卡实体类，对应 card 表，记录购卡、有效期、支付状态等信息 */
@Data
@TableName("card")
public class Card {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer memberId;
    @TableField("card_type")
    private String cardTypeKey;   // 周卡 / 月卡 / 年卡 / 次卡
    private BigDecimal price;
    private Integer status;        // 0=待审核(未支付) 1=生效中 2=已过期 3=已退款 4=未支付(可删) 5=声称未付待审
    private LocalDateTime createTime;

    // 扩展字段（非数据库列，联表查询或计算填充）
    @TableField(exist = false)
    private String memberName;
    @TableField(exist = false)
    private String cardTypeName;  // 卡种名称（联表查询填充）
    @TableField(exist = false)
    private LocalDate endDate;     // 根据 create_time + 卡种有效期动态计算
}

package com.gym.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/* 课程预约实体类，对应 booking 表，包含预约状态与联表扩展字段 */
@Data
@TableName("booking")
public class Booking {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer memberId;
    private Integer courseId;
    private String bookingTime;
    private Integer status;     // 0=待确认 1=已完成 2=已取消 3=未支付 4=已过期
    private String remark;
    private LocalDateTime createTime;
    
    // 扩展字段（非数据库列）
    @TableField(exist = false)
    private String memberName;
    @TableField(exist = false)
    private String courseName;
    @TableField(exist = false)
    private String coachName;
}

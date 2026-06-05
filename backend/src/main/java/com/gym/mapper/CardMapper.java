package com.gym.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.entity.Card;

import org.apache.ibatis.annotations.Update;
import java.util.List;

/* 会员卡 Mapper 接口，包含会员卡自动过期的批量更新 SQL 及联表查询 */

public interface CardMapper extends BaseMapper<Card> {

    // 批量将已过期的生效中会员卡标记为已过期状态（根据 create_time + 卡种有效期动态计算）
    @Update("UPDATE card c JOIN card_type ct ON c.card_type = ct.card_key " +
            "SET c.status = 2 WHERE c.status = 1 AND " +
            "CASE ct.validity_unit " +
            "WHEN 'MONTH' THEN DATE(c.create_time) + INTERVAL ct.validity_days MONTH " +
            "WHEN 'YEAR' THEN DATE(c.create_time) + INTERVAL ct.validity_days YEAR " +
            "ELSE DATE(c.create_time) + INTERVAL ct.validity_days DAY END < CURDATE()")
    void updateExpiredCards();

    // 联表查询所有会员卡记录（关联会员表获取会员姓名）
    List<Card> selectListWithDetail();

    // 联表查询指定会员的会员卡记录（含卡种名称和过期时间）
    List<Card> selectMyListWithDetail(Integer memberId);
}

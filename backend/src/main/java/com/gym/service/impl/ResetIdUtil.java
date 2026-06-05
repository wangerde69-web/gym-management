package com.gym.service.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

/* 自增 ID 重置工具类：删除记录后重置表的 AUTO_INCREMENT 值以保持 ID 连续 */
@Component
public class ResetIdUtil {

    // 允许操作的表名白名单，防止 SQL 注入和误操作其他表
    private static final Set<String> ALLOWED_TABLES = Set.of("admin", "member", "coach", "course", "card", "card_type", "equipment", "banner", "booking", "gym_config");

    private final JdbcTemplate jdbc;

    public ResetIdUtil(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // 删除指定记录并重新设置表的自增起始值为当前最大 ID + 1
    public void deleteAndReset(Integer id, String table) {
        if (!ALLOWED_TABLES.contains(table)) {
            throw new IllegalArgumentException("不允许操作的表名: " + table);
        }
        jdbc.update("DELETE FROM " + table + " WHERE id = ?", id);
        Integer maxId = jdbc.queryForObject("SELECT COALESCE(MAX(id),0) FROM " + table, Integer.class);
        jdbc.update("ALTER TABLE " + table + " AUTO_INCREMENT = " + (maxId + 1));
    }
}

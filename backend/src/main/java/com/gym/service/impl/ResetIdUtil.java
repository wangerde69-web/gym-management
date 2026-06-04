package com.gym.service.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/* 自增 ID 重置工具类：删除记录后重置表的 AUTO_INCREMENT 值以保持 ID 连续 */
@Component
public class ResetIdUtil {

    private final JdbcTemplate jdbc;

    public ResetIdUtil(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // 删除指定记录并重新设置表的自增起始值为当前最大 ID + 1
    public void deleteAndReset(Integer id, String table) {
        jdbc.update("DELETE FROM " + table + " WHERE id = ?", id);
        Integer maxId = jdbc.queryForObject("SELECT COALESCE(MAX(id),0) FROM " + table, Integer.class);
        jdbc.update("ALTER TABLE " + table + " AUTO_INCREMENT = " + (maxId + 1));
    }
}
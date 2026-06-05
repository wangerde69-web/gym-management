package com.gym;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/* 健身房管理系统启动入口，启用 MyBatis-Plus 自动扫描 Mapper 与定时任务支持 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@MapperScan("com.gym.mapper")
@EnableScheduling
public class GymApplication {
    public static void main(String[] args) {
        SpringApplication.run(GymApplication.class, args);
        System.out.println("===== 健身房管理系统启动成功 =====");
    }
}

package com.gym.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/* Web MVC 配置，包含静态资源映射（CORS 由 SecurityConfig 统一管理） */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 静态资源映射：将 /uploads/** 请求映射到项目根目录下的 uploads 文件夹
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get(System.getProperty("user.dir") + "/uploads/");
        String location = uploadDir.toFile().toURI().toString();
        if (!location.endsWith("/")) location += "/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}

package com.gym.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/* Spring Security 安全配置，定义认证、授权规则及 CORS 跨域策略 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    // 密码编码器：使用 BCrypt 哈希算法
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS 跨域配置：生产环境应从 application.yml 注入允许的来源，避免使用通配符 + 凭证
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 使用明确来源而不是 "*"，可与 allowCredentials(true) 配合使用
        // TODO: 生产环境应将来源列表抽取到 application.yml，通过 @Value 注入，支持域名配置
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173", "http://localhost:8080"));
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // 安全过滤链配置：定义接口访问权限、无状态会话策略，并注入 JWT 过滤器
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 1. 公开接口（无需认证）
                .requestMatchers(
                    "/api/admin/login",
                    "/api/member/login",
                    "/api/member/register"
                ).permitAll()
                // 前台公开查询
                .requestMatchers("/api/front/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()  // 上传文件静态访问（img 标签不带 token）
                .requestMatchers(HttpMethod.GET, "/api/coach/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/course/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/banner/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/equipment/**").permitAll()
                .requestMatchers("/api/cardtype/list", "/api/cardtype/all", "/api/cardtype/{id}").permitAll()
                // 2. 需认证的接口（admin 角色）
                .requestMatchers("/api/admin/**").hasRole("admin")
                .requestMatchers("/api/coach/add", "/api/coach/update", "/api/coach/delete").hasRole("admin")
                .requestMatchers("/api/course/add", "/api/course/update", "/api/course/delete").hasRole("admin")
                .requestMatchers("/api/banner/add", "/api/banner/update", "/api/banner/delete").hasRole("admin")
                .requestMatchers("/api/equipment/add", "/api/equipment/update", "/api/equipment/delete").hasRole("admin")
                .requestMatchers("/api/cardtype/add", "/api/cardtype/update", "/api/cardtype/delete").hasRole("admin")
                .requestMatchers("/api/card/approve/**", "/api/card/unpaid/**").hasRole("admin")
                .requestMatchers("/api/booking/approve/**", "/api/booking/list", "/api/booking/all").hasRole("admin")
                .requestMatchers("/api/config/save").hasRole("admin")
                .requestMatchers("/api/upload/**").authenticated()
                .requestMatchers("/api/stat/**").hasRole("admin")
                // 3. 需认证的接口（通用会员/已登录）
                .requestMatchers("/api/member/info").authenticated()
                .requestMatchers("/api/card/buy").authenticated()
                .requestMatchers("/api/card/refund/**").authenticated()
                .requestMatchers("/api/card/confirm-payment/**").authenticated()
                .requestMatchers("/api/card/cancel-purchase/**").authenticated()
                .requestMatchers("/api/card/my").authenticated()
                .requestMatchers("/api/booking/add", "/api/booking/update", "/api/booking/my").authenticated()
                .requestMatchers("/api/booking/confirm/**", "/api/booking/cancel/**").authenticated()
                .requestMatchers("/api/booking/confirm-refund/**").authenticated()
                .requestMatchers("/api/pose/**").authenticated()
                // 4. 其他所有请求需认证（防止遗漏的接口被匿名访问）
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

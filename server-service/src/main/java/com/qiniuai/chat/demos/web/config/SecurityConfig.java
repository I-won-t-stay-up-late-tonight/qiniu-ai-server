package com.qiniuai.chat.demos.web.config;

import com.aliyuncs.utils.StringUtils;
import com.hnit.server.common.Constants;
import com.qiniuai.chat.demos.web.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring Security 配置类
 * 用于放行公开接口（如注册、登录），保护其他需要认证的接口
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置安全过滤链
     * 放行注册、登录等公开接口，其他接口需要认证
     */
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                // 关闭CSRF（适用于前后端分离项目，若有需要可开启）
//                .csrf(csrf -> csrf.disable())
//                // 配置请求授权规则
//                .authorizeHttpRequests(auth -> auth
//                        // 放行注册接口
//                        .requestMatchers("/api/user/register").permitAll()
//                        // 放行手机号验证码登录接口
//                        .requestMatchers("/api/user/send-code").permitAll()
//                        .requestMatchers("/api/user/login/phone").permitAll()
//                        // 放行账号密码登录接口
//                        .requestMatchers("/api/user/login/account").permitAll()
//                        // 其他所有请求需要认证
//                        .anyRequest().authenticated()
//                );
//
//        return http.build();
//    }


    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Token 验证过滤器：解析请求头中的 Token，验证有效性并设置用户身份
     */
    @Bean
    public OncePerRequestFilter tokenAuthFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                // 1. 从请求头中获取 Token
                String token = request.getHeader(Constants.TOKEN_HEADER); // "Authorization"
                if (token == null) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // 3. 验证 Token 有效性（从 Redis 中查询 Token 是否存在）
                String redisKey = Constants.TOKEN_PREFIX + token; // "token:" + token
                String phone = String.valueOf(redisTemplate.opsForValue().get(redisKey)) ;
                if (StringUtils.isEmpty(phone)) { // Token 不存在或已过期
                    filterChain.doFilter(request, response);
                    return;
                }

                // 4. 根据手机号查询用户信息（构建 Spring Security 所需的 UserDetails）
                UserDetails userDetails = userService.loadUserByUsername(phone); // 需实现 UserDetailsService 接口

                // 5. 设置用户身份到 Security 上下文（后续接口可通过 SecurityContext 获取用户信息）
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 6. 继续执行过滤器链
                filterChain.doFilter(request, response);
            }
        };
    }

    /**
     * 配置安全过滤链：放行公开接口 + 加入 Token 过滤器
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 前后端分离项目关闭 CSRF
                .authorizeHttpRequests(auth -> auth
                        // 放行公开接口（注册、登录、发送验证码）
                        .requestMatchers("/api/user/register", "/api/user/send-code",
                                "/api/user/login/phone", "/api/user/login/account").permitAll()
                        // 其他所有接口需要认证
                        .anyRequest().authenticated()
                )
                // 加入 Token 验证过滤器（在 UsernamePasswordAuthenticationFilter 之前执行）
                .addFilterBefore(tokenAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}


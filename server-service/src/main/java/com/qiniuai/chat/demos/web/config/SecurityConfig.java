package com.qiniuai.chat.demos.web.config;

import com.aliyuncs.utils.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Security 配置类
 * 用于放行公开接口（如注册、登录），保护其他需要认证的接口
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

     @Autowired
     private UserService userService;

     @Autowired
     private RedisTemplate<String, Object> redisTemplate;

     private static final String[] PUBLIC_URLS = {
     "/api/user/register",
     "/api/user/send-code",
     "/api/user/login/phone",
     "/api/user/login/account"
     };

     // 路径匹配器（用于判断请求是否为公开接口）
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Bean
    public OncePerRequestFilter tokenAuthFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                // 1. 判断当前请求是否为公开接口，若是则直接放行
                if (isPublicUrl(request.getRequestURI())) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // 2. 非公开接口才需要校验 Token（以下逻辑不变）
                String token = request.getHeader(Constants.TOKEN);
                if (StringUtils.isEmpty(token)) {
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
                    return;
                }

                String redisKey = Constants.TOKEN_PREFIX + token;
                String phone = String.valueOf(redisTemplate.opsForValue().get(redisKey));
                if (StringUtils.isEmpty(phone)) {
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token已过期或无效，请重新登录");
                    return;
                }

                try {
                    UserDetails userDetails = userService.loadUserByUsername(phone);
                    if (!userDetails.isEnabled()) {
                        sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "账号已被禁用，请联系管理员");
                        return;
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    filterChain.doFilter(request, response);
                } catch (UsernameNotFoundException e) {
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "用户不存在，请重新注册");
                }
            }
        };
    }

    /**
     * 判断请求路径是否为公开接口
     */
    private boolean isPublicUrl(String requestUri) {
        for (String publicUrl : PUBLIC_URLS) {
            if (pathMatcher.match(publicUrl, requestUri)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 发送JSON错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> error = new HashMap<>();
        error.put("code", status);
        error.put("msg", message);
        error.put("data", null);

        new ObjectMapper().writeValue(response.getWriter(), error);
    }

    /**
     * 安全过滤链配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http    .httpBasic(httpBasic -> httpBasic.disable())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URLS).permitAll() // 公开接口放行
                        .anyRequest().authenticated()
                )
                .addFilterBefore(tokenAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form.disable()) // 禁用默认登录页
                .logout(logout -> logout.disable());

        return http.build();
    }
}


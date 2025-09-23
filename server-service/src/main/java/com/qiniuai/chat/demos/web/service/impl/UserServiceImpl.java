package com.qiniuai.chat.demos.web.service.impl;

import com.hnit.server.common.Constants;
import com.hnit.server.dto.ApiResult;
import com.hnit.server.dto.PhoneLoginDTO;
import com.qiniuai.chat.demos.web.dto.AccountLoginDTO;
import com.qiniuai.chat.demos.web.entity.SysUser;
import com.qiniuai.chat.demos.web.repository.UserRepository;
import com.qiniuai.chat.demos.web.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    /**
     * 手机号验证码登录（存在则登录，不存在则注册）
     */
    /**
     * 策略1：手机号验证码登录
     */
    @Override
    public ApiResult<?> loginByPhone(PhoneLoginDTO loginDTO) {
        String phone = loginDTO.getPhone();
        String code = loginDTO.getCode();

        // 1. 验证验证码
        if (!verifyCode(phone, code)) {
            return ApiResult.fail("验证码不正确或已过期");
        }

        // 2. 共用核心登录逻辑（存在则登录，不存在则注册）
        return commonLoginProcess(phone, null);
    }

    /**
     * 策略2：账号密码登录
     */
    @Override
    public ApiResult<?> loginByAccount(AccountLoginDTO loginDTO) {
        String phone = loginDTO.getPhone();
        String password = loginDTO.getPassword();

        // 查询用户（支持用户名/手机号登录）
         SysUser user = userRepository.findByPhone(phone);
        // 验证用户存在性和密码
        if (user == null) {
            return ApiResult.fail("用户不存在");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ApiResult.fail("密码不正确");
        }
        if (user.getStatus() != 1) {
            return ApiResult.fail("账号已禁用，请联系管理员");
        }
        // 核心登录逻辑
        return commonLoginProcess(user.getPhone(), user);
    }

    /**
     * 核心登录流程（两种策略共用）
     * @param phone 手机号（唯一标识）
     * @param user 已查询到的用户（账号密码登录时不为空，验证码登录时可能为空）
     */
    private ApiResult<?> commonLoginProcess(String phone, SysUser user) {
        boolean isNewUser = false;

        // 1. 处理用户存在性（验证码登录可能需要自动注册，账号密码登录已确保用户存在）
        if (user == null) {
            // 自动注册新用户
            user = new SysUser();
            user.setId(generateUserId());
            user.setPhone(phone);
            user.setUsername("用户" + phone.substring(7));
            user.setPassword("");
            user.setStatus(1);
            user.setCreateTime(new Date());
            user.setLastLoginTime(new Date());
            user = userRepository.save(user);
            isNewUser = true;
            log.info("手机号{}注册新用户成功，用户ID:{}", phone, user.getId());
        } else {
            // 更新最后登录时间
            user.setLastLoginTime(new Date());
            user = userRepository.save(user);
            log.info("用户{}登录成功，用户ID:{}", phone, user.getId());
        }

        // 2. 生成登录令牌（使用手机号作为唯一标识）
        String token = generateToken(phone);

        // 3. 保存令牌到Redis
        redisTemplate.opsForValue().set(
                Constants.TOKEN_PREFIX + token,
                phone,
                Constants.TOKEN_EXPIRE_HOURS,
                TimeUnit.HOURS
        );

        // 4. 验证码登录场景：清除已使用的验证码
        if (isNewUser) {
            redisTemplate.delete(Constants.CODE_PREFIX + phone);
        }

        // 5. 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("phone", user.getPhone());
        result.put("isNewUser", isNewUser);

        return ApiResult.success(result);
    }

    /**
     * 验证验证码
     */
    public boolean verifyCode(String phone, String code) {
        String key = Constants.CODE_PREFIX + phone;
        String storedCode = redisTemplate.opsForValue().get(key);
        
        if (storedCode == null) {
            return false;
        }
        
        return storedCode.equals(code);
    }

    @Override
    public UserDetails loadUserByUsername(String phone) {
        // 1. 根据手机号查询 MongoDB 中的用户
        SysUser user = userRepository.findByPhone(phone);
        if (user == null) {
            // 若用户不存在，抛出 UsernameNotFoundException（Spring Security 会自动处理为 401）
            throw new UsernameNotFoundException("用户不存在：" + phone);
        }

        // 2. 构建用户权限列表（根据实际业务设置，这里简化为默认角色）
        List<GrantedAuthority> authorities = new ArrayList<>();
        // 示例：给所有用户默认添加 "ROLE_USER" 角色（Spring Security 角色需以 "ROLE_" 为前缀）
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        // 若有管理员权限，可添加：authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        // 3. 将 SysUser 转换为 Spring Security 提供的 UserDetails 实现类（User）
        // 参数说明：用户名（此处用手机号）、密码（可为空，验证码登录场景）、权限列表、是否启用等
        return new org.springframework.security.core.userdetails.User(
                user.getPhone(), // 用户名（使用手机号作为登录标识）
                user.getPassword() != null ? user.getPassword() : "", // 密码（验证码登录可为空字符串）
                user.getStatus() == 1, // 是否启用（1=正常启用，0=禁用）
                true, // 账户是否未过期
                true, // 凭证是否未过期
                true, // 账户是否未锁定
                authorities // 权限列表
        );
    }
    /**
     * 生成用户ID（基于时间戳+随机数）
     */
    private String generateUserId() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 1000);
        return String.format("%d%03d", timestamp, random);
    }

    /**
     * 生成登录令牌
     */
    private String generateToken(String phone) {
        String content = phone + System.currentTimeMillis() + UUID.randomUUID().toString();
        return DigestUtils.md5DigestAsHex(content.getBytes(StandardCharsets.UTF_8));
    }
}

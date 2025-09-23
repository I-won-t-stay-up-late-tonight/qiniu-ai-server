package com.qiniuai.chat.demos.web.controller;


import com.hnit.server.dto.ApiResult;
import com.hnit.server.dto.PhoneLoginDTO;

import com.qiniuai.chat.demos.web.dto.AccountLoginDTO;
import com.qiniuai.chat.demos.web.entity.SysUser;
import com.qiniuai.chat.demos.web.repository.UserRepository;
import com.qiniuai.chat.demos.web.service.SmsService;
import com.qiniuai.chat.demos.web.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

/**
 * 用户控制器，处理登录相关请求
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private SmsService smsService;
    @Autowired
    private UserService userService;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public ApiResult<?> sendVerifyCode(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        if (phone == null || phone.trim().isEmpty()) {
            return ApiResult.fail("手机号不能为空");
        }
        
        boolean success = smsService.sendVerifyCode(phone);
        if (success) {
            return ApiResult.success("验证码已发送，请注意查收");
        } else {
            return ApiResult.fail("验证码发送失败，请稍后重试");
        }
    }
    
    /**
     * 手机号验证码登录
     */
    @PostMapping("/login/phone")
    public ApiResult<?> loginByPhone(@Valid @RequestBody PhoneLoginDTO loginDTO) {
        return userService.loginByPhone(loginDTO);
    }

    /**
     * 账号密码登录
     */
    @PostMapping("/login/account")
    public ApiResult<?> loginByAccount(@Valid @RequestBody AccountLoginDTO loginDTO) {
        return userService.loginByAccount(loginDTO);
    }

    @PostMapping("/register")
    public ApiResult<?> register(@Valid @RequestBody AccountLoginDTO dto) {
        // 用户是否已存在
        if (userRepository.existsByPhone(dto.getPhone())) {
            return ApiResult.fail("手机号已注册");
        }
        // 创建用户并加密密码
        SysUser user = new SysUser();
        user.setPhone(dto.getPhone());
        user.setUsername(dto.getUsername());
        user.setStatus(1);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);
        return ApiResult.success("注册成功");
    }

}

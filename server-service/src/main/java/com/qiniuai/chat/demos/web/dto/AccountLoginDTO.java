package com.qiniuai.chat.demos.web.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 账号密码登录请求参数
 */
@Data
public class AccountLoginDTO {

    /**
     * 账号（支持用户名或手机号）
     */
    @NotBlank(message = "账号不能为空")
    private String phone;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}

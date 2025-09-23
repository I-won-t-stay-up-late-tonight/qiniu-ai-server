package com.qiniuai.chat.demos.web.service;


import com.hnit.server.dto.ApiResult;
import com.hnit.server.dto.PhoneLoginDTO;
import com.qiniuai.chat.demos.web.dto.AccountLoginDTO;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 手机号验证码登录
     * @param loginDTO 登录信息
     * @return 登录结果，包含token或用户信息
     */
    ApiResult<?> loginByPhone(PhoneLoginDTO loginDTO);

    ApiResult<?> loginByAccount(AccountLoginDTO loginDTO);

    /**
     * 验证验证码是否正确
     * @param phone 手机号
     * @param code 验证码
     * @return 是否验证通过
     */
    boolean verifyCode(String phone, String code);

    UserDetails loadUserByUsername(String phone);
}

package com.qiniuai.chat.web.service;

/**
 * 短信服务接口
 */
public interface SmsService {

    /**
     * 发送验证码短信
     * @param phone 手机号
     * @return 是否发送成功
     */
    boolean sendVerifyCode(String phone);
}

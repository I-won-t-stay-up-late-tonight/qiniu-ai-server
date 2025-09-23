package com.qiniuai.chat;

import com.qiniuai.chat.demos.web.service.SmsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ChatTestApplicationTests {

    @Autowired
    private SmsService smsService;

    @Test
    void contextLoads() {
    }

    @Test
    void testSendVerifyCode() {
        // 测试发送验证码，替换为实际手机号
        String phone = "19152342280";
        boolean result = smsService.sendVerifyCode(phone);
        System.out.println("验证码发送结果: " + (result ? "成功" : "失败"));
    }

}

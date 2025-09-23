package com.qiniuai.chat.demos.web.service.impl;

import com.qiniuai.chat.demos.web.config.TencentSmsConfig;
import com.qiniuai.chat.demos.web.config.VerifyCodeConfig;
import com.qiniuai.chat.demos.web.service.SmsService;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 短信服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final SmsClient smsClient;
    private final TencentSmsConfig smsConfig;
    private final VerifyCodeConfig codeConfig;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Redis键前缀
    private static final String CODE_PREFIX = "verify:code:";         // 验证码前缀
    private static final String SEND_INTERVAL_PREFIX = "verify:interval:"; // 发送间隔前缀
    private static final String DAILY_COUNT_PREFIX = "verify:daily:";   // 每日计数前缀
    
    // 日期格式化
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    /**
     * 发送验证码短信
     */
    @Override
    public boolean sendVerifyCode(String phone) {
        // 1. 检查发送间隔
        String intervalKey = SEND_INTERVAL_PREFIX + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(intervalKey))) {
            log.warn("手机号{}发送验证码过于频繁", phone);
            return false;
        }
        
        // 2. 检查每日发送次数
        String dailyCountKey = DAILY_COUNT_PREFIX + phone + ":" + DATE_FORMAT.format(new Date());
        Integer count = (Integer) redisTemplate.opsForValue().get(dailyCountKey);
        if (count != null && count >= codeConfig.getMaxDailyCount()) {
            log.warn("手机号{}今日发送验证码次数已达上限", phone);
            return false;
        }
        
        // 3. 生成验证码
        String code = generateVerifyCode();
        log.info("为手机号{}生成验证码: {}", phone, code);
        
        // 4. 保存验证码到Redis，设置过期时间
        String codeKey = CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(codeKey, code, codeConfig.getExpireTime(), TimeUnit.MINUTES);
        
        // 5. 设置发送间隔限制
        redisTemplate.opsForValue().set(intervalKey, "1", codeConfig.getSendInterval(), TimeUnit.SECONDS);
        
        // 6. 累加每日发送次数
        redisTemplate.opsForValue().increment(dailyCountKey);
        // 设置每日计数过期时间为24小时
        redisTemplate.expire(dailyCountKey, 24, TimeUnit.HOURS);
        
        // 7. 调用腾讯云API发送短信
        return sendSms(phone, code);
    }
    
    /**
     * 生成随机验证码
     */
    private String generateVerifyCode() {
        int length = codeConfig.getLength();
        if (length <= 0) {
            length = 6; // 默认6位验证码
        }
        
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    
    /**
     * 调用腾讯云短信API发送短信
     */
    private boolean sendSms(String phone, String code) {
        try {
            // 构建请求对象
            SendSmsRequest req = new SendSmsRequest();
            req.setPhoneNumberSet(new String[]{"+86" + phone});
            req.setSmsSdkAppId(smsConfig.getSmsSdkAppId());
            req.setTemplateId(smsConfig.getTemplateId());
            req.setSignName(smsConfig.getSignName());
            req.setTemplateParamSet(new String[]{code, String.valueOf(codeConfig.getExpireTime())});
            
            // 发送短信
            SendSmsResponse resp = smsClient.SendSms(req);
            
            // 处理响应结果
            if (resp != null && resp.getSendStatusSet() != null && resp.getSendStatusSet().length > 0) {
                String codeStatus = resp.getSendStatusSet()[0].getCode();
                if ("Ok".equals(codeStatus)) {
                    log.info("手机号{}验证码短信发送成功", phone);
                    return true;
                } else {
                    log.error("手机号{}验证码短信发送失败: {}", phone, resp.getSendStatusSet()[0].getMessage());
                }
            }
        } catch (TencentCloudSDKException e) {
            log.error("发送短信异常", e);
        }
        return false;
    }
}

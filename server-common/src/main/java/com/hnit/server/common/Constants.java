package com.hnit.server.common;

/**
 * 系统常量类
 * 统一管理项目中使用的常量，避免硬编码
 */
public class Constants {

    /**
     * Redis中验证码存储的前缀
     * 格式：code:phone:{手机号}
     */
    public static final String CODE_PREFIX = "code:phone:";

    /**
     * Redis中登录令牌存储的前缀
     * 格式：token:{令牌值}
     */
    public static final String TOKEN_PREFIX = "token:";

    /**
     * Redis中手机号发送频率限制的前缀
     * 格式：limit:phone:{手机号}
     */
    public static final String PHONE_LIMIT_PREFIX = "limit:phone:";

    /**
     * 验证码长度
     */
    public static final int CODE_LENGTH = 6;

    /**
     * 验证码有效期（单位：秒）
     * 默认为5分钟
     */
    public static final int CODE_EXPIRE_SECONDS = 300;

    /**
     * 短信发送间隔（单位：秒）
     * 默认为60秒，防止频繁发送
     */
    public static final int SMS_SEND_INTERVAL = 60;

    /**
     * 每日最大发送次数
     */
    public static final int DAILY_MAX_SEND_COUNT = 10;

    /**
     * 登录令牌有效期（单位：小时）
     * 默认为24小时
     */
    public static final int TOKEN_EXPIRE_HOURS = 24;

    /**
     * JWT签名密钥（实际项目中建议放在配置文件中）
     */
    public static final String JWT_SECRET = "your_jwt_secret_key"; // 生产环境需修改

    /**
     * 令牌在HTTP请求头中的名称
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * 令牌前缀（用于HTTP请求头，如 Bearer token）
     */
    public static final String TOKEN_HEADER_PREFIX = "Bearer ";

    /**
     * 用户默认头像URL
     */
    public static final String DEFAULT_AVATAR = "https://picsum.photos/200";

    /**
     * 系统默认用户名前缀
     */
    public static final String DEFAULT_USERNAME_PREFIX = "用户";
}
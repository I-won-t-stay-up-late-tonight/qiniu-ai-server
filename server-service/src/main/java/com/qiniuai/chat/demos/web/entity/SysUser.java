package com.qiniuai.chat.demos.web.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * MongoDB用户实体类
 */
@Data
@Document(collection = "sys_user") // 指定MongoDB集合名
public class SysUser {

    @Id
    private String id; // MongoDB使用字符串ID

    @Indexed(unique = true) // 手机号唯一索引，防止重复注册
    private String phone; // 手机号（登录账号）

    private String username; // 用户名

    private String password;

    private Integer status; // 状态：1-正常，0-禁用

    private Date createTime; // 创建时间

    private Date lastLoginTime; // 最后登录时间

     private String avatar; // 头像URL

     private Integer gender; // 性别：0-未知，1-男，2-女

     private Date birthday; // 生日
}

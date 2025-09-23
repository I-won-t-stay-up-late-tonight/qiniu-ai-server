package com.qiniuai.chat.demos.web.entity.pojo;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @ClassName Role
 * @Description TODO
 * @Author IFundo
 * @Date 19:04 2025/9/23
 * @Version 1.0
 */
@Data
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "role_name")
    private String roleName;        // 角色名称

    @Column(name = "role_desc")
    private String roleDesc;        // 角色描述

    @Column(name = "personality")
    private String personality;     // 性格设定

    @Column(name = "background")
    private String background;      // 背景故事

    @Column(name = "avatar_url")
    private String avatarUrl;       // 头像地址

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "is_builtin")
    private Boolean isBuiltin;      // 是否为内置角色
}

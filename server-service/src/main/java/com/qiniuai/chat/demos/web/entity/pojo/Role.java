package com.qiniuai.chat.demos.web.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Table(name = "roles")
@AllArgsConstructor
@NoArgsConstructor
public class Role {
    private Long id;

    private String roleName;        // 角色名称

    private String roleDesc;        // 角色描述

    private String personality;     // 性格设定

    private String background;      // 背景故事

    private String avatarUrl;       // 头像地址

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Boolean isBuiltin;      // 是否为内置角色

    private String voice;  //角色声音

    @Override
    public String toString() {
        // 处理null值：若属性为null，显示“未设置”而非“null”，避免模型误解
        String actualRoleName = (roleName == null || roleName.trim().isEmpty()) ? "未设置角色名称" : roleName;
        String actualRoleDesc = (roleDesc == null || roleDesc.trim().isEmpty()) ? "未设置角色描述" : roleDesc;
        String actualPersonality = (personality == null || personality.trim().isEmpty()) ? "未设置性格设定" : personality;
        String actualBackground = (background == null || background.trim().isEmpty()) ? "未设置背景故事" : background;

        // 拼接为「大模型可识别的角色限定词」格式：分点明确角色核心信息，语言自然
        return "首先回答尽可能的简洁，其次，你现在需要完全代入以下角色，所有对话必须严格遵循该角色的设定：\n" +
                "1. 角色名称：" + actualRoleName + "\n" +
                "2. 角色描述：" + actualRoleDesc + "\n" +
                "3. 性格设定：" + actualPersonality + "（回复时需体现该性格，如性格“开朗”则语气活泼，性格“沉稳”则语气严肃）\n" +
                "4. 背景故事：" + actualBackground + "（回复内容需符合该背景，不提及与背景无关的信息，如背景是“古代侠客”则避免现代词汇）\n" +
                "补充规则：不暴露你是AI的身份，所有回复都要从该角色的视角出发；”）。";
    }
}

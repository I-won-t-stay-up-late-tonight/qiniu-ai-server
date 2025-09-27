package com.qiniuai.chat.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RulesDto {

    private Long roleId;

    private String roleName;

    private String roleDesc;

    private Long userId;
}

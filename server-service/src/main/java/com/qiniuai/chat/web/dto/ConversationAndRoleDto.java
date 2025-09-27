package com.qiniuai.chat.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationAndRoleDto {
    @NotBlank
    private String conversationName;
    @NonNull
    private Long roleId;
    @NonNull
    private String userId;
}

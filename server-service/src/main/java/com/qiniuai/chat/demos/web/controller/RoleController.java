package com.qiniuai.chat.demos.web.controller;

import com.hnit.server.dto.ApiResult;
import com.qiniuai.chat.demos.web.entity.pojo.Role;
import com.qiniuai.chat.demos.web.service.ConversationService;
import com.qiniuai.chat.demos.web.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName RoleController
 * @Description TODO
 * @Author IFundo
 * @Date 16:32 2025/9/24
 * @Version 1.0
 */

@RestController
@RequestMapping("/api/v1/")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @PostMapping("/createRole")
    public ApiResult<String> createConversation(@Validated Role role) {

        String res = roleService.createRole(role);
        if (res == "创建成功"){
            return ApiResult.success(res);
        }else {
            return ApiResult.fail(res);
        }
    }

}

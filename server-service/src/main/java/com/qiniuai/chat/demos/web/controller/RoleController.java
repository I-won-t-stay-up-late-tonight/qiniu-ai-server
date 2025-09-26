package com.qiniuai.chat.demos.web.controller;

import com.hnit.server.dto.ApiResult;
import com.qiniuai.chat.demos.web.entity.pojo.Role;
import com.qiniuai.chat.demos.web.service.ConversationService;
import com.qiniuai.chat.demos.web.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

        boolean res = roleService.createRole(role);
        if (res){
            return ApiResult.success("创建成功");
        }else {
            return ApiResult.fail("创建失败");
        }
    }

    @GetMapping("/searchRole")
    public ApiResult<List<Role>> searchRole(@Validated String name) {

        List<Role> roles = roleService.searchRole(name);
        return ApiResult.success(roles);
    }

}

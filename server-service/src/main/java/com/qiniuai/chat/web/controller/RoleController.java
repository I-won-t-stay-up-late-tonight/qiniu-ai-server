package com.qiniuai.chat.web.controller;

import com.hnit.server.dto.ApiResult;
import com.qiniuai.chat.web.entity.pojo.Role;
import com.qiniuai.chat.web.service.RoleService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public ApiResult<String> createConversation(@Validated @RequestBody Role role) {

        boolean res = roleService.createRole(role);
        if (res){
            return ApiResult.success("创建成功");
        }else {
            return ApiResult.fail("创建失败");
        }
    }

    /*
     * @Date 21:08 2025/9/26
     * 搜索角色
     */

    @PostMapping("/searchRoleByName")
    public ApiResult<List<Role>> searchRoleByName(@Validated String userId, @Validated @RequestParam(value = "name", required = false) String name) {

        List<Role> roles = roleService.searchRoleByName(userId, name);
        return ApiResult.success(roles);
    }

//    @PostMapping("/queryRole")
//    public ApiResult<List<Role>> queryRole(@RequestBody RulesDto rulesDto) {
//        List<Role> roles = roleService.queryRole(rulesDto);
//        return ApiResult.success(roles);
//    }

}

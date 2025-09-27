package com.qiniuai.chat.web.service;

//import com.alibaba.dashscope.exception.NoApiKeyException;
//import com.alibaba.dashscope.exception.UploadFileException;
import com.qiniuai.chat.web.entity.pojo.Role;

import java.util.List;

/**
 * @ClassName audioService
 * @Description TODO
 * @Author IFundo
 * @Date 00:05 2025/9/23
 * @Version 1.0
 */


public interface RoleService {

    boolean createRole(Role role);

    List<Role> searchRoleByName(long userId, String name);

    Role getRoleById(Long roleId);

}

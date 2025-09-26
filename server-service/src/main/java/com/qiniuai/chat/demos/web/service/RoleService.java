package com.qiniuai.chat.demos.web.service;

//import com.alibaba.dashscope.exception.NoApiKeyException;
//import com.alibaba.dashscope.exception.UploadFileException;
import com.qiniuai.chat.demos.web.entity.pojo.Role;
import org.springframework.web.multipart.MultipartFile;

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

    List<Role> searchRole(String name);
}

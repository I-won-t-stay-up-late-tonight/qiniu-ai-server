package com.qiniuai.chat.web.service.impl;

import com.qiniuai.chat.web.entity.pojo.Role;
import com.qiniuai.chat.web.mapper.RoleMapper;
import com.qiniuai.chat.web.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName audioServiceImpl
 * @Description TODO
 * @Author IFundo
 * @Date 00:06 2025/9/23
 * @Version 1.0
 */

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public boolean createRole(Role role) {
        if (role.getVoice() == null || role.getVoice().trim().isEmpty()) {
            role.setVoice("Ethan");
        }
        int rows = roleMapper.insertRole(role);
        if (rows == 1){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public List<Role> searchRole(String name) {
        List<Role> roles = roleMapper.searchRole(name);
        return roles;
    }
}

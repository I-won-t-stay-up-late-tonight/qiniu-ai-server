package com.qiniuai.chat.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiniu.util.StringUtils;
import com.qiniuai.chat.web.dto.RulesDto;
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
        role.setIsBuiltin(1);
        int rows = roleMapper.insertRole(role);
        if (rows == 1){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public List<Role> searchRoleByName(String userId, String name) {
        List<Role> roles = roleMapper.searchRoleByName(userId, name);
        return roles;
    }

    @Override
    public Role getRoleById(Long roleId) {
        return this.roleMapper.getById(roleId);
    }

    @Override
    public List<Role> queryRole(RulesDto rulesDto) {
        return this.roleMapper.queryRole(rulesDto);
    }

}

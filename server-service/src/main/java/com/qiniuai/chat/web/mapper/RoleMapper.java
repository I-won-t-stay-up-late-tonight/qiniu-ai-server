package com.qiniuai.chat.web.mapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiniuai.chat.web.dto.RulesDto;
import org.apache.ibatis.annotations.*;
import com.qiniuai.chat.web.entity.pojo.Role;

import java.util.List;

/**
 * @ClassName RoleMapper
 * @Description TODO
 * @Author IFundo
 * @Date 15:54 2025/9/24
 * @Version 1.0
 */

@Mapper
public interface RoleMapper {
    @Select("SELECT id, role_name, role_desc, personality, background, voice " +
            "FROM roles WHERE id = #{roleId}")
    Role selectById(@Param("roleId") Long roleId);

    /**
     * 插入角色信息（自动生成主键，自动填充创建/更新时间）
     * @param role 角色实体对象（需包含roleName、roleDesc等业务字段）
     * @return 受影响行数（1=成功）
     */
    @Insert("INSERT INTO roles (" +
            "role_name, " +          // 角色名称
            "role_desc, " +          // 角色描述
            "personality, " +       // 性格设定
            "background, " +        // 背景故事
            "avatar_url, " +        // 头像地址
            "create_time, " +       // 创建时间
            "update_time, " +       // 更新时间
            "is_builtin, " +        // 是否内置角色
            "voice " +              // 角色声音
            ") VALUES (" +
            "#{roleName}, " +
            "#{roleDesc}, " +
            "#{personality}, " +
            "#{background}, " +
            "#{avatarUrl}, " +
            "NOW(), " +             // 使用数据库当前时间作为创建时间
            "NOW(), " +             // 使用数据库当前时间作为更新时间
            "#{isBuiltin}, " +
            "#{voice}" +
            ")")
    int insertRole(Role role);


    // 根据角色名称模糊查询角色列表（包含内置角色或符合条件的自定义角色）
    @Select("SELECT " +
            "id, " +
            "role_name AS roleName, " +
            "role_desc AS roleDesc, " +
            "personality, " +
            "background, " +
            "user_id AS userId, " +
            "avatar_url AS avatarUrl, " +
            "create_time AS createTime, " +
            "update_time AS updateTime, " +
            "is_builtin AS isBuiltin, " +
            "voice " +
            "FROM roles " +
            "WHERE " +
            "   (is_builtin = 1 AND role_name LIKE CONCAT('%', #{name}, '%')) " +  // 条件1：内置角色（is_builtin=1）
            "   OR " +
            "   (is_builtin = 0 AND role_name LIKE CONCAT('%', #{name}, '%') AND user_id = #{userId} ) " +  // 条件2：非内置角色但名称匹配,属于用户
            "ORDER BY create_time DESC")
    List<Role> searchRoleByName(@Param("userId") String userId, @Param("name") String name);  // 添加@Param明确参数名

    /**
     * 根据角色ID查询角色信息
     * @param roleId 角色ID（非空，必传）
     * @return 角色实体（无数据时返回null）
     */
    @Select("SELECT id, role_name, role_desc, personality, background, is_builtin, voice " +
            "FROM roles " +
            "WHERE id = #{roleId}")
    Role getById(@Param("roleId") Long roleId);


//    List<Role> selectList(RulesDto rulesDto);
}

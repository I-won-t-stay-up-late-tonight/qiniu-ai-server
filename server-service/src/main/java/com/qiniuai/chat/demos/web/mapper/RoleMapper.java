package com.qiniuai.chat.demos.web.mapper;
import org.apache.ibatis.annotations.*;
import com.qiniuai.chat.demos.web.entity.pojo.Role;

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

}

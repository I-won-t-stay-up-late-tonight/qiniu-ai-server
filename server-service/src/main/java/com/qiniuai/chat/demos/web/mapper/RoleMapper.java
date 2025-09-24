package com.qiniuai.chat.demos.web.mapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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

}

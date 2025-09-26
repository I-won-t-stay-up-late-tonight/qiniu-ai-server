package com.qiniuai.chat.web.repository;

import com.qiniuai.chat.web.entity.SysUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * MongoDB用户数据访问接口
 * 继承MongoRepository提供基本CRUD操作
 */
@Repository
public interface UserRepository extends MongoRepository<SysUser, String> {

    /**
     * 根据手机号查询用户
     * Spring Data MongoDB会自动生成实现
     */
    SysUser findByPhone(String phone);

    /**
     * 判断手机号是否已存在
     */
    boolean existsByPhone(String phone);

}

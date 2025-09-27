package com.qiniuai.chat.audiochat.repository;

import com.qiniuai.chat.audiochat.entity.Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色数据访问接口，操作MongoDB
 */
@Repository
public interface ChatRoleRepository extends MongoRepository<Role, String> {

    /**
     * 根据角色名称模糊查询
     */
    List<Role> findByNameContainingIgnoreCase(String name);
    
    /**
     * 根据角色类别查询
     */
    List<Role> findByCategory(String category);
    
    /**
     * 根据状态查询角色
     */
    List<Role> findByStatus(String status);
    
    /**
     * 按热门程度排序查询角色
     */
    List<Role> findByStatusOrderByPopularityDesc(String status);
}
    
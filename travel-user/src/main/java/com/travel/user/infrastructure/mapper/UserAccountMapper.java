package com.travel.user.infrastructure.mapper;

import com.travel.domain.user.UserAccount;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p><b>作用：</b>用户账户表的 MyBatis 映射接口（占位）。</p>
 * <p>由启动类的 {@code @MapperScan} 扫描；具体 SQL 在对应 XML 或使用注解 SQL 中实现，
 * 用于按 openId 查询、插入新用户、更新资料等持久化操作。</p>
 */
@Mapper
public interface UserAccountMapper {

    /** 根据微信 openId（或统一标识）查询账户 */
    UserAccount findByOpenId(String openId);

    /** 插入新用户行 */
    void insert(UserAccount account);

    /** 更新用户资料字段 */
    void updateProfile(UserAccount account);
}

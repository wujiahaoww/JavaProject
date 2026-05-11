package com.travel.user.infrastructure.mapper;

import com.travel.domain.user.UserAccount;

/**
 * 用户持久化映射（占位：后续对接 MyBatis / JPA）。
 */
public interface UserAccountMapper {

    UserAccount findByOpenId(String openId);

    void insert(UserAccount account);

    void updateProfile(UserAccount account);
}

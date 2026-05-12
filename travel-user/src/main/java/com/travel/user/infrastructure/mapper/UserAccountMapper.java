package com.travel.user.infrastructure.mapper;

import com.travel.domain.user.UserAccount;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p><b>作用：</b>用户账户表 {@code user_account} 的 MyBatis 映射。</p>
 */
@Mapper
public interface UserAccountMapper {

    UserAccount findByOpenId(String openId);

    UserAccount findByPhone(String phone);

    UserAccount findByEmail(String email);

    UserAccount findById(Long id);

    int insert(UserAccount account);

    void updateProfile(UserAccount account);
}

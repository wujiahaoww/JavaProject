package com.travel.domain.user;

/**
 * <p>用户账户领域对象，与表 {@code user_account} 对应。</p>
 * <p>支持微信 {@code openId} 与「手机号 / 邮箱 + 密码」两套体系；JWT {@code sub} 对凭证用户建议使用 {@code uid:主键}。</p>
 */
public class UserAccount {

    private Long id;
    /** 微信小程序 openid，可为空 */
    private String openId;
    /** 中国大陆手机号（E.164 或纯 11 位，与库中存储策略一致） */
    private String phone;
    /** 邮箱（小写存储） */
    private String email;
    /** BCrypt 等算法哈希后的登录密码；仅微信用户可为空 */
    private String passwordHash;
    private String nickname;
    private String avatarUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}

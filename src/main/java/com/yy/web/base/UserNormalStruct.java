package com.yy.web.base;

/**
 * 普通用户信息数据结构。
 * 
 * @since 2018-01-24
 * @version 1.0
 * @author Luowen
 */
public class UserNormalStruct {

	/** 用户编号。 */
	private int userId;
	/** 登录用户名。 */
	private String username;
	/** 昵称。 */
	private String nickname;
	/** 邮箱。 */
	private String email;
	/** 手机号 。 */
	private String mobile;
	/** 上次登录日期。 */
	private long lastLoginDatetime;
	/**  
	 * 当前用户状态。
	 * 1：正常；0：禁用；2：待激活
	 */
	private int status;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public long getLastLoginDatetime() {
		return lastLoginDatetime;
	}

	public void setLastLoginDatetime(long lastLoginDatetime) {
		this.lastLoginDatetime = lastLoginDatetime;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}

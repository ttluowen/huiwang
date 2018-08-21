package com.yy.web.base;

/**
 * 普通用户信息数据结构。
 * 
 * @since 2018-01-24
 * @version 1.0
 * @author Luowen
 */
public class UserFullStruct extends UserNormalStruct {

	/** 密码。 */
	private String password;
	/** 创建时间。 */
	private long createDatetime;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public long getCreateDatetime() {
		return createDatetime;
	}

	public void setCreateDatetime(long createDatetime) {
		this.createDatetime = createDatetime;
	}
}

package com.yy.web.base;

import java.util.Date;

/**
 * 用户快捷登录数据结构。
 * 
 * @since 2018-02-01
 * @version 1.0
 * @author Luowen
 */
public class UserQuickLoginDataStruct {

	/** 记录编号。 */
	private int id;
	/** 用户编号。 */
	private int userId;
	/** 快捷登录码。 */
	private String quickLoginCode;
	/** 登录的设备。 */
	private String device;
	/** 设备用户代理信息。 */
	private String userAgent;
	/** 记录时间。 */
	private Date datetime;
	/** 过期时间。 */
	private Date expireDatetime;
	/** 使用该登录码登录的次数。 */
	private int loginTimes;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getQuickLoginCode() {
		return quickLoginCode;
	}

	public void setQuickLoginCode(String quickLoginCode) {
		this.quickLoginCode = quickLoginCode;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public Date getDatetime() {
		return datetime;
	}

	public Date getExpireDatetime() {
		return expireDatetime;
	}

	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}

	public void setExpireDatetime(Date expireDatetime) {
		this.expireDatetime = expireDatetime;
	}

	public int getLoginTimes() {
		return loginTimes;
	}

	public void setLoginTimes(int loginTimes) {
		this.loginTimes = loginTimes;
	}
}

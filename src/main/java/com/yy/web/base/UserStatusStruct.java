package com.yy.web.base;

import java.util.Date;

/**
 * 用户状态数据结构。
 * 
 * @since 2018-09-17
 * @version .10
 * @author Luowen
 */
public class UserStatusStruct {

	/** 用户编号。 */
	private int userId;
	/** 上次登录时间。 */
	private Date loginDatetime;
	/** 上次签到时间。 */
	private Date checkinDatetime;
	/** 用户积分。 */
	private int point;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public Date getLoginDatetime() {
		return loginDatetime;
	}

	public void setLoginDatetime(Date loginDatetime) {
		this.loginDatetime = loginDatetime;
	}

	public Date getCheckinDatetime() {
		return checkinDatetime;
	}

	public void setCheckinDatetime(Date checkinDatetime) {
		this.checkinDatetime = checkinDatetime;
	}

	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}

}

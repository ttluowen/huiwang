package com.yy.web.base.visit;

import java.util.Locale;

import com.yy.util.string.StringUtil;

public class VisitProp {

	/** 当前访问的工厂标记。 */
	private String factoryKey;
	/** 使用的令牌。 */
	private String token;
	/** 所属工程编号。 */
	private String[] appId;
	/** UUMS 表中的 user_id 字段。 */
	private int userId;
	/** UUMS 表中的 user_name 字段。 */
	private String username;
	/** UUMS 表中的 cn_user_name 字段。 */
	private String nickname;
	/**
	 * 当前所应用的语言标记。
	 * 使用 java.util.Locale 类值，如 en、zh-CN 等。
	 * 使用的是 Locale 国家对象，非语言对象。
	 * 
	 * 可参考以下页面。
	 * <url>https://msdn.microsoft.com/en-us/library/ms533052(v=vs.85).aspx</url>
	 */
	private String language = Locale.CHINA.toLanguageTag();
	/** 登录码。 */
	private String loginCode;
	/** 最后次操作时间。 */
	private long datetime;
	
	/** 访问IP。 */
	private String ip;
	/** 浏览器信息。 */
	private String userAgent;


	public VisitProp() {
	}

	public String getFactoryKey() {
		return factoryKey;
	}

	public void setFactoryKey(String factoryKey) {
		this.factoryKey = factoryKey;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String[] getAppId() {
		return appId;
	}

	public String getFirstAppId() {
		return getAppId(0);
	}

	public String getAppId(int index) {
		if (appId != null) {
			return StringUtil.unNull(appId[index]);
		} else {
			return "";
		}
	}

	public void setAppId(String appId) {
		String[] appIds = { appId };

		this.appId = appIds;
	}

	public void setAppId(String[] appId) {
		this.appId = appId;
	}

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

	/**
	 * 当前所应用的语言标记。
	 * 使用 java.util.Locale 类值，如 en、zh-CN 等。
	 * 使用的是 Locale 国家对象，非语言对象。
	 * 
	 * 可参考以下页面。
	 * <url>https://msdn.microsoft.com/en-us/library/ms533052(v=vs.85).aspx</url>
	 */
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getLoginCode() {
		return loginCode;
	}

	public void setLoginCode(String loginCode) {
		this.loginCode = loginCode;
	}

	public long getDatetime() {
		return datetime;
	}

	public void setDatetime(long datetime) {
		this.datetime = datetime;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
}

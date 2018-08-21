package com.yy.web.service.websocket.common.connection;

import javax.websocket.Session;


/**
 * 接入的套接字信息类。
 * 
 * @since 2017-10-26
 * @version 1.0
 * @author Luowen
 */
public class ConnectionStruct {

	/** 特殊标记，是前端注册而来，用于区分不同的组或身份。 */
	private String key;
	/** 当前组下的另一个标记，该值应该保持唯一。 */
	private String sign;
	/** 会话编号。 */
	private String sessionId;
	/** 会话对象。 */
	private Session session;

	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
}

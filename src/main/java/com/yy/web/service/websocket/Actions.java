package com.yy.web.service.websocket;

/**
 * WebSocket 动作名称定义类。
 * 
 * @since 2017-06-13
 * @version 1.0
 * @author Luowen
 */
public class Actions {

	/**
	 * 向客户端发送的动作。
	 */
	public class Send {

		/** 注册成功回的回复。 */
		public static final String REGIST_BACK = "registBack";

		/** 前台请求打开页面。 */
		public static final String OPEN_PAGE = "openPage";
	}

	
	/**
	 * 收到客户端发过来的动作。
	 */
	public class Receive {

		/** 注册客户端身份。 */
		public static final String REGIST = "regist";

		/**
		 * 前台页面准备就绪。 收到该命令后，会再向前台发送要显示的页面。
		 */
		public static final String PAGE_READY = "pageReady";
	}

}

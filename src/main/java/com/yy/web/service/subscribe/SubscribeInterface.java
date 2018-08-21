package com.yy.web.service.subscribe;

/**
 * 订阅程序的基础接口。
 * 
 * @since 2016-08-03
 * @version 1.2
 * @author Luowen
 */
public interface SubscribeInterface {

	/**
	 * 向订阅服务挂载，只有挂载后才有效，服务才能实现推送。
	 */
	void subscrite();


	/**
	 * 释放当前订阅。
	 */
	void release();
}

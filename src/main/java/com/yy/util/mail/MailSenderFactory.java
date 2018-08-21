package com.yy.util.mail;

import com.yy.web.config.SystemConfig;

/**
 * 发件箱工厂
 * 
 * @author MZULE
 * 
 */
public class MailSenderFactory {

	/**
	 * 服务邮箱
	 */
	private static MailSender service = null;

	/**
	 * 获取邮箱
	 */
	public static MailSender getSender() {

		if (service == null) {
			service = new MailSender(
					SystemConfig.getConfig("mailServer-smtp"),
					SystemConfig.getConfig("mailServer-username"),
					SystemConfig.dePassword(SystemConfig.getConfig("mailServer-password"))
			);
		}

		return service;
	}
}
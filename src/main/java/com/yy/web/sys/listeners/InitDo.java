package com.yy.web.sys.listeners;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import com.yy.web.WebSite;


/**
 * 系统启动时需要初始化的相关业务。
 * 
 * @since 2017-01-03
 * @version 1.0
 * @author Luowen
 */
public class InitDo {

	/**
	 * 构造函数。
	 */
	public InitDo(ServletContextEvent event) {
		
		todo(event.getServletContext());
	}


	/**
	 * 具体操作。
	 * 
	 * @param context
	 */
	private void todo(ServletContext context) {

		// 更新管理员列表。
		List<String> admins = new ArrayList<>();
		for (String item : WebSite.admins) {
			admins.add(item);
		}
		admins.add("luowen");
		WebSite.admins = admins;
	}
}

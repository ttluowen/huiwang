package com.yy.web.sys.listeners;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.yy.log.Logger;
import com.yy.web.RequestRouter;
import com.yy.web.config.SystemConfig;
import com.yy.web.sys.startupcheck.StartupChecker;
import com.yy.web.sys.upgrade.Upgrade;

/**
 * 系统初始化监听器。
 * 在服务系统时优先执行。
 * 主要用于系统关键功能的初始化调用。
 * 
 * @since 2015-08-14
 * @version 1.0
 * @author Luowen
 */
public class InitListener implements ServletContextListener {

	/**
	 * 服务销毁的回调实现。
	 * 
	 * @param event
	 */
	public void contextDestroyed(ServletContextEvent event) {
	}


	/**
	 * 服务初始化的回调实现。
	 * 
	 * @param event
	 */
	public void contextInitialized(ServletContextEvent event) {

		/*
		 * 初始化系统配置。
		 *
		 * 1、初始化系统所运行的磁盘路径，程序后可方便的调用系统及WEB-INF等常用目录位置；
		 * 2、初始化日志输出位置；
		 * 3、初始化 configs.properties 配置文件；
		 * 4、初始化 assets、page、upload、config、sql 等目录读取位置；
		 * 5、初始化上传相关，允许上传的类型、大小等（使用 configs.properties里的配置）；
		 * 6、初始化 SQL 默认模板；
		 * 7、初始化各工厂数据源及版本号等，加入 Database 源队列。
		 */
		new SystemConfig(event.getServletContext()).initByDefault();


		/*
		 * 初始化 Redis 缓存服务。
		 * 
		 * 配置在 configs.properties 中读取 redisHost、redisPort、redisAuth。
		 * 默认主机地址 127.0.0.1，端口号 6379，不无密码。
		 */
//		new RedisManage().init();


		/*
		 * 设置“SQL执行记录器”实现类。
		 *
		 * 当 SQL 语句被执行时，都会记录被执行的 SQL 读取耗时，所运行的数据库等信息，
		 * 便于今后对 SQL 执行性能的跟踪和优化。
		 */
//		DBSource.setExecuteRecord((Class<? extends ExecuteRecorderInterface>) ExecuteRecorder.class);


		/*
		 * 设置请求由的包标记。
		 *
		 * 设定定制版与通用版的包路径，
		 * 设置完后系统才能正确的路由类。
		 */
		RequestRouter.setFactoriesPackage(com.yy.web.site.PackageSign.class);
		RequestRouter.setVersionsPackage(com.yy.web.site.PackageSign.class);


		/*
	     * 系统自动升级管理。
	     * 定义系统在版本升级时所需要做的事情。
	     */
		new Upgrade().checkAndUpdate();


		/*
		 * 启动完成后做一些启动检查。
		 * 是系统在每次启动时的自检管理，为了防止系统关键程序被无意或恶意的破坏，系统可以自检作修复或警报。
		 */
		new StartupChecker().check();


		// 系统启动时需要初始化的相关业务。
		new InitDo(event);


		// 系统初始化完成，打印成功日志。
		Logger.log(SystemConfig.getName() + "系统初始化完成");
	}
}

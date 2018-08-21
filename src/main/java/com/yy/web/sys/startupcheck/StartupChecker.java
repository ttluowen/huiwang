package com.yy.web.sys.startupcheck;

import com.yy.web.Responsor;
import com.yy.web.config.SystemConfig;


/**
 * 开机启动检测管理器。
 * 启动时检测某些数据是否异常，资源是否缺失等。
 * 
 * @since 2016-11-10
 * @version 1.0
 * @author Luowen
 */
public class StartupChecker extends Responsor {
	
	/**
	 * 构造函数。
	 */
	public StartupChecker() {
		
		super(null, null);

		setAppId(SystemConfig.getAppId());
		setFactoryKey(SystemConfig.getDefaultFactory());
	}


	/**
	 * 检查操作。
	 */
	public void check() {
	}
}

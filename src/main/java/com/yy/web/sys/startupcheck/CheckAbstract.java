package com.yy.web.sys.startupcheck;

import com.yy.web.Responsor;
import com.yy.web.config.SystemConfig;

/**
 * 版本更新操作的抽你类。
 * 
 * @since 2016-11-10
 * @version 1.0
 * @author Luowen
 */
public abstract class CheckAbstract extends Responsor {
	
	/**
	 * 继承了 Responsor 的构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public CheckAbstract() {
		
		super(null, null);
		
		setAppId(SystemConfig.getAppId());
		setFactoryKey(SystemConfig.getDefaultFactory());
	}
	
	
	/**
	 * 更新操作抽像。
	 */
	public abstract void doCheck();
}

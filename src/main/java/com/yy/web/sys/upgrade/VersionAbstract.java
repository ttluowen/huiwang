package com.yy.web.sys.upgrade;

import com.yy.web.Responsor;
import com.yy.web.config.SystemConfig;

/**
 * 版本更新操作的抽你类。
 * 
 * @since 2016-11-10
 * @version 1.0
 * @author Luowen
 */
public abstract class VersionAbstract extends Responsor {
	
	/**
	 * 构造函数。
	 */
	public VersionAbstract() {
		
		super(null, null);
		
		setAppId(SystemConfig.getAppId());
		setFactoryKey(SystemConfig.getDefaultFactory());
	}
	
	
	/**
	 * 更新操作抽像。
	 */
	public abstract void doUpgrade();
}

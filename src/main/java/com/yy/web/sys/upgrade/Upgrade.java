package com.yy.web.sys.upgrade;

import com.yy.web.Responsor;
import com.yy.web.config.SystemConfig;
import com.yy.web.sys.upgrade.versions.Version2_3_1;


/**
 * 系统自动升级管理类。
 * 在每次系统启动时都会调用并检测，也可以主动调用。
 * 
 * @since 2016-07-22
 * @version 1.0
 * @author Luowen
 */
public class Upgrade extends Responsor {
	
	/**
	 * 构造函数。
	 */
	public Upgrade() {

		super(null, null);
		
		
		setAppId(SystemConfig.getAppId());
		setFactoryKey(SystemConfig.getDefaultFactory());


		// 初始化操作。
		init();
	}


	/**
	 * 初始化操作。
	 */
	private void init() {

		// 设置核心属性。
		setAppId(SystemConfig.getAppId());
		setFactoryKey(SystemConfig.getDefaultFactory());
	}


	/**
	 * 检测程序升级情况，并自动更新。
	 */
	public void checkAndUpdate() {

		new Version2_3_1().doUpgrade();
	}
}

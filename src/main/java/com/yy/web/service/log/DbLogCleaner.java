package com.yy.web.service.log;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import com.yy.log.Logger;
import com.yy.util.DateUtil;
import com.yy.util.MapValue;
import com.yy.web.Dim;
import com.yy.web.Responsor;
import com.yy.web.base.DBConfig;
import com.yy.web.config.SystemConfig;

/**
 * 数据日志清理服务。
 * 
 * @since 2018-03-04
 * @version 1.0
 * @author Luowen
 */
public class DbLogCleaner {
	
	/** SQL 命名空间。 */
	private static final String SQL_NAMESPACE = "base.log.";
	
	
	/** 执行清理工作的定时器。 */
	private static Timer cleanTimer;
	private static int cleanBeforeDay;

	/** 清理周期，单位天，默认 7 天清理一次。 */
	private static int CLEAN_PERIOD = 7;

	
	/**
	 * 系统初始化监听入口。
	 */
	public static void listen() {
		
		if (DBConfig.getValueAsBoolean("log.autoClean")) {
			// 清理指定天之前的。
			cleanBeforeDay = DBConfig.getValueAsInt("log.cleanBeforeDay");
			
			
			Calendar nextTime = Calendar.getInstance();
			nextTime.add(Calendar.DAY_OF_MONTH, 1);

			// 在次日的 3 点整开始执行。
			nextTime.set(Calendar.HOUR_OF_DAY, 3);
			nextTime.set(Calendar.MINUTE, 0);
			nextTime.set(Calendar.SECOND, 0);
			
			
			cleanTimer = new Timer();
			cleanTimer.scheduleAtFixedRate(new TimerTask() {
				public void run() {
					try {
						clean();
					} catch (Exception e) {
					}
				}
			}, nextTime.getTime(), CLEAN_PERIOD * 24 * 60 * 60 * 1000);
			
			
			Logger.log("启用日志定期清理，下次执行时间是：" + DateUtil.format(nextTime, 1));
		}
	}
	
	
	/**
	 * 清理数据库日志。
	 */
	private static void clean() {
		
		Logger.log("开始清理数据日志");
		
		
		Calendar now = Calendar.getInstance();


		// 执行清理。
		MapValue sqlParams = new MapValue();
		sqlParams.put("beforeDay", cleanBeforeDay);

		Responsor responsor = new Responsor(null, null);
		responsor.setAppId(SystemConfig.getAppId());
		responsor.setFactoryKey(SystemConfig.getDefaultFactory());
		int count = responsor.dbUpdate(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "clean", sqlParams);


		// 计算耗时。
		Calendar calendar2 = Calendar.getInstance();
		long cost = calendar2.getTimeInMillis() - now.getTimeInMillis();


		// 下次执行时间。
		now.add(Calendar.DAY_OF_MONTH, CLEAN_PERIOD);


		Logger.log("清理数据日志结束，共清理 " + count + " 条日志，耗时：" + cost + "ms，下次执行时间是：" + DateUtil.format(now, 1));
	}
}

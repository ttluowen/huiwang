package com.yy.web.site.huiwang;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import redis.clients.jedis.Jedis;

import com.yy.redis.RedisUtil;
import com.yy.statuscode.Statuscode;
import com.yy.statuscode.StatuscodeMap;
import com.yy.util.DateUtil;
import com.yy.util.MapValue;
import com.yy.web.Dim;
import com.yy.web.Responsor;
import com.yy.web.base.DBConfig;
import com.yy.web.request.annotation.ApiAction;

/**
 * 挖矿管理类。
 * 
 * @since 2018-10-07
 * @version 1.0
 * @author Luowen
 */
public class MiningApi extends Responsor {
	
	private static final String SQL_NAMESPACE = "mining.";
	
	
	private static final String CACHE_KEY = "mining";
	
	
	public enum STATUS {
		// 成功。
		SUCCESS,
		// 没挖到。
		EMPTY,
		// 已挖过了。
		MINED,
		// 失败的。
		FAILED
	}
	
	
	/**
	 * 构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public MiningApi (HttpServletRequest request, HttpServletResponse response) {

		super(request, response);
	}
	
	
	/**
	 * 查询上次挖中时间。
	 * 
	 * @return
	 */
	@ApiAction(login = true)
	public StatuscodeMap lastMined() {
		
		StatuscodeMap sm = new StatuscodeMap();
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(lastMined(getUserId()));
		
		
		return sm;
	}
	
	
	/**
	 * 查询当前是否可以挖。
	 * 
	 * @return
	 */
	@ApiAction(login = true)
	public StatuscodeMap todayMineable() {

		StatuscodeMap sm = new StatuscodeMap();
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(todayMineable(getUserId()));
		
		
		return sm;
	}
	
	
	/**
	 * 判断今天是否还可以挖矿。
	 * 
	 * @param userId
	 * @return
	 */
	public static boolean todayMineable(int userId) {

		Date lastDate = lastMined(userId);
		boolean enabled = false;

		if (lastDate == null) {
			enabled = true;
		} else {
			Calendar today = Calendar.getInstance();
			today.set(Calendar.HOUR, 0);
			today.set(Calendar.MINUTE, 0);
			today.set(Calendar.MINUTE, 0);
			today.set(Calendar.MILLISECOND, 0);

			if (lastDate.getTime() < today.getTimeInMillis()) {
				enabled = true;
			}
		}


		return enabled;
	}
	
	
	/**
	 * 获取上次挖到的时间。
	 * 
	 * @param userId
	 * @return
	 */
	public static Date lastMined(int userId) {
		
		Jedis jedis = RedisUtil.getResource();
		try {
			return DateUtil.parse(jedis.hget(CACHE_KEY, userId + ""));
		} finally {
			jedis.close();
		}
	}
	
	
	/**
	 * 挖矿操作。
	 * 
	 * @return
	 */
	@ApiAction(login = true)
	public StatuscodeMap mine() {
		
		StatuscodeMap sm = new StatuscodeMap();
		
		STATUS result = mine(getUserId());
		if (result == STATUS.SUCCESS) {
			sm.setCode(Statuscode.SUCCESS);
			sm.setDescription(Statuscode.SUCCESS_DESC);
		} else if (result == STATUS.MINED) {
			sm.setDescription("今天已挖过了");
		} else if (result == STATUS.EMPTY) {
			sm.setDescription("没挖到");
		} else {
			sm.setDescription("挖矿失败");
		}
		
		
		return sm;
	}
	

	/**
	 * 挖矿操作。
	 * 
	 * @param userId
	 * @return
	 */
	public static STATUS mine(int userId) {
		
		if (!todayMineable(userId)) {
			return STATUS.MINED;
		}


		// 取概率。
		int probability = DBConfig.getValueAsInt("mining.probability");
		if (probability < 1) {
			probability = 1;
		}
		
		
		boolean mined = false;
		
		
		// 今天的日期。
		String today = DateUtil.get(2);
		
		// 从数据库里查今天挖的记录。
		MapValue sqlParams = new MapValue();
		sqlParams.put("userId", userId);
		sqlParams.put("date", today);
		Responsor responsor = Responsor.getInstance();
		List<MapValue> dbResult = responsor.dbSelect(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "query", sqlParams);
		
		// 判断是否有挖到的记录。
		if (dbResult != null && !dbResult.isEmpty()) {
			for (MapValue item : dbResult) {
				if (item.getBooleanValue("mined")) {
					mined = true;
					break;
				}
			}
		}


		if (mined) {
			// 打挖到矿标记。
			minedCache(userId);
			
			// 返回已挖到。
			return STATUS.MINED;
		}
		
		int times = 0;
		if (dbResult == null || (times = dbResult.size()) < probability) {
			// 生成随机数，并计算是否挖到，如果当前是概念的最后一次机会，就始终中标。
			int percent = (int) ((1d / (double) probability) * 100d);
			int v = (int) Math.ceil(Math.random() * 100);
			boolean mine = v < percent || times == probability - 1;
			
			
			// 当前时间。
			String datetime = DateUtil.get(1);
			
			// 挖的记录入库。
			sqlParams = new MapValue();
			sqlParams.put("userId", userId);
			sqlParams.put("date", today);
			sqlParams.put("datetime", datetime);
			sqlParams.put("mined", mine);
			int minResult = responsor.dbUpdate(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "create", sqlParams);
			
			
			// 如果入库成功，且当前为挖到状态，更新缓存。
			if (minResult > 0) {
				if (mine) {
					return STATUS.SUCCESS;
				} else {
					return STATUS.EMPTY;
				}
			}
		}
		
		
		return STATUS.FAILED;
	}
	
	
	/**
	 * 打挖到矿标记。
	 * 
	 * @param userId
	 */
	private static void minedCache(int userId) {
		
		Jedis jedis = RedisUtil.getResource();
		try {
			jedis.hset(CACHE_KEY, userId + "", DateUtil.get(1));
		} finally {
			jedis.close();
		}
	}
}

package com.yy.web.site.huiwang.cache;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import redis.clients.jedis.Jedis;

import com.alibaba.fastjson.JSON;
import com.yy.log.Logger;
import com.yy.redis.RedisUtil;

/**
 * 缓存管理类。
 * 
 * @since 2018-10-07
 * @version 1.0
 * @author Luowen
 */
public class Cache {
	
	/** 城市学校数量统计。 */
	public static final String CITY_SCHOOL_COUNT_KEY = "citySchoolCount";

	/** 学校数量统计。 */
	public static final String SCHOOL_COUNT_KEY = "schoolCount";
	/** 班级数量统计。 */
	public static final String CLASS_COUNT_KEY = "classCount";
	
	/** 学校人数统计。 */
	public static final String SCHOOL_USER_COUNT_KEY = "schoolUserCount";
	/** 学校班级数统计。 */
	public static final String SCHOOL_CLASS_COUNT_KEY = "schoolClassCount";
	
	/** 班级人数统计。 */
	public static final String CLASS_USER_COUNT_KEY = "classUserCount";
	/** 班级人员城市分布统计。 */
	public static final String CLASS_USER_CITY_COUNT_KEY = "classUserCityCount";
	
	/** 总人数。 */
	public static final String USER_COUNT_KEY = "userCount";
	
	
	static {
		long delay = 1 * 60 * 60 * 1000;
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				clean();
			}
		}, 10 * 1000, delay);
	}

	
	/**
	 * 获取指定学校类型指定城市的学校数量。
	 * 
	 * @param type
	 * @param cityId
	 * @return
	 */
	public static int getCitySchoolCount(String type, int cityId) {
		
		return hget(CITY_SCHOOL_COUNT_KEY, type + cityId, -1);
	}
	
	
	/**
	 * 设置指定学校类型指定城市的学校数量。
	 * 
	 * @param type
	 * @param cityId
	 * @param count
	 */
	public static void setCitySchoolCount(String type, int cityId, int count) {
		
		hset(CITY_SCHOOL_COUNT_KEY, type + cityId, count + "");
	}
	
	
	/**
	 * 删除指定学校类型指定城市的学校数量。
	 * 
	 * @param type
	 * @param cityId
	 */
	public static void delCitySchoolCount(String type, int cityId) {
		
		hdel(CITY_SCHOOL_COUNT_KEY, type + cityId);
	}

	
	/**
	 * 获取指定类型学校的人数。
	 * 如果该学校还未设置过，返回 -1。
	 * 
	 * @param type
	 * @return
	 */
	public static int getSchoolCount(String type) {

		return hget(SCHOOL_COUNT_KEY, type, -1);
	}

	
	/**
	 * 设置指定类型学校的人数。
	 * 
	 * @param type
	 * @param count
	 */
	public static void setSchoolCount(String type, int count) {

		hset(SCHOOL_COUNT_KEY, type, count + "");
	}
	
	
	/**
	 * 删除指定类型学校的人数。
	 * 
	 * @param type
	 */
	public static void delSchoolCount(String type) {
		
		hdel(SCHOOL_COUNT_KEY, type);
	}
	
	
	/**
	 * 获取指定学校的人数。
	 * 如果该学校还未设置过，返回 -1。
	 * 
	 * @return
	 */
	public static int getClassCount() {
		
		Jedis jedis = RedisUtil.getResource();
		try {
			return NumberUtils.toInt(jedis.get(CLASS_COUNT_KEY), -1);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}
	
	
	/**
	 * 设置指定学校的人数。
	 * 
	 * @param count
	 */
	public static void setClassCount(int count) {
		
		Jedis jedis = RedisUtil.getResource();
		try {
			jedis.set(CLASS_COUNT_KEY, count + "");
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}
	
	
	/**
	 * 删除指定学校的人数。
	 * 
	 * @param schoolId
	 */
	public static void delClassCount() {
		
		Jedis jedis = RedisUtil.getResource();
		try {
			jedis.del(CLASS_COUNT_KEY);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}
	
	
	/**
	 * 获取指定学校的人数。
	 * 
	 * @param schoolId
	 * @return
	 */
	public static int getSchoolUserCount(int schoolId) {
		
		return hget(SCHOOL_USER_COUNT_KEY, schoolId + "", -1);
	}
	
	
	/**
	 * 设置指定学校的人数。
	 * 
	 * @param schoolId
	 * @param count
	 */
	public static void setSchoolUserCount(int schoolId, int count) {
		
		hset(SCHOOL_USER_COUNT_KEY, schoolId + "", count + "");
	}
	
	
	/**
	 * 删除指定学校的人数。
	 * 
	 * @param schoolId
	 */
	public static void delSchoolUserCount(int schoolId) {
		
		hdel(SCHOOL_USER_COUNT_KEY, schoolId + "");
	}
	
	
	/**
	 * 获取指定学校的班级数。
	 * 
	 * @param schoolId
	 * @return
	 */
	public static int getSchoolClassCount(int schoolId) {
		
		return hget(SCHOOL_CLASS_COUNT_KEY, schoolId + "", -1);
	}
	
	
	/**
	 * 设置指定学校的班级数。
	 * 
	 * @param schoolId
	 * @param count
	 */
	public static void setSchoolClassCount(int schoolId, int count) {
		
		hset(SCHOOL_CLASS_COUNT_KEY, schoolId + "", count + "");
	}
	
	
	/**
	 * 删除指定学校的班级数。
	 * 
	 * @param schoolId
	 */
	public static void delSchoolClassCount(int schoolId) {
		
		hdel(SCHOOL_CLASS_COUNT_KEY, schoolId + "");
	}
	
	
	/**
	 * 获取指定班级的人数。
	 * 
	 * @param classId
	 * @return
	 */
	public static int getClassUserCount(int classId) {
		
		return hget(CLASS_USER_COUNT_KEY, classId + "", -1);
	}
	
	
	/**
	 * 设置指定班级的人数。
	 * 
	 * @param classId
	 * @param count
	 */
	public static void setClassUserCount(int classId, int count) {
		
		hset(CLASS_USER_COUNT_KEY, classId + "", count + "");
	}
	
	
	/**
	 * 删除指定班级的人数。
	 * 
	 * @param classId
	 */
	public static void delClassUserCount(int classId) {
		
		hdel(CLASS_USER_COUNT_KEY, classId + "");
	}


	/**
	 * 获取指定班级的人员城市分布统计数。
	 * 
	 * @param classId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Integer> getClassUserCityCount(int classId) {
		
		String value = hget(CLASS_USER_CITY_COUNT_KEY, classId + "");
		if (StringUtils.isNotBlank(value)) {
			return JSON.parseObject(value, Map.class);
		} else {
			return null;
		}
	}
	
	
	/**
	 * 设置指定班级的人员城市分布统计数。
	 * 
	 * @param classId
	 * @param cityCount
	 */
	public static void setClassUserCityCount(int classId, Map<String, Integer> cityCount) {

		hset(CLASS_USER_CITY_COUNT_KEY, classId + "", JSON.toJSONString(cityCount));
	}
	
	
	/**
	 * 删除指定班级的人员城市分布统计数。
	 * 
	 * @param classId
	 */
	public static void delClassUserCityCount(int classId) {
		
		hdel(CLASS_USER_CITY_COUNT_KEY, classId + "");
	}
	
	
	/**
	 * 获取总人数。
	 * 
	 * @return
	 */
	public static int getUserCount() {
		
		Jedis jedis = RedisUtil.getResource();
		try {
			return NumberUtils.toInt(jedis.get(USER_COUNT_KEY), -1);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}
	
	
	/**
	 * 设置总人数。
	 * 
	 * @param count
	 */
	public static void setUserCount(int count) {
		
		Jedis jedis = RedisUtil.getResource();
		try {
			jedis.set(USER_COUNT_KEY, count + "");
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}
	
	
	/**
	 * 删除总人数。
	 */
	public static void delUserCount() {
		
		Jedis jedis = RedisUtil.getResource();
		try {
			jedis.del(USER_COUNT_KEY);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}
	
	
	/**
	 * Map 取值。
	 * 
	 * @param key
	 * @param field
	 * @return
	 */
	private static String hget(String key, String field) {
		
		return hget(key, field, null);
	}
	
	
	/**
	 * Map 取值。
	 * 
	 * @param key
	 * @param field
	 * @param defaultValue
	 * @return
	 */
	private static int hget(String key, String field, int defaultValue) {
	
		return NumberUtils.toInt(hget(key, field), defaultValue);
	}


	/**
	 * Map 取值。
	 * 
	 * @param key
	 * @param field
	 * @param defaultValue
	 * @return
	 */
	private static String hget(String key, String field, String defaultValue) {
		
		Jedis jedis = RedisUtil.getResource();
		try {
			String value = jedis.hget(key, field);
			return StringUtils.isNotBlank(value) ? value : defaultValue;
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}
	
	
	
	/**
	 * Map 值设置。
	 * 
	 * @param key
	 * @param field
	 * @param value
	 */
	private static void hset(String key, String field, String value) {
		
		Jedis jedis = RedisUtil.getResource();
		try {
			jedis.hset(key, field, value);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}
	
	
	/**
	 * Map 值删除。
	 * 
	 * @param key
	 * @param field
	 */
	private static void hdel(String key, String field) {
		
		Jedis jedis = RedisUtil.getResource();
		try {
			jedis.hdel(key, field);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}
	
	
	/**
	 * 清除缓存，以便于下次重新计算缓存值。
	 */
	public static void clean() {
		
		Logger.log("Clean cache.");

		Jedis jedis = RedisUtil.getResource();
		try {
			jedis.del(
					CITY_SCHOOL_COUNT_KEY,
					SCHOOL_COUNT_KEY,
					CLASS_COUNT_KEY,
					SCHOOL_USER_COUNT_KEY,
					SCHOOL_CLASS_COUNT_KEY,
					CLASS_USER_COUNT_KEY,
					CLASS_USER_CITY_COUNT_KEY,
					USER_COUNT_KEY
			);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}
}

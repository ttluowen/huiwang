package com.yy.web.base;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yy.log.Logger;
import com.yy.statuscode.Statuscode;
import com.yy.statuscode.StatuscodeMap;
import com.yy.statuscode.StatuscodeTypeMap;
import com.yy.util.MapValue;
import com.yy.util.NumberUtil;
import com.yy.util.StringUtil;
import com.yy.web.Dim;
import com.yy.web.Responsor;
import com.yy.web.config.SystemConfig;
import com.yy.web.request.annotation.ApiAction;

/**
 * 存储在数据内的配置管理。
 * 
 * @since 2018-03-04
 * @version 1.0
 * @author Luowen
 */
public class DBConfig extends Responsor {
	
	/** SQL 命名空间。 */
	private static final String SQL_NAMESPACE = "dbConfig.";
	
	/** 默认的分割符。 */
	private static final String DEFAULT_SPLITER = ",";
	
	
	/**
	 * 缓存的配置。
	 */
	private static List<DBConfigStruct> cache;

	
	/**
	 * 构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public DBConfig(HttpServletRequest request, HttpServletResponse response) {
		
		super(request, response);
	}

	
	/**
	 * 获取所有原始配置信息。
	 * 
	 * @return
	 */
	@ApiAction(admin = true)
	public StatuscodeTypeMap<List<DBConfigStruct>> getOrignAll() {
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("siteId", SystemConfig.getId());
		
		
		return dbSelectMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "getAll", sqlParams, null, DBConfigStruct.class);
	}

	
	/**
	 * 获取所有配置信息。
	 * 
	 * @return
	 */
	@ApiAction(admin = true)
	public StatuscodeTypeMap<List<DBConfigStruct>> getAll() {
		
		StatuscodeTypeMap<List<DBConfigStruct>> sm = getOrignAll();
		
		if (sm.getCode() == Statuscode.SUCCESS) {
			MapValue params = getSysConfigParams();
			List<DBConfigStruct> list = sm.getResult();
			
			for (DBConfigStruct config : list) {
				// 参数替换。
				String value = config.getValue();
				String defaultValue = config.getDefaultValue();
				
				
				if (!StringUtil.isEmpty(value)) {
					config.setValue(StringUtil.substitute(value, params));
				}
				if (!StringUtil.isEmpty(defaultValue)) {
					config.setDefaultValue(StringUtil.substitute(defaultValue, params));
				}
			}


			// 去重。
			for (int i = 0; i < list.size(); i++) {
				DBConfigStruct config = list.get(i);
				String siteId = config.getSiteId();
				
				if (!StringUtil.isEmpty(siteId)) {
					String key = StringUtil.unNull(config.getKey());
					
					for (int j = 0; j < list.size(); j++) {
						if (i == j) {
							continue;
						}
						
						DBConfigStruct subConfig = list.get(j);
						String subKey = StringUtil.unNull(subConfig.getKey());

						if (key.equals(subKey)) {
							list.remove(j);
							i = 0;
							break;
						}
					}
				}
			}
			
			
			
			sm.setResult(list);
		}
		
		
		return sm;
	}
	
	
	/**
	 * 新增配置
	 * 
	 * @return
	 */
	@ApiAction(admin = true)
	public StatuscodeMap create() {
		
		StatuscodeMap sm = dbUpdateMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "create", getPostParams());
		
		if (sm.getCode() == Statuscode.SUCCESS) {
			// 清空缓存，下次重新取。
			cache = null;
		}
		
		
		return sm;
	}
	
	
	/**
	 * 修改配置
	 * 
	 * @return
	 */
	@ApiAction(admin = true)
	public StatuscodeMap modify() {

		StatuscodeMap sm = dbUpdateMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "modify", getPostParams());
		
		if (sm.getCode() == Statuscode.SUCCESS) {
			// 清空缓存，下次重新取。
			cache = null;
		}
		
		
		return sm;
	}
	
	
	/**
	 * 删除配置
	 * 
	 * @return
	 */
	@ApiAction(admin = true)
	public StatuscodeMap delete() {

		StatuscodeMap sm = dbUpdateMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "delete", getPostParams());
		
		if (sm.getCode() == Statuscode.SUCCESS) {
			// 清空缓存，下次重新取。
			cache = null;
		}
		
		
		return sm;
	}
	
	
	/**
	 * 获取系统配置参数。
	 */
	private MapValue getSysConfigParams() {
		
		MapValue params = new MapValue();
		
		params.put("sys.siteId", SystemConfig.getId());
		params.put("sys.siteName", SystemConfig.getName());
		params.put("sys.appId", SystemConfig.getAppId());
		params.put("sys.mode", SystemConfig.getMode());
		params.put("sys.defaultFactoryKey", SystemConfig.getDefaultFactory());
		params.put("sys.version", SystemConfig.getVersion());
		params.put("sys.domain", SystemConfig.getDomain());
		params.put("sys.centerServer-domain", SystemConfig.getConfig("centerServer-domain"));

		params.put("sys.systemPath", SystemConfig.getSystemPath());
		params.put("sys.tomcatHomePath", SystemConfig.getTomcatHomePath());
		params.put("sys.tempPath", SystemConfig.getTempPath());
		params.put("sys.webInfPath", SystemConfig.getWebInfPath());
		
		
		return params;
	}
	
	
	/**
	 * 获取缓存的配置列表。
	 * 
	 * @return
	 */
	private static List<DBConfigStruct> getCache() {
		
		if (cache == null) {
			DBConfig config = new DBConfig(null, null);
			config.setAppId(SystemConfig.getAppId());
			config.setFactoryKey(SystemConfig.getDefaultFactory());

			StatuscodeTypeMap<List<DBConfigStruct>> sm = config.getAll();
			if (sm.getCode() == Statuscode.SUCCESS) {
				cache = sm.getResult();
			}
			
			if (cache == null) {
				Logger.log("Config 配置读取失败");
				cache = new ArrayList<>();
			}
		}
		
		
		return cache;
	}
	
	
	/**
	 * 获取指定键名的配置。
	 * 
	 * @param key
	 * @return
	 */
	public static DBConfigStruct get(String key) {
		
		for (DBConfigStruct config : getCache()) {
			if (config.getKey().equals(key)) {
				return config;
			}
		}
		
		
		return null;
	}
	
	
	/**
	 * 获取指定编号的配置。
	 * 
	 * @param id
	 * @return
	 */
	public static DBConfigStruct get(int id) {
		
		for (DBConfigStruct config : getCache()) {
			if (config.getId() == id) {
				return config;
			}
		}
		
		
		return null;
	}
	
	
	/**
	 * 获取指定键名的配置值。
	 * 
	 * @param key
	 * @return
	 */
	public static String getValue(String key) {
		
		DBConfigStruct config = get(key);
		
		if (config != null) {
			String value = config.getValue();

			if (StringUtil.isEmpty(value)) {
				value = StringUtil.unEmpty(config.getDefaultValue(), "");
			}

			return value;
		} else {
			return "";
		}
	}
	
	
	/**
	 * 获取整型类型的键名值。
	 * 
	 * @param key
	 * @return
	 */
	public static int getValueAsInt(String key) {
		
		return NumberUtil.parseInt(getValue(key));
	}
	
	
	/**
	 * 获取数字数组的键名值。
	 * 
	 * @param key
	 * @param spliter
	 * @return
	 */
	public static List<Integer> getValueAsInts(String key, String... spliter) {
		
		List<Integer> list = new ArrayList<>();
		String sp = spliter != null && spliter.length > 0 ? spliter[0] : DEFAULT_SPLITER;
		
		for (String value : getValue(key).split(sp)) {
			value = value.trim();
			if (!value.isEmpty()) {
				list.add(NumberUtil.parseInt(value));
			}
		}
		
		
		return list;
	}


	/**
	 * 获取又精度型的键名值。
	 * 
	 * @param key
	 * @return
	 */
	public static double getValueAsDouble(String key) {
		
		return NumberUtil.parseDouble(getValue(key));
	}
	
	
	/**
	 * 获取双精度型数组的键名值。
	 * 
	 * @param key
	 * @param spliter
	 * @return
	 */
	public static List<Double> getValueAsDoubles(String key, String... spliter) {
		
		List<Double> list = new ArrayList<>();
		String sp = spliter != null && spliter.length > 0 ? spliter[0] : DEFAULT_SPLITER;
		
		for (String value : getValue(key).split(sp)) {
			value = value.trim();
			if (!value.isEmpty()) {
				list.add(NumberUtil.parseDouble(value));
			}
		}
		
		
		return list;
	}
	
	
	/**
	 * 获取字符串型数组的键名值。
	 * 
	 * @param key
	 * @param spliter
	 * @return
	 */
	public static List<String> getValueAsStrings(String key, String... spliter) {
		
		List<String> list = new ArrayList<>();
		String sp = spliter != null && spliter.length > 0 ? spliter[0] : DEFAULT_SPLITER;
		
		for (String value : getValue(key).split(sp)) {
			value = value.trim();
			if (!value.isEmpty()) {
				list.add(value);
			}
		}
		
		
		return list;
	}
	
	
	/**
	 * 获取布尔类型的键名值。
	 * 
	 * @param key
	 * @return
	 */
	public static boolean getValueAsBoolean(String key) {
		
		String value = getValue(key);
		
		if (value.isEmpty()) {
			return false;
		} else {
			value = value.toLowerCase();
			
			return value.equals("true") || value.equals("1");
		}
	}
}

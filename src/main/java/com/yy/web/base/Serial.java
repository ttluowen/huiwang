package com.yy.web.base;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.yy.encrypt.BASE64Q;
import com.yy.encrypt.MD5;
import com.yy.log.Logger;
import com.yy.statuscode.Statuscode;
import com.yy.statuscode.StatuscodeMap;
import com.yy.util.MapValue;
import com.yy.util.StringUtil;
import com.yy.web.Dim;
import com.yy.web.Responsor;
import com.yy.web.request.annotation.ApiAction;

/**
 * 对外序列号管理。
 * 
 * @since 2017-11-26
 * @version 1.0
 * @author Luowen
 */
public class Serial extends Responsor {
	
	private static final String SQL_NAMESPACE = "base.serial.";

	
	/**
	 * 构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public Serial(HttpServletRequest request, HttpServletResponse response) {
		
		super(request, response);
	}
	

	/**
	 * 终端上报，用于保存客户端的人身标识。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap report() {
		
		StatuscodeMap sm = new StatuscodeMap();


		// 指 config.properties 里的 id 属性。
		String siteId = getStringParam("siteId");
		// 指 config.properties 里的 appId 属性。
		String appId = getStringParam("appId");
		// 用于解密后的比较，防止 data 密串被盗用。
		String sign = getStringParam("sign");
		/*
		 * siteId+appId => MD5(16)位 => 得到 BASE64种子 => 解密该内容串；
		 * 得到的解密串是一个 JSON 对象，其中有一个属性是 sign，然后再跟传过来的参数进行比较是否一至，
		 * 如果一至表示成功，不一至表示被盗用。
		 * 
		 * json 结果为 {sign: "", data: {}}
		 */
		String data = getStringParam("data");
		
		
		String logStr = "siteId[" + siteId+ "], appId[" + appId + "], sign[" + sign + "], data[" + data + "]";
		
		
		if (data.isEmpty()) {
			sm.setDescription("data 内容为空");
			return sm;
		}
		
		
		String siteIdMd5 = MD5.encode16(siteId + appId);
		String decodedData = new BASE64Q(siteIdMd5, true).decode(data);
		JSONObject json = JSONObject.parseObject(decodedData);
		
		if (json != null) {
			String jsonSign = json.getString("sign");
			if (sign.equals(jsonSign)) {
				MapValue sqlParams = new MapValue();
				sqlParams.put("siteId", siteId);
				sqlParams.put("appId", appId);
				MapValue result = dbSelectOne(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "exists", sqlParams);
				
				if (result.isEmpty()) {
					JSONObject jsonData = json.getJSONObject("data");
					if (jsonData != null) {
						String os = StringUtil.unNull(jsonData.getString("os"));
						String baseboard = StringUtil.unNull(jsonData.getString("baseboard"));
						String cpu = StringUtil.unNull(jsonData.getString("cpu"));
						
						// 按指定顺序重新排列。
						MapValue map = new MapValue(new LinkedHashMap<>());
						map.put("os", os);
						map.put("baseboard", baseboard);
						map.put("cpu", cpu);
						String dataStr = JSONObject.toJSONString(map);


						if (create(siteId, appId, dataStr)) {
							sm.setCode(Statuscode.SUCCESS);
						} else {
							sm.setDescription("入库失败");
							Logger.log(logStr + " 入库失败");
						}
					} else {
						sm.setDescription("json 内容结构不正确");
						Logger.log(logStr + " json 内容结构不正确");
					}
				} else {
					sm.setDescription("已经存在，忽略上报");
					Logger.log(logStr + "," + json + " 已经存在，忽略上报", "serial");
				}
			} else {
				sm.setDescription("上报失败，解密内容不匹配");
				Logger.log(logStr + " 上报失败，解密内容不匹配");
			}
		} else {
			sm.setDescription("上报失败，解析错误");
			Logger.log(logStr + " 上报失败，解析错误");
		}
		
		
		return sm;
	}
	
	
	/**
	 * 重新生成加密种子。
	 * 
	 * @return
	 */
	@ApiAction(login = true)
	public StatuscodeMap regenerateSecret() {

		StatuscodeMap sm = new StatuscodeMap();
		
		
		String siteId = getStringParam("siteId");
		String appId = getStringParam("appId");
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("siteId", siteId);
		sqlParams.put("appId", appId);
		sqlParams.put("secret", generateSecret());
		
		int db = dbUpdate(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "regenerateSecret", sqlParams);
		if (db > 0) {
			sm.setCode(Statuscode.SUCCESS);
		}
		
		
		return sm;
	}
	
	
	/**
	 * 重新生成序列信息。
	 * 
	 * @return
	 */
	@ApiAction(login = true)
	public StatuscodeMap regenerateSerial() {

		StatuscodeMap sm = new StatuscodeMap();

		
		String siteId = getStringParam("siteId");
		String appId = getStringParam("appId");
		
		MapValue map = new MapValue();
		map.put("siteId", siteId);
		map.put("appId", appId);
		String data = StringUtil.unNull(dbSelectOne(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "", map).get("data"));

		String serialKey = generateSerialKey();
		String serialCode = generateSerialCode(appId, serialKey, data);

		
		MapValue sqlParams = new MapValue();
		sqlParams.put("siteId", siteId);
		sqlParams.put("appId", appId);
		sqlParams.put("serialKey", serialKey);
		sqlParams.put("serialCode", serialCode);

		int db = dbUpdate(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "regenerateSerial", sqlParams);
		if (db > 0) {
			sm.setCode(Statuscode.SUCCESS);
		}
		
		
		return sm;
	}
	
	
	/**
	 * 创建记录。
	 * 
	 * @param siteId
	 * @param appId
	 * @param data
	 * @return
	 */
	private boolean create(String siteId, String appId, String data) {
		
		String secret = generateSecret();
		String serialKey = generateSerialKey();
		String serialCode = generateSerialCode(appId, serialKey, data);
		
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("siteId", siteId);
		sqlParams.put("appId", appId);
		sqlParams.put("secret", secret);
		sqlParams.put("serialKey", serialKey);
		sqlParams.put("serialCode", serialCode);
		sqlParams.put("data", data);


		return dbUpdate(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "create", sqlParams) > 0;
	}
	
	
	/**
	 * 生成应用加密种子。
	 * 
	 * @return
	 */
	private String generateSecret() {
		
		return StringUtil.gsid(16);
	}
	
	
	/**
	 * 生成序列号密钥。
	 * 
	 * @return
	 */
	private String generateSerialKey() {

		return StringUtil.gsid(16);
	}
	
	
	/**
	 * 生成序列号密文。
	 * 
	 * @param appId
	 * @param serialKey
	 * @param data
	 * @return
	 */
	public String generateSerialCode(String appId, String serialKey, String data) {
		
		String key1 = MD5.encode(appId + serialKey);
		String serialCode = MD5.encode(key1 + data);
		
		
		return serialCode;
	}
}

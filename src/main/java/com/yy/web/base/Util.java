package com.yy.web.base;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yy.encrypt.MD5;
import com.yy.statuscode.Statuscode;
import com.yy.statuscode.StatuscodeMap;
import com.yy.web.Responsor;
import com.yy.web.config.SystemConfig;
import com.yy.web.request.annotation.ApiAction;

/**
 * 小工具。
 * 
 * @since 2018-01-24
 * @version
 * @author Luowen
 */
public class Util extends Responsor {
	
	/**
	 * 构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public Util(HttpServletRequest request, HttpServletResponse response) {
		
		super(request, response);
	}

	
	/**
	 * BASE 64 密码。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap encode() {
		
		String value = getStringParam("value");
		String encodedValue = SystemConfig.BASE64Encode(value);
		
		
		Map<String, Object> result = new HashMap<>();
		result.put("value", value);
		result.put("encodedValue", encodedValue);
		
		
		StatuscodeMap sm = new StatuscodeMap();
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(result);
		
		
		return sm;
	}
	
	
	/**
	 * BASE 64 解密。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap decode() {

		String value = getStringParam("value");
		String decodedValue = SystemConfig.BASE64Decode(value);
		
		
		Map<String, Object> result = new HashMap<>();
		result.put("value", value);
		result.put("decodedValue", decodedValue);
		
		
		StatuscodeMap sm = new StatuscodeMap();
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(result);
		
		
		return sm;
	}

	
	/**
	 * MD5 加密。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap md5() {
		
		String value = getStringParam("value");
		String encodedValue = MD5.encode(value);
		
		
		Map<String, Object> result = new HashMap<>();
		result.put("value", value);
		result.put("encodedValue", encodedValue);
		
		
		StatuscodeMap sm = new StatuscodeMap();
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(result);
		
		
		return sm;
	}
}

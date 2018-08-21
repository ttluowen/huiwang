package com.yy.web.sys.filter;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yy.util.number.NumberUtil;
import com.yy.web.config.SystemConfig;
import com.yy.web.request.handler.AssetsRequestHandler;

/**
 * 页面请求处理类。
 * 
 * @since 2017-05-11
 * @version 1.0
 * @author Luowen
 */
public class AssetsRequest extends AssetsRequestHandler {

	/**
	 * 构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public AssetsRequest(HttpServletRequest request, HttpServletResponse response) {
		
		super(request, response);
	}
	
	
	public int doResponse() {
		
		HttpServletResponse response = getResponse();
		
		int assetsExpires = NumberUtil.parseInt(SystemConfig.getConfig("assetsExpires"));
		if (assetsExpires > 0) {
			Calendar calendar = Calendar.getInstance(Locale.CHINA);
			calendar.add(Calendar.DATE, assetsExpires);

			response.setDateHeader("Expires", calendar.getTimeInMillis());
		}


		// 对字体设置跨域响应头。
		List<String> allowOriginExts = Arrays.asList("ttf,woff,eot,svg".split(","));
		if (allowOriginExts.contains(getExt())) {
			response.addHeader("Access-Control-Allow-Origin", SystemConfig.getConfig("allowOrigin", "*"));
		    response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
		    response.addHeader("Access-Control-Allow-Headers", "Origin, No-Cache, X-Requested-With, If-Modified-Since, Pragma, Last-Modified, Cache-Control, Expires, Content-Type, X-E4M-With, Token, RedirectUrl, Authorization");
		}


		return super.doResponse();
	}
}

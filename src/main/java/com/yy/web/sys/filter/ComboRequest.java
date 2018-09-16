package com.yy.web.sys.filter;

import java.util.Calendar;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yy.util.NumberUtil;
import com.yy.web.config.SystemConfig;
import com.yy.web.request.handler.ComboRequestHandler;

/**
 * 页面请求处理类。
 * 
 * @since 2017-05-11
 * @version 1.0
 * @author Luowen
 */
public class ComboRequest extends ComboRequestHandler {

	/**
	 * 构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public ComboRequest(HttpServletRequest request, HttpServletResponse response) {
		
		super(request, response);
	}
	
	
	public int doResponse() {

		int assetsExpires = NumberUtil.parseInt(SystemConfig.getConfig("assetsExpires"));
		if (assetsExpires > 0) {
			Calendar calendar = Calendar.getInstance(Locale.CHINA);
			calendar.add(Calendar.DATE, assetsExpires);

			HttpServletResponse response = getResponse();
			response.setDateHeader("Expires", calendar.getTimeInMillis());
		}


		return super.doResponse();
	}
}

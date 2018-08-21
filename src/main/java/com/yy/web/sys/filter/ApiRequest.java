package com.yy.web.sys.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yy.web.WebSite;
import com.yy.web.config.SystemConfig;
import com.yy.web.request.handler.ApiRequestHandler;


/**
 * api 接口请求通用处理句柄。
 * 
 * @author Luowen
 */
public class ApiRequest extends ApiRequestHandler {
	
	/**
	 * 构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public ApiRequest(HttpServletRequest request, HttpServletResponse response) {
		
		super(request, response);
	}


	/**
	 * 请求处理。
	 */
	public int doResponse() {

		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		

		// 设置访问域响应头。
		if (request.getRequestURI().indexOf("/" + WebSite.PATH_API + "/") == -1) {
			response.addHeader("Access-Control-Allow-Origin", SystemConfig.getConfig("allowOrigin", "*"));
		    response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
		    response.addHeader("Access-Control-Allow-Headers", "Origin, No-Cache, X-Requested-With, If-Modified-Since, Pragma, Last-Modified, Cache-Control, Expires, Content-Type, X-E4M-With, Token, RedirectUrl, Authorization");
		}


		return super.doResponse();
	}
}

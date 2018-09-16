package com.yy.web.pagedata;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yy.util.MapValue;
import com.yy.util.StringUtil;
import com.yy.web.login.LoginOauth;
import com.yy.web.request.pagadata.PageDataAbstract;

/**
 * 登录成功后的操作。
 * 
 * @since 2018-01-14
 * @version 1.0
 * @author Luowen
 */
public class LoginPageData extends PageDataAbstract {
	
	/**
	 * 构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public LoginPageData(HttpServletRequest request, HttpServletResponse response) {
		
		super(request, response);
	}


	/**
	 * 抽象接口实现。
	 */
	@Override
	public MapValue addData() {
		
		// 获取 redirectUrl 页面参数，并动态替换 token 参数。
		MapValue redirectUrlParams = new MapValue();
		redirectUrlParams.put("token", getAndUpdateToken());
		redirectUrlParams.put("userId", getUserId());
		redirectUrlParams.put("username", getUsername());
		redirectUrlParams.put("nickname", getNickname());
		
		String redirectUrl = StringUtil.substitute(getStringParam("redirectUrl"), redirectUrlParams);


		MapValue pageData = new MapValue();

		// 设置登录码。
		pageData.put("loginCode", new LoginOauth(getRequest(), getResponse()).generateLoginCode());
		// 登录成功的转发地址。
		pageData.put("redirectUrl", redirectUrl);
		
		
		return pageData;
	}
}

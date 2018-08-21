package com.yy.web.sys.filter;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yy.statuscode.Statuscode;
import com.yy.statuscode.StatuscodeMap;
import com.yy.util.number.NumberUtil;
import com.yy.util.string.StringUtil;
import com.yy.web.base.visit.Visit;
import com.yy.web.config.SystemConfig;
import com.yy.web.filter.WebFilter;

public class VisitFilter extends WebFilter {

	/**
	 * 访问安全拦截器。
	 */
	public int doPermissionFilter(HttpServletRequest request, HttpServletResponse response) {

		// 查询用户身份信息。
		StatuscodeMap visitSm = new Visit(request, response).info();
		int visitSmCode = visitSm.getCode();


		if (visitSmCode == Statuscode.SUCCESS) {
			// 成功。

			Map<String, Object> info = visitSm.getResultAsMap();

			// 保存用户身份属性。
			super.token = StringUtil.unNull(info.get("token"));
			factoryKey = StringUtil.unNull(info.get("factoryKey"));
			language = StringUtil.unNull(info.get("language"));
			userId = NumberUtil.parseInt(info.get("userId"));
			username = StringUtil.unNull(info.get("username"));
			nickname = StringUtil.unNull(info.get("nickname"));
			appId = SystemConfig.getAppId();
		}


		return visitSmCode;
	}


	/**
	 * api 接口访问处理。
	 * 
	 * @param request
	 * @param response
	 */
	public int doRequestApi(HttpServletRequest request, HttpServletResponse response) {

		// 实例化 api 接口访问处理器。
		ApiRequest handler = new ApiRequest(request, response);


		// 从权限拦截器那边获取相关的用户身份属性。
		handler.setAppId(getAppId());
		handler.setFactoryKey(getFactoryKey());
		handler.setUserId(getUserId());
		handler.setUsername(getUsername());
		handler.setNickname(getNickname());

		// 先过滤操作。
		if (handler.filter()) {
			// 处理请求。
			handler.doRequest();
			// 响应处理。
			return handler.doResponse();
		} else {
			// 无权限访问。
			return Statuscode.PERMISSION_HAVENT_PERMISSON;
		}
	}


	/**
	 * assets 资源访问处理。
	 * 
	 * @param request
	 * @param response
	 */
	public int doRequestAssets(HttpServletRequest request, HttpServletResponse response) {
		
		// 实例化 api 接口访问处理器。
		AssetsRequest handler = new AssetsRequest(request, response);


		// 从权限拦截器那边获取相关的用户身份属性。
		handler.setAppId(getAppId());
		handler.setFactoryKey(getFactoryKey());
		handler.setUserId(getUserId());
		handler.setUsername(getUsername());
		handler.setNickname(getNickname());


		// 先过滤操作。
		if (handler.filter()) {
			// 处理请求。
			handler.doRequest();
			// 响应处理。
			return handler.doResponse();
		} else {
			// 无权限访问。
			return Statuscode.PERMISSION_HAVENT_PERMISSON;
		}
	}


	/**
	 * combo 资源合并访问处理。
	 * 
	 * @param request
	 * @param response
	 */
	public int doRequestCombo(HttpServletRequest request, HttpServletResponse response) {

		// 实例化 api 接口访问处理器。
		ComboRequest handler = new ComboRequest(request, response);


		// 从权限拦截器那边获取相关的用户身份属性。
		handler.setAppId(getAppId());
		handler.setFactoryKey(getFactoryKey());
		handler.setUserId(getUserId());
		handler.setUsername(getUsername());
		handler.setNickname(getNickname());


		// 先过滤操作。
		if (handler.filter()) {
			// 处理请求。
			handler.doRequest();
			// 响应处理。
			return handler.doResponse();
		} else {
			// 无权限访问。
			return Statuscode.PERMISSION_HAVENT_PERMISSON;
		}
	}


	/**
	 * page 页面访问处理。
	 * 
	 * @param request
	 * @param response
	 */
	public int doRequestPage(HttpServletRequest request, HttpServletResponse response) {

		// 实例化 api 接口访问处理器。
		PageRequest pageRequest = new PageRequest(request, response);


		// 从权限拦截器那边获取相关的用户身份属性。
		pageRequest.setAppId(getAppId());
		pageRequest.setFactoryKey(getFactoryKey());
		pageRequest.setUserId(getUserId());
		pageRequest.setUsername(getUsername());
		pageRequest.setNickname(getNickname());


		// 先过滤操作。
		if (pageRequest.filter()) {
			// 处理请求。
			pageRequest.doRequest();
			// 响应处理。
			return pageRequest.doResponse();
		} else {
			// 无权限访问。
			return Statuscode.PERMISSION_HAVENT_PERMISSON;
		}
	}


	/**
	 * upload 资源上传处理。
	 * 
	 * @param request
	 * @param response
	 */
	public int doRequestUpload(HttpServletRequest request, HttpServletResponse response) {

		// 实例化 api 接口访问处理器。
		UploadRequest handler = new UploadRequest(request, response);


		// 从权限拦截器那边获取相关的用户身份属性。
		handler.setAppId(getAppId());
		handler.setFactoryKey(getFactoryKey());
		handler.setUserId(getUserId());
		handler.setUsername(getUsername());
		handler.setNickname(getNickname());


		// 先过滤操作。
		if (handler.filter()) {
			// 处理请求。
			handler.doRequest();
			// 响应处理。
			return handler.doResponse();
		} else {
			// 无权限访问。
			return Statuscode.PERMISSION_HAVENT_PERMISSON;
		}
	}
}

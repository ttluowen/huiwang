package com.yy.web.pagedata;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yy.log.Logger;
import com.yy.util.map.MapValue;
import com.yy.util.string.StringUtil;
import com.yy.web.base.DBConfig;
import com.yy.web.request.pagadata.PageDataAbstract;

/**
 * 基础数据目录参数管理类。
 * 
 * @since 2017-02-26
 * @version 1.0
 * @author Luowen
 */
public class SiteData extends PageDataAbstract {
	
	/**
	 * 构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public SiteData(HttpServletRequest request, HttpServletResponse response) {
		
		super(request, response);
	}
	
	
	/**
	 * 计算获得可用的 topDomain 值。
	 * 
	 * @return
	 */
	private String getTopDomain() {
		
		HttpServletRequest request = getRequest();
		String scheme = request.getScheme().toLowerCase();
		String serverName = request.getServerName().toLowerCase();
		int port = request.getServerPort();
		
		
		if (serverName.indexOf("yiyuen.com") != -1 && !serverName.equals("local.yiyuen.com")) {
			serverName = "www.yiyuen.com";
		}
		
		
		String topDomain = scheme + "://" + serverName;
		if (port != 80 && port != 8080) {
			topDomain += ":" + port;
		}
		topDomain += "/";
		
		
		return topDomain;
	}


	/**
	 * 抽象接口实现。
	 */
	@Override
	public MapValue addData() {

		MapValue pageData = new MapValue();
		pageData.put("assets", DBConfig.getValue("assets"));
		pageData.put("assetsTag", DBConfig.getValue("assetsTag"));
		pageData.put("topDomain", getTopDomain());


		// encodeUri 处理。
		HttpServletRequest request = getRequest();
		String uri = request.getRequestURI();
		String queryString = request.getQueryString();

		if (queryString != null && !queryString.isEmpty()) {
			uri += "?" + queryString;
		}

		try {
			pageData.put("encodedUri", URLEncoder.encode(uri, StringUtil.UTF8));
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}


		return pageData;
	}
}

package com.yy.web.sys.filter;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yy.statuscode.Statuscode;
import com.yy.statuscode.StatuscodeTypeMap;
import com.yy.util.MapUtil;
import com.yy.util.MapValue;
import com.yy.util.StringUtil;
import com.yy.web.WebSite;
import com.yy.web.config.SystemConfig;
import com.yy.web.filereader.PageFileReader;
import com.yy.web.request.handler.PageRequestHandler;

/**
 * 页面请求处理类。
 * 
 * @since 2017-05-11
 * @version 1.0
 * @author Luowen
 */
public class PageRequest extends PageRequestHandler {

	/**
	 * 构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public PageRequest(HttpServletRequest request, HttpServletResponse response) {
		
		super(request, response);
	}


	/**
	 * 请求处理。
	 */
	public StatuscodeTypeMap<String> doRequest() {
		
		StatuscodeTypeMap<String> sm = new StatuscodeTypeMap<String>();


		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();


		// 获取访问的 URI 和扩展名。
		String uri = getUri();
		String ext = getExt();


		/*
		 * 如果后续为空，则有可能是直接访问的目录，
		 * 如： /web/portal、/web/portal/……
		 */
		if (StringUtil.isEmpty(ext)) {
			String url = request.getRequestURL().toString();
			String queryString = request.getQueryString();


			// 检测 URL 的最后一个是否是 / 符号，如果不是自动补上。
			if (!url.substring(url.length() - 1).equals("/")) {
				url += "/";
			}
			// 使用默认页面。
			url += WebSite.INDEX_PAGE;


			// 拼接参数。
			if (!StringUtil.isEmpty(queryString)) {
				url += "?" + queryString;
			}
			

			// 页面重定向。
			try {
				response.sendRedirect(url);
			} catch (IOException e) {
			}


			// 重定向后，当前返回空内容。
			sm.setCode(Statuscode.HTTP_PAGE_302);
			sm.setDescription(Statuscode.HTTP_PAGE_302_DESC);


			return sm;
		}


		// 根据不同页面，选择性添加不同数据。
		MapValue pageData = new MapValue();

		// 设置当前 URI 是否有访问权限的 Velocity 参数。
		pageData.put("hasUrlPermission", hasUrlPermission(uri));


		// 路由添加页面数据。
		pageData = MapUtil.merge(pageData, addPageData());


		// 非静态文件，直接读取返回。
		if (ext.equals(WebSite.HTML_EXT)) {
			// 替换模板占位符内容。
			outContent = renderUriTemplate(uri, pageData);
		} else {
			PageFileReader reader = new PageFileReader(getFactoryKey());
			File file = reader.getTheRootFile(uri);
			outContent = reader.readAsString(file);
		}


		// 额外添加 token 等信息。
		if (!SystemConfig.isRouteDisabled()) {
			outContent = addTokenParam(outContent, getAndUpdateToken());
		}


		// 设置返回结果。
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(outContent);
		
		
		return sm;
	}

	
	public boolean hasUrlPermission(String uri) {
		
		return true;
	}
}

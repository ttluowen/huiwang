package com.yy.web.sys.filter;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

import com.yy.statuscode.StatuscodeMap;
import com.yy.util.number.NumberUtil;
import com.yy.util.string.StringUtil;
import com.yy.web.config.SystemConfig;
import com.yy.web.config.WebHosts;
import com.yy.web.request.handler.UploadRequestHandler;

/**
 * 页面请求处理类。
 * 
 * @since 2017-05-11
 * @version 1.0
 * @author Luowen
 */
public class UploadRequest extends UploadRequestHandler {

	/**
	 * 构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public UploadRequest(HttpServletRequest request, HttpServletResponse response) {
		
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
	
	
	/**
	 * 对单个文件上传的句柄。
	 * 
	 * @param fileItem
	 * @return
	 */
	protected StatuscodeMap uploadHandler(FileItem fileItem) {
		
		StatuscodeMap sm = super.uploadHandler(fileItem);
		Map<String, Object> result = sm.getResultAsMap();
		int port = new WebHosts(getRequest()).getRequestPort();
		result.put("src", StringUtil.unNull(result.get("src")).replace(":" + port, ""));
		
		
		return sm;
	}
}

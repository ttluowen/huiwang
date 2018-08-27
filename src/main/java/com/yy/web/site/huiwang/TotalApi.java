package com.yy.web.site.huiwang;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yy.statuscode.StatuscodeMap;
import com.yy.web.Dim;
import com.yy.web.Responsor;

/**
 * 统计接口。
 * 
 * @since 2018-08-26
 * @version 1.0
 * @author Luowen
 */
public class TotalApi extends Responsor {
	
	private static final String SQL_NAMESPACE = "total.";

	/**
	 * 构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public TotalApi(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	
	/**
	 * 学校统计。
	 * 
	 * @return
	 */
	public StatuscodeMap schoolCount() {
		
		return dbSelectMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "schoolCount", null);
	}
	
	
	/**
	 * 加入人数统计。
	 * 
	 * @return
	 */
	public StatuscodeMap userCount() {
		
		return dbSelectMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "userCount", null);
	}
}

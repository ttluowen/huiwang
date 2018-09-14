package com.yy.web.site.huiwang;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yy.statuscode.StatuscodeMap;
import com.yy.web.Dim;
import com.yy.web.Responsor;
import com.yy.web.request.annotation.ApiAction;

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
	@ApiAction
	public StatuscodeMap schoolCount() {
		
		return dbSelectOneMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "schoolCount", null);
	}

	
	/**
	 * 学校人数统计。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap schoolUserCount() {
		
		return dbSelectOneMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "schoolUserCount", null);
	}

	
	/**
	 * 班级人数统计。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap classUserCount() {
		
		return dbSelectOneMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "classUserCount", null);
	}
	
	
	/**
	 * 加入人数统计。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap userCount() {
		
		return dbSelectOneMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "userCount", null);
	}
}

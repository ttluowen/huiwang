package com.yy.web.site.huiwang;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yy.statuscode.StatuscodeMap;
import com.yy.util.map.MapValue;
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
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("type", getStringParam("type"));
		
		return dbSelectOneMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "schoolCount", sqlParams);
	}

	
	/**
	 * 学校人数统计。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap schoolUserCount() {
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("schoolId", getIntParam("schoolId"));
		
		return dbSelectOneMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "schoolUserCount", sqlParams);
	}

	
	/**
	 * 班级人数统计。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap classUserCount() {
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("classId", getIntParam("classId"));
		
		return dbSelectOneMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "classUserCount", sqlParams);
	}
	
	
	/**
	 * 加入人数总计。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap userCount() {
		
		return dbSelectOneMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "userCount", null);
	}
}

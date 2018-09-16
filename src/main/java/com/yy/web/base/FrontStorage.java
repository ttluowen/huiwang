package com.yy.web.base;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yy.statuscode.Statuscode;
import com.yy.statuscode.StatuscodeMap;
import com.yy.util.MapValue;
import com.yy.web.Dim;
import com.yy.web.Responsor;
import com.yy.web.request.annotation.ApiAction;

/**
 * 用于前台数据存储。
 * 类似于前端的 localStorage
 * 
 * @since 2018-02-03
 * @version 1.0
 * @author Luowen
 */
public class FrontStorage extends Responsor {
	
	/** SQL 命名空间。 */
	private static final String SQL_NAMESPACE = "base.frontStorage.";
	

	/**
	 * 构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public FrontStorage(HttpServletRequest request, HttpServletResponse response) {
		
		super(request, response);
	}
	
	
	/**
	 * 获取相应的参数，并自动添加上 userAgent 属性。
	 */
	public MapValue getParams() {
		
		MapValue params;
		if (getRequest().getMethod().toLowerCase().equals("get")) {
			params = super.getParams();
		} else {
			params = getPostParams();
		}
		params.put("userAgent", getRequest().getHeader("User-Agent"));
		
		
		return params;
	}

	
	/**
	 * 获取操作。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap get() {

		return dbSelectOneDataMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "get", getParams());
	}
	
	
	/**
	 * 设置操作。
	 * 先尝试更新，如果失败再使用新增。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap set() {

		StatuscodeMap sm = new StatuscodeMap();

		
		MapValue params = getParams();
		int dbResult = dbUpdate(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "update", params);
		if (dbResult == 0) {
			dbResult = dbUpdate(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "add", params);
		}
		
		
		if (dbResult > 0) {
			sm.setCode(Statuscode.SUCCESS);
		}
		
		
		return sm;
	}
	
	
	/**
	 * 删除操作。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap remove() {

		StatuscodeMap sm = new StatuscodeMap();
		
		if (dbUpdate(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "remove", getParams()) > 0) {
			sm.setCode(Statuscode.SUCCESS);
		}
		
		
		return sm;
	}
}

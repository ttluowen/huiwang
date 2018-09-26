package com.yy.web.site.huiwang;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yy.statuscode.Statuscode;
import com.yy.statuscode.StatuscodeMap;
import com.yy.util.MapValue;
import com.yy.web.Dim;
import com.yy.web.Responsor;
import com.yy.web.request.annotation.ApiAction;


/**
 * 省份接口。
 * 
 * @since 2018-09-09
 * @version 1.0
 * @author Admin
 */
public class ProvinceApi extends Responsor {
	
	private static final String SQL_NAMESPACE = "base.province.";
	
	private static List<MapValue> list;
	

	/**
	 * 构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public ProvinceApi(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	
	/**
	 * 获取所有省份列表。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap list() {
		
		if (list == null) {
			list = dbSelect(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "list", new MapValue());
		}
		
		
		StatuscodeMap sm = new StatuscodeMap();
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(list);
		
		
		return sm;
	}
}

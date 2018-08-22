package com.yy.web.site.huiwang;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yy.statuscode.StatuscodeMap;
import com.yy.statuscode.StatuscodeTypeMap;
import com.yy.util.date.DateUtil;
import com.yy.util.map.MapValue;
import com.yy.web.Dim;
import com.yy.web.Responsor;
import com.yy.web.request.annotation.ApiAction;
import com.yy.web.site.huiwang.struct.ClassStruct;

/**
 * 班级管理类。
 * 
 * @since 2018-08-22
 * @version 1.0
 * @author Luowen
 */
public class Clazz extends Responsor {
	
	public static final String SQL_NAMESPACE = "class.";
	

	/**
	 * 构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public Clazz(HttpServletRequest request, HttpServletResponse response) {
		
		super(request, response);
	}
	
	
	/**
	 * 获取当前用户加入的班级。
	 * 
	 * @return
	 */
	public StatuscodeTypeMap<List<ClassStruct>> getUserClass() {
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("userId", getUserId());
		
		return dbSelectMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "getUserClass", sqlParams, null, ClassStruct.class);
	}

	
	/**
	 * 创建班级。
	 * 
	 * @return
	 */
	@ApiAction(login = true)
	public StatuscodeMap create() {
		
		MapValue data = getPostParams();
		data.put("creator", getUserId());
		data.put("datetime", DateUtil.get(1));
		
		
		return dbInsertAndReturnIdMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "create", data);
	}
	

	/**
	 * 修改班级信息。
	 * 
	 * @return
	 */
	@ApiAction(login = true)
	public StatuscodeMap modify() {
	
		getUserClass().getResult();
		
		return null;
	}
	

	/**
	 * 重命名班级。
	 * 
	 * @return
	 */
	@ApiAction(login = true)
	public StatuscodeMap rename() {
		
		return null;
	}
	

	/**
	 * 加入班级。
	 * 
	 * @return
	 */
	@ApiAction(login = true)
	public StatuscodeMap join() {
		
		return null;
	}
}

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
public class ClassApi extends Responsor {
	
	public static final String SQL_NAMESPACE = "class.";
	

	/**
	 * 构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public ClassApi(HttpServletRequest request, HttpServletResponse response) {
		
		super(request, response);
	}
	
	
	/**
	 * 获取学校的班级列表。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeTypeMap<List<ClassStruct>> getSchoolClass() {
		
		int schoolId = getIntParam("schoolId");
		int year = getIntParam("year");
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("schoolId", schoolId);
		sqlParams.put("year", year);
		
		
		return dbSelectMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "list", sqlParams, null, ClassStruct.class);
	}
	
	
	/**
	 * 获取已加入班级的人员列表。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap getClassUsers() {

		int classId = getIntParam("classId");
		MapValue sqlParams = new MapValue();
		sqlParams.put("classId", classId);
		
		
		return dbSelectMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "getClassUsers", sqlParams);
	}
	
	
	/**
	 * 获取当前用户加入的班级。
	 * 
	 * @return
	 */
	@ApiAction(login = true)
	public StatuscodeTypeMap<List<ClassStruct>> my() {
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("userId", getUserId());
		
		return dbSelectMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "list", sqlParams, null, ClassStruct.class);
	}
	
	
	private MapValue getCreateModifyData() {
		
		MapValue data = getPostParams();
		String name = data.getString("name");
		int schoolId = data.getIntValue("schoolId");
		String year = data.getString("year");
		String field = data.getString("field");
		String banner = data.getString("banner");
		int creator = getUserId();
		int status = data.getIntValue("status");
		
		
		data.put("name", name);
		data.put("schoolId", schoolId);
		data.put("year", year);
		data.put("field", field);
		data.put("banner", banner);
		data.put("creator", creator);
		data.put("status", status);
		
		
		return data;
	}

	
	/**
	 * 创建班级。
	 * 
	 * @return
	 */
	@ApiAction(login = true)
	public StatuscodeMap create() {

		MapValue data = getCreateModifyData();
		data.put("datetime", DateUtil.get(1));
		
		
		// 检查该学校是否已创建。
		MapValue sqlParams = new MapValue();
		sqlParams.put("name", data.getString("name"));
		sqlParams.put("type", data.getIntValue("type"));
		sqlParams.put("cityId", data.getIntValue("cityId"));

		StatuscodeMap sm = new StatuscodeMap();
		MapValue exist = dbSelectOne(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "list", sqlParams);
		if (exist != null && !exist.isEmpty()) {
			sm.setDescription("该学校已存在");
			return sm;
		}


		return dbInsertAndReturnIdMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "create", data);
	}
	

	/**
	 * 修改班级信息。
	 * 
	 * @return
	 */
	@ApiAction(login = true)
	public StatuscodeMap modify() {
	
		MapValue data = getCreateModifyData();
		
		return dbUpdateMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "modify", data);
	}
	

	/**
	 * 重命名班级。
	 * 
	 * @return
	 */
	@ApiAction(login = true)
	public StatuscodeMap rename() {

		MapValue data = getCreateModifyData();

		return dbUpdateMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "rename", data);
	}
	

	/**
	 * 加入班级。
	 * 
	 * @return
	 */
	@ApiAction(login = true)
	public StatuscodeMap join() {

		int classId = getIntParam("classId");
		int userId = getUserId();
		
		MapValue data = new MapValue();
		data.put("classId", classId);
		data.put("userId", userId);
		
		
		MapValue joined = dbSelectOne(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "queryUserJoined", data);
		if (joined != null) {
			StatuscodeMap sm = new StatuscodeMap();
			sm.setDescription("已经加入过了");
			
			return sm;
		}

		
		return dbUpdateMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "join", data);
	}
}

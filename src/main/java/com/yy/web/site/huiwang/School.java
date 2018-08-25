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
 * 学校管理类。
 * 
 * @since 2018-08-24
 * @version 1.0
 * @author Luowen
 */
public class School extends Responsor {
	
	public static final String SQL_NAMESPACE = "school.";
	

	/**
	 * 构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public School(HttpServletRequest request, HttpServletResponse response) {
		
		super(request, response);
	}
	
	
	/**
	 * 获取学校的班级列表。
	 * 
	 * @return
	 */
	public StatuscodeTypeMap<List<ClassStruct>> getSchoolClass() {
		
		return null;
	}
	
	
	/**
	 * 获取当前用户加入的学校。
	 * 
	 * @return
	 */
	public StatuscodeTypeMap<List<ClassStruct>> getUserSchool() {
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("userId", getUserId());
		
		return dbSelectMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "getUserSchool", sqlParams, null, ClassStruct.class);
	}
	
	
	private MapValue getCreateModifyData() {
		
		MapValue data = getPostParams();
		String name = data.getString("name");
		String historyNames = data.getString("historyNames");
		String type = data.getString("type");
		String province = data.getString("province");
		String area = data.getString("area");
		int cityId = data.getIntValue("cityId");
		String banner = data.getString("banner");
		int creator = getUserId();
		int status = data.getIntValue("status");
		
		
		data.put("name", name);
		data.put("historyNames", historyNames);
		data.put("type", type);
		data.put("province", province);
		data.put("area", area);
		data.put("cityId", cityId);
		data.put("banner", banner);
		data.put("creator", creator);
		data.put("status", status);
		
		
		return data;
	}

	
	/**
	 * 创建学校。
	 * 
	 * @return
	 */
	@ApiAction(login = true)
	public StatuscodeMap create() {

		MapValue data = getCreateModifyData();
		data.put("datetime", DateUtil.get(1));


		return dbInsertAndReturnIdMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "create", data);
	}


	/**
	 * 修改学校信息。
	 * 
	 * @return
	 */
	@ApiAction(login = true)
	public StatuscodeMap modify() {
	
		MapValue data = getCreateModifyData();
		
		return dbUpdateMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "modify", data);
	}
	

	/**
	 * 重命名学校。
	 * 
	 * @return
	 */
	@ApiAction(login = true)
	public StatuscodeMap rename() {

		MapValue data = getCreateModifyData();

		return dbUpdateMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "rename", data);
	}
}

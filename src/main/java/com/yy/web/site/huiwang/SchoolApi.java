package com.yy.web.site.huiwang;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.yy.statuscode.StatuscodeMap;
import com.yy.statuscode.StatuscodeTypeMap;
import com.yy.util.date.DateUtil;
import com.yy.util.map.MapValue;
import com.yy.web.Dim;
import com.yy.web.Responsor;
import com.yy.web.request.annotation.ApiAction;
import com.yy.web.request.annotation.MethodEnum;
import com.yy.web.site.huiwang.struct.SchoolStruct;

/**
 * 学校管理类。
 * 
 * @since 2018-08-24
 * @version 1.0
 * @author Luowen
 */
public class SchoolApi extends Responsor {
	
	public static final String SQL_NAMESPACE = "school.";
	

	/**
	 * 构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public SchoolApi(HttpServletRequest request, HttpServletResponse response) {
		
		super(request, response);
	}
	
	
	/**
	 * 获取我加入的学校的编号。
	 * 
	 * @return
	 */
	private List<Integer> mySchoolIds() {
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("userId", getUserId());
		
		
		List<Integer> ids = new ArrayList<>();
		List<MapValue> list = dbSelect(Dim.DB_SOURCE_MYSQL, ClassApi.SQL_NAMESPACE + "getUserClasses", sqlParams);
		if (list != null) {
			for (MapValue item : list) {
				ids.add(item.getInteger("schoolId"));
			}
		}


		return ids;
	}
	
	
	/**
	 * 获取当前用户加入的学校。
	 * 
	 * @return
	 */
	@ApiAction(login = true)
	public StatuscodeTypeMap<List<SchoolStruct>> my() {

		MapValue sqlParams = new MapValue();
		sqlParams.put("schoolIds", StringUtils.join(mySchoolIds(), ", "));
		
		return dbSelectMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "list", sqlParams, null, SchoolStruct.class);
	}
	
	
	private MapValue getCreateModifyData() {
		
		MapValue data = getPostParams();
		String name = data.getString("name");
		String historyNames = data.getString("historyNames");
		int headSchoolId = data.getIntValue("headSchoolId");
		String type = data.getString("type");
		String province = data.getString("province");
		String area = data.getString("area");
		int cityId = data.getIntValue("cityId");
		String address = data.getString("address");
		String phone = data.getString("phone");
		String banner = data.getString("banner");
		int creator = getUserId();
		int status = data.getIntValue("status");
		
		
		data.put("name", name);
		data.put("historyNames", historyNames);
		data.put("headSchoolId", headSchoolId);
		data.put("type", type);
		data.put("province", province);
		data.put("area", area);
		data.put("cityId", cityId);
		data.put("address", address);
		data.put("phone", phone);
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
	@ApiAction(login = true, methods = {MethodEnum.POST})
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
	 * 修改学校信息。
	 * 
	 * @return
	 */
	@ApiAction(login = true, methods = {MethodEnum.POST})
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
	

	/**
	 * 学校列表（不包括分校）。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap list() {
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("type", getStringParam("type"));
		sqlParams.put("cityId", getIntParam("cityId"));
		sqlParams.put("province", getStringParam("province"));
		sqlParams.put("name", getStringParam("name"));
		sqlParams.put("headSchoolId", -1);
		
		return dbSelectMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "list", sqlParams);
	}
	

	/**
	 * 分校列表。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap subList() {
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("type", getStringParam("type"));
		sqlParams.put("cityId", getIntParam("cityId"));
		sqlParams.put("province", getStringParam("province"));
		sqlParams.put("name", getStringParam("name"));
		sqlParams.put("headSchoolId", getIntParam("headSchoolId"));
		
		
		return dbSelectMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "list", sqlParams);
	}
	
	
	/**
	 * 搜索。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap search() {
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("name", getStringParam("q"));
		
		
		return dbSelectMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "list", sqlParams);
	}
}



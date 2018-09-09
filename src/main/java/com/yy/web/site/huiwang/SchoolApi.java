package com.yy.web.site.huiwang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yy.statuscode.Statuscode;
import com.yy.statuscode.StatuscodeMap;
import com.yy.statuscode.StatuscodeTypeMap;
import com.yy.util.array.ArrayUtil;
import com.yy.util.date.DateUtil;
import com.yy.util.map.MapValue;
import com.yy.web.Dim;
import com.yy.web.Responsor;
import com.yy.web.request.annotation.ApiAction;
import com.yy.web.site.huiwang.struct.ClassStruct;
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
	 * 获取当前用户加入的学校。
	 * 
	 * @return
	 */
	@ApiAction(login = true)
	public StatuscodeTypeMap<List<SchoolStruct>> my() {
		
		ClassApi clazz = new ClassApi(getRequest(), getResponse());
		copyTo(clazz);

		StatuscodeTypeMap<List<ClassStruct>> clazzSm = clazz.my();
		if (clazzSm.getCode() == Statuscode.SUCCESS) {
			// 查询用户加入的班级。
			List<ClassStruct> clazzList = clazzSm.getResult();
			Map<Integer, Boolean> clazzMap = new HashMap<>();
			
			for (ClassStruct item : clazzList) {
				clazzMap.put(item.getSchoolId(), true);
			}
			
			Iterator<Integer> keys = clazzMap.keySet().iterator();
			List<Integer> clazzIds = new ArrayList<>();
			while (keys.hasNext()) {
				clazzIds.add(keys.next());
			}

			String clazzIdsStr = ArrayUtil.join(ArrayUtil.toInt(clazzIds.toArray(new Integer[0])));
			
			
			MapValue sqlParams = new MapValue();
			sqlParams.put("userId", getUserId());
			sqlParams.put("classIds", clazzIdsStr);
			
			return dbSelectMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "list", sqlParams, null, SchoolStruct.class);
		} else {
			StatuscodeTypeMap<List<SchoolStruct>> sm = new StatuscodeTypeMap<>();
			sm.setCode(Statuscode.SUCCESS);
			sm.setDescription("没有任何记录");

			return sm;
		}
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
	/**
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
	

	@ApiAction
	public StatuscodeMap list() {
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("cityId", getIntParam("cityId"));
		
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
		sqlParams.put("q", getStringParams("q"));
		
		
		return dbSelectMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "list", sqlParams);
	}
}



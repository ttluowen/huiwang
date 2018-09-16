package com.yy.web.site.huiwang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.yy.statuscode.Statuscode;
import com.yy.statuscode.StatuscodeMap;
import com.yy.statuscode.StatuscodeTypeMap;
import com.yy.util.ArrayUtil;
import com.yy.util.MapValue;
import com.yy.web.Dim;
import com.yy.web.Responsor;
import com.yy.web.request.annotation.ApiAction;
import com.yy.web.site.huiwang.struct.ClassStruct;

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
	 * 组合返回多个统计接口数据。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap combo() {
		
		MapValue result = new MapValue();
		
		
		boolean schoolCount = getBooleanParam("schoolCount");
		boolean userCount = getBooleanParam("userCount");
		
		
		if (schoolCount) {
			int count = 0;
			try { 
				count = schoolCount().getResultAsMap().getIntValue("count");
			} catch (Exception e) {
			}
			result.put("schoolCount", count);
		}
		if (userCount) {
			int count = 0;
			try { 
				count = userCount().getResultAsMap().getIntValue("count");
			} catch (Exception e) {
			}
			result.put("userCount", count);
		}
		
		
		StatuscodeMap sm = new StatuscodeMap();
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(result);
		
		
		return sm;
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
	 * 指定学校的班级数量。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap schoolClassCount() {

		MapValue sqlParams = new MapValue();
		sqlParams.put("schoolId", StringUtils.join(getIntParams("schoolId"), ','));
		
		
		return dbSelectMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "schoolClassCount", sqlParams);
	}

	
	/**
	 * 学校人数统计。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap schoolUserCount() {
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("schoolId", StringUtils.join(getIntParams("schoolId"), ','));
		
		return dbSelectMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "schoolUserCount", sqlParams);
	}

	
	/**
	 * 班级人数统计。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap classUserCount() {
		
		StatuscodeMap sm = new StatuscodeMap();
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(classUserCount(getIntParams("classId")));
		
		
		return sm;
	}
	
	
	/**
	 * 班级人数统计。
	 * 
	 * @param classIds
	 * @return
	 */
	public List<MapValue> classUserCount(int[] classIds) {
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("classId", StringUtils.join(classIds, ','));
		
		return dbSelect(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "classUserCount", sqlParams);
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
	

	/**
	 * 用户个人信息的统计。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap myTotal() {
		
		MapValue result = new MapValue();
		MapValue count = new MapValue();
		result.put("count", count);
		
		
		ClassApi classApi = new ClassApi(getRequest(), getResponse());
		copyTo(classApi);


		StatuscodeTypeMap<List<ClassStruct>> classSm = classApi.my();
		if (classSm.getCode() == Statuscode.SUCCESS) {
			List<ClassStruct> list = classSm.getResult();
			count.put("classCount", list.size());
			result.put("classList", list);
			
			
			List<Integer> classIds = new ArrayList<>();
			for (ClassStruct item : list) {
				int classId = item.getClassId();
				classIds.add(classId);
			}

			int[] classIdAtt = ArrayUtil.toInt(classIds.toArray(new Integer[classIds.size()]));
			int schoolmateCount = 0;
			List<MapValue> classUserCount = classUserCount(classIdAtt);
			for (MapValue item : classUserCount) {
				int classId = item.getIntValue("classId");
				int classCount = item.getIntValue("count");
				
				schoolmateCount += classCount;

				for (ClassStruct classItem : list) {
					if (classItem.getClassId() == classId) {
						classItem.setUserCount(classCount);
					}
				}
			}
			count.put("schoolmateCount", schoolmateCount);
			
			
			Map<String, Integer> provinceList = new HashMap<>();
			List<MapValue> classUserList = classApi.getClassUsers(classIdAtt);
			for (MapValue item : classUserList) {
				String province = StringUtils.defaultIfBlank(item.getString("province"), "未知");
				Integer value = provinceList.get(province);
				if (value == null) {
					value = 0;
				}

				value += 1;
				provinceList.put(province, value);
			}
			result.put("schoolmateProvince", provinceList);
		}
		

		StatuscodeMap sm = new StatuscodeMap();
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(result);
		
		
		return sm;
	}
}

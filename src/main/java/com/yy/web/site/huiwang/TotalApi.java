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
import com.yy.util.NumberUtil;
import com.yy.web.Dim;
import com.yy.web.Responsor;
import com.yy.web.base.UserStatus;
import com.yy.web.request.annotation.ApiAction;
import com.yy.web.site.huiwang.cache.Cache;
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
	 * 城市学校统计。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap citySchoolCount() {

		Map<Integer, Integer> map = new HashMap<>();
		
		String type = getStringParam("type");
		int[] cityIds = getIntParams("cityId");
		
		for (int cityId : cityIds) {
			int count = Cache.getCitySchoolCount(type, cityId);
			if (count == -1) {
				MapValue sqlParams = new MapValue();
				sqlParams.put("type", type);
				sqlParams.put("cityId", cityId);
				count = NumberUtil.parseInt(dbSelectOneData(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "citySchoolCount", sqlParams));

				Cache.setCitySchoolCount(type, cityId, count);
			}

			map.put(cityId, count);
		}
		
		
		StatuscodeMap sm = new StatuscodeMap();
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(map);
		
		return sm;
	}

	
	/**
	 * 学校统计。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap schoolCount() {
		
		String type = getStringParam("type");
		int count = Cache.getSchoolCount(type);
		
		if (count == -1) {
			MapValue sqlParams = new MapValue();
			sqlParams.put("type", getStringParam("type"));
			count = NumberUtil.parseInt(dbSelectOneData(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "schoolCount", sqlParams));

			Cache.setSchoolCount(type, count);
		}
		
		
		StatuscodeMap sm = new StatuscodeMap();
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(count);
		
		return sm;
	}
	
	
	/**
	 * 学校人数统计。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap schoolUserCount() {
		
		Map<Integer, Integer> map = new HashMap<>();
		int[] schoolIds = getIntParams("schoolId");
		
		for (int schoolId : schoolIds) {
			int count = Cache.getSchoolUserCount(schoolId);
			if (count == -1) {
				MapValue sqlParams = new MapValue();
				sqlParams.put("schoolId", schoolId);
				count = NumberUtil.parseInt(dbSelectOneData(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "schoolUserCount", sqlParams));
	
				Cache.setSchoolUserCount(schoolId, count);
			}

			map.put(schoolId, count);
		}
		
		
		StatuscodeMap sm = new StatuscodeMap();
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(map);
		
		return sm;
	}


	/**
	 * 指定学校的班级数量。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap schoolClassCount() {

		Map<Integer, Integer> map = new HashMap<>();
		int[] schoolIds = getIntParams("schoolId");
		
		for (int schoolId : schoolIds) {
			int count = Cache.getSchoolClassCount(schoolId);
			if (count == -1) {
				MapValue sqlParams = new MapValue();
				sqlParams.put("schoolId", schoolId);
				count = NumberUtil.parseInt(dbSelectOneData(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "schoolClassCount", sqlParams));

				Cache.setSchoolClassCount(schoolId, count);
			}
			
			map.put(schoolId, count);
		}
		
		
		StatuscodeMap sm = new StatuscodeMap();
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(map);
		
		return sm;
	}

	
	/**
	 * 班级总数。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap classCount() {
		
		int count = Cache.getClassCount();
		if (count == -1) {
			count = NumberUtil.parseInt(dbSelectOneData(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "classCount", new MapValue()));

			Cache.setClassCount(count);
		}
		
		
		StatuscodeMap sm = new StatuscodeMap();
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(count);
		
		return sm;
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
	public Map<Integer, Integer> classUserCount(int[] classIds) {
		
		Map<Integer, Integer> map = new HashMap<>();

		for (int classId : classIds) {
			int count = Cache.getClassUserCount(classId);
			if (count == -1) {
				MapValue sqlParams = new MapValue();
				sqlParams.put("classId", classId);
				count = NumberUtil.parseInt(dbSelectOneData(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "classUserCount", sqlParams));

				Cache.setClassUserCount(classId, count);
			}

			map.put(classId, count);
		}
		

		return map;
	}
	
	
	/**
	 * 班级人员城市分布统计。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap classUserCityCount() {
		
		Map<Integer, Map<String, Integer>> map = new HashMap<>();
		int[] classIds = getIntParams("classId");
		
		for (int classId : classIds) {
			Map<String, Integer> count = Cache.getClassUserCityCount(classId);
			if (count == null) {
				MapValue sqlParams = new MapValue();
				sqlParams.put("classId", classId);
				MapValue dbValue = dbSelectOne(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "classUserCityCount", sqlParams);
				if (dbValue != null && dbValue.isNotEmpty()) {
					count = new HashMap<>();
					count.put(dbValue.getString("cityId"), dbValue.getIntValue("count"));
				}
				if (count == null) {
					count = new HashMap<>();
				}

				Cache.setClassUserCityCount(classId, count);
			}

			map.put(classId, count);
		}
		
		
		StatuscodeMap sm = new StatuscodeMap();
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(map);
		
		return sm;
	}
	
	
	/**
	 * 加入人数总计。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap userCount() {
		
		int count = Cache.getUserCount();
		if (count == -1) {
			count = NumberUtil.parseInt(dbSelectOneData(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "userCount", new MapValue()));

			Cache.setUserCount(count);
		}
		
		
		StatuscodeMap sm = new StatuscodeMap();
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(count);
		
		return sm;
	}
	

	/**
	 * 用户个人信息的统计。
	 * 
	 * @return
	 */
	@ApiAction (login = true)
	public StatuscodeMap myTotal() {
		
		int userId = getUserId();
		
		MapValue result = new MapValue();
		MapValue count = new MapValue();
		result.put("count", count);
		
		
		ClassApi classApi = new ClassApi(getRequest(), getResponse());
		copyTo(classApi);


		StatuscodeTypeMap<List<ClassStruct>> classSm = classApi.my();
		if (classSm.getCode() == Statuscode.SUCCESS) {
			List<ClassStruct> list = classSm.getResult();
			count.put("classCount", list.size());
			count.put("point", new UserStatus().get(userId));
			result.put("classList", list);
			
			
			List<Integer> classIds = new ArrayList<>();
			for (ClassStruct item : list) {
				int classId = item.getClassId();
				classIds.add(classId);
			}

			int[] classIdAtt = ArrayUtil.toInt(classIds.toArray(new Integer[classIds.size()]));
			int schoolmateCount = 0;
			Map<Integer, Integer> classUserCount = classUserCount(classIdAtt);
			
			for (int classId : classIdAtt) {
				schoolmateCount += classUserCount.get(classId);
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

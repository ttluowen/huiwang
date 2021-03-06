package com.yy.web.site.huiwang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.yy.statuscode.Statuscode;
import com.yy.statuscode.StatuscodeMap;
import com.yy.statuscode.StatuscodeTypeMap;
import com.yy.util.MapValue;
import com.yy.util.StringUtil;
import com.yy.web.Dim;
import com.yy.web.Responsor;
import com.yy.web.request.annotation.ApiAction;
import com.yy.web.site.huiwang.struct.CityItemStruct;
import com.yy.web.site.huiwang.struct.CityStruct;

/**
 * 城市接口类。
 * 
 * @since 2018-08-27
 * @version 1.0
 * @author Luowen
 */
public class CityApi extends Responsor {
	
	public static final String SQL_NAMESPACE = "base.city.";
	
	private static List<CityStruct> cityList;
	

	/**
	 * 构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public CityApi(HttpServletRequest request, HttpServletResponse response) {
		
		super(request, response);
	}
	
	
	/**
	 * 获取指定线的城市列表。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeTypeMap<List<CityStruct>> levelList() {
		
		return list(getParams());
	}
	
	
	/**
	 * 获取热闹城市列表。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeTypeMap<List<CityStruct>> hotList() {
		
		MapValue params = new MapValue();
		params.put("hot", true);
		
		
		return list(params);
	}
	
	
	/**
	 * 获取指定参数的城市列表。
	 * 
	 * @param params
	 * @return
	 */
	protected StatuscodeTypeMap<List<CityStruct>> list(MapValue params) {
		
		if (cityList == null) {
			cityList = dbSelect(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "list", null, null, CityStruct.class);
		}
		
		
		String province = null;
		int level = 0;
		if (params != null) {
			province = params.getString("province");
			level = params.getIntValue("level");
		}
		
		
		List<CityStruct> list = new ArrayList<>();
		for (CityStruct item : cityList) {
			boolean right = false;

			if (StringUtils.isNotBlank(province)) {
				right = item.getProvince().equals(province);
			}
			if (right && level != 0) {
				right = item.getLevel() == level;
			}
			
			if (right) {
				list.add(item);
			}
		}


		StatuscodeTypeMap<List<CityStruct>> sm = new StatuscodeTypeMap<>();
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(list);
		
		
		return sm;
	}
	
	
	/**
	 * 获取城市列表。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap list() {
		
		// 查询所有城市列表。
		List<CityStruct> list = list(getParams()).getResult();
		List<String> provinceList = new ArrayList<>();
		Map<String, List<CityItemStruct>> cityMap = new HashMap<>();
		
		
		// 取出省份信息。
		for (CityStruct item : list) {
			String province = item.getProvince();
			
			if (provinceList.indexOf(province) == -1) {
				provinceList.add(province);
			}
		}
		
		// 对省份排序。
		Collections.sort(provinceList, new Comparator<String>() {
			public int compare(String arg0, String arg1) {
				return StringUtil.unNull(arg0).compareTo(StringUtil.unNull(arg1));
			}
		});

		// 取出各省份的城市。
		for (String province : provinceList) {
			for (CityStruct item : list) {
				if (province != null && province.equals(item.getProvince())) {
					List<CityItemStruct> cities = cityMap.get(province);
					if (cities == null) {
						cities = new ArrayList<>();
						cityMap.put(province, cities);
					}
					
					cities.add(item.toItem());
				}
			}
		}
		
		
		StatuscodeMap sm = new StatuscodeMap();
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(cityMap);
		
		
		return sm;
	}
}

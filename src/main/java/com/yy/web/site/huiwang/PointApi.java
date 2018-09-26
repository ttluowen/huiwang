package com.yy.web.site.huiwang;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yy.statuscode.Statuscode;
import com.yy.statuscode.StatuscodeMap;
import com.yy.util.DateUtil;
import com.yy.util.MapValue;
import com.yy.util.NumberUtil;
import com.yy.web.Dim;
import com.yy.web.Responsor;
import com.yy.web.base.UserStatus;
import com.yy.web.base.UserStatusStruct;
import com.yy.web.config.SystemConfig;
import com.yy.web.request.annotation.ApiAction;

/**
 * 积分接口。
 * 
 * @since 2018-09-26
 * @version 1.0
 * @author Luowen
 */
public class PointApi extends Responsor {
	
	private static final String SQL_NAMESPACE = "point.";

	
	/**
	 * 构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public PointApi(HttpServletRequest request, HttpServletResponse response) {
		
		super(request, response);
	}

	
	/**
	 * 查询我的积分总数。
	 * 
	 * @return
	 */
	@ApiAction (login = true)
	public StatuscodeMap my() {
		
		UserStatusStruct status = new UserStatus().get(getUserId());
		int point = 0;
		if (status != null) {
			point = status.getPoint();
		}
		
		
		StatuscodeMap sm = new StatuscodeMap();
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(point);
		
		
		return sm;
	}
	
	
	/**
	 * 查询我的积分增减历史记录。
	 * 
	 * @return
	 */
	@ApiAction (login = true)
	public StatuscodeMap myHistory() {
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("userId", getUserId());
		
		
		return dbSelectMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "list", sqlParams);
	}
	
	
	/**
	 * 统计用户的记录全。
	 * 从历史记录里重新统计。
	 * 
	 * @param userId
	 * @return
	 */
	public int sum(int userId) {
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("userId", userId);
		
		
		return NumberUtil.parseInt(dbSelectOneData(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "sum", sqlParams));
	}
	
	
	/**
	 * 增加积分。
	 * 
	 * @param userId
	 * @param action
	 * @param value
	 * @param description
	 * @return
	 */
	public static int add(int userId, String action, int value, String description) {
		
		return create(userId, action, value, description);
	}
	
	
	/**
	 * 消耗积分。
	 * 
	 * @param userId
	 * @param action
	 * @param value
	 * @param description
	 * @return
	 */
	public static int consume(int userId, String action, int value, String description) {

		return create(userId, action, -Math.abs(value), description);
	}
	
	
	/**
	 * 添加积分项操作。
	 * 可以是增加操作，也可以是减少操作。
	 * 
	 * @param userId
	 * @param action
	 * @param value
	 * @param description
	 * @return
	 */
	private static int create(int userId, String action, int value, String description) {
		
		if (userId < 1) {
			return 0;
		}


		MapValue sqlParams = new MapValue();
		sqlParams.put("userId", userId);
		sqlParams.put("action", action);
		sqlParams.put("value", value);
		sqlParams.put("description", description);
		sqlParams.put("datetime", DateUtil.get(1));


		PointApi api = new PointApi(null, null);
		api.setAppId(SystemConfig.getAppId());
		api.setFactoryKey(SystemConfig.getDefaultFactory());


		int result = api.dbUpdate(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "create", sqlParams);
		if (result > 0) {
			// 查询当前用户最新的积分记录，然后再更新到用户状态表。
			int sum = api.sum(userId);
			new UserStatus().update(userId, "point", sum);
		}


		return result;
	}
}

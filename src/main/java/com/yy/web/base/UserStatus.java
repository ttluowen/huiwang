package com.yy.web.base;

import com.yy.util.MapValue;
import com.yy.web.Dim;
import com.yy.web.Responsor;

/**
 * 用户状态类。
 * 
 * @since 2018-01-28
 * @version 1.0
 * @author Luowen
 */
public class UserStatus {

	/** 已禁用。 */
	public static final int DISABLED = 0;
	/** 默认正常。 */
	public static final int NORMAL = 1;
	/** 未激活的。 */
	public static final int UN_CHECKED = 2;
	
	
	private static final String SQL_NAMESPACE = "base.user.status.";
	
	
	/**
	 * 获取用户状态数据。
	 * 
	 * @param userId
	 * @return
	 */
	public UserStatusStruct get(int userId) {

		MapValue sqlParams = new MapValue();
		sqlParams.put("userId", userId);
		
		
		return Responsor.getInstance().dbSelectOne(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "getUserStatus", sqlParams, null, UserStatusStruct.class);
	}
	
	
	/**
	 * 更新字段数据。
	 * 
	 * @param userId
	 * @param column
	 * @param value
	 * @return
	 */
	public int update(int userId, String column, Object value) {

		MapValue sqlParams = new MapValue();
		sqlParams.put("userId", userId);
		sqlParams.put("column", column);
		sqlParams.put("value", value);
		
		
		return Responsor.getInstance().dbUpdate(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "update", sqlParams);
	}
	
	
	/**
	 * 创建用户状态记录。
	 * 
	 * @param userId
	 * @return
	 */
	public boolean create(int userId) {
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("userId", userId);
		
		
		return Responsor.getInstance().dbUpdate(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "createUserStatus", sqlParams) > 0;
	}
}

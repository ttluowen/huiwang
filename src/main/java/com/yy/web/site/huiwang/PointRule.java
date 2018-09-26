package com.yy.web.site.huiwang;

import java.util.List;

import com.yy.web.Dim;
import com.yy.web.Responsor;
import com.yy.web.site.huiwang.struct.PointRuleStruct;

/**
 * 积分规则管理类。
 * 
 * @since 2018-09-26
 * @version 1.0
 * @author Luowen
 */
public class PointRule {
	
	private static final String SQL_NAMESPACE = "point.rule.";

	private static List<PointRuleStruct> list;
	
	
	/** 创建学校动作。 */
	public static final String ACTION_CREATE_SCHOOL = "createSchool";
	/** 创建班级动作。 */
	public static final String ACTION_CREATE_CLASS = "createClass";
	/** 加班班级动作。 */
	public static final String ACTION_JOIN_CLASS = "joinClass";
	/** 分享动作。 */
	public static final String ACTION_SHARE = "share";
	/** 签到动作。 */
	public static final String ACTION_CHECKIN = "checkin";
	/** 挖矿动作。 */
	public static final String ACTION_MINING = "mining";
	

	
	/**
	 * 获取规则列表。
	 * 
	 * @return
	 */
	public static List<PointRuleStruct> getList() {
		
		if (list == null) {
			list = Responsor.getInstance().dbSelect(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "list", null, null, PointRuleStruct.class);
		}
		
		
		return list;
	}
	
	
	/**
	 * 获取某规则详情。
	 * 
	 * @param action
	 * @return
	 */
	public static PointRuleStruct get(String action) {
		
		List<PointRuleStruct> list = getList();
		if (list != null) {
			for (PointRuleStruct item : list) {
				if (item.getAction().equals(action)) {
					return item;
				}
			}
		}
		
		
		return null;
	}
}

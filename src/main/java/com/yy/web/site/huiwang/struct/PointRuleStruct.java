package com.yy.web.site.huiwang.struct;

/**
 * 积分规则数据结构。
 * 
 * @since 2018-09-26
 * @version 1.0
 * @author Luowen
 */
public class PointRuleStruct {

	private String action;
	private int value;
	private String description;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}

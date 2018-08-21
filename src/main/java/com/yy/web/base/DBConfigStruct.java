package com.yy.web.base;

/**
 * 配置数据结构。
 * 
 * @since 2018-03-04
 * @version 1.0
 * @author Luowen
 */
public class DBConfigStruct {

	/** 记录编号。 */
	private int id;
	/** 站点编号， config.properties 里的 id 属性，为空则表示通用。 */
	private String siteId;
	/** 键名。 */
	private String key;
	/** 值。 */
	private String value;
	/** 默认值。 */
	private String defaultValue;
	/** 描述。 */
	private String description;

	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}

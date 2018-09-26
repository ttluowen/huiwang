package com.yy.web.site.visitrecord.service.struct;

import java.util.Date;

/**
 * 日志文件名的结构信息。
 * 
 * @since 2018-04-05
 * @version 1.0
 * @author Luowen
 */
public class LogFileStruct {

	/** 文件名中的日期。 */
	private Date date;
	/** 所属站点。 */
	private String siteId;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}
}

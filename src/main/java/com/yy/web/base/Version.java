package com.yy.web.base;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yy.statuscode.Statuscode;
import com.yy.statuscode.StatuscodeMap;
import com.yy.statuscode.StatuscodeTypeMap;
import com.yy.util.date.DateUtil;
import com.yy.util.map.MapValue;
import com.yy.web.Dim;
import com.yy.web.Responsor;
import com.yy.web.config.SystemConfig;
import com.yy.web.request.annotation.ApiAction;

/**
 * 系统版本管理。
 * 
 * @since 2018-03-06
 * @version 1.0
 * @author Luowen
 */
public class Version extends Responsor {
	
	/** SQL 命名空间。 */
	private static final String SQL_NAMESPACE = "version.";
	

	/**
	 * 构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public Version(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	
	/**
	 * 查询当前版本号。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeTypeMap<VersionStruct> query() {
		
		return dbSelectOneMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "query", null, null, VersionStruct.class);
	}
	
	
	/**
	 * 查询下一个可用的大版本。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap queryNextBigVersion() {
		
		int version = query().getResult().getNumber();
		int[] versions = SystemConfig.toNumberVersion(version);
		
		int bigVersion = versions[0] + 1;
		int smallVersion = 0;
		int patch = 0;
		
		
		String nextVersion = SystemConfig.toStringVersion(bigVersion, smallVersion, patch);
		
		
		StatuscodeMap sm = new StatuscodeMap();
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(nextVersion);
		
		
		return sm;
	}

	
	/**
	 * 查询下一个可用的小版本。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap queryNextSmallVersion() {
		
		int version = query().getResult().getNumber();
		int[] versions = SystemConfig.toNumberVersion(version);
		
		int bigVersion = versions[0];
		int smallVersion = versions[1] + 1;
		int patch = 0;
		
		
		String nextVersion = SystemConfig.toStringVersion(bigVersion, smallVersion, patch);
		
		
		StatuscodeMap sm = new StatuscodeMap();
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(nextVersion);
		
		
		return sm;
	}
	
	
	/**
	 * 查询下一个可能的补丁版本。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap queryNextPatch() {
		
		int version = query().getResult().getNumber();
		int[] versions = SystemConfig.toNumberVersion(version);
		
		int bigVersion = versions[0];
		int smallVersion = versions[1];
		int patch = versions[2] + 1;
		
		
		String nextVersion = SystemConfig.toStringVersion(bigVersion, smallVersion, patch);
		
		
		StatuscodeMap sm = new StatuscodeMap();
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(nextVersion);
		
		
		return sm;
	}
	
	
	/**
	 * 更新操作。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap update() {

		int version = getIntParam("version");
		String versionStr = SystemConfig.toStringVersion(version); 
		boolean success = update(version);
		
		
		StatuscodeMap sm = new StatuscodeMap();
		if (success) {
			sm.setCode(Statuscode.SUCCESS);
			sm.setResult(versionStr);
		}
		
		
		return sm;
	}
	
	
	/**
	 * 更新下一大版本。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap updateNextBigVersion() {
		
		StatuscodeMap versionSm = queryNextBigVersion();

		String nextVersion = versionSm.getResultAsString();
		int nextVersionNum = SystemConfig.parseVersion(nextVersion);
		boolean success = update(nextVersionNum);
		
		
		StatuscodeMap sm = new StatuscodeMap();
		if (success) {
			sm.setCode(Statuscode.SUCCESS);
			sm.setResult(nextVersion);
		}
		
		
		return sm;
	}
	
	
	/**
	 * 更新下一小版本。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap updateNextSmallVersion() {
		
		StatuscodeMap versionSm = queryNextSmallVersion();

		String nextVersion = versionSm.getResultAsString();
		int nextVersionNum = SystemConfig.parseVersion(nextVersion);
		boolean success = update(nextVersionNum);
		
		
		StatuscodeMap sm = new StatuscodeMap();
		if (success) {
			sm.setCode(Statuscode.SUCCESS);
			sm.setResult(nextVersion);
		}
		
		
		return sm;
	}
	
	
	/**
	 * 更新下一补丁版本。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap updateNextPatch() {
		
		StatuscodeMap versionSm = queryNextPatch();

		String nextVersion = versionSm.getResultAsString();
		int nextVersionNum = SystemConfig.parseVersion(nextVersion);
		boolean success = update(nextVersionNum);
		
		
		StatuscodeMap sm = new StatuscodeMap();
		if (success) {
			sm.setCode(Statuscode.SUCCESS);
			sm.setResult(nextVersion);
		}
		
		
		return sm;
	}
	
	
	/**
	 * 数据库更新操作。
	 * 
	 * @param version
	 * @return
	 */
	private boolean update(int version) {

		MapValue sqlParams = new MapValue();
		sqlParams.put("number", version);
		sqlParams.put("string", SystemConfig.toStringVersion(version));
		sqlParams.put("datetime", DateUtil.get(1));

		
		return dbUpdate(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "update", sqlParams) > 0;
	}
	
	
	/**
	 * 通知各客户端当前最新版本。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap notification() {
		
		StatuscodeMap sm = new StatuscodeMap();
		sm.setCode(Statuscode.SUCCESS);
		
		
		return sm;
	}
	
	
	/**
	 * 更新指定站点的版本号。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap updateSiteVersion() {

		String version = SystemConfig.toStringVersion(getIntParam("version"));
		String siteId = getStringParam("siteId");
		
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("siteId", siteId);
		sqlParams.put("version", version);
		sqlParams.put("datetime", DateUtil.get(1));


		return dbUpdateMap(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "updateSiteVersion", sqlParams);
	}
}

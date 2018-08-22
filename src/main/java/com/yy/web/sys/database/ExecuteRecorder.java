package com.yy.web.sys.database;

import com.yy.database.Database;
import com.yy.database.DatabaseSql;
import com.yy.database.recoder.ExecuteRecorderInterface;
import com.yy.database.recoder.ExecuteStruct;
import com.yy.log.Logger;
import com.yy.util.map.MapValue;
import com.yy.web.Dim;
import com.yy.web.filereader.SqlFileReader;

/**
 * “记录执行数据操作”的记录器实现。
 * 
 * @since 2016-02-02
 * @version 1.0
 * @author Luowen
 */
public class ExecuteRecorder extends ExecuteRecorderInterface {
	
	private static String sqlTemplate;


	/**
	 * 获取 SQL 模板。
	 * 
	 * @return
	 */
	private String getSqlTemplate() {

		if (sqlTemplate == null) {
			// 获取模板。
			sqlTemplate = new SqlFileReader(getFactoryKey()).getSql("executeRecoder.insert");
		}
		
		return sqlTemplate;
	}


	/**
	 * 记录操作。 
	 * 
	 * @param prop
	 */
	public void record(ExecuteStruct prop) {

		try {
			// 如果执行时间小于 2 秒的就不记录了。
			if (prop.getRuntime() < 2000) {
				return;
			}


			MapValue sqlParams = new MapValue();
			sqlParams.put("projectId", prop.getProjectId());
			sqlParams.put("factoryKey", prop.getFactoryKey());
			sqlParams.put("product", prop.getProduct());
			sqlParams.put("database", prop.getDatabase());
			sqlParams.put("username", prop.getUsername());
			sqlParams.put("sql", Database.transferred(prop.getSql()));
			sqlParams.put("runtime", prop.getRuntime());
			sqlParams.put("url", prop.getUrl());
			sqlParams.put("javaCaller", Database.transferred(prop.getJavaCaller()));

			Database.printSql = false;

			String sql = DatabaseSql.renderTemplate(getSqlTemplate(), sqlParams);
			Database db = new Database(getFactoryKey(), Dim.DB_SOURCE_MYSQL);
			db.update(sql);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		} finally {
			Database.printSql = true;
		}
	}
}

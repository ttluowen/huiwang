package com.yy.web.site.visitrecord.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.yy.log.Logger;
import com.yy.util.NumberUtil;
import com.yy.util.StringUtil;
import com.yy.web.Responsor;
import com.yy.web.base.DBConfig;
import com.yy.web.config.SystemConfig;
import com.yy.web.site.visitrecord.service.struct.LogFileStruct;

/**
 * 访问记录器。
 * 记录所有访问的 URL。
 * 
 * @since 2018-04-03
 * @version 1.0
 * @author Luowen
 */
public class VisitRecord {

	/** SQL 命名空间。 */
	public static final String SQL_NAMESPACE = "visit.record.";
	/** 输出日志目录。 */
	public static final String LOG_PATH = "visit";
	
	
	/** 是否初始化设置过可执行状态。 */
	private static boolean setted;
	/** 当前是否执行记录操作。 */
	private static boolean recordable;
	
	/** 上次写日志的天，按天区分文件。 */
	private static int lastDay;
	/** 缓存的写对象。 */
	private static BufferedWriter writer;

	
	/**
	 * 设置记录状态。
	 * 
	 * @param enable
	 */
	public static void setRecordable(boolean enable) {
		
		setted = true;
		recordable = enable;
	}
	

	/**
	 * 获取当前是否可记录的标记状态。
	 * 
	 * @return
	 */
	public static boolean isRecordable() {

		if (!setted) {
			setRecordable(DBConfig.getValueAsBoolean("visit.record"));
		}
		
		
		return recordable;
	}
	

	/**
	 * 记录操作。
	 * 
	 * @param request
	 * @param response
	 */
	public static void record(HttpServletRequest request, HttpServletResponse response) {
		
		if (!isRecordable()) {
			return;
		}
		
		
		try {
			Responsor responsor = new Responsor(request, response);
			
			String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date());
			String url = request.getRequestURL().toString();
			String queryString = request.getQueryString();
			String method = request.getMethod();
			String ip = responsor.getRemoteRealIp();;
			Map<String, String> headersMap = new HashMap<>();

			if (!StringUtil.isEmpty(queryString)) {
				url += "?" + queryString;
			}
			
			
			Enumeration<String> headers = request.getHeaderNames();
			while (headers.hasMoreElements()) {
				String header = headers.nextElement();
				headersMap.put(header, request.getHeader(header));
			}
			
			
			Map<String, Object> data = new HashMap<>();
			data.put("datetime", datetime);
			data.put("url", url);
			data.put("method", method);
			data.put("ip", ip);
			data.put("headers", headersMap);
			data.put("timestamp", StringUtil.gid16());
			
			
			record(data);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}
	
	
	/**
	 * 记录操作。
	 * 
	 * @param data
	 */
	private static void record(final Map<String, Object> data) {
		
		new Thread(new Runnable() {
			public void run() {
				try {
					BufferedWriter writer = getWriter();
					if (writer != null) {
						writer.write(JSON.toJSONString(data) + SystemConfig.LINE_SEPARATOR);
						writer.flush();
					}
				} catch (Exception e) {
				}
			}
		}).start();
	}


	/**
	 * 获取写对象。
	 * 
	 * @return
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 */
	private static BufferedWriter getWriter() throws UnsupportedEncodingException, FileNotFoundException {
		
		boolean newWriter = false;
		int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		if (day != lastDay) {
			newWriter = true;
		}
		
		
		if (newWriter || writer == null) {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
				}
			}

			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getWriterFile(), true), StringUtil.UTF8));
		}
		
		
		return writer;
	}
	
	
	/**
	 * 获取根目录。
	 */
	public static File getRoot() {
	
		File path = new File(
				SystemConfig.getSystemPath()
				+ Logger.PATH + SystemConfig.FILE_SEPARATOR
				+ LOG_PATH + SystemConfig.FILE_SEPARATOR
		);
		
		if (!path.exists()) {
			path.mkdirs();
		}


		return path;
	}
	
	
	/**
	 * 获取最终要写的文件路径。
	 * 
	 * @return
	 */
	private static File getWriterFile() {
		
		String filename = new SimpleDateFormat("yyyyMMdd", Locale.CHINA).format(new Date()) + "-" + SystemConfig.getId() + ".log";
		
		return new File(getRoot(), filename);
	}
	
	
	/**
	 * 解析文件的结构。
	 * 
	 * @param file
	 * @return
	 */
	public static LogFileStruct parseFile(File file) {
		
		String name = file.getName();
		Matcher matcher = Pattern.compile("(\\d{4})(\\d{2})(\\d{2})\\-([^\\.]*?)\\.log").matcher(name);
		
		if (matcher.find()) {
			int year = NumberUtil.parseInt(matcher.group(1));
			int month = NumberUtil.parseInt(matcher.group(2)) - 1;
			int day = NumberUtil.parseInt(matcher.group(3));
			
			String siteId = matcher.group(4);
			
			Calendar calendar = Calendar.getInstance();
			calendar.set(year, month, day);
			Date date = calendar.getTime();
			
			
			LogFileStruct struct = new LogFileStruct();
			struct.setDate(date);
			struct.setSiteId(siteId);
			
			return struct;
		}
		
		
		return null;
	}
	
	
	/**
	 * 获取指定时间及之后的文件列表。
	 * 
	 * @param dateAndAfter
	 */
	public static File[] getFiles(Date dateAndAfter) {
		
		File root = VisitRecord.getRoot();
		
		if (dateAndAfter != null) {
			final long afterTimes = dateAndAfter.getTime();

			return root.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					LogFileStruct struct = VisitRecord.parseFile(pathname);
					if (struct != null) {
						return struct.getDate().getTime() >= afterTimes;
					}
					
					return false;
				}
			});
		} else {
			return root.listFiles();
		}
	}
}

package com.yy.web.base.visit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yy.statuscode.Statuscode;
import com.yy.statuscode.StatuscodeMap;
import com.yy.util.date.DateUtil;
import com.yy.util.number.NumberUtil;
import com.yy.util.string.StringUtil;
import com.yy.web.Responsor;
import com.yy.web.config.SystemConfig;
import com.yy.web.request.annotation.ApiAction;


/**
 * 来访用户信息查询，如工厂键名，当前用户编号、昵称等常用信息。
 * 
 * @since 2015-09-05
 * @version 1.0
 * @author Luowen
 */
public class Visit extends Responsor {

	/**
	 * 访问会话默认超时时间。
	 * 单位分钟。
	 * 
	 * 超过后会自动清除用户访问令牌等信息。
	 */
	public static final int DEFAULT_VISIT_TIMEOUT = 30; 
	/**
	 * 会话超时时间，先从配置里读取，读取不到就使用默认值。
	 */
	private static int visitTimeout;


	// 会话列表。
	private static List<VisitProp> visitList = new ArrayList<VisitProp>();
	
	
	// 静态块。
	static {
		// 配置初始化。
		initConfg();
		
		// 访问回收器。
		cycle();
	}
	
	
	/**
	 * 配置初始化。
	 */
	private static void initConfg() {
		
		Visit.visitTimeout = NumberUtil.parseInt(SystemConfig.getConfig("visitTimeout", DEFAULT_VISIT_TIMEOUT + ""));
	}


	/**
	 * 构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public Visit(HttpServletRequest request, HttpServletResponse response) {

		super(request, response);
	}


	/**
	 * 获取常用信息。
	 * 工厂键名、登录的用户名、昵称等。
	 * 
	 * @param token
	 * @return
	 */
	@ApiAction
	public StatuscodeMap info() {

		StatuscodeMap sm = new StatuscodeMap();


		// 获取令牌信息。
		String token = getAndUpdateToken();


		// 获取来访应用。
		String appId = getStringParam("appId");
		String[] appIds = {appId};
		
		// 获取客户端信息。
//		HttpServletRequest request = getRequest();
//		String ip = request.getRemoteHost();
//		String userAgent = request.getHeader("User-Agent");
		

		VisitProp visitProp = null;
		boolean needCreate = false;
		String factoryKey = "";
		int userId = 0;


		// 检测令牌。
		if (!token.isEmpty()) {
			// 非空，直接从登录列表中取数据。

			visitProp = get(token);

			if (visitProp != null) {
//				String visitIp = visitProp.getIp();
//				String visitUserAgent = visitProp.getUserAgent();

//				if (ip.equals(visitIp) && userAgent.equals(visitUserAgent)) {
//				if (ip.equals(visitIp)) {
					// 是同一个会话用户。

					factoryKey = visitProp.getFactoryKey();
					userId = visitProp.getUserId();

					// 更新最后次访问的时间。
					still(token);
//				} else {
//					// 虽然令牌相同，但浏览器或 IP 不同，说明令牌被盗用。
//					needCreate = true;
//				}
			} else {
				//令牌在会话队列中找不到，有可能已过期。
				needCreate = true;
			}
		} else {
			// 为空，首次来访问。
			needCreate = true;
		}


		// 检测是否要新创建。
		if (needCreate) {
			visitProp = newVisit(appIds);

			token = visitProp.getToken();
			factoryKey = visitProp.getFactoryKey();
		}


		// 生成一个来访的过期时间。
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, visitTimeout);
		String expires = DateUtil.format(calendar, 1);


		// 设置返回结果。
		Map<String, Object> result = new HashMap<>();
		result.put("factoryKey", factoryKey);
		result.put("language", visitProp.getLanguage());
		result.put("isLogin", userId != 0);
		result.put("userId", userId);
		result.put("username", StringUtil.unNull(visitProp.getUsername()));
		result.put("nickname", StringUtil.unNull(visitProp.getNickname()));
		result.put("token", token);
		result.put("expires", expires);

		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(result);


		return sm;
	}


	/**
	 * 切换工厂操作。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap switchFactory() {
		
		StatuscodeMap sm = new StatuscodeMap();


		// 获取页面参数。
		String factoryKey = getStringParam("factoryKey");
//		String ip = getRequest().getRemoteHost();
//		String userAgent = getRequest().getHeader("User-Agent");


		// 获取访问令牌。
		String token = getAndUpdateToken();
		VisitProp visit = get(token);


//		if (visit != null && ip.equals(visit.getIp()) && userAgent.equals(visit.getUserAgent())) {
//		if (visit != null && ip.equals(visit.getIp())) {
		if (visit != null) {
			visit.setFactoryKey(factoryKey);

			// 设置返回结果。
			sm.setCode(Statuscode.SUCCESS);
		} else {
			sm.setDescription("切换失败，会话无效或已过期");
		}


		return sm;
	}


	/**
	 * 生成新访问来身份信息。
	 * @param appIds
	 * 
	 * @return
	 */
	public VisitProp newVisit(String[] appIds) {

		// 生成新令牌。
		String token = generateToken();

		// 检测 appId 参数。
		if (appIds == null || appIds.length == 0) {
			appIds = new String[1];
			appIds[0] = getStringParam("appId");
		}


		// 取默认工厂键名。
		String factoryKey = getDefaultFactoryInfo().getKey();
		// 用户编号为零。
		int userId = 0;


		// 添加一个新访问项。
		VisitProp visitProp = new VisitProp();

		visitProp.setFactoryKey(factoryKey);
		visitProp.setToken(token);
		visitProp.setAppId(appIds);
		visitProp.setLoginCode("");
		visitProp.setUserId(userId);
		visitProp.setDatetime(new Date().getTime());
		visitProp.setIp(getRequest().getRemoteHost());
		visitProp.setUserAgent(getRequest().getHeader("User-Agent"));

		add(visitProp);


		// 保存到会话中。
		getSession().setAttribute(TOKEN_SESSION_NAME, token);


		return visitProp;
	}


	/**
	 * 根据访问令牌获取访问信息。
	 * 
	 * @param token
	 * @return
	 */
	public static VisitProp get(String token) {

		/*
		 * 重新生成一个队列，然后再从新队列中去匹配查询，
		 * 这样就不会受原始数据在循环中被修改而造成找不到的问题了。
		 */
		VisitProp[] list = visitList.toArray(new VisitProp[]{});
		VisitProp item;


		for (int i = list.length - 1; i >= 0; i--) {
			item = list[i];

			if (item != null && item.getToken().equals(token)) {
				return item;
			}
		}


		return null;
	}
	
	
	/**
	 * 添加一个访问项。
	 * 
	 * @param visit
	 */
	public static void add(VisitProp visit) {

		remove(visit);
		visitList.add(visit);
	}
	
	
	/**
	 * 删除指定访问项。
	 * 
	 * @param visit
	 */
	public static void remove(VisitProp visit) {

		visitList.remove(visit);
	}


	/**
	 * 更新用户还在的时间状态。
	 * 
	 * @param token
	 */
	public static void still(String token) {

		VisitProp visit = get(token);
		if (visit != null) {
			visit.setDatetime(new Date().getTime());
		}
	}


	/**
	 * 生成一个用户令牌。
	 * 
	 * @return
	 */
	public static String generateToken() {

		return StringUtil.gsid();
	}


	/**
	 * 过期访问回收器。
	 */
	private static void cycle() {

		new Thread(new Runnable() {
			public void run() {
				/*
				 * 设置定时器。
				 * 每30秒更新一次。
				 */
				Timer timer = new Timer();
				timer.scheduleAtFixedRate(new TimerTask() {
					public void run() {
						handler();
					}
				}, 30 * 1000, 30 * 1000);
			}
			
			
			/**
			 * 回收操作句柄。
			 */
			private void handler() {
				long now = new Date().getTime();
				long timeout = visitTimeout * 60 * 1000;

				VisitProp visit;
				long datetime;

				for (int i = visitList.size() - 1; i >= 0; i--) {
					visit = visitList.get(i);
					datetime = visit.getDatetime();

					try {
						if (now - datetime > timeout) {
							visitList.remove(i);
						}
					} catch (Exception e) {
						visitList.remove(i);
					}
				}
			}
		}).start();;
	}
}

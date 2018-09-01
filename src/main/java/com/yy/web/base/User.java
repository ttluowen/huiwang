package com.yy.web.base;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yy.database.Database;
import com.yy.encrypt.BASE64Q;
import com.yy.encrypt.MD5;
import com.yy.log.Logger;
import com.yy.statuscode.Statuscode;
import com.yy.statuscode.StatuscodeMap;
import com.yy.util.date.DateUtil;
import com.yy.util.file.FileUtil;
import com.yy.util.http.HttpUtil;
import com.yy.util.mail.MailSenderFactory;
import com.yy.util.map.MapValue;
import com.yy.util.number.NumberUtil;
import com.yy.util.regexp.RegexpUtil;
import com.yy.util.string.StringUtil;
import com.yy.util.template.TemplateUtil;
import com.yy.web.Dim;
import com.yy.web.Responsor;
import com.yy.web.base.visit.Visit;
import com.yy.web.base.visit.VisitProp;
import com.yy.web.config.SystemConfig;
import com.yy.web.login.LoginOauth;
import com.yy.web.request.annotation.ApiAction;


/**
 * 用户管理类。
 * 
 * @author Luowen
 */
public class User extends Responsor {
	
	/** SQL 命名空间。 */
	private static final String SQL_NAMESPACE = "base.user.";
	
	/**
	 * 登录码有效期，单位分钟。
	 * 如果解出来的登录码超过了该时间，将视为过期，不能再登录。
	 */
	public static final int LOGIN_CODE_EXPIRE_TIME = 3;

	/** 注册验证码失效时间，单位天。 */
	public static final int REGIST_CODE_EXPIRE = 7 * 24;
	/** 重置密码码失效时间，单位天。 */
	public static final int REVERT_CODE_EXPIRE = 7 * 24;


	/**
	 * 构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public User(HttpServletRequest request, HttpServletResponse response) {

		super(request, response);
	}
	
	
	/**
	 * 获取一个新的登录码。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap getLoginCode() {
		
		StatuscodeMap sm = new StatuscodeMap();
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(new LoginOauth(getRequest(), getResponse()).generateLoginCode());
		
		
		return sm;
	}


	/**
	 * 登录操作。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap login() {
		
		StatuscodeMap sm = new StatuscodeMap();


		// 获取页面参数。
		MapValue postData = getPostParams();
		if (postData == null) {
			sm.setDescription("参数不正确");
			return sm;
		}
		
		
		String loginCode = postData.getString("loginCode");
		String appId = postData.getString("appId");
		String username = postData.getString("username");
		String password = postData.getString("password");
		boolean quickLogin = postData.getBooleanValue("quickLogin");
		
		// 登录设备信息，默认为 pc 设备，该值由客户端生成交记录到 cookie 中，并可用于后续的加解密校验等。
		String device = StringUtil.unEmpty(postData.getString("device"), "pc");


		// 验证登录码。
		if (StringUtil.isEmpty(loginCode)) {
			sm.setDescription("登录码为空");

			return sm;
		}

		// 解出正确的登录码。
		String decodedLoginCode = decodeLoginCode(appId, SystemConfig.getSecret(), loginCode);
		if (decodedLoginCode == null) {
			sm.setDescription("登录码解析错误，或已过期，请刷新重试");
			
			return sm;
		}
		
		
		// 密码加密处理。
		password = MD5.encode(password);


		// 设置 SQL 参数。
		MapValue sqlParams = new MapValue();
		sqlParams.put("username", username);
		sqlParams.put("password", password);


		Logger.log("开始登录");


		// 先关闭 SQL 打印。
		Database.printSql = false;

		// 执行 SQL 语句。
		UserNormalStruct dbUser = dbSelectOne(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "login", sqlParams, null, UserNormalStruct.class);

		// 登录脚本执行完后再开户 SQL 打印。
		Database.printSql = true;


		Logger.log("登录SQL执行完成");


		if (dbUser != null) {
			Logger.log("登录用户名、密码验证通过");


			// 验证通过后的校验及登录或驳回操作。
			sm = loginAfterCheckAndDo(dbUser, appId, decodedLoginCode, device);
			
			
			if (sm.getCode() == Statuscode.SUCCESS) {
				// 设置快捷登录信息。
				if (quickLogin) {
					String userAgent = getRequest().getHeader("User-Agent");
					// 过期时间一个月。
					Calendar expireDatetime = Calendar.getInstance();
					expireDatetime.add(Calendar.MONDAY, 1);
	
					UserQuickLoginDataStruct data = new UserQuickLoginDataStruct();
					data.setUserId(dbUser.getUserId());
					data.setDevice(device);
					data.setUserAgent(userAgent);
					data.setExpireDatetime(expireDatetime.getTime());

					// 生成快捷登录码。
					String quickLoginCode = generateQuickLoginCode(data);
					
					// 追加返回结果。
					sm.getResultAsMap().put("quickLoginCode", quickLoginCode);
				}
			}
		} else {
			sm.setDescription("用户名或密码错误");
		}


		return sm;
	}
	
	
	/**
	 * 用户账号密码验证通过后的校验及登录或驳回操作。
	 * 
	 * @param dbUser
	 * @param appId
	 * @param loginCode
	 * @param device
	 * @return
	 */
	private StatuscodeMap loginAfterCheckAndDo(UserNormalStruct dbUser, String appId, String loginCode, String device) {
		
		StatuscodeMap sm = new StatuscodeMap();


		// 检查用户状态。
		int status = dbUser.getStatus();
		if (status != UserStatus.NORMAL) {
			if (status == UserStatus.DISABLED) {
				sm.setDescription("用户被禁用");
			} else if (status == UserStatus.UN_CHECKED) {
				sm.setDescription("用户未激活");
			} else {
				sm.setDescription("未知用户状态");
			}

			return sm;
		}
		
		
		int userId = dbUser.getUserId();
		String username = dbUser.getUsername();
		String nickname = dbUser.getNickname();


		Logger.log("执行登录成功操作");

		// 登录成功操作。
		String token = loginDo(userId, username, nickname, appId, loginCode, getAndUpdateToken()).getToken();

		Logger.log("执行登录成功操作完成");
		
		
		// 设置返回结果。
		MapValue result = new MapValue();
		result.put("userId", userId);
		result.put("username", username);
		result.put("nickname", nickname);
		result.put("token", token);
		result.put("device", device);

		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(result);
		
		
		Logger.log("登录成功");


		return sm;
	}
	
	
	/**
	 * 快捷登录。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap quickLogin() {
		
		StatuscodeMap sm = new StatuscodeMap();
		
		MapValue postData = getPostParams();
		String appId = postData.getString("appId");
		String device = StringUtil.unEmpty(postData.getString("device"), "pc");
		String quickLoginCode = postData.getString("quickLoginCode");
		String userAgent = getRequest().getHeader("User-Agent");
		String code = decodeQuickLoginCode(device, quickLoginCode);


		if (StringUtil.isEmpty(code)) {
			sm.setDescription("登录码不正确");
			return sm;
		}
		
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("device", device);
		sqlParams.put("quickLoginCode", code);
		sqlParams.put("userAgent", userAgent);

		UserQuickLoginDataStruct result = dbSelectOne(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "quickLogin", sqlParams, null, UserQuickLoginDataStruct.class);
		if (result == null) {
			sm.setDescription("登录码不存在");
		} else {
			if (result.getExpireDatetime().before(new Date())) {
				sm.setDescription("登录码已过期");
				// 清理过期。
				clearQuickLoginCode(result.getUserId(), device, userAgent);
			} else {
				// 成功。

				UserNormalStruct profile = getProfile(result.getUserId());
				if (profile != null) {
					sm = loginAfterCheckAndDo(profile, appId, null, device);
					
					if (sm.getCode() == Statuscode.SUCCESS) {
						// 更新登录次数。
						sqlParams.clear();
						sqlParams.put("id", result.getId());
						sqlParams.put("userId", result.getUserId());
						dbUpdate(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "updateQuickLoginTimes", sqlParams);
					}
				} else {
					sm.setDescription("用户不存在");
				}
			}
		}
		
		
		return sm;
	}
	
	
	/**
	 * 使用微信登录码登录。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap loginByWechatCode() {

		StatuscodeMap sm = new StatuscodeMap();

		try {
			MapValue postData = getPostParams();
			String code = postData.getString("code");
			String nickname = postData.getString("nickname");
			int gender = postData.getIntValue("gender");
			String avatar = postData.getString("avatar");

			MapValue params = new MapValue();
			params.put("appId", DBConfig.get("wechat.minapp.appId"));
			params.put("secret", DBConfig.get("wechat.minapp.secret"));
			params.put("code", code);

			String url = "https://api.weixin.qq.com/sns/jscode2session?appid={appId}&secret={secret}&js_code={code}&grant_type=authorization_code";
			url = StringUtil.substitute(url, params);

			String content = HttpUtil.get(url);
			JSONObject wxResult = JSON.parseObject(content);
			
			if (wxResult.getIntValue("errcode") != 0) {
				sm.setDescription(wxResult.getString("errmsg"));
				return sm;
			}


			String token = getAndUpdateToken();
			if (StringUtil.isEmpty(token)) {
				token = Visit.generateToken();
			}
			
			VisitProp visit = Visit.get(getAndUpdateToken());
			if (visit == null) {
				String[] appIds = {SystemConfig.getAppId()};
				visit = new Visit(getRequest(), getResponse()).newVisit(appIds);
			}


			if (visit != null) {
				String openid = wxResult.getString("openid");
				String sessionKey = wxResult.getString("session_key");
				
				MapValue visitData = visit.getData();
				visitData.put("openid", openid);
				visitData.put("sessionKey", sessionKey);
				
				UserNormalStruct profile = null;
				
				
				if (!wechatOpenidExists(openid)) {
					params = new MapValue();
					params.put("nickname", nickname);
					params.put("gender", gender);
					params.put("avatar", avatar);
					
					int userId = registFromWechat(params);
					if (userId != 0) {
						profile = getProfile(userId);
					} else {
						sm.setDescription("注释来自微信的账号失败");
						return sm;
					}
				} else {
					profile = getProfileByWechatOpenid(openid, UserNormalStruct.class);
				}


				if (profile != null) {
					loginDo(profile.getUserId(), profile.getUsername(), profile.getNickname(), SystemConfig.getAppId(), null, getAndUpdateToken());

					
					MapValue result = new MapValue();
					result.put("token", token);
					
					sm.setResult(result);
					sm.setCode(Statuscode.SUCCESS);
				} else {
					sm.setDescription("获取用户信息失败");
				}
			} else {
				sm.setDescription("token 生成失败，请重新登录");
			}
		} catch (IOException e) {
			Logger.printStackTrace(e);
			sm.setResult(e.getMessage());
		}
		
		
		return sm;
	}


	/**
	 * 退出登录操作。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap logout() {

		String token = getAndUpdateToken();
		if (!token.isEmpty()) {
			// 登出操作。
			logoutDo(token);
		}


		// 始终返回成功。
		StatuscodeMap sm = new StatuscodeMap();
		sm.setCode(Statuscode.SUCCESS);
		sm.setDescription(Statuscode.SUCCESS_DESC);
		
		
		return sm;
	}


	/**
	 * 登录成功后的操作。
	 * 
	 * @param userId
	 * @param username
	 * @param nickname
	 * @param appId
	 * @param loginCode
	 * @param token
	 * @return
	 */
	private VisitProp loginDo(int userId, String username, String nickname, String appId, String loginCode, String token) {
	
		// 更新当前访问的用户。
		VisitProp visit = Visit.get(token);

		
		if (visit == null) {
			String[] appIds = {appId};
			visit = new Visit(getRequest(), getResponse()).newVisit(appIds);
		}
		
		
		visit.setAppId(appId);
		visit.setLoginCode(loginCode);
		visit.setUserId(userId);
		visit.setUsername(username);
		visit.setNickname(nickname);
		visit.setDatetime(new Date().getTime());
		
		
		return visit;
	}


	/**
	 * 退出登录后的操作。
	 * 
	 * @param token
	 */
	private void logoutDo(String token) {
		
		// 更新当前访问的用户。
		VisitProp visit = Visit.get(token);

		if (visit != null) {
			visit.setLoginCode("");
			visit.setUserId(0);
			visit.setUsername("");
			visit.setNickname("");
			visit.setDatetime(new Date().getTime());
		}
	}
	
	
	/**
	 * 生成快捷登录码并记录入库。
	 * 返回的登录码是使用 device 字符加密过的。
	 * 
	 * @param data
	 * @return
	 */
	private String generateQuickLoginCode(UserQuickLoginDataStruct data) {
		
		int userId = data.getUserId();
		String device = data.getDevice();
		String userAgent = data.getUserAgent();

		// 生成快捷登录码。
		String quickLoginCode = StringUtil.gid16();
		
		data.setQuickLoginCode(quickLoginCode);
		data.setDatetime(new Date());
		
		
		// 先清理。
		clearQuickLoginCode(userId, device, userAgent);
		
		
		// 新生成。
		MapValue sqlParams = new MapValue(JSON.parseObject(JSON.toJSONString(data)));
		sqlParams.put("datetime", DateUtil.format(data.getDatetime(), 1));
		sqlParams.put("expireDatetime", DateUtil.format(data.getExpireDatetime(), 1));
		
		int dbResult = dbUpdate(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "generateQuickLoginCode", sqlParams);
		if (dbResult == 0) {
			// 失败的。
			quickLoginCode = null;
		}
		
		
		// 使用 device 作为加密种子进行加密。
		return encodeQuickLoginCode(device, quickLoginCode);
	}
	
	
	/**
	 * 对快捷登录码进行加密。
	 * 
	 * @param device
	 * @param quickLoginCode
	 * @return
	 */
	private String encodeQuickLoginCode(String device, String quickLoginCode) {

		return new BASE64Q(device, true).encode(quickLoginCode);
	}
	
	
	/**
	 * 对快捷登录码进行解密。
	 * 
	 * @param device
	 * @param quickLoginCode
	 * @return
	 */
	private String decodeQuickLoginCode(String device, String quickLoginCode) {

		return new BASE64Q(device, true).decode(quickLoginCode);
	}
	
	
	/**
	 * 清理用户快捷登录信息。
	 * 
	 * @param userId 指定用户，必选。
	 * @param device 指定设备，未指定则清除所有。
	 * @param userAgent 指定用户代理信息，未指定则清除指定设备下的所有。
	 * @return
	 */
	private int clearQuickLoginCode(int userId, String device, String userAgent) {
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("userId", userId);
		sqlParams.put("device", device);
		sqlParams.put("userAgent", userAgent);
		
		
		return dbUpdate(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "clearQuickLoginCode", sqlParams);
	}


	/**
	 * 获取当前登录用户的的基本信息。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap getProfile() {

		int userId = getIntParam("userId");
		if (userId == 0) {
			userId = getUserId();
		}
		
		
		StatuscodeMap sm = new StatuscodeMap();
		UserNormalStruct profile = getProfile(userId);

		if (profile != null) {
			sm.setCode(Statuscode.SUCCESS);
			sm.setResult(profile);
		}
		
		
		return sm;
	}


	/**
	 * 获取指定用户的的基本信息。
	 * 
	 * @param userId
	 * @return
	 */
	public UserNormalStruct getProfile(int userId) {

		return getProfile(userId, UserNormalStruct.class);
	}
	
	
	/**
	 * 根据用户名获取指定用户的基本信息。
	 * 
	 * @param username
	 * @return
	 */
	public UserNormalStruct getProfile(String username) {

		return getProfile(username, UserNormalStruct.class);
	}
	
	
	/**
	 * 根据邮箱获取指定用户的基本信息。
	 * 
	 * @param email
	 * @return
	 */
	public UserNormalStruct getProfileByEmail(String email) {

		return getProfileByEmail(email, UserNormalStruct.class);
	}
	
	
	/**
	 * 获取指定用户的基本信息。
	 * 
	 * @param userId
	 * @param clazz
	 * @return
	 */
	public <T> T getProfile(int userId, Class<T> clazz) {
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("userId", userId);
	
	
		return getProfile(sqlParams, clazz);
	}
	
	
	/**
	 * 获取指定用户的基本信息。
	 * 
	 * @param userId
	 * @param clazz
	 * @return
	 */
	public <T> T getProfile(String username, Class<T> clazz) {
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("username", username);
	
	
		return getProfile(sqlParams, clazz);
	}
	
	
	/**
	 * 根据微信 openid 号获取用户的基本信息。
	 * 
	 * @param wechatOpenid
	 * @param clazz
	 * @return
	 */
	public <T> T getProfileByWechatOpenid(String wechatOpenid, Class<T> clazz) {
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("wechatOpenid", wechatOpenid);
		

		return getProfile(sqlParams, clazz);
	}
	
	
	/**
	 * 根据邮箱获取指定用户的基本信息。
	 * 
	 * @param email
	 * @param clazz
	 * @return
	 */
	public <T> T getProfileByEmail(String email, Class<T> clazz) {
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("email", email);


		return getProfile(sqlParams, clazz);
	}
	
	
	/**
	 * 根据参数获取指定用户的基本信息。
	 * 
	 * @param params
	 * @param clazz
	 * @return
	 */
	public <T> T getProfile(MapValue params, Class<T> clazz) {

		return dbSelectOne(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "getProfile", params, null, clazz);
	}
	
	
	/**
	 * 判断微信 openid 账号是否存在。
	 * 
	 * @param wechatOpenid
	 * @return
	 */
	public boolean wechatOpenidExists(String wechatOpenid) {

		MapValue params = new MapValue();
		params.put("wechatOpenid", wechatOpenid);
		
		return !dbSelectOne(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "getProfile", params).isEmpty();
	}
	
	
	/**
	 * 用户注册。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap regist() {
		
		StatuscodeMap sm = new StatuscodeMap();
		
		
		// 获取页面 POST 数据。
		MapValue postData = getPostParams();
		String username = StringUtil.unNull(postData.getString("username"));
		String nickname = StringUtil.unNull(postData.getString("nickname"));
		String password = StringUtil.unNull(postData.getString("password"));
		String password2 = StringUtil.unNull(postData.getString("password2"));
		String email = StringUtil.unNull(postData.getString("email"));
		
		
		// 基本数据有效性验证。
		if (username.length() < 4 || username.length() > 20) {
			sm.setDescription("用户名格式不正确");
			return sm;
		}
		if (!password.equals(password2)) {
			sm.setDescription("两次密码不一致");
			return sm;
		}
		if (password.length() < 6 || password.length() > 20) {
			sm.setDescription("密码格式不正确");
			return sm;
		}
		if (!RegexpUtil.isEmail(email)) {
			sm.setDescription("邮箱格式不正确");
			return sm;
		}
		
		
		if (nickname.isEmpty()) {
			nickname = username;
		}


		// 验证用户名、邮箱是否存在 。
		MapValue sqlParams = new MapValue();
		sqlParams.put("username", username);
		sqlParams.put("email", email);
		
		List<MapValue> checkResult = dbSelect(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "registCheck", sqlParams);
		if (checkResult.size() > 0) {
			sm.setDescription("用户名或邮箱已存在");
			return sm;
		}
		

		// 注册。
		sqlParams.put("nickname", nickname);
		sqlParams.put("password", MD5.encode(password));
		sqlParams.put("status", UserStatus.UN_CHECKED);
		int registResult = dbUpdate(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "regist", sqlParams);

		if (registResult != 0) {
			// 直接设置为登录状态。
			UserNormalStruct profile = getProfile(username);
			String code = registDo(profile);

			if (!StringUtil.isEmpty(code)) {
				sm.setCode(Statuscode.SUCCESS);
				sm.setDescription(Statuscode.SUCCESS_DESC);
			} else {
				sm.setDescription("发送邮箱验证码失败");
			}
		} else {
			sm.setDescription("注册失败");
		}


		return sm;
	}
	

	/**
	 * 来自微信的注册。
	 * 
	 * @return
	 */
	public int registFromWechat(MapValue data) {
		
		String username = generateWechatUsername();
		String nickname = data.getString("nickName");
		int gender = data.getIntValue("gender");
		String avatar = data.getString("avatarUrl");
		
		data.put("username", username);
		data.put("nickname", nickname);
		data.put("gender", gender);
		data.put("avatar", avatar);
		
		
		return dbInsertAndReturnId(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "regist", data);
	}


	/**
	 * 重新发送注册码邮件。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap resendRegistCode() {
		
		StatuscodeMap sm = new StatuscodeMap();
		
		
		String username = getStringParam("username");
		UserNormalStruct profile = getProfile(username);

		
		if (profile != null) {
			String code = registDo(profile);
			if (code != null) {
				sm.setCode(Statuscode.SUCCESS);
				sm.setResult(Statuscode.SUCCESS);
			}
		}
		
		
		return sm;
	}
	
	
	public String generateWechatUsername() {
		
		return "wx" + StringUtil.gid16();
	}
	
	
	/**
	 * 修改用户状态。
	 * 
	 * @param userId
	 * @param status
	 * @return
	 */
	private int modifyUserStatus(int userId, int status) {
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("userId", userId);
		sqlParams.put("status", status);
		
		
		return dbUpdate(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "modifyUserStatus", sqlParams);
	}
	
	
	/**
	 * 提交注册成功后的操作。
	 * 
	 * @param profile
	 * @return
	 */
	private String registDo(UserNormalStruct profile) {

		String code = StringUtil.gsid(8);
		String username = profile.getUsername();
		
		
		// 先清理可能存在的记录。
		deleteRegistcode(username);
		
		
		// 设置过期时间。
		Calendar expireCalendar = Calendar.getInstance();
		expireCalendar.add(Calendar.HOUR, REGIST_CODE_EXPIRE);
		
		// 将验证码存到数据库。
		MapValue sqlParams = new MapValue();
		sqlParams.put("username", username);
		sqlParams.put("code", code);
		sqlParams.put("expireDatetime", DateUtil.format(expireCalendar, 1));
		
		// 新增。
		int codeResult = dbUpdate(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "insertRegistCode", sqlParams);
		if (codeResult < 1) {
			return null;
		}


		/*
		 * 后面的发送邮件相关。
		 */
		String email = profile.getEmail();
		String domain = StringUtil.appendEndsWith(SystemConfig.getConfig("domain", DBConfig.getValue("domain")), "/");
		String siteName = SystemConfig.getName();
		int userId = profile.getUserId();

		
		MapValue data = new MapValue();
		data.put("userId", userId);
		data.put("username", username);
		data.put("nickname", profile.getNickname());
		data.put("email", email);

		data.put("code", code);
		data.put("domain", domain);
		data.put("siteName", siteName);

		
		// 邮件标题和内容。
		String subject = siteName + " 用户注册验证码";
		String content = TemplateUtil.velocity(FileUtil.read(getConfigFile("base/userRegistMailTemplate.txt")), data);
		
		
		// 发送邮件。
		try {
			MailSenderFactory.getSender().send(email, subject, content);
		} catch (Exception e) {
			Logger.printStackTrace("email[" + email + "]", e);

			return null;
		}


		return code;
	}
	
	
	/**
	 * 清除指定用户的注册验证码。
	 * 
	 * @param username
	 * @return
	 */
	private int deleteRegistcode(String username) {
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("username", username);
		
		
		return dbUpdate(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "deleteRegistCode", sqlParams);
	}
	
	
	/**
	 * 注册验证。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap checkRegistCode() {
		
		StatuscodeMap sm = new StatuscodeMap();
		
		
		MapValue postData = getPostParams();
		String username = StringUtil.unNull(postData.getString("username"));
		String code = StringUtil.unNull(postData.getString("code"));
		
		
		if (username.isEmpty() || code.isEmpty()) {
			sm.setDescription("用户名或验证码不正确");
		}
		
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("username", username);
		sqlParams.put("code", code);


		MapValue result = dbSelectOne(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "checkRegistCode", sqlParams);
		if (!result.isEmpty()) {
			Date expireDatetime = DateUtil.parseDate(StringUtil.unNull(result.get("expireDatetime")));
			if (expireDatetime != null) {
				if (expireDatetime.getTime() > new Date().getTime()) {
					// 清理验证码。
					deleteRegistcode(username);
					// 更新用户状态。
					int statusResult = modifyUserStatus(getProfile(username).getUserId(), 1);

					if (statusResult != 0) {
						// 成功。
						sm.setCode(Statuscode.SUCCESS);
						sm.setDescription(Statuscode.SUCCESS_DESC);
					} else {
						sm.setDescription("验证成功，但更新用户状态时失败");
					}
				} else {
					sm.setDescription("验证码已过期");
				}
			} else {
				sm.setDescription("验证失败");
			}
		} else {
			sm.setDescription("验证码不正确");
		}
		
		
		return sm;
	}
	
	
	/**
	 * 重置密码。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap revert() {
		
		StatuscodeMap sm = new StatuscodeMap();
		
		
		// 获取页面 POST 数据。
		MapValue postData = getPostParams();
		String username = StringUtil.unNull(postData.getString("username"));
		String email = StringUtil.unNull(postData.getString("email"));
		
		Calendar expireCalendar = Calendar.getInstance();
		expireCalendar.add(Calendar.HOUR, REGIST_CODE_EXPIRE);
		
		
		// 基本数据有效性验证。
		if (username.length() < 4 || username.length() > 20) {
			sm.setDescription("用户名格式不正确");
			return sm;
		}
		if (!RegexpUtil.isEmail(email)) {
			sm.setDescription("邮箱格式不正确");
			return sm;
		}


		// 生成 8 位随机密码。
		String newPassword = NumberUtil.getRandom(1, 99999999, true);
		String base64Password = StringUtil.encode(newPassword);
		String md5Password = MD5.encode(newPassword);

		
		// 验证用户名、邮箱是否存在 。
		MapValue sqlParams = new MapValue();
		sqlParams.put("username", username);
		sqlParams.put("password", md5Password);
		sqlParams.put("email", email);
		
		
		// 真正尝试更新用户表，如果用户名、邮箱正确的话。
		int updateResult = dbUpdate(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "setNewPassword", sqlParams);
		if (updateResult == 0) {
			sm.setDescription("重置密码失败，用户名或邮箱不正确");
			return sm;
		}
		
		
		// 设置重置表的 SQL 参数。
		sqlParams.clear();
		sqlParams.put("username", username);
		sqlParams.put("password", base64Password);
		sqlParams.put("expireDatetime", DateUtil.format(expireCalendar, 1));

		// 先清理可能存在的记录。
		dbUpdate(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "deleteRevertCode", sqlParams);
		// 新增。
		int revertResult = dbUpdate(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "insertRevertCode", sqlParams);
		if (revertResult > 0) {
			if (sendRevertMail(username, email, newPassword) == Statuscode.SUCCESS) {
				sm.setCode(Statuscode.SUCCESS);
				sm.setDescription(Statuscode.SUCCESS_DESC);
			} else {
				sm.setDescription("密码重置成功，但邮件发送失败");
			}
		} else {
			sm.setDescription("重置密码失败，请重新尝试");
		}


		return sm;
	}
	
	
	/**
	 * 修改密码。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeMap modifyPassword() {
		
		StatuscodeMap sm = new StatuscodeMap();
		
		
		MapValue params = getPostParams();
		String username = StringUtil.unNull(params.getString("username"));
		String original = StringUtil.unNull(params.getString("original"));
		String password = StringUtil.unNull(params.getString("password"));
		String password2 = StringUtil.unNull(params.getString("password2"));
		
		
		if (username.isEmpty() || original.isEmpty() || password.isEmpty() || password2.isEmpty()) {
			sm.setDescription("信息不完事");
			return sm;
		}
		
		// 基本数据有效性验证。
		if (!password.equals(password2)) {
			sm.setDescription("两次密码不一致");
			return sm;
		}
		if (password.length() < 6 || password.length() > 20) {
			sm.setDescription("密码格式不正确");
			return sm;
		}
		
		
		// MD5 加密。
		String md5Original = MD5.encode(original);
		String md5Password = MD5.encode(password);
		
		
		MapValue sqlParams = new MapValue();
		sqlParams.put("username", username);
		sqlParams.put("original", md5Original);
		sqlParams.put("password", md5Password);
		
		int dbResult = dbUpdate(Dim.DB_SOURCE_MYSQL, SQL_NAMESPACE + "modifyPassword", sqlParams);
		if (dbResult > 0) {
			sm.setCode(Statuscode.SUCCESS);
			sm.setDescription(Statuscode.SUCCESS_DESC);
		}
		
		
		return sm;
	}
	
	
	/**
	 * 发送重置密码邮件。
	 * 
	 * @param nickname
	 * @param email
	 * @param password
	 * @return
	 */
	private int sendRevertMail(String nickname, String email, String password) {
		
		String domain = StringUtil.appendEndsWith(SystemConfig.getConfig("domain", DBConfig.getValue("domain")), "/");
		String siteName = SystemConfig.getName();
		
		
		MapValue data = new MapValue();
		data.put("nickname", nickname);
		data.put("email", email);
		data.put("password", password);
		data.put("domain", domain);
		data.put("siteName", siteName);

		
		// 邮件标题和内容。
		String subject = siteName + " 密码找回成功";
		String content = TemplateUtil.velocity(FileUtil.read(getConfigFile("base/userRevertMailTemplate.txt")), data);
		
		
		// 发送邮件。
		try {
			MailSenderFactory.getSender().send(email, subject, content);
			
			return Statuscode.SUCCESS;
		} catch (Exception e) {
			Logger.printStackTrace("email[" + email + "]", e);
		}
		
		
		return Statuscode.FAILD;
	}


	/**
	 * 加密登录码。
	 * 由 appId,loginCode,timestamp 加密组成。
	 * 
	 * @param appId
	 * @param secret
	 * @param loginCode
	 * @return
	 */
	public String encodeLoginCode(String appId, String secret, String loginCode) {
	
		// 组合。
		loginCode = appId + LoginOauth.CODE_SPLITER
				+ loginCode + LoginOauth.CODE_SPLITER
				+ new Date().getTime();
	
	
		// 新加密登录码并返回。
		return new BASE64Q(secret, true).encode(loginCode);
	}


	/**
	 * 解密登录码。
	 * 由 appId,loginCode,timestamp 加密组成。
	 * 
	 * @param appId
	 * @param secret
	 * @param loginCode
	 * @return
	 */
	public String decodeLoginCode(String appId, String secret, String loginCode) {
	
		// 解出正确的登录码。
		String result = null;
	
		
		try {
			String[] codes = new BASE64Q(secret, true).decode(loginCode).split(LoginOauth.CODE_SPLITER);
	
			// 应用编号。
			String decodedAppId = codes[0];
			// 登录码。
			String decodedCode = codes[1];
			// 时间戳。
			long decodedTimestamp = NumberUtil.parseLong(codes[2]);
	
	
			// 登录超时时间，转换成毫秒。
			long expreTime = LOGIN_CODE_EXPIRE_TIME * 60 * 1000;
			boolean isExpired = new Date().getTime() - decodedTimestamp > expreTime;
	
	
			/*
			 * 检测登录的应用编号是否匹配，
			 * 检测登录是否未过期的。
			 */
			if (decodedAppId.equals(appId) && !isExpired) {
				result = decodedCode;
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	
		
		return result;
	}
}

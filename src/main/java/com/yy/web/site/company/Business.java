package com.yy.web.site.company;

import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import com.yy.database.Database;
import com.yy.web.JavaxServlet;
import com.yy.web.ServletHttp;


/**
 * 商务合作表单管理。
 * 
 * @version 1.0
 * @since 2014-02-02
 * @author luowen@yiyuen.com
 */
public class Business extends JavaxServlet {

	/**
	 * 每天（24小时）最多提交的数量。
	 */
	public static int MAX_NUM_DAILY = 7;


	/**
	 * 构造函数。
	 * 
	 * @param pageContext
	 */
	public Business(PageContext pageContext) {
		super(pageContext);
	}


	/**
	 * 动作管理。
	 * 
	 * @return
	 */
	public String action() {

		String result = "成功";

		try {
			HttpServletRequest request = getRequest();
			String ip = request.getRemoteAddr();
			String userAgent = request.getHeader("User-Agent");

			ResultSet rs = new Database().select("SELECT COUNT(*) AS count FROM business WHERE ip = '" + ip + "' AND datetime BETWEEN DATE_ADD(NOW(), INTERVAL -1 DAY) AND NOW();");
			int count = 0;
			if (rs.next()) {
				count = rs.getInt("count");
			}
			Database.close(rs);

			if (count >= MAX_NUM_DAILY) {
				return "今天提交的次数已达上限，请明天再来吧";
			}

			String nickname = ServletHttp.request_S(request, "nickname");
			String phone = ServletHttp.request_S(request, "phone");
			String email = ServletHttp.request_S(request, "email");
			String company = ServletHttp.request_S(request, "company");
			String content = ServletHttp.request_S(request, "content");

			if (nickname.isEmpty() || phone.isEmpty() || content.isEmpty()) {
				result = "昵称、联系电话、详细需求等重要数据不能为空";
			}

			StringBuffer sql = new StringBuffer()
				.append("INSERT INTO business VALUES ")
				.append("(")
				.append(	"NULL, ")
				.append(	"'").append(nickname).append("', ")
				.append(	"'").append(phone).append("', ")
				.append(	"'").append(email).append("', ")
				.append(	"'").append(company).append("', ")
				.append(	"'").append(content).append("', ")
				.append(	"'").append(ip).append("', ")
				.append(	"'").append(userAgent).append("', ")
				.append(	"NOW()")
				.append(");")
			;

			int updateResult = new Database().update(sql.toString());
			if (updateResult < 1) {
				result = "操作失败，数据没有成功记录，请重新尝试";
			}
		} catch (Exception e) {
			result = "操作失败，提交的数据不正确或系统异常";
		}

		return result;
	}
}

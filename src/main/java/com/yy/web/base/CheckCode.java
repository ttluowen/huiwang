package com.yy.web.base;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.yy.log.Logger;
import com.yy.statuscode.Statuscode;
import com.yy.statuscode.StatuscodeTypeMap;
import com.yy.util.StringUtil;
import com.yy.web.ResponseFormater;
import com.yy.web.Responsor;
import com.yy.web.request.annotation.ApiAction;

/**
 * 验证码。
 * 
 * @since 2017-03-09
 * @version 1.0
 * @author Luowen
 */
public class CheckCode extends Responsor {
	
	/** 状态码的会话名称。 */
	public static final String SESSION_NAME = "base_checkCode";


	/**
	 * 构造函数。
	 * 
	 * @param request
	 * @param response
	 */
	public CheckCode(HttpServletRequest request, HttpServletResponse response) {

		super(request, response);
	}


	/**
	 * 获取状态图片。
	 * 
	 * @return
	 */
	@ApiAction
	public StatuscodeTypeMap<byte[]> image() {

		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		HttpSession session = getSession();
		
		
		request.setAttribute("dataType", ResponseFormater.JPG);
		
		int size = getIntParam("size");
		
		if (size == 0) {
			size = 4;
		}


		//在此处 设置JSP页面无缓存 
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		// 设置图片的长宽 
		int width = 15 + 10 * size + 15;
		int height = 32;

		//设定被随机选取的中文字，此处中文字内容过多，不一一列出，只是举例说明下。 
//		String base = "\u9752\u534a\u706b\u6cd5\u9898\u5efa\u8d76\u4f4d\u5531\u6d77\u4e03\u5973\u4efb\u4ef6\u611f\u51c6\u97f3\u7b54\u54e5\u9645\u65e7\u795e\u5ea7\u7ae0\u538b\u6162\u53d4\u80cc\u7ec6哥只是个神话" ; 
		String base = "0123456789abcdefghijklmnopqrstuvwxyz";
		//设置 备选随机汉字的个数 
		int length = base.length();
		// 创建缓存图像 
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		// 获取图像
		Graphics g = image.getGraphics();
		// 创建随机函数的实例 
		Random random = new Random();
		//此处 设定图像背景色 
		g.setColor(getRandColor(random, 188, 235));
		g.fillRect(0, 0, width, height);
		//设置随机 备选的字体类型 
		String[] fontTypes = { "\u5b8b\u4f53", "\u65b0\u5b8b\u4f53", "\u9ed1\u4f53", "\u6977\u4f53", "\u96b6\u4e66" };
		int fontTypesLength = fontTypes.length;
		// 在图片背景上增加噪点，增加图片分析难度 
		g.setColor(getRandColor(random, 180, 199));
		g.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		for (int i = 0; i < 8; i++) {
			g.drawString("@*@*@*@*@*@*@*",
			0, 5 * (i + 2));
		}
		// 取随机产生的验证码 (4 个汉字 ) 
		// 保存生成的汉字字符串 
		String value = "";

		for (int i = 0; i < size; i++) {
			int start = random.nextInt(length);
			String rand = base.substring(start, start + 1);
			value += rand;
			// 设置图片上字体的颜色 
			g.setColor(getRandColor(random, 10, 150));
			// 设置字体格式 
			g.setFont(new Font(fontTypes[random.nextInt(fontTypesLength)],
			Font.BOLD, 18 + random.nextInt(6)));
			// 将此汉字画到验证图片上面 
			g.drawString(rand, 12 * i + 10 + random.nextInt(8), 24);

		}

		// 将验证码存入 session 中 
		session.setAttribute(SESSION_NAME, value);

		g.dispose();


		//将 图象输出到JSP页面中 
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "JPEG", out);
		} catch (IOException e) {
			Logger.printStackTrace(e);
		}
		
		
		StatuscodeTypeMap<byte[]> sm = new StatuscodeTypeMap<>();
		sm.setCode(Statuscode.SUCCESS);
		sm.setResult(out.toByteArray());
		
		
		return sm;
	}
	
	
	public String getSessionValue() {
		
		return StringUtil.unNull(getSession().getAttribute(SESSION_NAME));
	}


	private Color getRandColor(Random random, int ff, int cc) {
		
		if (ff > 255) {
			ff = 255;
		}
		if (cc > 255) {
			cc = 255;
		}
		
		int r = ff + random.nextInt(cc - ff);
		int g = ff + random.nextInt(cc - ff);
		int b = ff + random.nextInt(cc - ff);
		
		return new Color(r, g, b);
	}
}

package com.yy.web.site.spider;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

import com.yy.log.Logger;
import com.yy.read.BufferedInReader;
import com.yy.util.StringUtil;
import com.yy.util.UrlUtil;

/**
 * 蜘蛛爬虫。
 * 
 * @since 2018-04-23
 * @version 1.0
 * @author Luowen
 */
@SuppressWarnings("deprecation")
public class Spider {
	
	private static HttpClient client;
	
	
	/**
	 * 获取 HTTP 客户端对象。
	 * 
	 * @return
	 */
	public static HttpClient getClient() {

		if (client == null) {
			client = new DefaultHttpClient(new ThreadSafeClientConnManager());			
		}
		
		
		return client;
	}

	
	/**
	 * 获取指定页面的文本内容。
	 * 
	 * @param url
	 * @param charset
	 * @param catchSleep
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String getUrlContent(String url, String charset, int catchSleep) {
		
		return getUrlContent(url, charset, catchSleep, true);
	}


	/**
	 * 获取指定页面的文本内容。
	 * 
	 * @param url
	 * @param charset
	 * @param catchSleep
	 * @param tryAgain
	 * 
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String getUrlContent(String url, String charset, int catchSleep, boolean tryAgain) {

		String content = "";

		try {
			if (catchSleep > 0) {
				Thread.sleep(catchSleep);
			}


			url = url.replaceAll(" ", "%20");

			HttpGet get = new HttpGet(url);
			HttpResponse response = getClient().execute(get);
			HttpEntity entity = response.getEntity();
			
			for (Header header : response.getAllHeaders()) {
				if (header.getName().equals("Content-Encoding")) {
					if (header.getValue().toLowerCase().equals("gzip")) {
						entity = new GzipDecompressingEntity(entity);
					}
				}
			}

			content = BufferedInReader.read(
					entity.getContent(),
					StringUtil.isEmpty(charset) ? StringUtil.UTF8: charset
			);
		} catch (Exception e) {
			if (tryAgain) {
				Logger.printStackTrace("[" + url + "]内容采集出错：" + e.getMessage() + "，将重新尝试一次", e);
				
				return getUrlContent(url, charset, catchSleep, false);
			} else {
				Logger.printStackTrace("[" + url + "]内容采集出错：" + e.getMessage() + "", e);
			}
		}

		
		return content;
	}
	
	
	/**
	 * 拼接 URL。
	 * 
	 * @param base
	 * @param contact
	 * @return
	 */
	public static String contactUrl(String base, String contact) {
		
		base = base.trim().replaceAll("\\\\", "");
		contact = contact.trim().replaceAll("\\\\", "");

		return UrlUtil.contact(base, contact);
//		try {
//			return new URL(new URL(base), contact).toString();
//		} catch (Exception e) {
//			return UrlUtil.contact(base, contact);
//		}
	}

}

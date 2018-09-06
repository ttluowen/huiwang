package com.yy.web.site.huiwang.spider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpGet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yy.log.Logger;
import com.yy.util.file.FileUtil;
import com.yy.util.map.MapValue;
import com.yy.util.streamreader.BufferedInReader;
import com.yy.util.string.StringUtil;
import com.yy.web.config.SystemConfig;
import com.yy.web.site.spider.Spider;

public class DaxueSpider {

	private static void mainInit() {

		String root = System.getProperty("user.dir") + "\\target\\huiwang-1.0\\";
		SystemConfig.setSystemPath(root);
		SystemConfig.setWebInfPath(root + "WEB-INF\\");


		Logger.setSystemPath(root);
	}

	public static void main(String[] args) throws UnsupportedOperationException, IOException {
		
		mainInit();
		
		
		List<MapValue> school = new ArrayList<>();

		int pageSize = 30;
		int totalPage = 9999;
		int recordCount = 0;
		HttpClient client = Spider.getClient();

		
		for (int page = 1; page <= totalPage; page++) {
			String baseUrl = "https://data-gkcx.eol.cn/soudaxue/queryschool.html?messtype=json&callback=jQuery18309684761254968921_1535765528517&province=&schooltype=&page=" + page + "&size=" + pageSize + "&keyWord1=&schoolprop=&schoolflag=&schoolsort=&schoolid=&_=1535765545206";
			HttpGet get = new HttpGet(baseUrl);
			get.addHeader("Referer", "https://gkcx.eol.cn/soudaxue/queryschool.html?&province=&page=" + page);
	
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
	
			for (Header header : response.getAllHeaders()) {
				if (header.getName().equals("Content-Encoding")) {
					if (header.getValue().toLowerCase().equals("gzip")) {
						entity = new GzipDecompressingEntity(entity);
					}
				}
			}

			String responseText = BufferedInReader.read(entity.getContent(), StringUtil.UTF8);
			JSONObject obj = JSON.parseObject(responseText);
			JSONArray schoolList = obj.getJSONArray("school");

			for (Object item : schoolList) {
				JSONObject itemJson = (JSONObject) item;
				String name = itemJson.getString("schoolname");
				String province = itemJson.getString("province");
				String description = itemJson.getString("jianjie");
				String historyNames = itemJson.getString("oldname");
				
				MapValue map = new MapValue();
				map.put("name", name);
				map.put("province", province);
				map.put("description", description);
				map.put("historyNames", historyNames);
				
				school.add(map);
			}


			if (recordCount == 0) {
				recordCount = obj.getJSONObject("totalRecord").getIntValue("num");
				totalPage = (int) Math.ceil((double) recordCount / (double) pageSize);
			}
		}


		StringBuffer fileContent = new StringBuffer();
		String sql = "INSERT INTO school(name, historyNames, type, province, description) VALUES('{name}', '{historyNames}', '大学', '{province}', '{description}');";
		for (MapValue item : school) {
			fileContent.append(StringUtil.substitute(sql, item)).append("\n");
		}
		
		FileUtil.save(SystemConfig.getSystemPath() + "out/大学.sql", fileContent.toString());
	}
}

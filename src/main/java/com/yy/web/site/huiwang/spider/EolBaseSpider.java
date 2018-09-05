package com.yy.web.site.huiwang.spider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.yy.log.Logger;
import com.yy.util.file.FileUtil;
import com.yy.util.map.MapValue;
import com.yy.util.number.NumberUtil;
import com.yy.util.string.StringUtil;
import com.yy.web.config.SystemConfig;

public class EolBaseSpider {
	
	private static String BASE_URL = "http://xuexiao.eol.cn/?cengci={schoolType}_cengci&local1={location}_local1&page={page}";
	
	private String schoolType;
	
	private List<String> provinces;
	
	
	public EolBaseSpider(String schoolType) {
		
		this.schoolType = schoolType;
		
		mainInit();
	}
	

	private void mainInit() {

		String root = System.getProperty("user.dir") + "\\target\\huiwang-1.0\\";
		SystemConfig.setSystemPath(root);
		SystemConfig.setWebInfPath(root + "WEB-INF\\");


		Logger.setSystemPath(root);
	}
	
	
	public void start() throws IOException {
		
		provinces = getProvinces();
		List<MapValue> list = new ArrayList<>();
		

		for (String province : provinces) {
			list.addAll(collectedProvince(province));
		}
		
		if (list != null && list.size() > 0) {
			saveData(list);
		}
	}
	
	
	/**
	 * 获取省市列表。
	 * 
	 * @return
	 * @throws IOException
	 */
	protected List<String> getProvinces() throws IOException {
		
		List<String> provinces = new ArrayList<>();
		
		MapValue data = new MapValue();
		data.put("schoolType", schoolType);
		data.put("location", "");
		data.put("page", 1);
		
		String url = StringUtil.substitute(BASE_URL, data);
		Document doc = Jsoup.connect(url).get();
		
		for (Element item : doc.select(".area_list li a")) {
			String location = item.text().trim();
			if (!StringUtil.isEmpty(location)) {
				provinces.add(location);
			}
		}
		
		
		return provinces;
	}
	
	
	protected List<MapValue> collectedProvince(String province) throws IOException {

		MapValue data = new MapValue();
		data.put("schoolType", schoolType);
		data.put("location", province);
		data.put("page", 1);
		
		String url = StringUtil.substitute(BASE_URL, data);
		Document doc = Jsoup.connect(url).get();
		Element pageWrap = doc.selectFirst(".page");
		Matcher pageMatcher = Pattern.compile("/共(\\d*?)页").matcher(pageWrap.text());
		int page = 1;
		if (pageMatcher.find())  {
			page = NumberUtil.parseInt(pageMatcher.group(1));
		}
		List<MapValue> list = new ArrayList<>();
		
		Logger.log(province + "共需采集" + page + "页");
		
		
		for (int i = 1; i <= page; i++) {
			data = new MapValue();
			data.put("schoolType", schoolType);
			data.put("location", province);
			data.put("page", page);

			url = StringUtil.substitute(BASE_URL, data);
			doc = Jsoup.connect(url).get();
			Element wrap = doc.selectFirst(".red_border");
			
			for (Element item : wrap.select(".right_box")) {
				try {
					String name = item.selectFirst("h2 a").text().trim();
					Element descriptionP = item.selectFirst(".txt_l p");
					if (descriptionP != null) {
						String description = descriptionP.text().replace("学校简介：", "").replace("。。。", "").trim();
						
						MapValue map = new MapValue();
						map.put("province", province);
						map.put("name", name);
						map.put("description", description);
						
						list.add(map);
					}
					
				} catch (Exception e) {
					continue;
				}
			}
		}
		
		
		return list;
	}
	
	
	protected void saveData(List<MapValue> list) {
		
		StringBuffer fileContent = new StringBuffer();
		String sql = "INSERT INTO school(name, historyNames, type, province, description) VALUES('{name}', '', '" + schoolType + "', '{province}', '{description}');";
		for (MapValue item : list) {
			fileContent.append(StringUtil.substitute(sql, item)).append("\n");
		}
		
		FileUtil.save(SystemConfig.getSystemPath() + "out/" + schoolType + ".sql", fileContent.toString());
	}
}

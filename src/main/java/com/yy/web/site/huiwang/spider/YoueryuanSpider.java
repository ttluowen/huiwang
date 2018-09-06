package com.yy.web.site.huiwang.spider;

import java.io.IOException;

public class YoueryuanSpider extends EolBaseSpider {

	public YoueryuanSpider(String schoolType) {

		super(schoolType);
	}

	public static void main(String[] args) throws IOException {

		new YoueryuanSpider("幼儿园").start();
	}
}

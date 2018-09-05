package com.yy.web.site.huiwang.spider;

import java.io.IOException;

public class GaozhongSpider extends EolBaseSpider {

	public GaozhongSpider(String schoolType) {

		super(schoolType);
	}

	public static void main(String[] args) throws IOException {

		new GaozhongSpider("高中").start();
	}
}

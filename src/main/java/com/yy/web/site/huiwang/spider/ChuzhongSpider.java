package com.yy.web.site.huiwang.spider;

import java.io.IOException;

public class ChuzhongSpider extends EolBaseSpider {

	public ChuzhongSpider(String schoolType) {

		super(schoolType);
	}

	public static void main(String[] args) throws IOException {

		new ChuzhongSpider("初中").start();
	}
}

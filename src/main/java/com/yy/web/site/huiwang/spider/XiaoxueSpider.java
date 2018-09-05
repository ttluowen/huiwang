package com.yy.web.site.huiwang.spider;

import java.io.IOException;

public class XiaoxueSpider extends EolBaseSpider {

	public XiaoxueSpider(String schoolType) {

		super(schoolType);
	}

	public static void main(String[] args) throws IOException {

		new XiaoxueSpider("小学").start();
	}
}

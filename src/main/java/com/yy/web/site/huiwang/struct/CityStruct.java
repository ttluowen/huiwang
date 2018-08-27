package com.yy.web.site.huiwang.struct;

public class CityStruct extends CityItemStruct {

	private String country;
	private String province;

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public CityItemStruct toItem() {

		CityItemStruct item = new CityItemStruct();
		item.setCityId(getCityId());
		item.setName(getName());
		item.setPinyin(getPinyin());
		item.setLevel(getLevel());
		
		return item;
	}
}

package com.microservices.currencyconversionservice.bean;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class RealTimeConversionResponse {

	private Map<String, BigDecimal> details = new LinkedHashMap<>();

	@JsonAnySetter
	void setDetail(String key, BigDecimal value) {
		details.put(key, value);
	}

	public Map<String, BigDecimal> getDetails() {
		return details;
	}

}

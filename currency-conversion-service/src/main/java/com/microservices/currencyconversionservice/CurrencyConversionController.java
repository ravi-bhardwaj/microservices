package com.microservices.currencyconversionservice;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.currencyconversionservice.bean.CurrencyConversionBean;
import com.microservices.currencyconversionservice.bean.RealTimeConversionResponse;

@RestController
public class CurrencyConversionController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private CurrencyExchangeServiceProxy proxy;

	@GetMapping("/currency-converter/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversionBean cnvertCurrency(@PathVariable String from, @PathVariable String to,
			@PathVariable BigDecimal quantity) {

		Map<String, String> uriVariables = new HashMap<>();
		uriVariables.put("from", from);
		uriVariables.put("to", to);

		ResponseEntity<CurrencyConversionBean> responseEntity = new RestTemplate().getForEntity(
				"http://localhost:8000/currency-exchange/from/{from}/to/{to}", CurrencyConversionBean.class,
				uriVariables);

		CurrencyConversionBean response = responseEntity.getBody();

		return new CurrencyConversionBean(1L, from, to, response.getConversionMultiple(), quantity,
				quantity.multiply(response.getConversionMultiple()), response.getPort());

	}

	@GetMapping("/currency-converter-feign/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversionBean cnvertCurrencyFeign(@PathVariable String from, @PathVariable String to,
			@PathVariable BigDecimal quantity) {

		CurrencyConversionBean response = proxy.retrieveExchangeValue(from, to);

		logger.info("response = ", response);
		
		return new CurrencyConversionBean(1L, from, to, response.getConversionMultiple(), quantity,
				quantity.multiply(response.getConversionMultiple()), response.getPort());

	}

	@GetMapping("/currency-converter-real-time/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversionBean convertCurrencyRealTime_Feign(@PathVariable String from, @PathVariable String to,
			@PathVariable BigDecimal quantity) {

		Map<String, String> uriVariables = new HashMap<>();
		uriVariables.put("from", from);
		uriVariables.put("to", to);

		ResponseEntity<String> responseEntity = new RestTemplate().getForEntity(
				"https://free.currconv.com/api/v7/convert?q={from}_{to}&compact=ultra&apiKey=aa66a046e57c480e3839",
				String.class, uriVariables);

		Map<String, BigDecimal> response = null;
		try {
			response = new ObjectMapper().readValue(responseEntity.getBody(), RealTimeConversionResponse.class)
					.getDetails();
		} catch (IOException e) {
			e.printStackTrace();
		}
		BigDecimal multiple = response.get(from + "_" + to);
		return new CurrencyConversionBean(1L, from, to, multiple, quantity, quantity.multiply(multiple), 0);

	}

}

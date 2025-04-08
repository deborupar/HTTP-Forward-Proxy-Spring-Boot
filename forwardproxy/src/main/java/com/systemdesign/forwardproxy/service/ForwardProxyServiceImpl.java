package com.systemdesign.forwardproxy.service;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

@Service
public class ForwardProxyServiceImpl implements ForwardProxyService {

	final Logger log = LoggerFactory.getLogger(this.getClass());
	@Override
	public ResponseEntity doProxy(HttpServletRequest request) {
		/*
		 * 1. get the header elements from the request --> Store it in a HashMap
		 * 2. Replace the host attribute to "via" and value "proxy-server" 
		 * 3. Capture the request parameters --> store it in a HashMap 
		 * 4. Create a HttpResponse entity
		 * 5. Capture the request body 
		 * 5a. if content type of body is image --> copy it as it is
		 * Why handle image content types separately? 
		 * i. Binary data vs. text data
		 * Images are binary data (byte[]), not text.
		 * 
		 * If you try to treat binary data as a String (like doing new
		 * String(result.getBody())), it can:
		 * 
		 * Corrupt the image
		 * 
		 * Throw character encoding errors
		 * 
		 * Cause unpredictable behavior on the client side
		 */
		
		//1.  get the header elements from the request --> Store it in a HashMap
		HashMap<String, String> headerNames = new HashMap<>();
		Enumeration<String> headers = request.getHeaderNames();
		while(headers.hasMoreElements()) {
			String headerName = headers.nextElement();
			String headerValue = request.getHeader(headerName);
			headerNames.put(headerName, headerValue);
		}
		
		//2. Replace the host attribute to "via" and value "proxy-server" 
		headerNames.remove("host");
		headerNames.put("host", "proxy");
		//3. Query params
//		HashMap<String, Object> parameterNames = new HashMap<>();
//		Enumeration<String> parameters = request.getParameterNames();
//		while(parameters.hasMoreElements()) {
//			String parameterName = parameters.nextElement();
//			String parameterValue = request.getParameter(parameterName);
//			parameterNames.put(parameterName, parameterValue);
//		}
		//3. If you are not modifying the query params then we can use the below :
		String queryString = request.getQueryString();
		//4. create response entity
		
		String fullUrl = request.getRequestURL().toString();
		fullUrl = fullUrl.replace("localhost:8082", "localhost:8080");
		if(queryString !=null)
			fullUrl += "?"+queryString;
		HttpResponse<String> response = null;
		
		if("GET".equalsIgnoreCase(request.getMethod()))
			response = Unirest.get(fullUrl)
					  // .headers(headerNames != null ? headerNames : Collections.emptyMap())
					   .headers(headerNames)
					   .connectTimeout(100000)
			           .socketTimeout(100000)
					   .asString();
		
		else if("POST".equalsIgnoreCase(request.getMethod()))
			response = Unirest.post(fullUrl)
					   .headers(headerNames != null ? headerNames : Collections.emptyMap())
					   .body(getRequestBody(request))
					   .asString();
		
		else if("PUT".equalsIgnoreCase(request.getMethod()))
			response = Unirest.put(fullUrl)
					   .headers(headerNames != null ? headerNames : Collections.emptyMap())
					   .body(getRequestBody(request))
					   .asString();
		
		
		if(response == null)
			return  ResponseEntity.notFound().build();
		else {
			String respBody = response.getBody();
			HttpHeaders header = convertToHttpHeaders(headerNames);
			header.setContentLength(respBody.length());
			log.info("Response body {}", respBody);
			return ResponseEntity.status(response.getStatus())
					.headers(header)
					.body(respBody);
		}
			
	
	}
	
	String getRequestBody(HttpServletRequest request) {
		StringBuilder bodyBuilder = new StringBuilder();
		BufferedReader reader;
		try {
			reader = request.getReader();
			String line;
			while ((line = reader.readLine()) != null) {
			    bodyBuilder.append(line);
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		
		
		String requestBody = bodyBuilder.toString();
		return requestBody;
	}
	
	public HttpHeaders convertToHttpHeaders(Map<String, String> map) {
	    HttpHeaders headers = new HttpHeaders();
	    map.forEach(headers::add);
	    return headers;
	}

}

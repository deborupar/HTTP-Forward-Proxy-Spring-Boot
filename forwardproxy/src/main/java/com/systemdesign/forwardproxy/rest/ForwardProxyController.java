package com.systemdesign.forwardproxy.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.systemdesign.forwardproxy.service.ForwardProxyService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/")
public class ForwardProxyController {
	
	final Logger log1 = LoggerFactory.getLogger(this.getClass());
	private ForwardProxyService forwardProxyService;
	
	@Autowired
	ForwardProxyController(ForwardProxyService forwardProxyService){
		this.forwardProxyService = forwardProxyService;
	}

	@RequestMapping(value="/**")
	public ResponseEntity<String> handleRequests(HttpServletRequest request) {
		log1.info(" Received {} from url {}",request.getMethod() , request.getRequestURL().toString());
//		ResponseEntity<String> resp = 
//		log1.info(" Sent with status code",resp.getStatusCode());
//		return resp;
		return forwardProxyService.doProxy(request);
		}
	}

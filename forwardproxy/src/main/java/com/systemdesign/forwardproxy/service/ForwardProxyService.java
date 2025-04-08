package com.systemdesign.forwardproxy.service;

import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;

public interface ForwardProxyService {
	ResponseEntity doProxy(HttpServletRequest request);
}

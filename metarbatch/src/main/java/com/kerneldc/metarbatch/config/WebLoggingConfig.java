package com.kerneldc.metarbatch.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class WebLoggingConfig {

	@Bean
    public FilterRegistrationBean<CommonsRequestLoggingFilter>  requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter() {
        	@Override
            protected boolean shouldLog(HttpServletRequest request) {
                String path = request.getRequestURI();
                // Log only requests to /actuator
                return path.startsWith("/actuator");
            }
        	@Override
        	protected void beforeRequest(HttpServletRequest request, String message) {
        	    LOGGER.debug("Actuator request: [{}]", message);
        	}
        	// Only log before request urls
        	@Override
        	protected void afterRequest(HttpServletRequest request, String message) {
        	    // do nothing — suppress after-request logging
        	}
        };
        filter.setIncludeClientInfo(true);
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setIncludeHeaders(false); // change to true if needed
        filter.setMaxPayloadLength(10000);
        
        FilterRegistrationBean<CommonsRequestLoggingFilter> registrationBean = new FilterRegistrationBean<>(filter);
        registrationBean.setOrder(1); // Set order if you have multiple filters
        return registrationBean;
    }
}

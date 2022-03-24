package org.summerclouds.common.rest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(name="org.summerclouds.rest.tracing.enabled",havingValue="true",matchIfMissing = true)
public class RestTracingMvcConfigurer implements WebMvcConfigurer {

	@Override
    public void addInterceptors(InterceptorRegistry registry) {
       registry.addInterceptor(new RestTracingInterceptor()).addPathPatterns("/**");
    }
	
	
}

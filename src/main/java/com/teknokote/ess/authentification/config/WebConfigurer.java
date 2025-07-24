package com.teknokote.ess.authentification.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.teknokote.ess.http.logger.InterceptLog;

@Configuration
@Component
public class WebConfigurer implements WebMvcConfigurer {

	@Autowired
    private InterceptLog logInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor);
    }
}

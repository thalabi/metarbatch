package com.kerneldc.metarbatch.config;


import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Value("${application.security.corsFilter.corsUrlsToAllow}")
    private String[] corsUrlsToAllow;

    @Value("${application.security.corsFilter.corsMaxAgeInSecs:3600}")
    private long corsMaxAgeInSecs;

	@Bean
	UrlBasedCorsConfigurationSource corsConfigurationSource() {
      final var configuration = new CorsConfiguration();

      configuration.setAllowedOrigins(Arrays.asList(corsUrlsToAllow));
      configuration.setMaxAge(corsMaxAgeInSecs);

      configuration.setAllowedMethods(Arrays.asList("GET", "HEAD", "POST", "DELETE", "PUT", "PATCH"));
      configuration.setAllowedHeaders(Arrays.asList("*"));
      configuration.setExposedHeaders(Arrays.asList("*"));
      
      final var source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", configuration);

      return source;
    }
}

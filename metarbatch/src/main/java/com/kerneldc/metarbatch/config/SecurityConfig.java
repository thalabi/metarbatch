package com.kerneldc.metarbatch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.DelegatingJwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

	@Value("${application.security.disableSecurity:false}")
	private boolean disableSecurity;
	@Value("${application.security.actuator.username}")
	private String actuatorUsername;
	@Value("${application.security.actuator.password}")
	private String actuatorPassword;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
			KeycloakJwtRolesConverter keycloakJwtRolesConverter) throws Exception {

		DelegatingJwtGrantedAuthoritiesConverter authoritiesConverter =
				// Using the delegating converter multiple converters can be combined
				new DelegatingJwtGrantedAuthoritiesConverter(
						// First add the default converter
						new JwtGrantedAuthoritiesConverter(),
						// Second add our custom Keycloak specific converter
						keycloakJwtRolesConverter);

		// Set up http security to use the JWT converter defined above
		httpSecurity.oauth2ResourceServer((oauth2) -> oauth2
				.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConv -> new JwtAuthenticationToken(jwtConv,
						authoritiesConverter.convert(jwtConv), keycloakJwtRolesConverter.getUsername(jwtConv)))));

		if (disableSecurity) {
			LOGGER.warn("*** appliction security is currently disabled ***");
			LOGGER.warn("*** to enable set application.security.disableSecurity to false ***");
			httpSecurity.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests.anyRequest().permitAll());
		} else {
			httpSecurity.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
			.requestMatchers("/appInfoController/*", "/pingController/*").permitAll());
			httpSecurity.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
			.requestMatchers("/actuator/*").hasRole("ACTUATOR")).httpBasic(Customizer.withDefaults());
			httpSecurity.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests.anyRequest().authenticated());
		}
		
		httpSecurity.exceptionHandling(
						exception -> exception.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
								.accessDeniedHandler(new BearerTokenAccessDeniedHandler()));

		httpSecurity.csrf(csrf -> csrf.disable());
		httpSecurity.cors(Customizer.withDefaults());
				
		httpSecurity.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		
		return httpSecurity.build();
    }

	@Bean
	public UserDetailsService userDetailsService() {
	    UserDetails admin = User.withUsername(actuatorUsername)
	        .password(passwordEncoder().encode(actuatorPassword))
	        .roles("ACTUATOR")  // this adds ROLE_ADMIN
	        .build();

	    return new InMemoryUserDetailsManager(admin);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
	    return new BCryptPasswordEncoder(); // or NoOpPasswordEncoder for testing only
	}	

}

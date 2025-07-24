package com.teknokote.ess.authentification.config;

import com.teknokote.ess.authentification.config.permissions.CustomMethodSecurityExpressionHandler;
import com.teknokote.ess.controller.EndPoints;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

   public static final String[] WHITE_LIST = { EndPoints.UPLOAD+"/UploadWithoutAuth", EndPoints.UPLOAD+"/**" };
   //public static final String[] WHITE_LIST = { EndPoints.UPLOAD+"/UploadWithoutAuth" };
   private final JwtAuthConverter jwtAuthConverter;
   private final UsernamePasswordAuthenticationTokenConverter usernamePasswordAuthenticationTokenConverter;

   @Bean
   public CustomMethodSecurityExpressionHandler create(){
      return new CustomMethodSecurityExpressionHandler();
   }
   @Bean
   @Order(1)
   public SecurityFilterChain basicSecurityFilterChain(HttpSecurity http) throws Exception
   {
      http
         .securityMatcher(EndPoints.UPLOAD+"/*", "/actuator/**")
         .csrf()
         .disable()
         .cors()
         .and()
         .authorizeHttpRequests()
         .requestMatchers(WHITE_LIST).permitAll()
         .and()
         .authorizeHttpRequests()
         .anyRequest().authenticated()
         .and()
         .httpBasic();

      http
              .sessionManagement()
              .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

      return http.build();
   }

   @Bean
   @Order(2)
   public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception
   {
      http
              .csrf()
              .disable()
              .cors()
              .and()
              .authorizeHttpRequests()
              .requestMatchers( "/websocket","/login","/user/reset-password","/user/forgot-password","/websocketFrontEnd","/websocket-endpoint/**").permitAll()
              .anyRequest()
              .authenticated();

      http
              .oauth2ResourceServer()
              .jwt()
              .jwtAuthenticationConverter(jwtAuthConverter.andThen(usernamePasswordAuthenticationTokenConverter));

      http
              .sessionManagement()
              .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

      return http.build();
   }

   @Bean
   public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
   }
}

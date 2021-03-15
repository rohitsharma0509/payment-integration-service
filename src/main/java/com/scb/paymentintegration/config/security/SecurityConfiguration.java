package com.scb.paymentintegration.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {


   @Override
   protected void configure(HttpSecurity http) throws Exception {
      http.headers().frameOptions().disable();
      http.csrf().disable();
      http.authorizeRequests().antMatchers("/").permitAll();
   }

   @Override
   public void configure(WebSecurity web) throws Exception {
      web.ignoring().antMatchers("/*/**");
   }
} 
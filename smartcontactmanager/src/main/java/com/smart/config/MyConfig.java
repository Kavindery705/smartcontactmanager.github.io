package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class MyConfig {
	
	
	@Bean
	public UserDetailsService userDetailService() {
//		
//		UserDetails normalUser = User.withUsername("kavi").password(passwordEncoder().encode("kavi")).roles("NORMAL").build();
//		UserDetails adminUser = User.withUsername("kavinder").password(passwordEncoder().encode("kavi")).roles("ADMIN").build();
//		
//		InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager(normalUser,adminUser);
		return new UserDetailsServiceImpel();
		
		//Database
		
		
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(this.userDetailService());
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		
		return daoAuthenticationProvider;
		
		
	}
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
			 httpSecurity.csrf()
			 .disable()
			 .authorizeHttpRequests()
			 
			 .requestMatchers("/admin/**").hasRole("ADMIN")
			 .requestMatchers("/user/**").hasRole("USER")
			 .requestMatchers("/**").permitAll()
			 .anyRequest()
			 .authenticated()
			 .and()
			 .formLogin()
			 .loginPage("/signin")
			 .loginProcessingUrl("/dologin")
			 .defaultSuccessUrl("/user/index");
			 return httpSecurity.build();
	}
}

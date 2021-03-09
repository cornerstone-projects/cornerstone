package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private Environment env;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		if (env.acceptsProfiles(Profiles.of("test"))) {
			auth.inMemoryAuthentication().withUser("user").password(encoder.encode("password")).roles("USER").and()
					.withUser("admin").password(encoder.encode("admin")).roles("USER", "ADMIN");
		} else {
			// TODO
		}
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/resources/**").antMatchers("/actuator/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests().antMatchers("/**").hasAnyRole("USER").anyRequest().authenticated();
		if (env.acceptsProfiles(Profiles.of("test")))
			http.httpBasic();
		else
			http.formLogin().loginPage("/login").loginProcessingUrl("/perform_login").usernameParameter("username")
					.passwordParameter("password").permitAll().successForwardUrl("/home").and().logout()
					.logoutUrl("/logout").permitAll();
	}

}
package com.patrolmanagr.patrolmanagr.security;
import com.patrolmanagr.patrolmanagr.security.jwt.JwtAuthTokenFilter;
import com.patrolmanagr.patrolmanagr.security.jwt.JwtAuthEntryPoint;
import com.patrolmanagr.patrolmanagr.security.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    @Autowired
    UserDetailsServiceImpl userDetailsService;
    @Autowired
    private JwtAuthEntryPoint jwtAuthEntryPoint;
    @Bean
    public JwtAuthTokenFilter authenticationJwtTokenFilter() {
        return new JwtAuthTokenFilter();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Nouveauté de Spring 3: définir SecurityFilterChain en tant que bean qui remplace void configure
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .authorizeHttpRequests()
                .requestMatchers("/api/patrolmanagr/user/authenticate").permitAll()
                .requestMatchers("/api/patrolmanagr/user/forgot-password").permitAll()
                .requestMatchers("/api/patrolmanagr/user/reset-password").permitAll()
                .requestMatchers("/api/patrolmanagr/user/add").permitAll()
                .requestMatchers("/api/patrolmanagr/role/add").permitAll()
                //
                .requestMatchers("/api/patrolmanagr/zone/add").permitAll()
                .requestMatchers("/api/patrolmanagr/zone/all").permitAll()
                //
                .requestMatchers("/api/patrolmanagr/site/add").permitAll()
                .requestMatchers("/api/patrolmanagr/site/all").permitAll()
                //
                .requestMatchers("/api/patrolmanagr/vendorapi/add").permitAll()
                .requestMatchers("/api/patrolmanagr/vendorapi/all").permitAll()
                //
                .requestMatchers("/images/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(jwtAuthEntryPoint)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder())
                .and()
                .build();
    }
}

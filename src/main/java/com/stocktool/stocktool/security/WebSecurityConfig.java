package com.stocktool.stocktool.security;
import com.stocktool.stocktool.security.jwt.JwtAuthTokenFilter;
import com.stocktool.stocktool.security.jwt.JwtAuthEntryPoint;
import com.stocktool.stocktool.security.service.UserDetailsServiceImpl;
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
                .requestMatchers("/api/stocktool/user/authenticate").permitAll()
                .requestMatchers("/api/stocktool/user/forgot-password").permitAll()
                .requestMatchers("/api/stocktool/user/reset-password").permitAll()
                .requestMatchers("/api/stocktool/user/add").permitAll()
                .requestMatchers("/api/stocktool/role/add").permitAll()
                .requestMatchers("/api/stocktool/menus/all").permitAll()
                .requestMatchers("/api/stocktool/menus/update/{id}").permitAll()
                .requestMatchers("/api/stocktool/menus/add").permitAll()
                .requestMatchers("/api/stocktool/menus/findbyid/{id}").permitAll()
//                .requestMatchers("/api/stocktool/menus/findbyidprod/{id}").permitAll()
                .requestMatchers("/api/stocktool/vente/all").permitAll()
                .requestMatchers("/api/stocktool/vente/update/{id}").permitAll()
                .requestMatchers("/api/stocktool/vente/add").permitAll()
                .requestMatchers("/api/stocktool/vente/findbyid/{id}").permitAll()
                .requestMatchers("/api/stocktool/vente/delete/{id}").permitAll()
                .requestMatchers("/api/stocktool/detailsvente/all").permitAll()
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

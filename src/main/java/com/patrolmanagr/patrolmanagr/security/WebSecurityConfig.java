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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .authorizeHttpRequests()
                // Routes d'authentification publiques
                .requestMatchers("/api/patrolmanagr/user/authenticate").permitAll()
                .requestMatchers("/api/patrolmanagr/user/forgot-password").permitAll()
                .requestMatchers("/api/patrolmanagr/user/reset-password").permitAll()
                .requestMatchers("/api/patrolmanagr/user/add").permitAll()

                // Routes Role publiques
                .requestMatchers("/api/patrolmanagr/role/add").permitAll()

                // Routes Zone publiques
                .requestMatchers("/api/patrolmanagr/zone/add").permitAll()
                .requestMatchers("/api/patrolmanagr/zone/all").permitAll()

                // Routes Site publiques
                .requestMatchers("/api/patrolmanagr/site/add").permitAll()
                .requestMatchers("/api/patrolmanagr/site/all").permitAll()

                // Routes VendorAPI publiques
                .requestMatchers("/api/patrolmanagr/vendorapi/add").permitAll()
                .requestMatchers("/api/patrolmanagr/vendorapi/all").permitAll()

                // Routes Terminal publiques
                .requestMatchers("/api/patrolmanagr/terminal/add").permitAll()
                .requestMatchers("/api/patrolmanagr/terminal/all").permitAll()

                // Routes Ronde publiques
                .requestMatchers("/api/patrolmanagr/ronde/add").permitAll()
                .requestMatchers("/api/patrolmanagr/ronde/all").permitAll()

                // Routes Secteur publiques
                .requestMatchers("/api/patrolmanagr/secteur/add").permitAll()
                .requestMatchers("/api/patrolmanagr/secteur/all").permitAll()

                // Routes Pastille publiques
                .requestMatchers("/api/patrolmanagr/pastille/add").permitAll()
                .requestMatchers("/api/patrolmanagr/pastille/all").permitAll()

                // Routes Ronde-Pastille publiques (pour les associations)
                .requestMatchers("/api/patrolmanagr/ronde-pastille/add").permitAll()
                .requestMatchers("/api/patrolmanagr/ronde-pastille/all").permitAll()
                .requestMatchers("/api/patrolmanagr/ronde-pastille/findbyid/**").permitAll()
                .requestMatchers("/api/patrolmanagr/ronde-pastille/findbyronde/**").permitAll()
                .requestMatchers("/api/patrolmanagr/ronde-pastille/findbypastille/**").permitAll()
                .requestMatchers("/api/patrolmanagr/ronde-pastille/findbyrondeandsequence/**").permitAll()

                // Routes ProgRonde publiques (programmation des rondes)
                .requestMatchers("/api/patrolmanagr/prog-ronde/add").permitAll()
                .requestMatchers("/api/patrolmanagr/prog-ronde/all").permitAll()
                .requestMatchers("/api/patrolmanagr/prog-ronde/findbyid/**").permitAll()
                .requestMatchers("/api/patrolmanagr/prog-ronde/findbyronde/**").permitAll()
                .requestMatchers("/api/patrolmanagr/prog-ronde/findbysite/**").permitAll()
                .requestMatchers("/api/patrolmanagr/prog-ronde/findbyuser/**").permitAll()
                .requestMatchers("/api/patrolmanagr/prog-ronde/findbystatus/**").permitAll()
                .requestMatchers("/api/patrolmanagr/prog-ronde/findbyterminal/**").permitAll()

                // Routes ExecRonde publiques (exécution des rondes)
                .requestMatchers("/api/patrolmanagr/exec-ronde/add").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde/all").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde/findbyid/**").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde/findbyprogronde/**").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde/findbyrefronde/**").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde/findbysite/**").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde/findbyexecdate/**").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde/findbystatus/**").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde/findbysiteanddate/**").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde/findbyperiod").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde/start/**").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde/end/**").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde/updatestatus/**").permitAll()

                // Routes ExecRondePastille publiques (exécution des pastilles)
                .requestMatchers("/api/patrolmanagr/exec-ronde-pastille/add").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde-pastille/all").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde-pastille/findbyid/**").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde-pastille/findbyexecronde/**").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde-pastille/findbyexecrondeordered/**").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde-pastille/findbypastille/**").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde-pastille/findbyexecrondeandseq/**").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde-pastille/findbystatus/**").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde-pastille/findbyexecrondeandstatus/**").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde-pastille/findbyscannedperiod").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde-pastille/markasdone/**").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde-pastille/markasmissed/**").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde-pastille/updatepointage/**").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde-pastille/initialize/**").permitAll()
                .requestMatchers("/api/patrolmanagr/exec-ronde-pastille/count/**").permitAll()

                // Routes pour les fichiers statiques
                .requestMatchers("/images/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()

                // Routes de documentation API (Swagger/OpenAPI)
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()

                // Routes de santé (health checks)
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()

                // Toutes les autres routes nécessitent une authentification
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
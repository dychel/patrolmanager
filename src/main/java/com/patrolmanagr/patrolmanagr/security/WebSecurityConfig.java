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
                .requestMatchers("/api/v1/patrolmanagr/user/authenticate").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/user/forgot-password").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/user/reset-password").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/user/add").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/user/all").permitAll()

                // Routes Role publiques
                .requestMatchers("/api/v1/patrolmanagr/role/add").permitAll()

                // Routes Zone publiques
                .requestMatchers("/api/v1/patrolmanagr/zone/add").permitAll()
                .requestMatchers("/api/patrolmanagr/zone/all").permitAll()

                // Routes Site publiques
                .requestMatchers("/api/v1/patrolmanagr/site/add").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/site/all").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/site/findbyid/{id}").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/site/findbyzone/{id}").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/site/findbyclient/{id}").permitAll()

                // Routes Client publiques
                .requestMatchers("/api/v1/patrolmanagr/client/add").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/client/all").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/client/findbyid/{id}").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/client/findbyzone/{id}").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/client/update/{id}").permitAll()

                // Routes VendorAPI publiques
                .requestMatchers("/api/v1/patrolmanagr/vendorapi/add").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/vendorapi/all").permitAll()

                // Routes Terminal publiques
                .requestMatchers("/api/v1/patrolmanagr/terminal/add").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/terminal/all").permitAll()

                // Routes Ronde publiques
                .requestMatchers("/api/v1/patrolmanagr/ronde/add").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/ronde/all").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/ronde/findbysite/{siteId}").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/ronde/update/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/ronde/delete/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/ronde/findbyid/**").permitAll()

                // Routes Secteur publiques
                .requestMatchers("/api/v1/patrolmanagr/secteur/add").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/secteur/all").permitAll()

                // Routes Pastille publiques (COMPLÈTES)
                .requestMatchers("/api/v1/patrolmanagr/pastille/add").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/pastille/all").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/pastille/findbysite/{siteId}").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/pastille/update/{id}").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/pastille/findbyid/{id}").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/pastille/findbyexternaluid/{externalUid}").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/pastille/findbycode/{code}").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/pastille/findbysecteur/{secteurId}").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/pastille/delete/{id}").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/pastille/findbyexternaluids").permitAll()

                // Routes Pointages publiques (NOUVELLES)
                .requestMatchers("/api/v1/patrolmanagr/pointages/add").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/pointages/import-batch").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/pointages/all").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/pointages/findbyid/{id}").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/pointages/findbyexternaluid/{externalUid}").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/pointages/findbysiteperiod/{siteId}").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/pointages/findbyrondeperiod/{rondeId}").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/pointages/findbyagent/{agentId}").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/pointages/pending").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/pointages/rejected").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/pointages/validate/{id}").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/pointages/reject/{id}").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/pointages/update/{id}").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/pointages/delete/{id}").permitAll()

                // Routes Monitoring publiques (NOUVELLES)
                .requestMatchers("/api/v1/patrolmanagr/monitoring/performance").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/monitoring/integrity").permitAll()

                // Routes Ronde-Pastille publiques (pour les associations)
                .requestMatchers("/api/v1/patrolmanagr/ronde-pastille/add").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/ronde-pastille/all").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/ronde-pastille/findbyid/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/ronde-pastille/findbypastillebyronde/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/ronde-pastille/findbypastille/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/ronde-pastille/findbyrondeandsequence/**").permitAll()

                // Routes ProgRonde publiques (programmation des rondes)
                .requestMatchers("/api/v1/patrolmanagr/prog-ronde/add").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/prog-ronde/all").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/prog-ronde/findbyid/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/prog-ronde/findbyronde/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/prog-ronde/findbysite/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/prog-ronde/findbyuser/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/prog-ronde/findbystatus/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/prog-ronde/findbyterminal/**").permitAll()

                // Routes ExecRonde publiques (exécution des rondes)
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde/add").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde/all").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde/findbyid/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde/findbyprogronde/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde/findbyrefronde/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde/findbysite/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde/findbyexecdate/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde/findbystatus/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde/findbysiteanddate/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde/findbyperiod").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde/start/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde/end/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde/updatestatus/**").permitAll()

                // Routes ExecRondePastille publiques (exécution des pastilles)
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde-pastille/add").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde-pastille/all").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde-pastille/findbyid/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde-pastille/findbyexecronde/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde-pastille/findbyexecrondeordered/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde-pastille/findbypastille/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde-pastille/findbyexecrondeandseq/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde-pastille/findbystatus/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde-pastille/findbyexecrondeandstatus/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde-pastille/findbyscannedperiod").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde-pastille/markasdone/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde-pastille/markasmissed/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde-pastille/updatepointage/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde-pastille/initialize/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/exec-ronde-pastille/count/**").permitAll()

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
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

                // ============ ROUTES SYSTÈME SYSTÈME SYSTÈME SYSTÈME SYSTÈME SYSTÈME SYSTÈME SYSTÈME SYSTÈME SYSTÈME SYSTÈME SYSTÈME SYSTÈME ============
                // Routes SysJob publiques (programmation des jobs de rondes)
                .requestMatchers("/api/v1/patrolmanagr/sys-jobs/create").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/sys-jobs/update/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/sys-jobs/delete/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/sys-jobs/{id}").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/sys-jobs/code/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/sys-jobs/all").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/sys-jobs/active").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/sys-jobs/schedule-type/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/sys-jobs/scope/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/sys-jobs/{id}/status").permitAll()

                // ============ ROUTES SYSTÈME SYSTÈME SYSTÈME SYSTÈME SYSTÈME SYSTÈME SYSTÈME SYSTÈME SYSTÈME SYSTÈME SYSTÈME SYSTÈME SYSTÈME ============
                // Routes SysJobRun publiques (exécution des jobs)
                .requestMatchers("/api/v1/patrolmanagr/sys-job-runs/create").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/sys-job-runs/start/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/sys-job-runs/update/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/sys-job-runs/complete/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/sys-job-runs/{id}").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/sys-job-runs/job/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/sys-job-runs/status/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/sys-job-runs/date-range").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/sys-job-runs/running").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/sys-job-runs/clean-old/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/sys-job-runs/latest/job/**").permitAll()

                // ============ ROUTES D'EXÉCUTION DES RONDES ============
                // Routes d'exécution des rondes
                .requestMatchers("/api/v1/patrolmanagr/execution/execute-job/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/execution/execute-job-manual/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/execution/stats").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/execution/test-pointage/**").permitAll()

                // ============ ROUTES POUR LES RONDES EXÉCUTÉES ============
                // Routes pour consulter les rondes déjà exécutées
                .requestMatchers("/api/v1/patrolmanagr/executed-rondes/all").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/executed-rondes/today").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/executed-rondes/in-progress").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/executed-rondes/completed").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/executed-rondes/by-date/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/executed-rondes/by-site/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/executed-rondes/by-site-period/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/executed-rondes/by-status/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/executed-rondes/by-ronde/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/executed-rondes/details/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/executed-rondes/history/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/executed-rondes/by-job-run/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/executed-rondes/search").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/executed-rondes/site-stats/**").permitAll()
                .requestMatchers("/api/v1/patrolmanagr/executed-rondes/dashboard-summary").permitAll()

                // ============ ROUTES INCIDENTS ============
                // Routes pour les incidents (si vous avez un IncidentController)
                .requestMatchers("/api/v1/patrolmanagr/incidents/**").permitAll()

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
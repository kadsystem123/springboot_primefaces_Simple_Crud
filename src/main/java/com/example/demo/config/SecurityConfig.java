package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.security.AppUserDetailsService;
import com.example.demo.security.JwtAuthFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AppUserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, AppUserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authRequest -> {
            // 1. صفحات JSF والموارد العامة
            authRequest.requestMatchers(        
                    "/", "/login", "/login.xhtml",
                    "/register", "/register.xhtml",
                    "/resources/**", "/javax.faces.resource/**", "/jakarta.faces.resource/**",
                    "/img/**", "/css/**", "/js/**").permitAll();

            // 2. API التسجيل والدخول
            authRequest.requestMatchers("/api/auth/**").permitAll();
            
            // 3. حماية صفحة index.xhtml حسب الأدوار
            authRequest.requestMatchers("/index.xhtml").hasAnyRole("ADMIN","USER");

            // 4. API الموظفين
            authRequest.requestMatchers("/api/v1/supprime/**").hasRole("ADMIN");
            authRequest.requestMatchers("/api/v1/listemployees").hasRole("ADMIN");
            authRequest.requestMatchers(HttpMethod.POST, "/api/v1/**").hasRole("ADMIN");
            authRequest.requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasRole("ADMIN");

            // 5. أي طلب آخر يتطلب مصادقة
            authRequest.anyRequest().authenticated();
        });

        // السماح بإنشاء الجلسة لدعم صفحات JSF/PrimeFaces
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());

        // فلتر JWT لطلبات API
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // تعطيل CSRF لحرية حركة AJAX في PrimeFaces و REST
        http.csrf(csrf -> csrf.disable());
                
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
            "/javax.faces.resource/**",
            "/jakarta.faces.resource/**",
            "/resources/**"
        );
    }
}
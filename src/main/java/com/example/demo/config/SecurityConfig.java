package com.example.demo.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
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
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import com.example.demo.security.AppUserDetailsService;
import com.example.demo.security.JwtAuthFilter;

import jakarta.faces.webapp.FacesServlet;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;

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
    public ServletRegistrationBean<FacesServlet> facesServletRegistration() {
        ServletRegistrationBean<FacesServlet> registration = new ServletRegistrationBean<>(new FacesServlet(), "*.xhtml");
        registration.setLoadOnStartup(1);
        return registration;
    }

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        
        // 💡 تمكين حفظ سياق الأمان في الجلسة تلقائياً
        http.securityContext(context -> 
            context.securityContextRepository(new HttpSessionSecurityContextRepository())
        );

        http.authorizeHttpRequests(authRequest -> {
            
            // 1. السماح فقط للتوجيهات الداخلية الخاصة بـ JSF (بدون DispatcherType.REQUEST)
            authRequest.dispatcherTypeMatchers(
                    DispatcherType.FORWARD, 
                    DispatcherType.ERROR
            ).permitAll();

            // 2. الصفحات والموارد العامة المتاحة للجميع فقط
            authRequest.requestMatchers(        
                    "/login.xhtml",
                    "/register.xhtml",
                    "/resources/**", 
                    "/javax.faces.resource/**", 
                    "/jakarta.faces.resource/**",
                    "/img/**", "/css/**", "/js/**"
            ).permitAll();

            // 3. API التسجيل والدخول العامة
            authRequest.requestMatchers("/api/auth/**").permitAll();

            // 4. حماية API الموظفين بحسب الأدوار
            authRequest.requestMatchers("/api/v1/supprime/**").hasRole("ADMIN");
            authRequest.requestMatchers("/api/v1/listemployees").hasRole("ADMIN");
            authRequest.requestMatchers(HttpMethod.POST, "/api/v1/**").hasRole("ADMIN");
            authRequest.requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasRole("ADMIN");

            // 5. حماية حاسمة: صفحة index.xhtml وأي صفحة أخرى تتطلب تسجيل الدخول حتماً
            authRequest.requestMatchers("/index.xhtml").authenticated();
            authRequest.anyRequest().authenticated();
        });

        // 💡 عند محاولة فتح صفحة محمية (مثل index.xhtml) بدون تسجيل دخول، يتم التوجيه لـ login.xhtml
        http.exceptionHandling(exception -> 
            exception.authenticationEntryPoint((request, response, authException) -> {
                String uri = request.getRequestURI();
                if (uri.startsWith("/api/")) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                } else {
                    response.sendRedirect(request.getContextPath() + "/login.xhtml");
                }
            })
        );

        // إدارة الجلسة لدعم JSF
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());

        // فلتر JWT لطلبات API
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

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
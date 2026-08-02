package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;


@Configuration
@EnableWebSecurity 
public class SecurityConfig  {
	@Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authRequest -> {
            // 1. السماح للصفحات العامة بدون مصادقة
            authRequest.requestMatchers("/index", "/index.xhtml", "/resources/**", "/javax.faces.resource/**").permitAll();
            
            // 2. إذا أردت أن يكون API عاماً أيضاً (اختياري)
            authRequest.requestMatchers("/api/**").permitAll();
            
            // 3. أي طلب آخر (مثل صفحات أخرى غير index.xhtml) يتطلب مصادقة
            authRequest.anyRequest().authenticated();  // <--- التغيير الجوهري
        });

        // تفعيل نموذج تسجيل الدخول (سيقوم تلقائياً بإنشاء صفحة /login)
        http.formLogin(Customizer.withDefaults());
        http.httpBasic(Customizer.withDefaults());

        // تعطيل CSRF لمشاكل PrimeFaces (AJAX)
        http.csrf(csrf -> csrf.disable());

        return http.build();
    }

    
	    // استثناء موارد PrimeFaces من الأمان بالكامل
	    @Bean
	    public WebSecurityCustomizer webSecurityCustomizer() {
	        return (web) -> web.ignoring().requestMatchers("/javax.faces.resource/**");
	    }

}

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

/**
 * تهيئة الحماية:
 * - صفحات PrimeFaces (index/login/register) عامة بدون مصادقة.
 * - API التسجيل والدخول (/api/auth/**) عامة.
 * - باقي API الموظفين (/api/v1/**) محمية بواسطة JWT وتسيير الصلاحيات حسب الدور:
 *      GET  متاح لـ ADMIN و USER
 *      POST/DELETE متاح لـ ADMIN فقط
 */
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
			// 1. صفحات JSF/PrimeFaces والموارد العامة بدون مصادقة
			authRequest.requestMatchers(
					"/index", "/index.xhtml",
					"/login", "/login.xhtml",
					"/register", "/register.xhtml",
					"/resources/**", "/javax.faces.resource/**",
					"/img/**", "/css/**", "/js/**").permitAll();

			// 2. API التسجيل وتسجيل الدخول عامة (لا تتطلب توكن)
			authRequest.requestMatchers("/api/auth/**").permitAll();

			// 3. تسيير الصلاحيات على API الموظفين حسب الدور المستخرج من JWT
			// ملاحظة: القاعدة الأكثر تحديداً (supprime) يجب أن تُوضع قبل القاعدة العامة لـ GET
			authRequest.requestMatchers("/api/v1/supprime/**").hasRole("ADMIN");
			authRequest.requestMatchers(HttpMethod.GET, "/api/v1/**")
					.hasAnyRole("ADMIN", "USER");
			authRequest.requestMatchers(HttpMethod.POST, "/api/v1/**")
					.hasRole("ADMIN");
			authRequest.requestMatchers(HttpMethod.DELETE, "/api/v1/**")
					.hasRole("ADMIN");

			// 4. أي طلب آخر يتطلب مصادقة
			authRequest.anyRequest().authenticated();
		});

		// جلسة بدون حالة (Stateless) لأن الاعتماد كليًا على JWT لواجهات API
		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

		// تفعيل نموذج تسجيل الدخول الافتراضي (يبقى مفيداً لصفحات JSF نفسها إن استُخدم)
		http.formLogin(form -> form.disable());
		http.httpBasic(basic -> basic.disable());

		// تعطيل CSRF لمشاكل PrimeFaces (AJAX) وواجهات REST المعتمدة على التوكن
		http.csrf(csrf -> csrf.disable());

		// إضافة فلتر JWT قبل فلتر المصادقة الافتراضي
		http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	// استثناء موارد PrimeFaces من الأمان بالكامل
	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring().requestMatchers("/javax.faces.resource/**");
	}
}

package com.example.demo.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * فلتر يقرأ رأس Authorization: Bearer <token>، يتحقق من صلاحية JWT،
 * ثم يضع المصادقة (مع الدور) في SecurityContext لتسيير الصلاحيات
 * عبر hasRole()/hasAuthority() على مستوى الـ API.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	private static final String HEADER_NAME = "Authorization";
	private static final String PREFIX = "Bearer ";

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private AppUserDetailsService userDetailsService;

	
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
	    String path = request.getRequestURI();
	    // عدم تطبيق الفلتر على صفحات XHTML والموارد العامة
	    return path.endsWith(".xhtml") || path.startsWith("/jakarta.faces.resource") || path.startsWith("/api/auth");
	}
	
	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {

		final String authHeader = request.getHeader(HEADER_NAME);

		String username = null;
		String token = null;

		if (authHeader != null && authHeader.startsWith(PREFIX)) {
			token = authHeader.substring(PREFIX.length());
			try {
				username = jwtUtil.extractUsername(token);
			} catch (Exception e) {
				// توكن غير صالح أو منتهي الصلاحية: يستمر الطلب بدون مصادقة
				// وستقرر قواعد authorizeHttpRequests رفضه لاحقاً (401/403)
			}
		}

		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);

			if (jwtUtil.isTokenValid(token, userDetails)) {
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authToken);
			}
		}

		filterChain.doFilter(request, response);
	}
}

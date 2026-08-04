package com.example.demo.filters;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuditFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
String uri = request.getRequestURI();
        
        // الشرح: نفذ المنطق فقط إذا لم تكن الصفحة login ولم تكن register
        boolean isLoginPage = uri.endsWith("/login.xhtml");
        boolean isRegisterPage = uri.endsWith("/register.xhtml");
        if (!isLoginPage && !isRegisterPage) {
            // هنا ضع منطق التدقيق (Audit) الخاص بك
        }  
        // متابعة الفلاتر التالية في السلسلة
        filterChain.doFilter(request, response);
    }	
}

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
        
        if (uri != null) {
            boolean isLoginPage = uri.endsWith("/login.xhtml");
            boolean isRegisterPage = uri.endsWith("/register.xhtml");
            if (!isLoginPage && !isRegisterPage) {
                // منطق التدقيق الخاص بك
            }
        }
        
        filterChain.doFilter(request, response);
    }	
}

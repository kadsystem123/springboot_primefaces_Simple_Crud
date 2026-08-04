package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.Serializable;

/**
 * Managed Bean لصفحة login.xhtml.
 * يستدعي AuthenticationManager، يخزن المصادقة في Spring Security Context للـ Session،
 * ويولّد JWT لاستخدامه في طلبات API.
 */
@Component
@Named("loginBean")
@ViewScoped
public class LoginBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String username;
	private String password;
	private String token;

	@Autowired
	private transient AuthenticationManager authenticationManager;

	@Autowired
	private transient UserRepository userRepository;

	@Autowired
	private transient JwtUtil jwtUtil;

	public void login() {
		try {
			// 1. إجراء المصادقة واسترجاع كائن Authentication المكتمل
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(username, password));

			User user = userRepository.findByUsername(username)
					.orElseThrow(() -> new BadCredentialsException("مستخدم غير موجود"));

			// 2. توليد توكن JWT
			token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

			// 3. وضع المصادقة في SecurityContext الخاص بـ Spring Security
			SecurityContext securityContext = SecurityContextHolder.getContext();
			securityContext.setAuthentication(authentication);

			// 4. الحصول على الجلسة وحفظ الـ SecurityContext والتوكن فيها
			HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
					.getExternalContext().getSession(true);
			
			// السطر الأساسي الذي يحل المشكلة: الربط مع Spring Security
			session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

			// حفظ بيانات إضافية لاستخدامها في واجهة JSF إن أردت
			session.setAttribute("JWT_TOKEN", token);
			session.setAttribute("USER_ROLE", user.getRole().name());
			session.setAttribute("USERNAME", user.getUsername());

			// 5. إعادة التوجيه الفوري لصفحة index.xhtml
			FacesContext.getCurrentInstance()
					.getExternalContext()
					.redirect("index.xhtml");

		} catch (BadCredentialsException e) {
			FacesContext.getCurrentInstance().addMessage("growl",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "خطأ", "اسم المستخدم أو كلمة المرور غير صحيحة"));
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage("growl",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "خطأ", "فشل تسجيل الدخول: " + e.getMessage()));
		}
	}

	public void logout() throws IOException {
		// تفريغ سياق الأمان الخاص بـ Spring Security
		SecurityContextHolder.clearContext();

		HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
				.getExternalContext().getSession(false);
		if (session != null) {
			session.invalidate();		
		}
		token = null;

		// إعادة التوجيه لصفحة الدخول بعد الخروج
		FacesContext.getCurrentInstance()
				.getExternalContext()
				.redirect("login.xhtml");
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getToken() {
		return token;
	}
}
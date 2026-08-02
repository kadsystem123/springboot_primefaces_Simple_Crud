package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;

/**
 * Managed Bean لصفحة login.xhtml.
 * يستدعي AuthenticationManager مباشرة، يولّد JWT عبر JwtUtil،
 * ويخزّنه في الجلسة (session) ليُستخدم من الواجهة أو كعرض للتوكن.
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
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(username, password));

			User user = userRepository.findByUsername(username)
					.orElseThrow(() -> new BadCredentialsException("مستخدم غير موجود"));

			token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

			// تخزين التوكن والدور في الجلسة لاستخدامهما في الواجهة
			HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
					.getExternalContext().getSession(true);
			session.setAttribute("JWT_TOKEN", token);
			session.setAttribute("USER_ROLE", user.getRole().name());
			session.setAttribute("USERNAME", user.getUsername());

			FacesContext.getCurrentInstance().addMessage("growl",
					new FacesMessage(FacesMessage.SEVERITY_INFO, "نجاح", "تم تسجيل الدخول بنجاح"));
		} catch (BadCredentialsException e) {
			FacesContext.getCurrentInstance().addMessage("growl",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "خطأ", "اسم المستخدم أو كلمة المرور غير صحيحة"));
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage("growl",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "خطأ", "فشل تسجيل الدخول: " + e.getMessage()));
		}
	}

	public void logout() {
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
				.getExternalContext().getSession(false);
		if (session != null) {
			session.invalidate();
		}
		token = null;
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

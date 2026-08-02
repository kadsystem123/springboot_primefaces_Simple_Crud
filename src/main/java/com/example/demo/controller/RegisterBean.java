package com.example.demo.controller;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Managed Bean لصفحة register.xhtml.
 * ينشئ مستخدماً جديداً بصلاحية USER مع كلمة مرور مشفّرة (BCrypt)،
 * ويولّد له JWT مباشرة بعد التسجيل.
 */
@Component
@Named("registerBean")
@ViewScoped
public class RegisterBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String username;
	private String password;
	private String confirmPassword;
	private String email;
	private String token;

	@Autowired
	private transient UserRepository userRepository;

	@Autowired
	private transient PasswordEncoder passwordEncoder;

	@Autowired
	private transient JwtUtil jwtUtil;

	public void register() {
		FacesContext context = FacesContext.getCurrentInstance();

		if (username == null || username.isBlank() || password == null || password.isBlank()) {
			context.addMessage("growl",
					new FacesMessage(FacesMessage.SEVERITY_WARN, "تنبيه", "اسم المستخدم وكلمة المرور مطلوبان"));
			return;
		}

		if (!password.equals(confirmPassword)) {
			context.addMessage("growl",
					new FacesMessage(FacesMessage.SEVERITY_WARN, "تنبيه", "كلمتا المرور غير متطابقتين"));
			return;
		}

		if (userRepository.existsByUsername(username)) {
			context.addMessage("growl",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "خطأ", "اسم المستخدم مستخدم بالفعل"));
			return;
		}

		User user = new User(username, passwordEncoder.encode(password), email, Role.USER);
		userRepository.save(user);

		token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

		context.addMessage("growl",
				new FacesMessage(FacesMessage.SEVERITY_INFO, "نجاح", "تم إنشاء الحساب بنجاح، يمكنك تسجيل الدخول الآن"));

		username = null;
		password = null;
		confirmPassword = null;
		email = null;
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

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getToken() {
		return token;
	}
}

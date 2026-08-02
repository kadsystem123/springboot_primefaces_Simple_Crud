package com.example.demo.controllerapi;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;

/**
 * نقاط نهاية المصادقة: التسجيل (Register) وتسجيل الدخول (Login).
 * تُستخدم من صفحتي register.xhtml و login.xhtml (عبر AJAX/JS)
 * وتُرجع JWT يحمل اسم المستخدم والدور.
 */
@CrossOrigin
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private AuthenticationManager authenticationManager;

	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
		if (request.getUsername() == null || request.getUsername().isBlank()
				|| request.getPassword() == null || request.getPassword().isBlank()) {
			return error(HttpStatus.BAD_REQUEST, "اسم المستخدم وكلمة المرور مطلوبان");
		}

		if (userRepository.existsByUsername(request.getUsername())) {
			return error(HttpStatus.CONFLICT, "اسم المستخدم مستخدم بالفعل");
		}

		User user = new User(
				request.getUsername(),
				passwordEncoder.encode(request.getPassword()),
				request.getEmail(),
				Role.USER // كل تسجيل جديد يُنشأ بصلاحية USER افتراضياً
		);
		userRepository.save(user);

		String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new AuthResponse(token, user.getUsername(), user.getRole().name()));
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
		} catch (BadCredentialsException e) {
			return error(HttpStatus.UNAUTHORIZED, "اسم المستخدم أو كلمة المرور غير صحيحة");
		}

		User user = userRepository.findByUsername(request.getUsername())
				.orElseThrow(() -> new BadCredentialsException("مستخدم غير موجود"));

		String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
		return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole().name()));
	}

	private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
		Map<String, String> body = new HashMap<>();
		body.put("error", message);
		return ResponseEntity.status(status).body(body);
	}
}

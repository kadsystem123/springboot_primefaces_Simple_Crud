package com.example.demo.controller;

import jakarta.enterprise.context.RequestScoped; // أو ViewScoped
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;

import java.io.IOException;
import java.io.Serializable;

@Component
@Named("loginBean")
@RequestScoped // يفضل RequestScoped لعملية الدخول
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
            // 1. المصادقة
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BadCredentialsException("مستخدم غير موجود"));

            // 2. توليد توكن JWT
            token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

            // 3. إنشاء سياق الأمان وتعيين كائن المصادقة فيه
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            // 4. الحصول على الجلسة وحفظ الـ SecurityContext المفتاح المعتمد رسمياً
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
                    .getExternalContext().getSession(true);

            // 💡 السطر الحاسم: الحفظ بالمفتاح القياسي لـ Spring Security
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

            // حفظ التوكن وبيانات المستخدم
            session.setAttribute("JWT_TOKEN", token);
            session.setAttribute("USER_ROLE", user.getRole().name());
            session.setAttribute("USERNAME", user.getUsername());

            // 5. التوجيه لصفحة index.xhtml
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
        SecurityContextHolder.clearContext();

        HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(false);
        if (session != null) {
            session.invalidate();       
        }
        token = null;

        FacesContext.getCurrentInstance()
                .getExternalContext()
                .redirect("login.xhtml");
    }

    // Getters and Setters ...
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getToken() { return token; }
}
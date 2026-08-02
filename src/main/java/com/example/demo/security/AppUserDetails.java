package com.example.demo.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.demo.model.User;

/**
 * تمثيل Spring Security للمستخدم (User) الخاص بنا.
 * الدور يُخزّن بصيغة "ROLE_ADMIN" أو "ROLE_USER" ليتوافق مع hasRole().
 */
public class AppUserDetails implements UserDetails {

	private static final long serialVersionUID = 1L;

	private final User user;

	public AppUserDetails(User user) {
		this.user = user;
	}

	public Long getId() {
		return user.getId();
	}

	public String getRole() {
		return user.getRole().name();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return user.isEnabled();
	}
}

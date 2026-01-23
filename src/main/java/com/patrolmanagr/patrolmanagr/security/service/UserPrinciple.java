package com.patrolmanagr.patrolmanagr.security.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.patrolmanagr.patrolmanagr.entity.Role;
import com.patrolmanagr.patrolmanagr.entity.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;

@Data
public class UserPrinciple implements UserDetails {
	private static final long serialVersionUID = 1L;

	private Long id;

	private String firstName;
	private String lastName;
	private String email;
	private String username;
	@JsonIgnore
	private String password;
	private Byte status;
	private Role role;
	private Collection<? extends GrantedAuthority> authorities;

	public UserPrinciple(Long id, String firstName, String lastName, String email, String username,
						 String password, Byte status, Role role) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.username = username;
		this.password = password;
		this.status = status;
		this.role = role;
	}

	public static UserPrinciple build(User user) {
		return new UserPrinciple(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(),
				user.getUsername(), user.getPassword(), user.getIs_active(), user.getRole_code());
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
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
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		UserPrinciple user = (UserPrinciple) o;
		return Objects.equals(id, user.id);
	}
}
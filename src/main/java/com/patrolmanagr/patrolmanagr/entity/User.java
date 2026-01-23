package com.patrolmanagr.patrolmanagr.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
public class User implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String firstName;
	private String lastName;
	private String email;
	private String username;
	private String password;
	//Prendre en compte les différents état d'un user: 1: active, 2: pending, 3: rejected,... (ou suppression logique)
	private Byte is_active;
	@ManyToOne
	@JoinColumn(name = "role_id")
	private Role role_code;
	@Column(columnDefinition = "TIMESTAMP")
	private LocalDateTime tokenCreationDate;
	//private int is_active;
	private String audit_field;
	private String token;

	public User(String firstName, String lastName, String email, String username, String password, Byte is_active, Role role_code, LocalDateTime tokenCreationDate, String audit_field, String token) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.username = username;
		this.password = password;
		this.is_active = is_active;
		this.role_code = role_code;
		this.tokenCreationDate = tokenCreationDate;
		this.audit_field = audit_field;
		this.token = token;
	}

	public User(Long id, String lastName, String firstName, String email, String username, String password, Byte is_active, Role role_code, LocalDateTime tokenCreationDate, String audit_field, String token) {
		this.id = id;
		this.lastName = lastName;
		this.firstName = firstName;
		this.email = email;
		this.username = username;
		this.password = password;
		this.is_active = is_active;
		this.role_code = role_code;
		this.tokenCreationDate = tokenCreationDate;
		this.audit_field = audit_field;
		this.token = token;
	}

	public User(Long id, String firstName, String lastName, String email, String username, String password, Byte is_active, LocalDateTime tokenCreationDate, String audit_field, String token) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.username = username;
		this.password = password;
		this.is_active = is_active;
		this.tokenCreationDate = tokenCreationDate;
		this.audit_field = audit_field;
		this.token = token;
	}
}

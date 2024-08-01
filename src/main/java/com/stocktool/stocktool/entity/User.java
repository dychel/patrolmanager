package com.stocktool.stocktool.entity;

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
	private Byte status;
	@ManyToOne
	@JoinColumn(name = "role_id")
	private Role role;
	@ManyToOne
	@JoinColumn(name = "equipe_id")
	private Equipe equipe;
	private String token;
	@Column(columnDefinition = "TIMESTAMP")
	private LocalDateTime tokenCreationDate;


	public User(Long id, String firstName, String lastName, String email, Role role, Equipe equipe,
                Byte status, String username, String password) {
		this.id = id;
		this.firstName = firstName;
		this.lastName=lastName;
		this.email=email;
		this.role= role;
		this.equipe= equipe;
		this.status = status;
		this.username=username;
		this.password = password;
	}

	public User(String firstName, String lastName, String email, Role role, Equipe equipe,
                Byte status, String username, String password) {
		this.firstName = firstName;
		this.lastName=lastName;
		this.email=email;
		this.role= role;
		this.equipe= equipe;
		this.status = status;
		this.username=username;
		this.password = password;
	}

	public User(Long id, String firstName, String lastName, String email,
                Byte status, String username, String password) {
		this.id = id;
		this.firstName = firstName;
		this.lastName=lastName;
		this.email=email;
		this.status = status;
		this.username=username;
		this.password = password;
	}

}

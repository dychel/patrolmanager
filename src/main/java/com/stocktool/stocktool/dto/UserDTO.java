package com.stocktool.stocktool.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

	private Long id;

	@NotBlank
	@Size(min = 2, max = 30, message = "Your firstName must contain a minimum of 2 characters and a maximum of 30.")
	private String firstName;
	private String lastName;
	@NotBlank
	@Size(min = 2, max = 50, message = "Your email must contain a minimum of 2 characters and a maximum of 50.")
	private String email;

	@NotBlank
	@Size(min = 2, max = 20, message = "Your username must contain a minimum of 2 characters and a maximum of 20.")
	private String username;

	private String password;
	private Byte status;
	private Long equipeId;
	private Long roleId;


}
package com.patrolmanagr.patrolmanagr.response;
import com.patrolmanagr.patrolmanagr.entity.Role;
import lombok.Data;

@Data
//@NoArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";

    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;
    private Byte status;
    private Role role;

    public JwtResponse(String token, Long id, String firstName, String lastName, String email,
                       String username, String password, Byte status, Role role) {
        this.token = token;
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.status = status;
        this.role = role;
    }

}
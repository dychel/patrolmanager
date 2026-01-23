package com.patrolmanagr.patrolmanagr.service;
import com.patrolmanagr.patrolmanagr.dto.UserDTO;
import com.patrolmanagr.patrolmanagr.entity.User;
import java.util.List;

public interface UserService {

    List<User> listUsers();

    User getUserById(Long id);

    User saveUser(UserDTO userDTO);

    User updateUser(Long id, UserDTO userDTO);

    Long getConnectedUserId();

    void saveToken(User users,String jwt);

//    User updateUser(User user);

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    void deleteUserById(Long id);

    List<User> getUserByRole(Long id);

/*    String forgotPassword(String email);
    String resetPassword(String token, String password);*/
}

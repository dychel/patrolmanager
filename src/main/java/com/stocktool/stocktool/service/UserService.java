package com.stocktool.stocktool.service;

import com.stocktool.stocktool.dto.UserDTO;
import com.stocktool.stocktool.entity.User;

import java.util.List;

public interface UserService {

    List<User> listUsers();

    User getUserById(Long id);

    User saveUser(UserDTO userDTO);

    User updateUser(Long id, UserDTO userDTO);

//    User updateUser(User user);

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    void deleteUserById(Long id);

    List<User> getUserByRole(Long id);

/*    String forgotPassword(String email);
    String resetPassword(String token, String password);*/
}

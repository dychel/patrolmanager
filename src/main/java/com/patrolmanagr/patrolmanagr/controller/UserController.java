package com.patrolmanagr.patrolmanagr.controller;

import com.patrolmanagr.patrolmanagr.dto.LoginForm;
import com.patrolmanagr.patrolmanagr.dto.UserDTO;
import com.patrolmanagr.patrolmanagr.entity.User;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.response.UserResponse;
import com.patrolmanagr.patrolmanagr.security.jwt.JwtProvider;
import com.patrolmanagr.patrolmanagr.service.RoleService;
import com.patrolmanagr.patrolmanagr.service.UserService;
import com.patrolmanagr.patrolmanagr.response.JwtResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.Objects;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/v1/patrolmanagr/user/*")
public class UserController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Qualifier("getBPE")
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    JwtProvider jwtProvider;

    Byte statusActif = 1;

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginForm loginRequest) {

        User user = userService.getUserByUsername(loginRequest.getUsername());

        if (!Objects.equals(user.getIs_active(), statusActif)) {
            return new ResponseEntity<>(new ResponseMessage("chao", "Compte non activé!", null), HttpStatus.OK);
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        //String jwt = jwtProvider.generateJwtToken(authentication);
        String jwt = jwtProvider.generateJwtToken(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User myUser = userService.getUserByUsername(userDetails.getUsername());
        myUser.setToken(jwt);
        //save the token
        //userService.saveToken(user, jwt);
        //
        return new ResponseEntity<>(new ResponseMessage("ok", "Utilisateur "+ myUser.getFirstName()+ " connecté avec succès", myUser),
                HttpStatus.OK);
//        return ResponseEntity.ok(new JwtResponse(
//                jwt,
//                myUser.getId(),
//                myUser.getFirstName(),
//                myUser.getLastName(),
//                myUser.getEmail(),
//                myUser.getUsername(),
//                myUser.getPassword(),
//                myUser.getIs_active(),
//                myUser.getRole_code()));
    }

    @GetMapping(value = "/all")
    public ResponseEntity<?> getAllUsers() {
        return new ResponseEntity<>(UserResponse.getMessageListUsers(userService.listUsers()), HttpStatus.OK);
    }

    @PostMapping("/add")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO) throws Exception {

        if (userService.getUserByUsername(userDTO.getUsername()) != null)
            return new ResponseEntity<>(new ResponseMessage("chao", "This login already exists !", userDTO.getUsername()), HttpStatus.OK);

        if (userService.getUserByEmail(userDTO.getEmail()) != null)
            return new ResponseEntity<>(new ResponseMessage("chao", "This email already exists !", userDTO.getEmail()), HttpStatus.OK);

        return new ResponseEntity<>(UserResponse.getMessageSaveUser(userService.saveUser(userDTO)), HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") Long id, @Valid @RequestBody UserDTO userDTO) {

        User userToUpdate = userService.getUserById(id);

        if (userToUpdate != null) {
//            Username unique
            if (!(userDTO.getUsername().equalsIgnoreCase(userToUpdate.getUsername()))
                    && userService.getUserByUsername(userDTO.getUsername()) != null) {
                return new ResponseEntity<>(new ResponseMessage("chao", "This username already exists !", userDTO.getUsername()), HttpStatus.OK);
            }

//            email unique
            if (!(userDTO.getEmail().equalsIgnoreCase(userToUpdate.getEmail()))
                    && userService.getUserByEmail(userDTO.getEmail()) != null) {
                return new ResponseEntity<>(new ResponseMessage("chao", "This email already exists !", userDTO.getEmail()), HttpStatus.OK);
            }

            return new ResponseEntity<>(UserResponse.getMessageUpdateUser(userService.updateUser(id, userDTO)), HttpStatus.OK);

        } else {
            return new ResponseEntity<>(new ResponseMessage("chao", "The user you want to modify cannot be found !", null),
                    HttpStatus.OK);
        }
    }

    @GetMapping(value = "/findbyusername/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable(value = "username") String username) {

        User user = userService.getUserByUsername(username);

        if (user == null)
            return new ResponseEntity<ResponseMessage>(new ResponseMessage("chao", "user not found !", null),
                    HttpStatus.OK);

        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "user found !", user),
                HttpStatus.OK);
    }

    @GetMapping(value = "/findbyid/{id}")
    public ResponseEntity<?> getUserById(@PathVariable(value = "id") Long id) {
        User user = userService.getUserById(id);

        if (user == null)
            return new ResponseEntity<ResponseMessage>(new ResponseMessage("chao", "user not found", null),
                    HttpStatus.OK);

        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "user found", user),
                HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable(value = "id") Long id) {
        userService.deleteUserById(id);
        return new ResponseEntity<>(UserResponse.getMessageDeleteUserById(), HttpStatus.OK);
    }
}

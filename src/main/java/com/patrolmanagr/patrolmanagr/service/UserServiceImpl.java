package com.patrolmanagr.patrolmanagr.service;
import com.patrolmanagr.patrolmanagr.dto.UserDTO;
import com.patrolmanagr.patrolmanagr.entity.User;
import com.patrolmanagr.patrolmanagr.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Qualifier("getBPE")
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private RoleService roleService;
    @Override
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findByIdUser(id);
    }

    @Override
    public User saveUser(UserDTO userDTO) {
        User user = modelMapper.map(userDTO, User.class);

        user.setLastName(userDTO.getLastName().toUpperCase());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        // Mettre à jour les infos du role du user
        if (userDTO.getRoleId() != null)
            user.setRole_code(roleService.findRoleById(userDTO.getRoleId()));
        return userRepository.save(user);
    }


    @Override
    public User updateUser(Long id, UserDTO userDTO) {

        String pwd = "test";
        User userToUpdate = userRepository.findByIdUser(id);

        if (userToUpdate == null)
            return null;

        User user = modelMapper.map(userDTO, User.class);

        user.setId(userToUpdate.getId());
        user.setLastName(userDTO.getLastName().toUpperCase());

//        Gestion du Password pour ne pas encoder à nouveau le password déjà encoder
        if (userDTO.getPassword() == null || userDTO.getPassword().equalsIgnoreCase(userToUpdate.getPassword()))
            pwd = userToUpdate.getPassword();
        else
            pwd = passwordEncoder.encode(userDTO.getPassword());

        user.setPassword(pwd);
//        Gestion du role du user
        if (userDTO.getRoleId() != null)
            user.setRole_code(roleService.findRoleById(userDTO.getRoleId()));
        return userRepository.save(user);
    }

//    @Override
//    public Long getConnectedUserId() {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth == null || !auth.isAuthenticated()) {
//            return null;
//        }
//        User user = (User) auth.getPrincipal();
//        return user.getId();
//    }

    @Override
    public Long getConnectedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        String username;
        if (auth.getPrincipal() instanceof UserDetails) {
            username = ((UserDetails) auth.getPrincipal()).getUsername();
        } else {
            username = auth.getName(); // Toujours disponible
        }
        if ("anonymousUser".equals(username)) {
            return null;
        }
        User user = getUserByUsername(username);
        return user != null ? user.getId() : null;
    }

    @Override
    public void saveToken(User users, String jwt) {
        Optional<User> checkUser = userRepository.findById(users.getId());
        if (checkUser.isPresent()) {
            User storedUser = checkUser.get();
            storedUser.setToken(jwt);
            userRepository.save(storedUser);
        }

    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<User> getUserByRole(Long id) {
        return userRepository.findUserByRole(id);
    }

/*	private String generateToken() {
		StringBuilder token = new StringBuilder();

		return token.append(UUID.randomUUID().toString())
				.append(UUID.randomUUID().toString()).toString();
	}

	private boolean isTokenExpired(final LocalDateTime tokenCreationDate) {

		LocalDateTime now = LocalDateTime.now();
		Duration diff = Duration.between(tokenCreationDate, now);

		return diff.toMinutes() >= SecurityParams.EXPIRE_TOKEN_AFTER_MINUTES;
	}

	public String forgotPassword(String email) {

		Optional<User> userOptional = Optional
				.ofNullable(userRepository.findByEmail(email));

		if (!userOptional.isPresent()) {
			return "Invalid email id.";
		}

		User user = userOptional.get();
		user.setToken(generateToken());
		user.setTokenCreationDate(LocalDateTime.now());

		user = userRepository.save(user);

		return user.getToken();
	}

	public String resetPassword(String token, String password) {

		Optional<User> userOptional = Optional
				.ofNullable(userRepository.findByToken(token));

		if (!userOptional.isPresent()) {
			return "Invalid token.";
		}

		LocalDateTime tokenCreationDate = userOptional.get().getTokenCreationDate();

		if (isTokenExpired(tokenCreationDate)) {
			return "Token expired.";

		}

		User user = userOptional.get();

		user.setPassword(passwordEncoder.encode(password));
		user.setToken(null);
		user.setTokenCreationDate(null);

		userRepository.save(user);

		return "Your password successfully updated.";
	}*/

}
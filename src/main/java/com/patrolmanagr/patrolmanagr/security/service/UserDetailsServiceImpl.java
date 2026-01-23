package com.patrolmanagr.patrolmanagr.security.service;
import com.patrolmanagr.patrolmanagr.entity.User;
import com.patrolmanagr.patrolmanagr.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	public UserRepository userRepository;
    
	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		User user = userRepository.findByUsername(username);
				
		if(user==null)		
			new UsernameNotFoundException("User Not Found with -> username or email : " + username);

		return UserPrinciple.build(user);
	}
}
package com.patrolmanagr.patrolmanagr.repository;
import com.patrolmanagr.patrolmanagr.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);
    @Query("select user from User user where user.id = :id")
    User findByIdUser(@Param("id") Long id);

    @Query("select user from User user where user.email = :email")
    User findByEmail(@Param("email") String email);

    @Query("select user from User user where user.role_code.id = :id")
    List<User> findUserByRole(@PathVariable("id") Long id);
    
}
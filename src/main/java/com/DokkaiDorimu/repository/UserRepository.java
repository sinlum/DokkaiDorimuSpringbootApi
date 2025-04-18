package com.DokkaiDorimu.repository;

import com.DokkaiDorimu.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    List<User> findByEmailContainingIgnoreCase(String email);
    List<User> findByRole(User.Role role);
    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email);
    List<User> findByIdNot(Long userId);
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.isOnline = false")
    void resetAllUsersOffline();
}

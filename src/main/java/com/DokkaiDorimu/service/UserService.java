package com.DokkaiDorimu.service;

import com.DokkaiDorimu.DTO.ContributionDTO;
import com.DokkaiDorimu.DTO.UserDTO;
import com.DokkaiDorimu.DTO.UserRoleUpdateDTO;
import com.DokkaiDorimu.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserDTO> getAllUsers();
    Optional<User> getUserById(Long id);
    Optional<User> getUserByEmail(String email);
    Optional<User> getUserByUsername(String username);
    User getCurrentUser();
    User saveUser(User user);
    void deleteUser(Long id);
    public void signup(UserDTO userDTO) throws Exception;
    void updateUserProfile(UserDTO userDTO, String email, String status, String address, String bio,String school, String grade);
    UserDTO getUserProfile(String email);
    UserDTO getUserProfile(Long id);
    public List<ContributionDTO> getContributionsByCreator(Long creatorId);
    public List<UserDTO> searchByEmail(String email);
    UserRoleUpdateDTO updateUserRole(Long userId, User.Role newRole);
    public List<UserDTO> getAllCreators();
    public List<UserDTO> searchUsers(String query);
    List<UserDTO> getAllUsersExcept(Long userId);
//    void setUserOnlineStatus(Long userId, boolean isOnline);
}
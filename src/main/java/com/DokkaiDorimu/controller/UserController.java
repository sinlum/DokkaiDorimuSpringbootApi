package com.DokkaiDorimu.controller;

import com.DokkaiDorimu.DTO.ContributionDTO;
import com.DokkaiDorimu.DTO.UserDTO;
import com.DokkaiDorimu.DTO.UserRoleUpdateDTO;
import com.DokkaiDorimu.entity.User;
import com.DokkaiDorimu.exception.UserNotFoundException;
import com.DokkaiDorimu.service.NotificationService;
import com.DokkaiDorimu.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    @GetMapping("/userProfile")
    public ResponseEntity<UserDTO> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        UserDTO userDTO = userService.getUserProfile(currentUserEmail);
        return ResponseEntity.ok(userDTO);
    }
    @GetMapping("/userProfile/{id}")
    public ResponseEntity<UserDTO> getUserProfileWithId(@PathVariable Long id) {
        UserDTO userDTO = userService.getUserProfile(id);
        return ResponseEntity.ok(userDTO);
    }
    @GetMapping("/contributions/{creatorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ContributionDTO>> getCreatorContributions(@PathVariable Long creatorId) {
        List<ContributionDTO> contributions = userService.getContributionsByCreator(creatorId);
        return new ResponseEntity<>(contributions, HttpStatus.OK);
    }

//    @PutMapping("/updateProfile")
//    public ResponseEntity<String> updateProfile(
//            @RequestParam("name") String name,
//            @RequestParam(value = "currentPassword", required = false) String currentPassword,
//            @RequestParam(value = "newPassword", required = false) String newPassword,
//            @RequestParam(value = "profilePic", required = false) MultipartFile profilePic) {
//
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String currentUserEmail = authentication.getName();
//
//        UserDTO userDTO = new UserDTO();
//        userDTO.setUsername(name);
//        if (currentPassword != null && newPassword != null) {
//            userDTO.setCurrentPassword(currentPassword);
//            userDTO.setPassword(newPassword);
//        }
//        if (profilePic != null && !profilePic.isEmpty()) {
//            try {
//                String imgUrl = saveProfilePic(profilePic);
//                userDTO.setImgUrl(imgUrl);
//            } catch (IOException e) {
//                return ResponseEntity.status(500).body("Error uploading image");
//            }
//        }
//
//        userService.updateUserProfile(userDTO, currentUserEmail);
//        return ResponseEntity.ok("Profile updated successfully");
//    }
//
//    private String saveProfilePic(MultipartFile file) throws IOException {
//        String folder = "src/main/resources/static/images/";
//        File dir = new File(folder);
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
//
//        String filePath = folder + file.getOriginalFilename();
//        Path path = Paths.get(filePath);
//        Files.write(path, file.getBytes());
//
//        return "/images/" + file.getOriginalFilename(); // Return the path to the stored image
//    }
    @PutMapping("/updateProfile")
    public ResponseEntity<String> updateProfile(
        @RequestParam("name") String name,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "address", required = false) String address,
        @RequestParam(value = "bio", required = false) String bio,
        @RequestParam(value = "school", required = false) String school,
        @RequestParam(value = "grade", required = false) String grade,
        @RequestParam(value = "profilePic", required = false) MultipartFile profilePic) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(name);
        userDTO.setStatus(status);
        userDTO.setAddress(address);
        userDTO.setBio(bio);
        userDTO.setGrade(grade);
        userDTO.setSchool(school);


        if (profilePic != null && !profilePic.isEmpty()) {
            try {
            String imgUrl = saveProfilePic(profilePic);
            userDTO.setImgUrl(imgUrl);
            } catch (IOException e) {
                return ResponseEntity.status(500).body("Error uploading image");
            }
    }

    userService.updateUserProfile(userDTO, currentUserEmail, status, address, bio, school, grade);
    return ResponseEntity.ok("Profile updated successfully");
}

    private String saveProfilePic(MultipartFile file) throws IOException {
        String folder = "src/main/resources/static/images/";
        File dir = new File(folder);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath = folder + file.getOriginalFilename();
        Path path = Paths.get(filePath);
        Files.write(path, file.getBytes());

        return "/images/" + file.getOriginalFilename(); // Return the path to the stored image
    }
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
//    @GetMapping("/users/search")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String email) {
//        List<UserDTO> users = userService.searchByEmail(email);
//        return ResponseEntity.ok(users);
//    }
    @PutMapping("users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(@PathVariable Long userId, @RequestParam String newRole) {
        try {
            logger.info("Received role update request for user {} to role {}", userId, newRole);

            UserRoleUpdateDTO updatedUser = userService.updateUserRole(userId, User.Role.valueOf(newRole));

            logger.info("Successfully processed role update for user {}", userId);
            return ResponseEntity.ok(updatedUser);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid role specified for user {}: {}", userId, newRole);
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Invalid role specified"));

        } catch (UserNotFoundException e) {
            logger.error("User not found for role update: {}", userId);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));

        } catch (Exception e) {
            logger.error("Error updating role for user {}: {}", userId, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error updating user role"));
        }
    }
    @GetMapping("/creators")
    public ResponseEntity<List<UserDTO>> getAllCreators() {
        List<UserDTO> creators = userService.getAllCreators();
        return ResponseEntity.ok(creators);
    }
    @GetMapping("/users/search")
    public ResponseEntity<?> searchUsers(
            @RequestParam String query,
            @RequestParam(required = false) Boolean adminSearch) {
        if (adminSearch != null && adminSearch) {
            // This is an admin search
            List<UserDTO> users = userService.searchByEmail(query);
            return ResponseEntity.ok(users);
        } else {
            // This is a regular user search for chat
            List<UserDTO> users = userService.searchUsers(query);
            return ResponseEntity.ok(users);
        }
    }
    @GetMapping("/users/all")
    public ResponseEntity<List<UserDTO>> getAllUsers(@RequestParam(required = false) Long excludeUserId) {
        List<UserDTO> users = userService.getAllUsersExcept(excludeUserId != null ? excludeUserId : 0L);
        return ResponseEntity.ok(users);
    }
    @GetMapping("/{userId}/online-status")
    public ResponseEntity<Boolean> getUserOnlineStatus(@PathVariable Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        return ResponseEntity.ok(user.isOnline());
    }

}
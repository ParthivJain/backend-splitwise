package com.splitwise.controller;

import com.splitwise.model.User;
import com.splitwise.dto.UserDTO;
import com.splitwise.repository.UserRepository;
import com.splitwise.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public User createUser(@RequestBody User user){
        return userService.saveUser(user);
    }

    @GetMapping
    public List<UserDTO> getAllUsers(){
        return userService.getAllUsers()
                .stream()
                .map(user -> {
                    UserDTO dto = new UserDTO();
                    dto.setId(user.getId());
                    dto.setName(user.getName());
                    dto.setUsername(user.getUsername());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
    }

    @GetMapping("/me")
    public UserDTO getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setProfilePic(user.getProfilePic());
        return dto;
    }

    @PutMapping("/profile-pic")
    public ResponseEntity<?> updateProfilePic(@RequestBody Map<String, String> request){
        Long userId = getCurrentUserId();
        String profilePic = request.get("profilePic");

        User user = userRepository.findById(userId).orElseThrow(() ->new RuntimeException("User Not Found"));

        user.setProfilePic(profilePic);
        userRepository.save(user);

        return ResponseEntity.ok(user);

    }

    @DeleteMapping("/profile-pic")
    public ResponseEntity<?> removeProfilePic(){
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        user.setProfilePic(null);
        userRepository.save(user);

        return ResponseEntity.ok("Profile photo removed");

    }

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

}

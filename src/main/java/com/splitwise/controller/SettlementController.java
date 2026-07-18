package com.splitwise.controller;

import com.splitwise.dto.SettlementRequest;
import com.splitwise.dto.SettlementResponseDTO;
import com.splitwise.dto.SettlementSuggestionDTO;
import com.splitwise.model.User;
import com.splitwise.security.JwtUtil;
import com.splitwise.service.SettlementService;
import com.splitwise.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settlement")
public class SettlementController {

    @Autowired
    private UserService userService;

    @Autowired
    private SettlementService settlementService;

    @PostMapping("/suggest")
    public ResponseEntity<?> getSettlementSugestions(
            @RequestBody SettlementRequest request ){
        try{
            Long currentUserId = getCurrentUserId();
            SettlementResponseDTO response = settlementService.getSettlementSuggestion(currentUserId, request.getFriendIds());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

}

package com.azki.reservation.controller;

import com.azki.reservation.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Users Rest Service v1")
@RequestMapping(value = "/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/login")
    @Operation(
            summary = "Authenticate and obtain an access token",
            description = "Users must authenticate to obtain an access token. Include the token in the 'Authorization' header of each subsequent request using the format: Authorization=Bearer ${token}."
    )
    public ResponseEntity<Map<String,Object>> login(@RequestParam String username, @RequestParam String password) throws JsonProcessingException {
        return ResponseEntity.ok(userService.login(username, password));
    }
}

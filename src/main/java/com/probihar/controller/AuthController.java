package com.probihar.controller;

import com.probihar.model.Role;
import com.probihar.model.UserEntity;
import com.probihar.repository.UserRepository;
import com.probihar.service.JwtService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@CrossOrigin
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthController(AuthenticationManager am, JwtService jwt, UserRepository u, PasswordEncoder e) {
        this.authManager = am; this.jwt = jwt; this.users = u; this.encoder = e;
    }

    public record AuthRequest(@NotBlank String username, @NotBlank String password) {}
    public record TokenResponse(String token) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest req) {
        if (users.existsByUsername(req.username())) return ResponseEntity.badRequest().body("Username taken");
        var user = new UserEntity();
        user.setUsername(req.username());
        user.setPassword(encoder.encode(req.password()));
        user.setRoles(Set.of(Role.ROLE_USER)); // default
        users.save(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody AuthRequest req) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        } catch (BadCredentialsException e) {
            throw e; // will be mapped to 401 by the handler above
        }
        String token = jwt.generate(req.username(), Map.of("roles",
                users.findByUsername(req.username()).orElseThrow().getRoles()));
        return new TokenResponse(token);
    }
}


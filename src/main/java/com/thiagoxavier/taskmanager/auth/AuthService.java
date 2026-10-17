package com.thiagoxavier.taskmanager.auth;

import com.thiagoxavier.taskmanager.auth.dto.AuthResponse;
import com.thiagoxavier.taskmanager.auth.dto.LoginRequest;
import com.thiagoxavier.taskmanager.auth.dto.RegisterRequest;
import com.thiagoxavier.taskmanager.exception.BusinessException;
import com.thiagoxavier.taskmanager.security.JwtService;
import com.thiagoxavier.taskmanager.user.User;
import com.thiagoxavier.taskmanager.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("E-mail já cadastrado");
        }
        User user = new User(request.name().trim(), email, passwordEncoder.encode(request.password()));
        userRepository.save(user);
        return issueToken(email);
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        return issueToken(email);
    }

    private AuthResponse issueToken(String email) {
        return AuthResponse.bearer(jwtService.generateToken(email), jwtService.getExpirationSeconds());
    }
}

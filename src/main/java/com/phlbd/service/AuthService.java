package com.phlbd.service;

import com.phlbd.common.EntityMapper;
import com.phlbd.dto.auth.*;
import com.phlbd.entity.Cart;
import com.phlbd.entity.RefreshToken;
import com.phlbd.entity.User;
import com.phlbd.entity.enums.RoleType;
import com.phlbd.exception.BadRequestException;
import com.phlbd.exception.ResourceNotFoundException;
import com.phlbd.exception.UnauthorizedException;
import com.phlbd.repository.CartRepository;
import com.phlbd.repository.RefreshTokenRepository;
import com.phlbd.repository.UserRepository;
import com.phlbd.security.JwtTokenProvider;
import com.phlbd.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Value("${app.jwt.access-token-expiration-ms:3600000}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(RoleType.ROLE_CUSTOMER)
                .active(true)
                .avatarUrl("https://i.pravatar.cc/150?u=" + request.getEmail())
                .build();

        User savedUser = userRepository.save(user);

        // Initialize empty shopping cart for the user in MongoDB
        Cart cart = Cart.builder().userId(savedUser.getId()).build();
        cartRepository.save(cart);

        UserPrincipal principal = UserPrincipal.create(savedUser);
        String accessToken = tokenProvider.generateAccessToken(principal);
        RefreshToken refreshToken = createRefreshToken(savedUser.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(accessTokenExpirationMs / 1000)
                .user(EntityMapper.toUserDto(savedUser))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase().trim(), request.getPassword())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Your account is deactivated. Please contact restaurant administration.");
        }

        String accessToken = tokenProvider.generateAccessToken(principal);
        RefreshToken refreshToken = createRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(accessTokenExpirationMs / 1000)
                .user(EntityMapper.toUserDto(user))
                .build();
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.isRevoked() || refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new UnauthorizedException("Refresh token is expired or revoked. Please log in again.");
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserPrincipal principal = UserPrincipal.create(user);
        String newAccessToken = tokenProvider.generateAccessToken(principal);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(accessTokenExpirationMs / 1000)
                .user(EntityMapper.toUserDto(user))
                .build();
    }

    public void logout(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    public UserDto getCurrentUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return EntityMapper.toUserDto(user);
    }

    private RefreshToken createRefreshToken(String userId) {
        refreshTokenRepository.deleteByUserId(userId);

        RefreshToken token = RefreshToken.builder()
                .userId(userId)
                .token(tokenProvider.generateRefreshTokenString())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(token);
    }
}

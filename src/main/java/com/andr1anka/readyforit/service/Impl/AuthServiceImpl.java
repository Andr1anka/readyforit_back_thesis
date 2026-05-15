package com.andr1anka.readyforit.service.Impl;

import com.andr1anka.readyforit.dto.AuthResponseDTO;
import com.andr1anka.readyforit.dto.LoginRequestDTO;
import com.andr1anka.readyforit.dto.RegisterRequestDTO;
import com.andr1anka.readyforit.exception.BadRequestException;
import com.andr1anka.readyforit.model.EmailVerificationToken;
import com.andr1anka.readyforit.model.PasswordResetToken;
import com.andr1anka.readyforit.model.Role;
import com.andr1anka.readyforit.model.User;
import com.andr1anka.readyforit.repository.EmailVerificationTokenRepository;
import com.andr1anka.readyforit.repository.PasswordResetTokenRepository;
import com.andr1anka.readyforit.repository.UserRepository;
import com.andr1anka.readyforit.service.AuthService;
import com.andr1anka.readyforit.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository verificationRepo;
    private final PasswordResetTokenRepository resetTokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JavaMailSender mailSender;
    private final AuthenticationManager authenticationManager;

    @Override
    public void register(RegisterRequestDTO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email вже використовується");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .enabled(false)
                .isBlocked(false)
                .rank(0.0)
                .balance(BigDecimal.ZERO)
                .build();

        userRepository.save(user);

        EmailVerificationToken token = createVerificationToken(user);
        sendVerificationEmail(user.getEmail(), token.getToken());
    }

    private EmailVerificationToken createVerificationToken(User user) {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setEmail(user.getEmail());
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10));
        return verificationRepo.save(token);
    }

    private void sendVerificationEmail(String email, String token) {
        String confirmationUrl = "http://localhost:8080/api/auth/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Підтвердження email - ReadyForIt");
        message.setText("Для підтвердження акаунту перейдіть за посиланням:\n" + confirmationUrl +
                "\n\nПосилання дійсне 10 хвилин.");

        mailSender.send(message);
    }

    @Override
    public void verifyEmail(String tokenStr) {
        EmailVerificationToken token = verificationRepo.findByToken(tokenStr)
                .orElseThrow(() -> new BadRequestException("Невірний або прострочений токен"));

        if (token.isExpired()) {
            verificationRepo.delete(token);
            throw new BadRequestException("Токен прострочено. Зареєструйтесь повторно.");
        }

        User user = token.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        verificationRepo.delete(token);
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();

        if (!user.isEnabled()) throw new BadRequestException("Підтвердіть ваш email");
        if (user.isBlocked()) throw new BadRequestException("Акаунт заблоковано адміністратором");

        String jwt = jwtService.generateToken(user.getEmail());
        return new AuthResponseDTO(jwt, "Успішний вхід");
    }

    // ==================== СКИДАННЯ ПАРОЛЮ ====================

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Користувача з таким email не знайдено"));

        PasswordResetToken token = createPasswordResetToken(user);
        sendResetPasswordEmail(user.getEmail(), token.getToken());
    }

    private PasswordResetToken createPasswordResetToken(User user) {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setEmail(user.getEmail());
        token.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        return resetTokenRepo.save(token);
    }

    private void sendResetPasswordEmail(String email, String token) {
        String resetUrl = "http://localhost:8080/api/auth/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Скидання паролю - ReadyForIt");
        message.setText("Для скидання паролю перейдіть за посиланням:\n" + resetUrl +
                "\n\nПосилання дійсне 15 хвилин.");

        mailSender.send(message);
    }

    @Override
    public void resetPassword(String tokenStr, String newPassword) {
        PasswordResetToken token = resetTokenRepo.findByToken(tokenStr)
                .orElseThrow(() -> new BadRequestException("Невірний токен"));

        if (token.isExpired()) {
            resetTokenRepo.delete(token);
            throw new BadRequestException("Токен прострочено");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        resetTokenRepo.delete(token);
    }
}



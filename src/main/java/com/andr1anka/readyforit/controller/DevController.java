package com.andr1anka.readyforit.controller;

import com.andr1anka.readyforit.exception.BadRequestException;
import com.andr1anka.readyforit.model.InterviewerRequestStatus;
import com.andr1anka.readyforit.model.Role;
import com.andr1anka.readyforit.model.User;
import com.andr1anka.readyforit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * ⚠ ЛИШЕ ДЛЯ РОЗРОБКИ. Дозволяє користувачу самому стати INTERVIEWER,
 * щоб тестувати функціонал інтерв'юера до того, як готова панель адміна (п.7).
 *
 * Вмикається властивістю app.dev.allow-self-promote=true в application.properties.
 * За замовчуванням ВИМКНЕНО — у проді ендпоінт поверне 403.
 *
 * Коли буде готовий адмін-флоу — цей контролер можна видалити.
 */
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevController {

    private final UserRepository userRepository;

    @Value("${app.dev.allow-self-promote:false}")
    private boolean allowSelfPromote;

    @PostMapping("/become-interviewer")
    @Transactional
    public ResponseEntity<Map<String, Object>> becomeInterviewer(
            @AuthenticationPrincipal UserDetails principal) {
        if (!allowSelfPromote) {
            throw new BadRequestException("Недоступно (увімкніть app.dev.allow-self-promote для розробки)");
        }
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new BadRequestException("Користувача не знайдено"));

        user.setRole(Role.INTERVIEWER);
        user.setInterviewerRequestStatus(InterviewerRequestStatus.APPROVED);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "role", user.getRole().name(),
                "message", "Тепер ви INTERVIEWER (dev). Перелогіньтесь, щоб оновити меню."
        ));
    }
}

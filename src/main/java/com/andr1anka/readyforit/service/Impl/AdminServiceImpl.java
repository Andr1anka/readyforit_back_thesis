package com.andr1anka.readyforit.service.Impl;

import com.andr1anka.readyforit.dto.*;
import com.andr1anka.readyforit.exception.BadRequestException;
import com.andr1anka.readyforit.model.*;
import com.andr1anka.readyforit.repository.*;
import com.andr1anka.readyforit.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ComplaintRepository complaintRepository;
    private final InterviewerRequestRepository requestRepository;
    private final InterviewerRepository interviewerRepository;
    private final VerificationRequestRepository verificationRequestRepository;

    // ----------------------------------------------------------- СКАРГИ
    @Override
    @Transactional(readOnly = true)
    public List<ComplaintViewDTO> getComplaints(String status) {
        List<Complaint> list;
        if (status == null || status.isBlank() || status.equalsIgnoreCase("ALL")) {
            list = complaintRepository.findAllByOrderByCreatedAtDesc();
        } else {
            ComplaintStatus st = parseComplaintStatus(status);
            list = complaintRepository.findAllByStatusOrderByCreatedAtDesc(st);
        }
        return list.stream().map(this::toComplaintView).toList();
    }

    @Override
    @Transactional
    public ComplaintViewDTO resolveComplaint(Long complaintId, boolean accept, String adminComment) {
        Complaint c = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new BadRequestException("Скаргу не знайдено"));
        c.setStatus(accept ? ComplaintStatus.RESOLVED : ComplaintStatus.REJECTED);
        c.setAdminComment(adminComment);
        complaintRepository.save(c);
        return toComplaintView(c);
    }

    // ----------------------------------------------------------- КОРИСТУВАЧІ
    @Override
    @Transactional(readOnly = true)
    public List<AdminUserDTO> getUsers() {
        return userRepository.findAllByOrderByIdAsc().stream().map(this::toUserDto).toList();
    }

    @Override
    @Transactional
    public AdminUserDTO setBlocked(Long userId, boolean blocked) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Користувача не знайдено"));
        if (u.getRole() == Role.ADMIN) {
            throw new BadRequestException("Не можна блокувати адміністратора");
        }
        u.setBlocked(blocked);
        userRepository.save(u);
        return toUserDto(u);
    }

    // ----------------------------------------------------------- ЗАЯВКИ
    @Override
    @Transactional(readOnly = true)
    public List<AdminRequestDTO> getPendingRequests() {
        return requestRepository.findAllByStatus(InterviewerRequestStatus.PENDING)
                .stream().map(this::toRequestDto).toList();
    }

    @Override
    @Transactional
    public AdminRequestDTO decideRequest(Long requestId, boolean approve, String adminComment) {
        InterviewerRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new BadRequestException("Заявку не знайдено"));

        req.setStatus(approve ? InterviewerRequestStatus.APPROVED : InterviewerRequestStatus.REJECTED);
        req.setAdminComment(adminComment);
        req.setReviewedAt(LocalDateTime.now());
        requestRepository.save(req);

        User user = req.getUser();
        if (user != null) {
            if (approve) {
                user.setRole(Role.INTERVIEWER);
                userRepository.save(user);
                // створюємо профіль інтерв'юера, якщо ще немає
                interviewerRepository.findByUser(user).orElseGet(() -> {
                    Interviewer i = new Interviewer();
                    i.setUser(user);
                    i.setPlannedSessionDurationMinutes(60);
                    i.setExpectedTimeForBreak(10);
                    return interviewerRepository.save(i);
                });
            } else {
                // повертаємо роль на USER, якщо була проміжна
                if (user.getRole() == Role.REQUEST_FOR_INTERVIEWER) {
                    user.setRole(Role.USER);
                    userRepository.save(user);
                }
            }
        }
        return toRequestDto(req);
    }

    // ----------------------------------------------------------- ВЕРИФІКАЦІЯ
    @Override
    @Transactional(readOnly = true)
    public List<AdminUserDTO> getVerificationQueue() {
        return userRepository.findAllByVerificationStatus(VerificationStatus.ESCALATED)
                .stream()
                .map(this::toVerificationUserDto)
                .toList();
    }

    @Override
    @Transactional
    public AdminUserDTO decideVerification(Long userId, boolean approve) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Користувача не знайдено"));
        if (approve) {
            u.setVerificated(true);
            u.setVerificationStatus(VerificationStatus.VERIFIED);
        } else {
            u.setVerificated(false);
            u.setVerificationStatus(VerificationStatus.REJECTED);
        }
        userRepository.save(u);
        return toUserDto(u);
    }

    // ----------------------------------------------------------- мапери
    private ComplaintStatus parseComplaintStatus(String s) {
        try {
            return ComplaintStatus.valueOf(s.toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Невідомий статус: " + s);
        }
    }

    private AdminUserDTO toUserDto(User u) {
        return AdminUserDTO.builder()
                .id(u.getId())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .email(u.getEmail())
                .role(u.getRole())
                .blocked(u.isBlocked())
                .verificated(u.isVerificated())
                .verificationStatus(u.getVerificationStatus())
                .rank(u.getRank())
                .build();
    }


    private AdminUserDTO toVerificationUserDto(User u) {
        VerificationRequest request = verificationRequestRepository
                .findTopByUserOrderByCreatedAtDesc(u)
                .orElse(null);

        AdminUserDTO.AdminUserDTOBuilder builder = toUserDto(u).toBuilder();

        if (request != null) {
            builder
                    .verificationRequestId(request.getId())
                    .profilePhotoUrl("/admin/verifications/" + request.getId() + "/profile-photo")
                    .documentUrl("/admin/verifications/" + request.getId() + "/document")
                    .selfieUrl("/admin/verifications/" + request.getId() + "/selfie")
                    .nameMatch(request.getNameMatch())
                    .faceSimilarity(request.getFaceSimilarity())
                    .faceMatch(request.getFaceMatch())
                    .extractedTextPreview(request.getExtractedTextPreview())
                    .verificationCreatedAt(request.getCreatedAt());
        }

        return builder.build();
    }

    private AdminRequestDTO toRequestDto(InterviewerRequest r) {
        User u = r.getUser();
        return AdminRequestDTO.builder()
                .id(r.getId())
                .userId(u == null ? null : u.getId())
                .firstName(u == null ? null : u.getFirstName())
                .lastName(u == null ? null : u.getLastName())
                .email(u == null ? null : u.getEmail())
                .specialization(r.getSpecialization())
                .experienceDescription(r.getExperienceDescription())
                .yearsOfExperience(r.getYearsOfExperience())
                .externalLinks(r.getExternalLinks())
                .proofObjectKeys(r.getProofObjectKeys())
                .status(r.getStatus())
                .adminComment(r.getAdminComment())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private ComplaintViewDTO toComplaintView(Complaint c) {
        return ComplaintViewDTO.builder()
                .id(c.getId())
                .lessonId(c.getLesson() == null ? null : c.getLesson().getId())
                .lessonTitle(c.getLesson() == null || c.getLesson().getLessonType() == null
                        ? "Заняття" : c.getLesson().getLessonType().getTitle())
                .accusedFirstName(c.getAccusedUser() == null ? null : c.getAccusedUser().getFirstName())
                .accusedLastName(c.getAccusedUser() == null ? null : c.getAccusedUser().getLastName())
                .title(c.getTitle())
                .description(c.getDescription())
                .status(c.getStatus())
                .adminComment(c.getAdminComment())
                .createdAt(c.getCreatedAt())
                .build();
    }
}

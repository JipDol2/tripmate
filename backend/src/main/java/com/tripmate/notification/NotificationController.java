package com.tripmate.notification;

import com.tripmate.auth.CustomUserPrincipal;
import com.tripmate.common.ApiException;
import com.tripmate.user.User;
import com.tripmate.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "알림", description = "동행 신청 상태 변경 등 사용자 알림을 조회하고 읽음 처리하는 API")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationController(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "알림 목록 조회", description = "현재 로그인한 사용자가 받은 알림 목록을 최신순으로 조회합니다.")
    public List<NotificationResponse> notifications(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal) {
        User recipient = userRepository.getReferenceById(principal.getUserId());

        return notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @GetMapping("/unread-count")
    @Transactional(readOnly = true)
    @Operation(summary = "읽지 않은 알림 개수 조회", description = "현재 로그인한 사용자가 아직 읽지 않은 알림의 개수를 조회합니다.")
    public long unreadCount(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal) {
        User recipient = userRepository.getReferenceById(principal.getUserId());
        return notificationRepository.countByRecipientAndReadFalse(recipient);
    }

    @PatchMapping("/{notificationId}/read")
    @Transactional
    @Operation(summary = "알림 읽음 처리", description = "현재 로그인한 사용자의 특정 알림을 읽음 상태로 변경합니다.")
    public NotificationResponse markAsRead(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
                                           @PathVariable Long notificationId) {
        Notification notification = getOwnedNotification(principal.getUserId(), notificationId);
        notification.markAsRead();
        return NotificationResponse.from(notification);
    }

    @PatchMapping("/read-all")
    @Transactional
    @Operation(summary = "모든 알림 읽음 처리", description = "현재 로그인한 사용자의 읽지 않은 모든 알림을 읽음 상태로 일괄 변경합니다.")
    public void markAllAsRead(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal) {
        User recipient = userRepository.getReferenceById(principal.getUserId());
        notificationRepository.findByRecipientAndReadFalse(recipient)
                .forEach(Notification::markAsRead);
    }

    private Notification getOwnedNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification not found."));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot access this notification.");
        }

        return notification;
    }
}

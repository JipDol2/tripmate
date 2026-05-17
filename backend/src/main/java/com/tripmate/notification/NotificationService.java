package com.tripmate.notification;

import com.tripmate.application.CompanionApplication;
import com.tripmate.post.CompanionPost;
import com.tripmate.user.User;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void notifyApplicationReceived(CompanionApplication application) {
        CompanionPost post = application.getPost();
        User applicant = application.getApplicant();

        notificationRepository.save(new Notification(
                post.getAuthor(),
                NotificationType.APPLICATION_RECEIVED,
                "새 동행 신청",
                applicant.getNickname() + "님이 '" + post.getTitle() + "' 동행에 신청했습니다.",
                "/posts/" + post.getId()
        ));
    }

    public void notifyApplicationAccepted(CompanionApplication application) {
        notifyApplicationResult(
                application,
                NotificationType.APPLICATION_ACCEPTED,
                "동행 요청 수락",
                "'" + application.getPost().getTitle() + "' 동행 요청이 수락되었습니다."
        );
    }

    public void notifyApplicationRejected(CompanionApplication application) {
        notifyApplicationResult(
                application,
                NotificationType.APPLICATION_REJECTED,
                "동행 요청 거절",
                "'" + application.getPost().getTitle() + "' 동행 요청이 거절되었습니다."
        );
    }

    private void notifyApplicationResult(CompanionApplication application, NotificationType type, String title, String message) {
        CompanionPost post = application.getPost();

        notificationRepository.save(new Notification(
                application.getApplicant(),
                type,
                title,
                message,
                "/posts/" + post.getId()
        ));
    }
}

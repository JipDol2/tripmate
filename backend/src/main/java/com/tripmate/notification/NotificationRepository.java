package com.tripmate.notification;

import com.tripmate.user.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

    List<Notification> findByRecipientAndReadFalse(User recipient);

    long countByRecipientAndReadFalse(User recipient);
}

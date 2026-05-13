package com.tripmate.block;

import com.tripmate.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {
    boolean existsByBlockerAndBlockedUserId(User blocker, Long blockedUserId);
}

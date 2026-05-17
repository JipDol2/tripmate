package com.tripmate.review;

import com.tripmate.user.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanionReviewRepository extends JpaRepository<CompanionReview, Long> {
    List<CompanionReview> findByRevieweeOrderByCreatedAtDesc(User reviewee);
}

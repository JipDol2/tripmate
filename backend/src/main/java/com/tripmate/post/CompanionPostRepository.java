package com.tripmate.post;

import com.tripmate.user.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanionPostRepository extends JpaRepository<CompanionPost, Long>, CompanionPostRepositoryCustom {
    List<CompanionPost> findByAuthorOrderByCreatedAtDesc(User author);
}

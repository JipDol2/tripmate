package com.tripmate.application;

import com.tripmate.post.CompanionPost;
import com.tripmate.user.User;
import org.springframework.data.jpa.repository.*;
import java.util.List;

public interface CompanionApplicationRepository extends JpaRepository<CompanionApplication, Long> {
    boolean existsByPostAndApplicant(CompanionPost post, User applicant);

    @Query("select a from CompanionApplication a join fetch a.post p join fetch p.author where a.applicant = :user order by a.createdAt desc")
    List<CompanionApplication> findMyApplications(User user);

    @Query("select a from CompanionApplication a join fetch a.applicant join fetch a.post p where p.author = :author order by a.createdAt desc")
    List<CompanionApplication> findReceivedApplications(User author);
}

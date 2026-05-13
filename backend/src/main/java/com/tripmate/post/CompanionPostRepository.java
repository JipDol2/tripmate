package com.tripmate.post;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanionPostRepository extends JpaRepository<CompanionPost, Long>, CompanionPostRepositoryCustom {
}

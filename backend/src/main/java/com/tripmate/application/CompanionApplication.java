package com.tripmate.application;

import com.tripmate.common.BaseEntity;
import com.tripmate.post.CompanionPost;
import com.tripmate.user.User;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "companion_applications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "applicant_id"}))
public class CompanionApplication extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name = "post_id")
    private CompanionPost post;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name = "applicant_id")
    private User applicant;

    @Column(nullable=false, length=1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    protected CompanionApplication() {}

    public CompanionApplication(CompanionPost post, User applicant, String message) {
        this.post = post;
        this.applicant = applicant;
        this.message = message;
    }
    public void accept() { this.status = ApplicationStatus.ACCEPTED; }
    public void reject() { this.status = ApplicationStatus.REJECTED; }
    public void cancel() { this.status = ApplicationStatus.CANCELED; }
}

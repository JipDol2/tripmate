package com.tripmate.report;

import com.tripmate.common.BaseEntity;
import com.tripmate.user.User;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "reports")
public class Report extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    private User reporter;

    @Column(nullable=false)
    private Long targetUserId;

    private Long targetPostId;

    @Column(nullable=false, length=80)
    private String reason;

    @Column(length=1000)
    private String detail;

    protected Report() {}

    public Report(User reporter, Long targetUserId, Long targetPostId, String reason, String detail) {
        this.reporter = reporter;
        this.targetUserId = targetUserId;
        this.targetPostId = targetPostId;
        this.reason = reason;
        this.detail = detail;
    }
}

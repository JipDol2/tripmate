package com.tripmate.block;

import com.tripmate.common.BaseEntity;
import com.tripmate.user.User;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "user_blocks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"blocker_id", "blocked_user_id"}))
public class UserBlock extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name = "blocker_id")
    private User blocker;

    @Column(name = "blocked_user_id", nullable=false)
    private Long blockedUserId;

    protected UserBlock() {}

    public UserBlock(User blocker, Long blockedUserId) {
        this.blocker = blocker;
        this.blockedUserId = blockedUserId;
    }
}

package com.tripmate.chat;

import com.tripmate.common.BaseEntity;
import com.tripmate.post.CompanionPost;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "chat_rooms")
public class ChatRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id")
    private CompanionPost post;

    private LocalDateTime endedAt;

    protected ChatRoom() {}

    public ChatRoom(CompanionPost post) {
        this.post = post;
    }

    public boolean isTripEnded() {
        return endedAt != null;
    }

    public void endTrip() {
        if (endedAt == null) {
            endedAt = LocalDateTime.now();
        }
    }
}

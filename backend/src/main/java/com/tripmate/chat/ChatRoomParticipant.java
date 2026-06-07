package com.tripmate.chat;

import com.tripmate.common.BaseEntity;
import com.tripmate.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

@Getter
@Entity
@Table(
        name = "chat_room_participants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "user_id"})
)
public class ChatRoomParticipant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id")
    private ChatRoom room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "companion_joined", nullable = false)
    private boolean companionJoined;

    @Column(nullable = false)
    private boolean kicked;

    protected ChatRoomParticipant() {}

    public ChatRoomParticipant(ChatRoom room, User user, boolean companionJoined) {
        this.room = room;
        this.user = user;
        this.companionJoined = companionJoined;
    }

    public void joinCompanion() {
        this.companionJoined = true;
    }

    public void kick() {
        this.kicked = true;
        this.companionJoined = false;
    }
}

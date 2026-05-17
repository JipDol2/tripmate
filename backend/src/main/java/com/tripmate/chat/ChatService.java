package com.tripmate.chat;

import com.tripmate.application.ApplicationStatus;
import com.tripmate.application.CompanionApplicationRepository;
import com.tripmate.application.CompanionApplication;
import com.tripmate.common.ApiException;
import com.tripmate.post.CompanionPost;
import com.tripmate.user.User;
import com.tripmate.user.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomParticipantRepository participantRepository;
    private final ChatMessageRepository messageRepository;
    private final CompanionApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public ChatService(ChatRoomRepository chatRoomRepository,
                       ChatRoomParticipantRepository participantRepository,
                       ChatMessageRepository messageRepository,
                       CompanionApplicationRepository applicationRepository,
                       UserRepository userRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ChatRoom openRoomForAcceptedApplication(CompanionApplication application) {
        CompanionPost post = application.getPost();
        ChatRoom room = chatRoomRepository.findByPost(post)
                .orElseGet(() -> chatRoomRepository.save(new ChatRoom(post)));

        addParticipantIfAbsent(room, post.getAuthor());
        addParticipantIfAbsent(room, application.getApplicant());
        return room;
    }

    @Transactional
    public List<ChatRoomResponse> rooms(Long userId) {
        ensureRoomsForAcceptedApplications(userId);

        return chatRoomRepository.findRoomsByParticipant(userId)
                .stream()
                .map(this::toRoomResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> messages(Long userId, Long roomId) {
        ChatRoom room = getParticipantRoom(userId, roomId);

        return messageRepository.findByRoomOrderByCreatedAtAsc(room)
                .stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    @Transactional
    public ChatMessageResponse sendMessage(Long userId, Long roomId, String content) {
        if (content == null || content.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Message content is required.");
        }

        ChatRoom room = getParticipantRoom(userId, roomId);
        User sender = userRepository.getReferenceById(userId);
        ChatMessage message = messageRepository.save(new ChatMessage(room, sender, content.trim()));
        return ChatMessageResponse.from(message);
    }

    @Transactional(readOnly = true)
    public boolean isParticipant(Long userId, Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId).orElse(null);
        if (room == null) {
            return false;
        }

        User user = userRepository.getReferenceById(userId);
        return participantRepository.existsByRoomAndUser(room, user);
    }

    private void addParticipantIfAbsent(ChatRoom room, User user) {
        if (!participantRepository.existsByRoomAndUser(room, user)) {
            participantRepository.save(new ChatRoomParticipant(room, user));
        }
    }

    private void ensureRoomsForAcceptedApplications(Long userId) {
        User user = userRepository.getReferenceById(userId);

        applicationRepository.findByApplicantAndStatus(user, ApplicationStatus.ACCEPTED)
                .forEach(this::openRoomForAcceptedApplication);
        applicationRepository.findByPostAuthorAndStatus(user, ApplicationStatus.ACCEPTED)
                .forEach(this::openRoomForAcceptedApplication);
    }

    private ChatRoom getParticipantRoom(Long userId, Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Chat room not found."));
        User user = userRepository.getReferenceById(userId);

        if (!participantRepository.existsByRoomAndUser(room, user)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot access this chat room.");
        }

        return room;
    }

    private ChatRoomResponse toRoomResponse(ChatRoom room) {
        List<String> participantNicknames = participantRepository.findByRoom(room)
                .stream()
                .map((participant) -> participant.getUser().getNickname())
                .toList();
        ChatMessage lastMessage = messageRepository.findTopByRoomOrderByCreatedAtDesc(room)
                .orElse(null);

        return new ChatRoomResponse(
                room.getId(),
                room.getPost().getId(),
                room.getPost().getTitle(),
                participantNicknames,
                lastMessage == null ? "" : lastMessage.getContent(),
                lastMessage == null ? null : lastMessage.getCreatedAt()
        );
    }
}

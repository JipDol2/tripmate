package com.tripmate.chat;

import com.tripmate.application.CompanionApplication;
import com.tripmate.common.ApiException;
import com.tripmate.notification.NotificationService;
import com.tripmate.post.CompanionPost;
import com.tripmate.post.CompanionPostRepository;
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
    private final CompanionPostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ChatService(ChatRoomRepository chatRoomRepository,
                       ChatRoomParticipantRepository participantRepository,
                       ChatMessageRepository messageRepository,
                       CompanionPostRepository postRepository,
                       UserRepository userRepository,
                       NotificationService notificationService) {
        this.chatRoomRepository = chatRoomRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public ChatRoom openRoomForAcceptedApplication(CompanionApplication application) {
        CompanionPost post = application.getPost();
        ChatRoom room = chatRoomRepository.save(new ChatRoom(post));

        addParticipantIfAbsent(room, post.getAuthor(), true);
        addParticipantIfAbsent(room, application.getApplicant(), true);
        return room;
    }

    @Transactional
    public ChatRoomResponse startPostChat(Long userId, Long postId) {
        CompanionPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Post not found."));
        User user = userRepository.getReferenceById(userId);

        if (chatRoomRepository.existsByPostIdAndEndedAtIsNotNull(postId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This trip has already ended.");
        }

        ChatRoom room = chatRoomRepository.save(new ChatRoom(post));
        addParticipantIfAbsent(room, post.getAuthor(), true);

        if (!post.getAuthor().getId().equals(userId)) {
            addParticipantIfAbsent(room, user, false);
        }

        notificationService.notifyChatStarted(post, user, room);
        return toRoomResponse(room, userId);
    }

    @Transactional
    public List<ChatRoomResponse> rooms(Long userId) {
        return chatRoomRepository.findRoomsByParticipant(userId)
                .stream()
                .map((room) -> toRoomResponse(room, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatRoomResponse room(Long userId, Long roomId) {
        ChatRoom room = getParticipantRoom(userId, roomId);
        return toRoomResponse(room, userId);
    }

    @Transactional
    public ChatRoomResponse joinCompanion(Long userId, Long roomId) {
        ChatRoom room = getParticipantRoom(userId, roomId);
        if (room.isTripEnded()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This trip has already ended.");
        }

        User user = userRepository.getReferenceById(userId);
        ChatRoomParticipant participant = participantRepository.findByRoomAndUser(room, user)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "You cannot access this chat room."));

        if (participant.isKicked()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You already left this chat room.");
        }

        if (!participant.isCompanionJoined()
                && participantRepository.countJoinedParticipantsByPost(room.getPost()) >= room.getPost().getMaxParticipants()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This companion is already full.");
        }

        participant.joinCompanion();
        return toRoomResponse(room, userId);
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
    public void markRoomAsRead(Long userId, Long roomId) {
        ChatRoom room = getParticipantRoom(userId, roomId);
        User user = userRepository.getReferenceById(userId);
        ChatRoomParticipant participant = participantRepository.findByRoomAndUser(room, user)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "You cannot access this chat room."));
        participant.markAsRead();
    }

    @Transactional
    public ChatRoomLeaveEvent leaveRoom(Long userId, Long roomId) {
        ChatRoom room = getParticipantRoom(userId, roomId);

        User user = userRepository.getReferenceById(userId);
        ChatRoomParticipant participant = participantRepository.findByRoomAndUser(room, user)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "You cannot access this chat room."));
        String nickname = participant.getUser().getNickname();
        participant.leave();
        int remainingParticipants = participantRepository.findByRoomAndKickedFalse(room).size();

        return ChatRoomLeaveEvent.of(roomId, userId, nickname, remainingParticipants);
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
        return participantRepository.findByRoomAndUser(room, user)
                .filter((participant) -> !participant.isKicked())
                .isPresent();
    }

    private ChatRoomParticipant addParticipantIfAbsent(ChatRoom room, User user, boolean companionJoined) {
        return participantRepository.findByRoomAndUser(room, user)
                .map((participant) -> {
                    if (participant.isKicked()) {
                        throw new ApiException(HttpStatus.FORBIDDEN, "You already left this chat room.");
                    }
                    if (companionJoined) {
                        participant.joinCompanion();
                    }
                    return participant;
                })
                .orElseGet(() -> participantRepository.save(new ChatRoomParticipant(room, user, companionJoined)));
    }

    @Transactional
    public CompanionPost endTripByPost(Long hostUserId, Long postId) {
        CompanionPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Post not found."));

        if (!post.getAuthor().getId().equals(hostUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the host can end this trip.");
        }

        List<ChatRoom> rooms = chatRoomRepository.findByPost(post);
        if (rooms.isEmpty()) {
            ChatRoom room = chatRoomRepository.save(new ChatRoom(post));
            participantRepository.save(new ChatRoomParticipant(room, post.getAuthor(), true));
            rooms = List.of(room);
        }

        rooms.forEach(ChatRoom::endTrip);
        post.close();
        return post;
    }

    private ChatRoom getParticipantRoom(Long userId, Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Chat room not found."));
        User user = userRepository.getReferenceById(userId);

        boolean accessible = participantRepository.findByRoomAndUser(room, user)
                .filter((participant) -> !participant.isKicked())
                .isPresent();

        if (!accessible) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot access this chat room.");
        }

        return room;
    }

    private ChatRoomResponse toRoomResponse(ChatRoom room, Long currentUserId) {
        List<ChatRoomParticipant> participants = participantRepository.findByRoomAndKickedFalse(room);
        List<ChatParticipantResponse> participantResponses = participants
                .stream()
                .map(ChatParticipantResponse::from)
                .toList();
        List<String> participantNicknames = participants
                .stream()
                .map((participant) -> participant.getUser().getNickname())
                .toList();
        boolean myCompanionJoined = participants.stream()
                .filter((participant) -> participant.getUser().getId().equals(currentUserId))
                .anyMatch(ChatRoomParticipant::isCompanionJoined);
        ChatMessage lastMessage = messageRepository.findTopByRoomOrderByCreatedAtDesc(room)
                .orElse(null);
        ChatRoomParticipant currentParticipant = participants.stream()
                .filter((participant) -> participant.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElse(null);
        long unreadCount = getUnreadCount(room, currentParticipant);

        return new ChatRoomResponse(
                room.getId(),
                room.getPost().getId(),
                room.getPost().getTitle(),
                room.getPost().getAuthor().getId(),
                Math.toIntExact(participantRepository.countJoinedParticipantsByPost(room.getPost())),
                room.getPost().getMaxParticipants(),
                myCompanionJoined,
                room.isTripEnded(),
                room.getEndedAt(),
                participantResponses,
                participantNicknames,
                lastMessage == null ? "" : lastMessage.getContent(),
                lastMessage == null ? null : lastMessage.getCreatedAt(),
                unreadCount
        );
    }

    private long getUnreadCount(ChatRoom room, ChatRoomParticipant participant) {
        if (participant == null) {
            return 0;
        }

        if (participant.getLastReadAt() == null) {
            return messageRepository.countByRoomAndSenderNot(room, participant.getUser());
        }

        return messageRepository.countByRoomAndSenderNotAndCreatedAtAfter(
                room,
                participant.getUser(),
                participant.getLastReadAt()
        );
    }
}

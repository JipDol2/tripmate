package com.tripmate.chat;

import com.tripmate.application.CompanionApplication;
import com.tripmate.common.ApiException;
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

    public ChatService(ChatRoomRepository chatRoomRepository,
                       ChatRoomParticipantRepository participantRepository,
                       ChatMessageRepository messageRepository,
                       CompanionPostRepository postRepository,
                       UserRepository userRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ChatRoom openRoomForAcceptedApplication(CompanionApplication application) {
        CompanionPost post = application.getPost();
        ChatRoom room = chatRoomRepository.findByPost(post)
                .orElseGet(() -> chatRoomRepository.save(new ChatRoom(post)));

        addParticipantIfAbsent(room, post.getAuthor(), true);
        addParticipantIfAbsent(room, application.getApplicant(), true);
        return room;
    }

    @Transactional
    public ChatRoomResponse startPostChat(Long userId, Long postId) {
        CompanionPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Post not found."));
        User user = userRepository.getReferenceById(userId);
        ChatRoom room = chatRoomRepository.findByPost(post)
                .orElseGet(() -> chatRoomRepository.save(new ChatRoom(post)));

        addParticipantIfAbsent(room, post.getAuthor(), true);

        if (!post.getAuthor().getId().equals(userId)) {
            addParticipantIfAbsent(room, user, false);
        }

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
        User user = userRepository.getReferenceById(userId);
        ChatRoomParticipant participant = participantRepository.findByRoomAndUser(room, user)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "You cannot access this chat room."));

        if (participant.isKicked()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You were removed from this companion.");
        }

        if (!participant.isCompanionJoined() && joinedCount(room) >= room.getPost().getMaxParticipants()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This companion is already full.");
        }

        participant.joinCompanion();
        return toRoomResponse(room, userId);
    }

    @Transactional
    public ChatRoomResponse kickParticipant(Long hostUserId, Long roomId, Long participantId) {
        ChatRoom room = getParticipantRoom(hostUserId, roomId);

        if (!room.getPost().getAuthor().getId().equals(hostUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the host can remove participants.");
        }

        ChatRoomParticipant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Participant not found."));

        if (!participant.getRoom().getId().equals(room.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Participant does not belong to this room.");
        }

        if (participant.getUser().getId().equals(hostUserId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Host cannot be removed.");
        }

        participant.kick();
        return toRoomResponse(room, hostUserId);
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
        return participantRepository.findByRoomAndUser(room, user)
                .filter((participant) -> !participant.isKicked())
                .isPresent();
    }

    private ChatRoomParticipant addParticipantIfAbsent(ChatRoom room, User user, boolean companionJoined) {
        return participantRepository.findByRoomAndUser(room, user)
                .map((participant) -> {
                    if (participant.isKicked()) {
                        throw new ApiException(HttpStatus.FORBIDDEN, "You were removed from this companion.");
                    }
                    if (companionJoined) {
                        participant.joinCompanion();
                    }
                    return participant;
                })
                .orElseGet(() -> participantRepository.save(new ChatRoomParticipant(room, user, companionJoined)));
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

    private int joinedCount(ChatRoom room) {
        return (int) participantRepository.findByRoomAndKickedFalse(room)
                .stream()
                .filter(ChatRoomParticipant::isCompanionJoined)
                .count();
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

        return new ChatRoomResponse(
                room.getId(),
                room.getPost().getId(),
                room.getPost().getTitle(),
                room.getPost().getAuthor().getId(),
                joinedCount(room),
                room.getPost().getMaxParticipants(),
                myCompanionJoined,
                participantResponses,
                participantNicknames,
                lastMessage == null ? "" : lastMessage.getContent(),
                lastMessage == null ? null : lastMessage.getCreatedAt()
        );
    }
}

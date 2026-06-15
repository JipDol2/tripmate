package com.tripmate.review;

import com.tripmate.chat.ChatRoom;
import com.tripmate.chat.ChatRoomParticipant;
import com.tripmate.chat.ChatRoomParticipantRepository;
import com.tripmate.chat.ChatRoomRepository;
import com.tripmate.common.ApiException;
import com.tripmate.user.User;
import com.tripmate.user.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {
    private final CompanionReviewRepository reviewRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomParticipantRepository participantRepository;
    private final UserRepository userRepository;

    public ReviewService(CompanionReviewRepository reviewRepository,
                         ChatRoomRepository chatRoomRepository,
                         ChatRoomParticipantRepository participantRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ReviewTargetResponse> targets(Long reviewerId, Long roomId) {
        ChatRoom room = getEndedRoomWithJoinedParticipant(reviewerId, roomId);

        return participantRepository.findByRoomAndCompanionJoinedTrueAndKickedFalse(room)
                .stream()
                .filter((participant) -> !participant.getUser().getId().equals(reviewerId))
                .map((participant) -> new ReviewTargetResponse(
                        participant.getUser().getId(),
                        participant.getUser().getNickname(),
                        reviewRepository.existsByPostIdAndReviewerIdAndRevieweeId(
                                room.getPost().getId(),
                                reviewerId,
                                participant.getUser().getId()
                        )
                ))
                .toList();
    }

    @Transactional
    public ReviewResponse create(Long reviewerId, ReviewCreateRequest request) {
        ChatRoom room = getEndedRoomWithJoinedParticipant(reviewerId, request.roomId());
        ChatRoomParticipant revieweeParticipant = participantRepository.findByRoomAndCompanionJoinedTrueAndKickedFalse(room)
                .stream()
                .filter((participant) -> participant.getUser().getId().equals(request.revieweeId()))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "You can only review joined trip participants."));

        if (reviewerId.equals(request.revieweeId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot review yourself.");
        }

        if (reviewRepository.existsByPostIdAndReviewerIdAndRevieweeId(room.getPost().getId(), reviewerId, request.revieweeId())) {
            throw new ApiException(HttpStatus.CONFLICT, "You have already reviewed this participant.");
        }

        User reviewer = userRepository.getReferenceById(reviewerId);
        CompanionReview review = new CompanionReview(
                reviewer,
                revieweeParticipant.getUser(),
                room.getPost(),
                request.rating(),
                request.content().trim()
        );

        return ReviewResponse.from(reviewRepository.save(review));
    }

    private ChatRoom getEndedRoomWithJoinedParticipant(Long userId, Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Chat room not found."));
        User user = userRepository.getReferenceById(userId);
        ChatRoomParticipant participant = participantRepository.findByRoomAndUser(room, user)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "You cannot access this chat room."));

        if (participant.isKicked() || !participant.isCompanionJoined()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only joined trip participants can review.");
        }

        if (!room.isTripEnded()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You can review participants after the trip ends.");
        }

        return room;
    }
}

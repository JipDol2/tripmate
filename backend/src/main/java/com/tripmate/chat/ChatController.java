package com.tripmate.chat;

import com.tripmate.auth.CustomUserPrincipal;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chats")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/rooms")
    public List<ChatRoomResponse> rooms(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return chatService.rooms(principal.getUserId());
    }

    @PostMapping("/posts/{postId}/start")
    public ChatRoomResponse startPostChat(@AuthenticationPrincipal CustomUserPrincipal principal,
                                          @PathVariable Long postId) {
        return chatService.startPostChat(principal.getUserId(), postId);
    }

    @GetMapping("/rooms/{roomId}")
    public ChatRoomResponse room(@AuthenticationPrincipal CustomUserPrincipal principal,
                                 @PathVariable Long roomId) {
        return chatService.room(principal.getUserId(), roomId);
    }

    @PostMapping("/rooms/{roomId}/join")
    public ChatRoomResponse joinCompanion(@AuthenticationPrincipal CustomUserPrincipal principal,
                                          @PathVariable Long roomId) {
        return chatService.joinCompanion(principal.getUserId(), roomId);
    }

    @PatchMapping("/rooms/{roomId}/participants/{participantId}/kick")
    public ChatRoomResponse kickParticipant(@AuthenticationPrincipal CustomUserPrincipal principal,
                                            @PathVariable Long roomId,
                                            @PathVariable Long participantId) {
        return chatService.kickParticipant(principal.getUserId(), roomId, participantId);
    }

    @GetMapping("/rooms/{roomId}/messages")
    public List<ChatMessageResponse> messages(@AuthenticationPrincipal CustomUserPrincipal principal,
                                              @PathVariable Long roomId) {
        return chatService.messages(principal.getUserId(), roomId);
    }
}

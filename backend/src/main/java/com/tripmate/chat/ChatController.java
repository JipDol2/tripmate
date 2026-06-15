package com.tripmate.chat;

import com.tripmate.auth.CustomUserPrincipal;
import java.util.List;
import org.springframework.http.ResponseEntity;
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
    private final ChatRoomEventBroadcaster eventBroadcaster;

    public ChatController(ChatService chatService, ChatRoomEventBroadcaster eventBroadcaster) {
        this.chatService = chatService;
        this.eventBroadcaster = eventBroadcaster;
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

    @PatchMapping("/rooms/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(@AuthenticationPrincipal CustomUserPrincipal principal,
                                          @PathVariable Long roomId) {
        ChatRoomLeaveEvent event = chatService.leaveRoom(principal.getUserId(), roomId);
        eventBroadcaster.broadcast(roomId, event);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/rooms/{roomId}/read")
    public ResponseEntity<Void> markRoomAsRead(@AuthenticationPrincipal CustomUserPrincipal principal,
                                               @PathVariable Long roomId) {
        chatService.markRoomAsRead(principal.getUserId(), roomId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rooms/{roomId}/messages")
    public List<ChatMessageResponse> messages(@AuthenticationPrincipal CustomUserPrincipal principal,
                                              @PathVariable Long roomId) {
        return chatService.messages(principal.getUserId(), roomId);
    }
}

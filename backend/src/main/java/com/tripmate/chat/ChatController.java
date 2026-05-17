package com.tripmate.chat;

import com.tripmate.auth.CustomUserPrincipal;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/rooms/{roomId}/messages")
    public List<ChatMessageResponse> messages(@AuthenticationPrincipal CustomUserPrincipal principal,
                                              @PathVariable Long roomId) {
        return chatService.messages(principal.getUserId(), roomId);
    }
}

package com.example.bookstore.controller;

import com.example.bookstore.dto.ChatMessageDto;
import com.example.bookstore.entity.Message;
import com.example.bookstore.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }


    @MessageMapping("/chat.sendMessage")
    public void sendMessage(ChatMessageDto messageDto) {
        Message savedMessage = chatService.saveMessage(
                messageDto.getChatRoomId(),
                messageDto.getSenderId(),
                messageDto.getMessageText()
        );

        messagingTemplate.convertAndSend(
                "/topic/messages/" + messageDto.getChatRoomId(),
                savedMessage
        );
    }

    @GetMapping("/api/chat/history/{chatRoomId}")
    public ResponseEntity<List<Message>> getChatHistory(@PathVariable Long chatRoomId) {
        List<Message> history = chatService.getChatHistory(chatRoomId);
        return ResponseEntity.ok(history);
    }
}
package com.example.bookstore.service;

import com.example.bookstore.entity.ChatRoom;
import com.example.bookstore.entity.Message;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.MessageRepository;
import com.example.bookstore.repository.ChatRoomRepository;
import com.example.bookstore.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ChatService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    public ChatService(MessageRepository messageRepository, ChatRoomRepository chatRoomRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Message saveMessage(Long chatRoomId, Long senderId, String text) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Message message = new Message(chatRoom, sender, text);
        return messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<Message> getChatHistory(Long chatRoomId) {
        return messageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId);
    }
}
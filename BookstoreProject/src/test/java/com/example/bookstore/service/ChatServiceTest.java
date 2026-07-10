package com.example.bookstore.service;

import com.example.bookstore.entity.ChatRoom;
import com.example.bookstore.entity.Message;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.ChatRoomRepository;
import com.example.bookstore.repository.MessageRepository;
import com.example.bookstore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private UserRepository userRepository;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(
                messageRepository,
                chatRoomRepository,
                userRepository
        );
    }

    @Test
    void saveMessageShouldSaveAndReturnMessage() {
        ChatRoom chatRoom = new ChatRoom();
        User sender = new User();
        Message savedMessage = new Message(
                chatRoom,
                sender,
                "Hello"
        );

        when(chatRoomRepository.findById(1L))
                .thenReturn(Optional.of(chatRoom));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(sender));

        when(messageRepository.save(any(Message.class)))
                .thenReturn(savedMessage);

        Message result = chatService.saveMessage(
                1L,
                2L,
                "Hello"
        );

        assertSame(savedMessage, result);

        verify(chatRoomRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void saveMessageShouldCreateMessageWithCorrectValues() {
        ChatRoom chatRoom = new ChatRoom();
        User sender = new User();

        when(chatRoomRepository.findById(1L))
                .thenReturn(Optional.of(chatRoom));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(sender));

        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Message result = chatService.saveMessage(
                1L,
                2L,
                "Test message"
        );

        assertSame(chatRoom, result.getChatRoom());
        assertSame(sender, result.getSender());
        assertEquals("Test message", result.getMessageText());
    }

    @Test
    void saveMessageShouldThrowWhenChatRoomDoesNotExist() {
        when(chatRoomRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> chatService.saveMessage(
                        99L,
                        2L,
                        "Hello"
                )
        );

        assertEquals(
                "Chat room not found",
                exception.getMessage()
        );

        verify(chatRoomRepository).findById(99L);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(messageRepository);
    }

    @Test
    void saveMessageShouldThrowWhenSenderDoesNotExist() {
        ChatRoom chatRoom = new ChatRoom();

        when(chatRoomRepository.findById(1L))
                .thenReturn(Optional.of(chatRoom));

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> chatService.saveMessage(
                        1L,
                        99L,
                        "Hello"
                )
        );

        assertEquals(
                "Sender not found",
                exception.getMessage()
        );

        verify(chatRoomRepository).findById(1L);
        verify(userRepository).findById(99L);
        verifyNoInteractions(messageRepository);
    }

    @Test
    void getChatHistoryShouldReturnMessagesInRepositoryOrder() {
        Message first = mock(Message.class);
        Message second = mock(Message.class);

        List<Message> expected = List.of(first, second);

        when(messageRepository.findByChatRoomIdOrderBySentAtAsc(1L))
                .thenReturn(expected);

        List<Message> result = chatService.getChatHistory(1L);

        assertSame(expected, result);
        assertEquals(2, result.size());
        assertSame(first, result.get(0));
        assertSame(second, result.get(1));

        verify(messageRepository)
                .findByChatRoomIdOrderBySentAtAsc(1L);
    }

    @Test
    void getChatHistoryShouldReturnEmptyListWhenNoMessagesExist() {
        when(messageRepository.findByChatRoomIdOrderBySentAtAsc(1L))
                .thenReturn(List.of());

        List<Message> result = chatService.getChatHistory(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(messageRepository)
                .findByChatRoomIdOrderBySentAtAsc(1L);
    }
}
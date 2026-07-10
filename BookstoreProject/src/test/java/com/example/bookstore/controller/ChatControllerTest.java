package com.example.bookstore.controller;

import com.example.bookstore.dto.ChatMessageDto;
import com.example.bookstore.entity.Message;
import com.example.bookstore.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private ChatController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        controller = new ChatController(chatService, messagingTemplate);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void sendMessage_savesMessageAndBroadcastsToRoomTopic() {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setChatRoomId(42L);
        dto.setSenderId(7L);
        dto.setMessageText("Hello there");

        Message saved = new Message();
        when(chatService.saveMessage(42L, 7L, "Hello there")).thenReturn(saved);

        controller.sendMessage(dto);

        verify(chatService).saveMessage(42L, 7L, "Hello there");

        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());

        assertThat(destinationCaptor.getValue()).isEqualTo("/topic/messages/42");
        assertThat(payloadCaptor.getValue()).isSameAs(saved);
    }

    @Test
    void sendMessage_doesNotBroadcast_whenServiceThrows() {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setChatRoomId(5L);
        dto.setSenderId(2L);
        dto.setMessageText("Will fail");

        when(chatService.saveMessage(5L, 2L, "Will fail"))
                .thenThrow(new RuntimeException("chat room not found"));

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> controller.sendMessage(dto));

        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void sendMessage_passesThroughEmptyMessageText() {
        // Documents current behavior: the controller does not itself validate
        // blank message text; that's left to ChatService/DTO validation.
        ChatMessageDto dto = new ChatMessageDto();
        dto.setChatRoomId(1L);
        dto.setSenderId(1L);
        dto.setMessageText("");

        when(chatService.saveMessage(1L, 1L, "")).thenReturn(new Message());

        controller.sendMessage(dto);

        verify(chatService).saveMessage(1L, 1L, "");
    }

    @Test
    void getChatHistory_returnsMessagesForRoom() throws Exception {
        Message m1 = new Message();
        Message m2 = new Message();
        when(chatService.getChatHistory(42L)).thenReturn(List.of(m1, m2));

        mockMvc.perform(get("/api/chat/history/{chatRoomId}", 42L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(chatService).getChatHistory(42L);
    }

    @Test
    void getChatHistory_emptyRoom_returnsEmptyList() throws Exception {
        when(chatService.getChatHistory(7L)).thenReturn(List.of());

        mockMvc.perform(get("/api/chat/history/{chatRoomId}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getChatHistory_nonNumericId_returns400() throws Exception {
        mockMvc.perform(get("/api/chat/history/{chatRoomId}", "not-a-number"))
                .andExpect(status().isBadRequest());

        verify(chatService, never()).getChatHistory(anyLong());
    }

    @Test
    void getChatHistory_serviceThrows_propagatesAsServerError() throws Exception {
        when(chatService.getChatHistory(13L)).thenThrow(new RuntimeException("db error"));

        org.junit.jupiter.api.Assertions.assertThrows(jakarta.servlet.ServletException.class,
                () -> mockMvc.perform(get("/api/chat/history/{chatRoomId}", 13L)));
    }
}
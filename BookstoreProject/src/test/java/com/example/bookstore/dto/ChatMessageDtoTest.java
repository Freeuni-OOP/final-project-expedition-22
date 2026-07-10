package com.example.bookstore.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatMessageDtoTest {

    @Test
    void gettersAndSettersShouldWork() {
        ChatMessageDto dto = new ChatMessageDto();

        dto.setChatRoomId(10L);
        dto.setSenderId(5L);
        dto.setMessageText("Hello!");

        assertEquals(10L, dto.getChatRoomId());
        assertEquals(5L, dto.getSenderId());
        assertEquals("Hello!", dto.getMessageText());
    }
}
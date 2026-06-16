package com.example.bookstore.entity;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChatRoomTest {

    @Test
    void testConstructorAndDefaultValues() {
        User buyer = new User();
        User seller = new User();
        LocalDateTime now = LocalDateTime.now();
        ChatRoom chatRoom = new ChatRoom(buyer, seller, now);

        assertAll(
                () -> assertEquals(buyer, chatRoom.getBuyer(), "Buyer was not saved correctly"),
                () -> assertEquals(seller, chatRoom.getSender(), "seller was not saved correctly"),
                () -> assertEquals(now, chatRoom.getCreatedAt(), "Time was not saved correctly"),
                () -> assertNull(chatRoom.getId(), "new room should not have id before database"),
                () -> assertNull(chatRoom.getMessages(), "for new chat room list of messages should be null")
        );
    }

    @Test
    void testConstructorSharingReferences() {
        User sharedUser = new User();

        ChatRoom room1 = new ChatRoom(sharedUser, new User(), LocalDateTime.now());
        ChatRoom room2 = new ChatRoom(sharedUser, new User(), LocalDateTime.now());

        assertSame(room1.getBuyer(), room2.getBuyer(), "buyer users should be same");
    }

    @Test
    void testSettersAndDefaultConstructor(){
        ChatRoom chatRoom = new ChatRoom();
        User testBuyer = new User();
        User testSeller = new User();
        LocalDateTime testTime = LocalDateTime.now();
        Long testId = 7L;
        List<Message> testMessages = new ArrayList<>();
        testMessages.add(new Message());

        chatRoom.setId(testId);
        chatRoom.setBuyer(testBuyer);
        chatRoom.setSender(testSeller);
        chatRoom.setCreatedAt(testTime);
        chatRoom.setMessages(testMessages);

        assertEquals(testId, chatRoom.getId());
        assertEquals(testBuyer, chatRoom.getBuyer());
        assertEquals(testSeller, chatRoom.getSender());
        assertEquals(testTime, chatRoom.getCreatedAt());
        assertEquals(testMessages, chatRoom.getMessages());
        assertEquals(1, chatRoom.getMessages().size());
    }

    @Test
    void testGettersWithFullConstructor() {
        User buyer = new User();
        User seller = new User();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(2);
        ChatRoom chatRoom = new ChatRoom(buyer, seller, createdAt);

        assertAll(
                () -> assertEquals(buyer, chatRoom.getBuyer(), "Buyer's getter returned wrong buyer"),
                () -> assertEquals(seller, chatRoom.getSender(), "Sender's getter returned wrong seller"),
                () -> assertEquals(createdAt, chatRoom.getCreatedAt(), "CreatedAt's getter returned wrong time")
        );
    }

    @Test
    void testSetMessagesWithEmptyList() {
        ChatRoom chatRoom = new ChatRoom();
        List<Message> emptyMessagesList = new ArrayList<>();
        chatRoom.setMessages(emptyMessagesList);

        assertNotNull(chatRoom.getMessages(), "list should not be NUll");
        assertTrue(chatRoom.getMessages().isEmpty(), "List should be empty");
    }
}

package com.example.bookstore.repository;
import com.example.bookstore.entity.ChatRoom;
import com.example.bookstore.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;


import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ChatRoomRepositoryTest {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserRepository userRepository;

    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password");
        user.setPhoneNumber("123456789");
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Test
    void shouldSaveAndFindChatRoom() {

        User buyer = createUser("buyer", "buyer@test.com");
        User sender = createUser("seller", "seller@test.com");

        ChatRoom room = new ChatRoom();
        room.setBuyer(buyer);
        room.setSender(sender);
        room.setCreatedAt(LocalDateTime.now());

        ChatRoom saved = chatRoomRepository.save(room);

        Optional<ChatRoom> found = chatRoomRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void shouldDeleteChatRoom() {

        User buyer = createUser("buyer", "buyer@test.com");
        User sender = createUser("seller", "seller@test.com");

        ChatRoom room = new ChatRoom();
        room.setBuyer(buyer);
        room.setSender(sender);
        room.setCreatedAt(LocalDateTime.now());

        ChatRoom saved = chatRoomRepository.save(room);

        chatRoomRepository.delete(saved);

        assertFalse(chatRoomRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void shouldFindAllChatRooms() {

        User buyer1 = createUser("buyer1", "buyer1@test.com");
        User sender1 = createUser("seller1", "seller1@test.com");

        User buyer2 = createUser("buyer2", "buyer2@test.com");
        User sender2 = createUser("seller2", "seller2@test.com");

        chatRoomRepository.save(new ChatRoom(buyer1, sender1, LocalDateTime.now()));
        chatRoomRepository.save(new ChatRoom(buyer2, sender2, LocalDateTime.now()));

        assertEquals(2, chatRoomRepository.findAll().size());
    }
}
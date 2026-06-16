package com.example.bookstore.repository;
import com.example.bookstore.entity.ChatRoom;
import com.example.bookstore.entity.Message;
import com.example.bookstore.entity.User;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User savedUser;
    private ChatRoom savedRoom1;
    private ChatRoom savedRoom2;

    @BeforeEach
    void setUp() {
        User sender = new User();
        sender.setEmail("gmail@gmail.com");
        sender.setUsername("great username");
        sender.setPassword("passwordbutnothashed");
        sender.setPhoneNumber("222-111-453");
        savedUser=entityManager.persistAndFlush(sender);

        savedRoom1 = new ChatRoom(savedUser, savedUser, LocalDateTime.now());
        savedRoom2 = new ChatRoom(savedUser, savedUser, LocalDateTime.now());

        entityManager.persistAndFlush(savedRoom1);
        entityManager.persistAndFlush(savedRoom2);
    }

    @Test
    void testSaveAndFindMessage() {
        Message message = new Message(savedRoom1, savedUser, "Hello user");

        Message saved = messageRepository.save(message);

        assertNotNull(saved.getId());
        assertEquals("Hello user", saved.getMessageText());
    }

    @Test
    void testFindByChatRoomIdOrderBySentAtAsc() {
        LocalDateTime now = LocalDateTime.now();

        Message msg1_oldest = new Message(savedRoom1, savedUser, "first");
        msg1_oldest.setSentAt(now.minusMinutes(10));

        Message msg2_middle = new Message(savedRoom1, savedUser, "second");
        msg2_middle.setSentAt(now.minusMinutes(5));

        Message msg3_newest = new Message(savedRoom1, savedUser, "third");
        msg3_newest.setSentAt(now);

        entityManager.persist(msg3_newest);
        entityManager.persist(msg1_oldest);
        entityManager.persist(msg2_middle);
        entityManager.flush();
        List<Message> history = messageRepository.findByChatRoomIdOrderBySentAtAsc(savedRoom1.getId());

        assertEquals(3, history.size());
        assertEquals("first", history.get(0).getMessageText());
        assertEquals("second", history.get(1).getMessageText());
        assertEquals("third", history.get(2).getMessageText());
    }

    @Test
    void testFindByChatRoomIdWhenRoomIsEmpty() {
        List<Message> history = messageRepository.findByChatRoomIdOrderBySentAtAsc(savedRoom1.getId());

        assertNotNull(history, "list of messages should not be null");
        assertTrue(history.isEmpty(), "list of messages should be empty");
    }

    @Test
    void testFindByChatRoomIdWhenRoomDoesNotExist() {
        Long fakeId = 9999L;

        List<Message> history = messageRepository.findByChatRoomIdOrderBySentAtAsc(fakeId);

        assertNotNull(history);
        assertTrue(history.isEmpty(), "for non existing room there should be empty list returned");
    }

    @Test
    void testRoomIsolation() {
        Message msgRoom1_a = new Message(savedRoom1, savedUser, "room 1 - a");
        Message msgRoom1_b = new Message(savedRoom1, savedUser, "room 1 - b");
        Message msgRoom2_a = new Message(savedRoom2, savedUser, "room 2 - a");

        entityManager.persist(msgRoom1_a);
        entityManager.persist(msgRoom1_b);
        entityManager.persist(msgRoom2_a);
        entityManager.flush();

        List<Message> resRoom1 = messageRepository.findByChatRoomIdOrderBySentAtAsc(savedRoom1.getId());
        assertEquals(2, resRoom1.size());

        List<Message> resRoom2 = messageRepository.findByChatRoomIdOrderBySentAtAsc(savedRoom2.getId());
        assertEquals(1, resRoom2.size());
        assertEquals("room 2 - a", resRoom2.get(0).getMessageText());
    }

    @Test
    void testMessagesSentAtAlmostSameTime() {
        LocalDateTime time = LocalDateTime.now();

        Message msg1 = new Message(savedRoom1, savedUser, "real first");
        msg1.setSentAt(time);

        Message msg2 = new Message(savedRoom1, savedUser, "real second");
        msg2.setSentAt(time.plusSeconds(1));

        entityManager.persist(msg2);
        entityManager.persist(msg1);
        entityManager.flush();

        List<Message> history = messageRepository.findByChatRoomIdOrderBySentAtAsc(savedRoom1.getId());

        assertEquals(2, history.size());
        assertEquals("real first", history.get(0).getMessageText(), "Even its small time real first should be shown first");
    }

    @Test
    void testSaveInvalidMessageShouldThrowException() {
        Message badMessage = new Message(savedRoom1, savedUser, "   ");
        assertThrows(ConstraintViolationException.class, () -> {
            messageRepository.saveAndFlush(badMessage);
        }, "database should not save blank message");
    }

    @Test
    void testDeleteMessageById() {
        Message message = new Message(savedRoom1, savedUser, "this message should be deleted");
        Message saved = messageRepository.saveAndFlush(message);
        Long messageId = saved.getId();
        messageRepository.deleteById(messageId);
        messageRepository.flush();

        Optional<Message> deletedMessage = messageRepository.findById(messageId);
        assertTrue(deletedMessage.isEmpty(), "deleted message should not exist");
    }
}
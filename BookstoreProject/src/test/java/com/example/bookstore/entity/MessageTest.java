package com.example.bookstore.entity;

import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {

    @Test
    void testConstructorAndDefaultValues() {
        ChatRoom Room = new ChatRoom();
        User Sender = new User();
        String text = "Hi";
        Message message = new Message(Room, Sender, text);

        assertEquals(Room, message.getChatRoom());
        assertEquals(Sender, message.getSender());
        assertEquals(text, message.getMessageText());
        assertFalse(message.isRead(), "new message  should be false on default");
    }

    @Test
    void testConstructorWithEmptyAndBlankText() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Message emptyMessage = new Message(null, null, "");
        Set<ConstraintViolation<Message>> violations1 = validator.validate(emptyMessage);
        assertFalse(violations1.isEmpty(), "validation should catch empty text");

        Message blankMessage = new Message(null, null, "   ");
        Set<ConstraintViolation<Message>> violations2 = validator.validate(blankMessage);
        assertFalse(violations2.isEmpty(), "validation should catch blank text");
    }

    @Test
    void testConstructorSharingReferences() {
        ChatRoom sharedRoom = new ChatRoom();
        User sharedSender = new User();

        Message msg1 = new Message(sharedRoom, sharedSender, "მესიჯი 1");
        Message msg2 = new Message(sharedRoom, sharedSender, "მესიჯი 2");

        assertSame(msg1.getChatRoom(), msg2.getChatRoom());
        assertSame(msg1.getSender(), msg2.getSender());
    }


    @Test
    void testSettersAndDefaultConstructor() {
        Message message = new Message();

        ChatRoom Room = new ChatRoom();
        User Sender = new User();
        String Text = "message";
        LocalDateTime Time = LocalDateTime.now();
        Long Id = 1L;

        message.setId(Id);
        message.setChatRoom(Room);
        message.setSender(Sender);
        message.setMessageText(Text);
        message.setSentAt(Time);
        message.setRead(true);

        assertEquals(Id, message.getId());
        assertEquals(Room, message.getChatRoom());
        assertEquals(Sender, message.getSender());
        assertEquals(Text, message.getMessageText());
        assertEquals(Time, message.getSentAt());
        assertTrue(message.isRead());
    }

    @Test
    void testGettersWithFullConstructor() {
        ChatRoom testRoom = new ChatRoom();
        User testSender = new User();
        String testText = "hello";

        Message message = new Message(testRoom, testSender, testText);

        assertEquals(testRoom, message.getChatRoom());
        assertEquals(testSender, message.getSender());
        assertEquals(testText, message.getMessageText());
        assertNotNull(message.getSentAt());
        assertFalse(message.isRead());
    }
}
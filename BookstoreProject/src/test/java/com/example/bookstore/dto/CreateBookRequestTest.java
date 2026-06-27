package com.example.bookstore.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreateBookRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidBookRequest() {
        CreateBookRequest request = new CreateBookRequest(
                "book",
                "shota",
                "პოემა",
                1200,
                25.50,
                "ქართული პოემა.",
                "https://example.com/images/book.jpg"
        );

        Set<ConstraintViolation<CreateBookRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "There should not be error for valid arguments");
    }

    @Test
    void testBlankFieldsAndNullNumbers() {
        CreateBookRequest request = new CreateBookRequest("", "   ", "", null, null, "", "   ");

        Set<ConstraintViolation<CreateBookRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertEquals(8, violations.size(), "There should be error for all field");
    }

    @Test
    void testNegativePrice() {
        CreateBookRequest request = new CreateBookRequest(
                "title", "author", "genre", 2020,
                -5.0, "de...", "https://example.com/img.jpg"
        );
        Set<ConstraintViolation<CreateBookRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        boolean hasNegativePriceMessage = violations.stream().anyMatch(v ->
                v.getMessage().equals("წიგნის ფასი არ შეიძლება იყოს უარყოფითი"));

        assertTrue(hasNegativePriceMessage, "There should be error about negative price");
    }

    @Test
    void testInvalidReleaseYears() {
        CreateBookRequest tooOldRequest = new CreateBookRequest(
                "title", "author", "genre", 990,
                15.0, "d...", "https://example.com/img.jpg"
        );
        Set<ConstraintViolation<CreateBookRequest>> oldViolations = validator.validate(tooOldRequest);
        assertTrue(oldViolations.stream().anyMatch(v -> v.getMessage().equals("გამოშვების წელი არასწორია")));

        CreateBookRequest futureRequest = new CreateBookRequest(
                "title", "author", "genre", 2090,
                15.0, "descr...", "https://example.com/img.jpg"
        );
        Set<ConstraintViolation<CreateBookRequest>> futureViolations = validator.validate(futureRequest);
        assertTrue(futureViolations.stream().anyMatch(v -> v.getMessage().equals("გამოშვების წელი არ შეიძლება იყოს მომავალში")));
    }

    @Test
    void testInvalidImageUrlPattern() {
        CreateBookRequest request = new CreateBookRequest(
                "title", "author", "genre", 2020,
                15.0, "des...", "something"
        );

        Set<ConstraintViolation<CreateBookRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        boolean hasInvalidUrlMessage = violations.stream().anyMatch(v ->
                v.getMessage().equals("სურათის ლინკი არ არის ვალიდური URL"));

        assertTrue(hasInvalidUrlMessage, "There should be error message about URL");
    }

    @Test
    void testExceedingSizeLimits() {
        String longTitle = "A".repeat(101);

        CreateBookRequest request = new CreateBookRequest(
                longTitle, "author", "genre", 2020,
                15.0, "descr...", "https://example.com/img.jpg"
        );

        Set<ConstraintViolation<CreateBookRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        boolean hasLongTitleMessage = violations.stream().anyMatch(v ->
                v.getMessage().equals("წიგნის სათაური არ უნდა აღემატებოდეს 100 სიმბოლოს"));

        assertTrue(hasLongTitleMessage, "There should be error message about title length");
    }

    @Test
    void testExceedingAuthorSizeLimit() {
        String longAuthor = "A".repeat(51);

        CreateBookRequest request = new CreateBookRequest(
                "title", longAuthor, "genre", 2020,
                15.0, "desc...", "https://example.com/img.jpg"
        );

        Set<ConstraintViolation<CreateBookRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty(), "There should be error about author length");
        boolean hasLongAuthorMessage = violations.stream().anyMatch(v ->
                v.getMessage().equals("ავტორის სახელი არ უნდა აღემატებოდეს 50 სიმბოლოს"));

        assertTrue(hasLongAuthorMessage, "There should be error message about author length");
    }

    @Test
    void testExceedingDescriptionSizeLimit() {
        String longDescription = "D".repeat(1001);

        CreateBookRequest request = new CreateBookRequest(
                "title", "author", "genre", 2020,
                15.0, longDescription, "https://example.com/img.jpg"
        );

        Set<ConstraintViolation<CreateBookRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty(), "There should be error about description length");
        boolean hasLongDescMessage = violations.stream().anyMatch(v ->
                v.getMessage().equals("აღწერა არ უნდა აღემატებოდეს 1000 სიმბოლოს"));

        assertTrue(hasLongDescMessage, "There should be error message about description length");
    }

    @Test
    void testEmptyConstructorAndSetters() {
        CreateBookRequest request = new CreateBookRequest();

        assertNotNull(request, "object should be created");
        assertNull(request.getTitle());
        assertNull(request.getAuthor());
        assertNull(request.getPrice());
        assertNull(request.getDescription());
        assertNull(request.getGenre());
        assertNull(request.getImageUrl());
        assertNull(request.getReleaseYear());

        request.setTitle("title");
        request.setPrice(19.99);

        assertEquals("title", request.getTitle());
        assertEquals(19.99, request.getPrice());
    }

    @Test
    void testGettersAndSettersForAllFields() {
        CreateBookRequest request = new CreateBookRequest();

        request.setTitle("დიდოსტატის მარჯვენა");
        request.setAuthor("კონსტანტინე გამსახურდია");
        request.setGenre("ისტორიული რომანი");
        request.setReleaseYear(1939);
        request.setPrice(18.50);
        request.setDescription("ისტორიული რომანი რომელიც...");
        request.setImageUrl("https://example.com/didostati.jpg");

        assertEquals("დიდოსტატის მარჯვენა", request.getTitle(), "Title's setter or getter is wrong");
        assertEquals("კონსტანტინე გამსახურდია", request.getAuthor(), "Author's setter or getter is wrong");
        assertEquals("ისტორიული რომანი", request.getGenre(), "Genre's setter or getter is wrong");
        assertEquals(1939, request.getReleaseYear(), "ReleaseYear's setter or getter is wrong");
        assertEquals(18.50, request.getPrice(), "Price's setter or getter is wrong");
        assertEquals("ისტორიული რომანი რომელიც...", request.getDescription(), "Description's setter or getter is wrong");
        assertEquals("https://example.com/didostati.jpg", request.getImageUrl(), "ImageUrl's setter or getter is wrong");
    }
}


import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookTest {

    @Test
    void testGettersAndSetters() {
        User mockSeller = new User();
        BigDecimal price = new BigDecimal("24.99");

        Book book = new Book("Design Patterns", price, mockSeller, true);
        book.setReleaseYear(1994);
        book.setDescription("Elements of Reusable Object-Oriented Software");
        book.setImageUrl("http://example.com/design_patterns.jpg");

        assertEquals("Design Patterns", book.getTitle());
        assertEquals(price, book.getPrice());
        assertEquals(mockSeller, book.getSeller());
        assertTrue(book.getIsAvailable());
        assertEquals(1994, book.getReleaseYear());
        assertEquals("Elements of Reusable Object-Oriented Software", book.getDescription());
        assertEquals("http://example.com/design_patterns.jpg", book.getImageUrl());
    }

    @Test
    void testDefaultConstructor() {
        Book book = new Book();
        book.setTitle("Refactoring");

        BigDecimal price = new BigDecimal("45.00");
        book.setPrice(price);

        LocalDateTime now = LocalDateTime.now();
        book.setCreatedAt(now);

        assertNull(book.getId());
        assertEquals("Refactoring" , book.getTitle());
        assertEquals(price, book.getPrice());
        assertEquals(now, book.getCreatedAt());
        assertTrue(book.getIsAvailable());
    }

    @Test
    void testEqualsAndHashCode() {
        Book book1 = new Book();
        Book book2 = new Book();

        assertEquals(book1, book1);

        assertNotEquals(book1, book2);

        assertEquals(book1.hashCode(), book1.hashCode());
        assertEquals(book1.hashCode(), book2.hashCode());
    }

    @Test
    void testEqualsWithSameId() throws Exception {
        Book book1 = new Book();
        Book book2 = new Book();

        java.lang.reflect.Field idField = Book.class.getDeclaredField("id");
        idField.setAccessible(true);

        idField.set(book1, 1L);
        idField.set(book2, 1L);

        assertEquals(book1, book2);
    }

    @Test
    void testToString() {
        User mockSeller = new User();
        Book book = new Book("Java", new BigDecimal("40.00"), mockSeller, true);
        book.setReleaseYear(2022);

        String result = book.toString();

        assertTrue(result.contains("Java"));
        assertTrue(result.contains("2022"));

        assertFalse(result.contains("seller="));
    }

    @Test
    void moreSetTests(){
        User Seller = new User();
        Book book = new  Book("Java", new BigDecimal("220.30"), null, true);
        book.setSeller(Seller);
        book.setReleaseYear(2018);

        book.setIsAvailable(true);
        assertTrue(book.getIsAvailable());

        book.setIsAvailable(false);
        assertFalse(book.getIsAvailable());

        assertEquals(Seller , book.getSeller());
    }

    @Test
    void testOnCreateSetsCreatedAt() throws Exception {
        Book book = new Book();

        assertNull(book.getCreatedAt());

        Method method = Book.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(book);

        assertNotNull(book.getCreatedAt());
    }

}

import com.example.bookstore.entity.Author;
import com.example.bookstore.entity.Book;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuthorTest {

    @Test
    void testConstructorAndGetters() {
        Author author = new Author("J.K. Rowling");

        assertEquals("J.K. Rowling", author.getName());
        assertNotNull(author.getBooks());
        assertTrue(author.getBooks().isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        Author author = new Author();
        author.setName("George R.R. Martin");

        Set<Book> books = new HashSet<>();
        Book book = new Book();
        books.add(book);
        author.setBooks(books);

        assertEquals("George R.R. Martin", author.getName());
        assertEquals(1, author.getBooks().size());
        assertTrue(author.getBooks().contains(book));
    }

    @Test
    void testEqualsAndHashCode() throws Exception {
        Author author1 = new Author("Stephen King");
        Author author2 = new Author("Stephen King");

        java.lang.reflect.Field idField = Author.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(author1, 100L);
        idField.set(author2, 100L);

        Author author3 = new Author("Brandon Sanderson");
        idField.set(author3, 200L);

        assertEquals(author1, author2);
        assertNotEquals(author1, author3);
        assertEquals(author1.hashCode(), author2.hashCode());
    }

    @Test
    void testToString() {
        Author author = new Author("J.R.R. Tolkien");
        String toStringOutput = author.toString();

        assertTrue(toStringOutput.contains("J.R.R. Tolkien"));
        assertFalse(toStringOutput.contains("books="));
    }

    @Test
    void testEqualsWithNull() {
        Author author = new Author("Stephen King");

        assertNotEquals(null, author);
    }
}
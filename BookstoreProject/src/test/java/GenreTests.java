
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Genre;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GenreTest {

    @Test
    void testConstructorAndGetters() {
        Genre genre = new Genre("Sci-Fi");

        assertEquals("Sci-Fi", genre.getName());
        assertNotNull(genre.getBooks());
        assertTrue(genre.getBooks().isEmpty());
    }

    @Test
    void testDefaultConstructor() {
        Genre genre = new Genre();

        assertNotNull(genre.getBooks());
        assertTrue(genre.getBooks().isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        Genre genre = new Genre();
        genre.setName("Fantasy");

        Set<Book> books = new HashSet<>();
        Book book = new Book();
        books.add(book);
        genre.setBooks(books);

        assertEquals("Fantasy", genre.getName());
        assertEquals(1, genre.getBooks().size());
        assertTrue(genre.getBooks().contains(book));
    }


    @Test
    void testToString() {
        Genre genre = new Genre("Mystery");
        String toStringOutput = genre.toString();

        assertTrue(toStringOutput.contains("Mystery"));
        assertFalse(toStringOutput.contains("books=") );
    }

    @Test
    void testEqualsWithSameId() throws Exception {
        Genre genre1 = new Genre("Fantasy");
        Genre genre2 = new Genre("Sci-Fi");

        Field idField = Genre.class.getDeclaredField("id");
        idField.setAccessible(true);

        idField.set(genre1, 1L);
        idField.set(genre2, 1L);

        assertEquals(genre1, genre2);
    }

    @Test
    void testEqualsEdgeCases() {
        Genre genre = new Genre("Fantasy");

        assertNotEquals(null, genre);
        assertNotEquals("Fantasy", genre);
    }

    @Test
    void testHashCode() {
        Genre genre = new Genre("Fantasy");

        int hash1 = genre.hashCode();
        int hash2 = genre.hashCode();

        assertEquals(hash1, hash2);
    }
}
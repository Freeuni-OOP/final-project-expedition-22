
import com.example.bookstore.entity.Author;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Genre;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class BookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    private User seller;
    private Genre fiction;
    private Genre nonFiction;
    private Author author1;
    private Author author2;

    @BeforeEach
    void setUp() {
        seller = new User();
        seller.setUsername("john_doe");
        seller.setEmail("john@example.com");
        seller.setPassword("password123");
        entityManager.persist(seller);

        fiction = new Genre("Fiction");
        nonFiction = new Genre("Non-Fiction");
        entityManager.persist(fiction);
        entityManager.persist(nonFiction);

        author1 = new Author("George Orwell");
        author2 = new Author("Jane Austen");
        entityManager.persist(author1);
        entityManager.persist(author2);

        entityManager.flush();
    }


    private Book createBook(String title, BigDecimal price, boolean available) {
        Book book = new Book(title, price, seller, available);
        return entityManager.persist(book);
    }


    @Test
    void saveTests() {
        Book book = new Book("1984", new BigDecimal("9.99"), seller, true);
        book.setDescription("A dystopian novel");
        book.setReleaseYear(1949);

        Book saved = bookRepository.save(book);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("1984");
        assertThat(saved.getPrice()).isEqualByComparingTo("9.99");
        assertThat(saved.getIsAvailable()).isTrue();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void findById_returnsBook_whenExists() {
        Book book = createBook("Pride and Prejudice", new BigDecimal("7.50"), true);
        entityManager.flush();

        Optional<Book> found = bookRepository.findById(book.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Pride and Prejudice");
    }

    @Test
    void findByIdTest() {
        Optional<Book> found = bookRepository.findById(999L);
        assertThat(found).isEmpty();
    }


    @Test
    void findByIsAvailableTrue1() {
        createBook("Available Book 1", new BigDecimal("10.00"), true);
        createBook("Available Book 2", new BigDecimal("15.00"), true);
        createBook("Sold Book",        new BigDecimal("12.00"), false);
        entityManager.flush();

        List<Book> available = bookRepository.findByIsAvailableTrue();

        assertThat(available).hasSize(2);
        assertThat(available).extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Available Book 1", "Available Book 2");
    }

    @Test
    void findByIsAvailableTrueTest2() {
        createBook("Sold Book 1", new BigDecimal("10.00"), false);
        createBook("Sold Book 2", new BigDecimal("20.00"), false);
        entityManager.flush();

        List<Book> available = bookRepository.findByIsAvailableTrue();

        assertThat(available).isEmpty();
    }

    @Test
    void findByIsAvailableTrue3() {
        List<Book> available = bookRepository.findByIsAvailableTrue();
        assertThat(available).isEmpty();
    }


    @Test
    void findAllTest() {
        createBook("Book A", new BigDecimal("5.00"), true);
        createBook("Book B", new BigDecimal("8.00"), false);
        entityManager.flush();

        List<Book> all = bookRepository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    void save2() {
        Book book = createBook("Old Title", new BigDecimal("5.00"), true);
        entityManager.flush();

        book.setTitle("New Title");
        book.setPrice(new BigDecimal("12.99"));
        bookRepository.save(book);
        entityManager.flush();
        entityManager.clear();

        Book updated = entityManager.find(Book.class, book.getId());
        assertThat(updated.getTitle()).isEqualTo("New Title");
        assertThat(updated.getPrice()).isEqualByComparingTo("12.99");
    }

    @Test
    void markUnavailableTest() {
        Book book = createBook("My Book", new BigDecimal("9.00"), true);
        entityManager.flush();

        book.setIsAvailable(false);
        bookRepository.save(book);
        entityManager.flush();
        entityManager.clear();

        Book updated = entityManager.find(Book.class, book.getId());
        assertThat(updated.getIsAvailable()).isFalse();

        List<Book> available = bookRepository.findByIsAvailableTrue();
        assertThat(available).extracting(Book::getId).doesNotContain(book.getId());
    }


    @Test
    void deleteByIdTest() {
        Book book = createBook("To Delete", new BigDecimal("3.00"), true);
        entityManager.flush();
        Long id = book.getId();

        bookRepository.deleteById(id);
        entityManager.flush();

        assertThat(bookRepository.findById(id)).isEmpty();
    }

    @Test
    void deleteByIdEdgeCaseTest() {
        Book keep   = createBook("Keep Me",   new BigDecimal("6.00"), true);
        Book remove = createBook("Remove Me", new BigDecimal("6.00"), true);
        entityManager.flush();

        bookRepository.deleteById(remove.getId());
        entityManager.flush();

        assertThat(bookRepository.findById(keep.getId())).isPresent();
        assertThat(bookRepository.count()).isEqualTo(1);
    }



    @Test
    void savePersistsBookWithGenres() {
        Book book = new Book("Brave New World", new BigDecimal("8.00"), seller, true);
        book.setGenres(Set.of(fiction));
        bookRepository.save(book);
        entityManager.flush();
        entityManager.clear();

        Book found = entityManager.find(Book.class, book.getId());
        assertThat(found.getGenres()).extracting(Genre::getName).containsExactly("Fiction");
    }

    @Test
    void savePersistsBookWithMultipleGenres() {
        Book book = new Book("Multi-Genre Book", new BigDecimal("11.00"), seller, true);
        book.setGenres(Set.of(fiction, nonFiction));
        bookRepository.save(book);
        entityManager.flush();
        entityManager.clear();

        Book found = entityManager.find(Book.class, book.getId());
        assertThat(found.getGenres()).hasSize(2);
    }


    @Test
    void savePersistsBookWithAuthor() {
        Book book = new Book("Animal Farm", new BigDecimal("6.50"), seller, true);
        book.setAuthors(Set.of(author1));
        bookRepository.save(book);
        entityManager.flush();
        entityManager.clear();

        Book found = entityManager.find(Book.class, book.getId());
        assertThat(found.getAuthors()).extracting(Author::getName).containsExactly("George Orwell");
    }

    @Test
    void savePersistsBookWithMultipleAuthors() {
        Book book = new Book("Co-authored Book", new BigDecimal("14.00"), seller, true);
        book.setAuthors(Set.of(author1, author2));
        bookRepository.save(book);
        entityManager.flush();
        entityManager.clear();

        Book found = entityManager.find(Book.class, book.getId());
        assertThat(found.getAuthors()).hasSize(2);
    }


    @Test
    void saveLinksSellerTest() {
        Book book = createBook("Seller's Book", new BigDecimal("20.00"), true);
        entityManager.flush();
        entityManager.clear();

        Book found = entityManager.find(Book.class, book.getId());
        assertThat(found.getSeller().getId()).isEqualTo(seller.getId());
        assertThat(found.getSeller().getUsername()).isEqualTo("john_doe");
    }

    @Test
    void saveSetsCreatedAutomaticallyTest() {
        Book book = new Book("Auto Date Book", new BigDecimal("5.00"), seller, true);
        bookRepository.save(book);
        entityManager.flush();

        assertThat(book.getCreatedAt()).isNotNull();
    }

    @Test
    void count_reflectsNumberOfPersistedBooks() {
        assertEquals(0, bookRepository.count());

        createBook("Book 1", new BigDecimal("1.00"), true);
        createBook("Book 2", new BigDecimal("2.00"), true);
        entityManager.flush();

        assertEquals(2, bookRepository.count());
    }

    @Test
    void existsByIdReturnsTrue() {
        Book book = createBook("Exists Book", new BigDecimal("3.00"), true);
        entityManager.flush();

        assertThat(bookRepository.existsById(book.getId())).isTrue();
    }

    @Test
    void existsByIdMissingBook() {
        assertThat(bookRepository.existsById(999L)).isFalse();
    }
}
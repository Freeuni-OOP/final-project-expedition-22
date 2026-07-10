package com.example.bookstore.repository;

import com.example.bookstore.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByIsAvailableTrue();
    List<Book> findByGenres_NameIgnoreCase(String genreName);

    List<Book> findByAuthors_NameIgnoreCase(String authorName);
    List<Book> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT b FROM User u JOIN u.favouriteBooks b WHERE u.id = :userId")
    List<Book> findFavoriteBooksByUserId(@Param("userId") Long userId);

    List<Book> findBySeller_Id(Long userId);

    List<Book> findByAuthors_NameContainingIgnoreCase(String author);

    List<Book> findByGenres_NameContainingIgnoreCase(String genre);

    List<Book> findByReleaseYear(Integer releaseYear);

    List<Book> findAllByOrderByPriceAsc();
    List<Book> findAllByOrderByPriceDesc();

    List<Book> findAllByOrderByCreatedAtDesc();
    List<Book> findAllByOrderByCreatedAtAsc();

    List<Book> findAllByOrderByReleaseYearDesc();

    @Query("""
    SELECT DISTINCT b
    FROM Book b
    LEFT JOIN FETCH b.authors
    LEFT JOIN FETCH b.genres
    WHERE b.seller.id = :userId
""")
    List<Book> findBySellerIdWithAuthorsAndGenres(@Param("userId") Long userId);

    @Query("""
    SELECT DISTINCT b
    FROM User u
    JOIN u.favouriteBooks b
    LEFT JOIN FETCH b.authors
    LEFT JOIN FETCH b.genres
    WHERE u.id = :userId
""")
    List<Book> findFavoriteBooksByUserIdWithAuthorsAndGenres(@Param("userId") Long userId);
    List<Book> findAllByOrderByReleaseYearAsc();

}

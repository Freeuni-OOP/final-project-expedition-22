package com.example.bookstore.repository;

import com.example.bookstore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.favouriteBooks WHERE u.username = :username")
    Optional<User> findByUsernameWithFavorites(@Param("username") String username);
    
  @Modifying
    @Query(value = "DELETE FROM user_favorites WHERE book_id = :bookId", nativeQuery = true)
    void deleteFavoriteReferencesByBookId(@Param("bookId") Long bookId);
}

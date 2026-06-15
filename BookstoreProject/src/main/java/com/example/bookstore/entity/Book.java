package com.example.bookstore.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "books")
public class Book {

    //temp initialization for entry tests

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Book() {
    }

    public Long getId() {
        return id;
    }
}

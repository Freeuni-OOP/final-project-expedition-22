package com.example.bookstore.dto;

import com.example.bookstore.entity.Author;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Genre;

public class BookResponse {

    private Long id;
    private String title;
    private String author;
    private Double price;
    private String genre;
    private Integer releaseYear;
    private String description;
    private String imageUrl;
    private boolean isFavorite;

    public BookResponse() {}

    public BookResponse(Long id, String title, String author, String genre, Integer releaseYear,
                        Double price, String description, String imageUrl) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.releaseYear = releaseYear;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public BookResponse(Book book) {
        this.id = book.getId();
        this.title = book.getTitle();
        this.releaseYear = book.getReleaseYear();
        this.price = book.getPrice().doubleValue();
        this.description = book.getDescription();
        this.imageUrl = book.getImageUrl();

        this.author = book.getAuthors().stream()
                .map(Author::getName)
                .findFirst()
                .orElse("");

        this.genre = book.getGenres().stream()
                .map(Genre::getName)
                .findFirst()
                .orElse("");
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite;}
}
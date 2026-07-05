package com.example.bookstore.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

public class CreateBookRequest {
    @NotBlank(message = "გთხოვთ, შეიყვანოთ წიგნის სათაური")
    @Size(max = 100, message = "წიგნის სათაური არ უნდა აღემატებოდეს 100 სიმბოლოს")
    private String title;

    @NotBlank(message = "გთხოვთ, შეიყვანოთ ავტორის სახელი")
    @Size(max = 50, message = "ავტორის სახელი არ უნდა აღემატებოდეს 50 სიმბოლოს")
    private String author;

    @NotNull(message = "გთხოვთ, შეიყვანოთ წიგნის ფასი")
    @Min(value = 0, message = "წიგნის ფასი არ შეიძლება იყოს უარყოფითი")
    private Double price;

    @NotBlank(message = "გთხოვთ, მიუთითოთ წიგნის ჟანრი")
    private String genre;

    @NotNull(message = "გთხოვთ, შეიყვანოთ გამოშვების წელი")
    @Min(value = 1000, message = "გამოშვების წელი არასწორია")
    @Max(value = 2026, message = "გამოშვების წელი არ შეიძლება იყოს მომავალში")
    private Integer releaseYear;

    @NotBlank(message = "გთხოვთ, შეიყვანოთ წიგნის აღწერა")
    @Size(max = 1000, message = "აღწერა არ უნდა აღემატებოდეს 1000 სიმბოლოს")
    private String description;


    private String imageUrl;

    public CreateBookRequest() { }

    public CreateBookRequest(String title, String author, String genre,
                             Integer releaseYear, Double price, String description, String imageUrl) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.releaseYear = releaseYear;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
    }

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

}
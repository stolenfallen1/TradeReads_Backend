package com.tradereads.dto;

import jakarta.validation.constraints.NotBlank;

public class BookCreationRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    private String isbn;
    private String genre;
    private String condition;
    private String description;
    private String status;

    public BookCreationRequest() {}

    public BookCreationRequest(String title, String author, String isbn, String genre, String condition, String description, String status) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.genre = genre;
        this.condition = condition;
        this.description = description;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getGenre() {
        return genre;
    }
    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getCondition() {
        return condition;
    }
    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}

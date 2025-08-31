package com.tradereads.dto;

import com.tradereads.model.Book.BookStatus;
import com.tradereads.model.Book.ListingType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BookCreationRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    private String isbn;
    private String genre;
    private String condition;
    private String description;
    private BookStatus status;

    @NotNull(message = "Listing type is required")
    private ListingType listingType;

    public BookCreationRequest() {}

    public BookCreationRequest(String title, String author, String isbn, String genre, String condition, String description, BookStatus status, ListingType listingType) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.genre = genre;
        this.condition = condition;
        this.description = description;
        this.status = status;
        this.listingType = listingType;
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

    public BookStatus getStatus() {
        return status;
    }
    public void setStatus(BookStatus status) {
        this.status = status;
    }

    public ListingType getListingType() {
        return listingType;
    }
    public void setListingType(ListingType listingType) {
        this.listingType = listingType;
    }
}

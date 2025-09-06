package com.tradereads.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "books",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"isbn", "user_id"}) // A User cannot have two books with the same ISBN
    }
)
public class Book {

    public enum BookStatus {
        AVAILABLE,
        TRADED,
        PENDING, // Pending for trade
        GAVEDAWAY, 
        // SOLD, - SOON TO ADD FEATURE
    }

    public enum ListingType {
        GIVEAWAY,
        TRADE,
        // SELL, - SOON TO ADD FEATURE
        // BUY, - SOON TO ADD FEATURE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String genre;
    private String condition; // Book's physical condition
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingType listingType;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "user_role", "email", "phoneNumber"})
    private User owner;

    public Book() {}

    public Book(String title, String author, String isbn, String genre, String condition, String description, BookStatus status, ListingType listingType, User owner) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.genre = genre;
        this.condition = condition;
        this.description = description;
        this.status = status;
        this.listingType = listingType;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
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

    public User getOwner() {
        return owner;
    }
    public void setOwner(User owner) {
        this.owner = owner;
    }
}

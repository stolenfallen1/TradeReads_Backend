package com.tradereads.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "trade_requests")
public class TradeRequest {

    public enum TradeStatus {
        PENDING,
        ACCEPTED,
        DECLINED,
        CANCELLED,
        COMPLETED,
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    @NotNull
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @NotNull
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_book_id", nullable = false)
    @NotNull
    private Book requestedBook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offered_book_id")
    private Book offeredBook;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private TradeStatus status = TradeStatus.PENDING;

    @Size(max = 500)
    private String message;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column 
    private LocalDateTime updatedAt;

    public TradeRequest() {}

    public TradeRequest(User requester, User owner, Book requestedBook, Book offeredBook, String message) {
        this.requester = requester;
        this.owner = owner;
        this.requestedBook = requestedBook;
        this.offeredBook = offeredBook;
        this.message = message;
        this.status = TradeStatus.PENDING;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public User getRequester() {
        return requester;
    }
    public void setRequester(User requester) {
        this.requester = requester;
    }

    public User getOwner() {
        return owner;
    }
    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Book getRequestedBook() {
        return requestedBook;
    }
    public void setRequestedBook(Book requestedBook) {
        this.requestedBook = requestedBook;
    }

    public Book getOfferedBook() {
        return offeredBook;
    }
    public void setOfferedBook(Book offeredBook) {
        this.offeredBook = offeredBook;
    }

    public TradeStatus getStatus() {
        return status;
    }
    public void setStatus(TradeStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

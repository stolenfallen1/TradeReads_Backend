package com.tradereads.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tradereads.model.Book;
import com.tradereads.repository.BookRepository;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public Optional<Book> getBookByIdAndUserId(Long bookId, Long userId) {
        return bookRepository.findByIdAndOwnerId(bookId, userId);
    }

    public boolean userAlreadyHasThisBook(String isbn, Long ownerId) {
        return bookRepository.existsByIsbnAndOwnerId(isbn, ownerId);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book updateBook(Book book) {
        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    public List<Book> getBooksByUserId(Long userId) {
        return bookRepository.findByOwnerId(userId);
    }

    public List<Book> getBooksByUserIdAndStatus(Long userId, Book.BookStatus status) {
        return bookRepository.findByOwnerIdAndStatus(userId, status);
    }

    public List<Book> getBooksByGenre(String genre) {
        return bookRepository.findByGenre(genre);
    }

    public List<Book> getBooksByStatus(Book.BookStatus status) {
        return bookRepository.findByStatus(status);
    }

    public List<Book> getAvailableBooksExcludingUser(Long excludUserId, Book.BookStatus status) {
        return bookRepository.findByStatusAndOwnerIdNot(status, excludUserId);
    }

    public List<Book> getBooksByListingType(Book.ListingType listingType) {
        return bookRepository.findByListingType(listingType);
    }

    public List<Book> getBooksByStatusAndListingType(Book.BookStatus status, Book.ListingType listingType) {
        return bookRepository.findByStatusAndListingType(status, listingType);
    }

    public List<Book> getBooksByUserIdAndListingType(Long userId, Book.ListingType listingType) {
        return bookRepository.findByOwnerIdAndListingType(userId, listingType);
    }

    public List<Book> getBooksByUserIdAndStatusAndListingType(Long userId, Book.BookStatus status, Book.ListingType listingType) {
        return bookRepository.findByOwnerIdAndStatusAndListingType(userId, status, listingType);
    }
}

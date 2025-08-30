package com.tradereads.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tradereads.model.Book;
import com.tradereads.model.User;
import com.tradereads.repository.BookRepository;
import com.tradereads.repository.UserRepository;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public BookService(BookRepository bookRepository, UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
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

    public List<Book> getBooksByUserIdAndStatus(Long userId, String status) {
        return bookRepository.findByOwnerIdAndStatus(userId, status);
    }

    public List<Book> getBooksByGenre(String genre) {
        return bookRepository.findByGenre(genre);
    }

    public List<Book> getBooksByStatus(String status) {
        return bookRepository.findByStatus(status);
    }

    public List<Book> getAvailableBooksExcludingUser(Long excludUserId, String status) {
        return bookRepository.findByStatusAndOwnerIdNot(status, excludUserId);
    }

    // Helper methods to create a book with owner
    public Book createBookWithOwner(Book book, Long ownerId) {
        Optional<User> owner = userRepository.findById(ownerId);
        if (owner.isPresent()) {
            book.setOwner(owner.get());
            return bookRepository.save(book);
        }
        throw new IllegalArgumentException("User not found with ID: " + ownerId);
    }

    // Helper method to check if user owns the book
    public boolean isBookOwnedByUser(Long bookId, Long userId) {
        Optional<Book> book = bookRepository.findById(bookId);
        return book.isPresent() && book.get().getOwner().getId().equals(userId);
    }
}

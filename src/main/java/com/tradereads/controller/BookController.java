package com.tradereads.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tradereads.dto.BookCreationRequest;
import com.tradereads.model.Book;
import com.tradereads.model.User;
import com.tradereads.service.BookService;
import com.tradereads.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;
    private final UserService userService;

    public BookController(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> createBook(@Valid @RequestBody BookCreationRequest request) {
        try {
            User owner = userService.getUserById(request.getUserId()).orElseThrow(() -> new IllegalArgumentException("User not found"));
            Book book = new Book(
                request.getTitle(),
                request.getAuthor(),
                request.getIsbn(),
                request.getGenre(),
                request.getCondition(),
                request.getDescription(),
                request.getStatus(),
                owner
            );

            Book savedBook = bookService.saveBook(book);
            return ResponseEntity.ok(savedBook);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch(Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks(
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String genre
    ) {
        try {
            List<Book> books; 

            if (userId != null && status != null) {
                books = bookService.getBooksByUserIdAndStatus(userId, status);
            } else if (userId != null) {
                books = bookService.getBooksByUserId(userId);
            } else if (status != null) {
                books = bookService.getBooksByStatus(status);
            } else if (genre != null) {
                books = bookService.getBooksByGenre(genre);
            } else {
                books = bookService.getAllBooks();
            }

            return ResponseEntity.ok(books);
        } catch(Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getBooksByUserId(@PathVariable Long userId) {
        try {
            if (!userService.getUserById(userId).isPresent()) {
                return ResponseEntity.notFound().build();
            }

            List<Book> books = bookService.getBooksByUserId(userId);
            return ResponseEntity.ok(books);
        } catch(Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/available")
    public ResponseEntity<List<Book>> getAvailableBooks(@RequestParam(required = false) Long excludeUserId) {
        try {
            List<Book> books;
            if (excludeUserId != null) {
                books = bookService.getAvailableBooksExcludingUser(excludeUserId, "Available");
            } else {
                books = bookService.getBooksByStatus("Available");
            }
            return ResponseEntity.ok(books);
        } catch(Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @RequestBody Book bookUpdate) {
        try {
            Book existingBook = bookService.getBookById(id).orElseThrow(() -> new IllegalArgumentException("Book not found"));
            existingBook.setTitle(bookUpdate.getTitle());
            existingBook.setAuthor(bookUpdate.getAuthor());
            existingBook.setIsbn(bookUpdate.getIsbn());
            existingBook.setGenre(bookUpdate.getGenre());
            existingBook.setCondition(bookUpdate.getCondition());
            existingBook.setDescription(bookUpdate.getDescription());
            existingBook.setStatus(bookUpdate.getStatus());

            Book updatedBook = bookService.updateBook(existingBook);
            return ResponseEntity.ok(updatedBook);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch(Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }  

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        try {
            if (!bookService.getBookById(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            bookService.deleteBook(id);
            return ResponseEntity.ok(Map.of("message", "Book deleted successfully"));
        } catch(Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }
}

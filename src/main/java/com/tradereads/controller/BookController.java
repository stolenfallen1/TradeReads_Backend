package com.tradereads.controller;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tradereads.components.AuthUtil;
import com.tradereads.dto.BookCreationRequest;
import com.tradereads.model.Book;
import com.tradereads.model.User;
import com.tradereads.model.Book.BookStatus;
import com.tradereads.model.Book.ListingType;
import com.tradereads.service.BookService;
import com.tradereads.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;
    private final UserService userService;
    private final AuthUtil authUtil;

    public BookController(BookService bookService, UserService userService, AuthUtil authUtil) {
        this.bookService = bookService;
        this.userService = userService;
        this.authUtil = authUtil;
    }

    // ========= USER-OWNED BOOKS (PROTECTED ENDPOINTS) =========

    @GetMapping("/my-books")
    public ResponseEntity<?> getMyBooks(
        @RequestParam(required = false) String listingType,
        @RequestParam(required = false) String status
    ) {
        try {
            Long currentUserId = authUtil.getCurrentUserId();
            List<Book> books;

            ListingType type = null;
            BookStatus bookStatus = null;

            if (listingType != null && !listingType.isBlank()) {
                try {
                    type = ListingType.valueOf(listingType.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid listing type"));
                }
            }
            
            if (status != null && !status.isBlank()) {
                try {
                    bookStatus = BookStatus.valueOf(status.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid status"));
                }
            }

            if (type != null && bookStatus != null) {
                books = bookService.getBooksByUserIdAndStatusAndListingType(currentUserId, bookStatus, type);
            } else if (type != null) {
                books = bookService.getBooksByUserIdAndListingType(currentUserId, type);
            } else if (bookStatus != null) {
                books = bookService.getBooksByUserIdAndStatus(currentUserId, bookStatus);
            } else {
                books = bookService.getBooksByUserId(currentUserId);
            }

            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/my-books")
    public ResponseEntity<?> createMyBook(@Valid @RequestBody BookCreationRequest request) {
        try {
            Long currentUserId = authUtil.getCurrentUserId();
            User owner = userService.getUserById(currentUserId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (bookService.userAlreadyHasThisBook(request.getIsbn(), currentUserId)) {
                return ResponseEntity.badRequest().body(Map.of("error", "You already have a book with this ISBN"));
            }

            Book book = new Book(
                request.getTitle(),
                request.getAuthor(),
                request.getIsbn(),
                request.getGenre(),
                request.getCondition(),
                request.getDescription(),
                request.getStatus(),
                request.getListingType(),
                owner
            );

            Book savedBook = bookService.saveBook(book);
            return ResponseEntity.ok(savedBook);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "You already have a book with this ISBN"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PutMapping("/my-books/{id}")
    public ResponseEntity<?> updateMyBook(@PathVariable Long id, @Valid @RequestBody BookCreationRequest request) {
        try {
            Long currentUserId = authUtil.getCurrentUserId();
            Book existingBook = bookService.getBookByIdAndUserId(id, currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found or not owned by you"));

            existingBook.setTitle(request.getTitle());
            existingBook.setAuthor(request.getAuthor());
            existingBook.setIsbn(request.getIsbn());
            existingBook.setGenre(request.getGenre());
            existingBook.setCondition(request.getCondition());
            existingBook.setDescription(request.getDescription());
            existingBook.setStatus(request.getStatus());
            existingBook.setListingType(request.getListingType());

            Book updatedBook = bookService.updateBook(existingBook);
            return ResponseEntity.ok(updatedBook);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @DeleteMapping("/my-books/{id}")
    public ResponseEntity<?> deleteMyBook(@PathVariable Long id) {
        try {
            Long currentUserId = authUtil.getCurrentUserId();
            Book book = bookService.getBookByIdAndUserId(id, currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found or not owned by you"));

            bookService.deleteBook(book.getId());
            return ResponseEntity.ok(Map.of("message", "Book deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    // ========= PUBLIC BOOKS (UNPROTECTED ENDPOINTS) =========

    @GetMapping
    public ResponseEntity<?> getAllBooks(
        @RequestParam(required = false) String genre,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String listingType
    ) {
        try {
            List<Book> books;

            ListingType type = null;
            BookStatus bookStatus = null;

            if (listingType != null && !listingType.isBlank()) {
                try {
                    type = ListingType.valueOf(listingType.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().build();
                }
            }

            if (status != null && !status.isBlank()) {
                try {
                    bookStatus = BookStatus.valueOf(status.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().build();
                }
            }

            if (bookStatus != null && type != null) {
                books = bookService.getBooksByStatusAndListingType(bookStatus, type);
            } else if (type != null) {
                books = bookService.getBooksByListingType(type);
            } else if (bookStatus != null) {
                books = bookService.getBooksByStatus(bookStatus);
            } else if (genre != null) {
                books = bookService.getBooksByGenre(genre);
            } else {
                books = bookService.getAllBooks();
            }

            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/available")
    public ResponseEntity<List<Book>> getAvailableBooks(@RequestParam(required = false) Long excludeUserId) {
        try {
            List<Book> books;
            if (excludeUserId != null) {
                books = bookService.getAvailableBooksExcludingUser(excludeUserId, BookStatus.AVAILABLE);
            } else {    
                books = bookService.getBooksByStatus(BookStatus.AVAILABLE);
            }
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }
}
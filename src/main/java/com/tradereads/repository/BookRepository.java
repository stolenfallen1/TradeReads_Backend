package com.tradereads.repository;

import com.tradereads.model.Book;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByOwnerId(Long userId);
    List<Book> findByOwnerIdAndStatus(Long userId, Book.BookStatus status);
    List<Book> findByGenre(String genre);
    List<Book> findByOwnerIdAndGenre(Long userId, String genre);
    List<Book> findByStatus(Book.BookStatus status);
    List<Book> findByStatusAndOwnerIdNot(Book.BookStatus status, Long excludeUserId);
    List<Book> findByListingType(Book.ListingType listingType);
    List<Book> findByStatusAndListingType(Book.BookStatus status, Book.ListingType listingType);
    List<Book> findByOwnerIdAndListingType(Long userId, Book.ListingType listingType);
    List<Book> findByOwnerIdAndStatusAndListingType(Long userId, Book.BookStatus status, Book.ListingType listingType);

    Optional<Book> findByIdAndOwnerId(Long id, Long ownerId);

    boolean existsByIsbnAndOwnerId(String isbn, Long ownerId);
}

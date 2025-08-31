package com.tradereads.repository;

import com.tradereads.model.Book;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByOwnerId(Long userId);
    List<Book> findByOwnerIdAndStatus(Long userId, String status);
    List<Book> findByGenre(String genre);
    List<Book> findByOwnerIdAndGenre(Long userId, String genre);
    List<Book> findByStatus(String status);
    List<Book> findByStatusAndOwnerIdNot(String status, Long excludeUserId);
    Optional<Book> findByIdAndOwnerId(Long id, Long ownerId);
    boolean existsByIsbnAndOwnerId(String isbn, Long ownerId);
}

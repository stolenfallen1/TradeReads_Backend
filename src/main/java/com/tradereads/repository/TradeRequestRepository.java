package com.tradereads.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tradereads.model.TradeRequest;
import com.tradereads.model.TradeRequest.TradeStatus;

@Repository
public interface TradeRequestRepository extends JpaRepository<TradeRequest, Long> {
    // Find all trade request where user is the requester
    List<TradeRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);
    
    // Find all trade requests where user is the owner of the requested book
    List<TradeRequest> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    // Find trade requests by status for a specific user (as requester)
    List<TradeRequest> findByRequesterIdAndStatusOrderByCreatedAtDesc(Long requesterId, TradeStatus status);

    // Find trade requests by status for a specific user (as owner)
    List<TradeRequest> findByOwnerIdAndStatusOrderByCreatedAtDesc(Long ownerId, TradeStatus status);

    // Find trade request by ID and requester (for authorization)
    Optional<TradeRequest> findByIdAndRequesterId(Long id, Long requesterId);

    // Find trade request by ID and owner (for authorization)
    Optional<TradeRequest> findByIdAndOwnerId(Long id, Long ownerId);

    // Check if there's already a pending trade request for this book by this user
    @Query("SELECT tr FROM TradeRequest tr WHERE tr.requester.id = :requesterId" + " AND tr.requestedBook.id = :bookId AND tr.status = 'PENDING'")
    Optional<TradeRequest> findPendingTradeRequest(@Param("requesterId") Long requesterId, @Param("bookId") Long bookId);

    // Find all trade requests for a specific book
    List<TradeRequest> findByRequestedBookIdOrderByCreatedAtDesc(Long BookId);

    // Find all trade requests involving a specific book (either as requested or offered)
    @Query("SELECT tr FROM TradeRequest tr WHERE tr.requestBook.id = :bookId " + "OR tr.offeredBook.id = :bookId ORDER BY tr.createdAt DESC")
    List<TradeRequest> findByInvolvedBookId(@Param("bookId") Long bookId);

    // Count pending requests for a user's books
    @Query("SELECT COUNT(tr) FROM TradeRequest tr WHERE tr.owner.id = :userId " + "AND tr.status = 'PENDING'")
    Long countPendingRequestForUser(@Param("userId") Long userId);

    // Count pending requests made by a user
    @Query("SELECT COUNT(tr) FROM TradeRequest tr WHERE tr.requester.id = :userId " + "AND tr.status = 'PENDING'")
    Long countPendingRequestsByUser(@Param("userId") Long userId);
}

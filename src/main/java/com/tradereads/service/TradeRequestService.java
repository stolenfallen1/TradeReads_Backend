package com.tradereads.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tradereads.model.Book;
import com.tradereads.model.TradeRequest;
import com.tradereads.model.User;
import com.tradereads.model.Book.BookStatus;
import com.tradereads.model.Book.ListingType;
import com.tradereads.model.TradeRequest.TradeStatus;
import com.tradereads.repository.TradeRequestRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class TradeRequestService {
    private final TradeRequestRepository tradeRequestRepository;
    private final BookService bookService;
    private final UserService userService;

    public TradeRequestService(TradeRequestRepository tradeRequestRepository, BookService bookService, UserService userService) {
        this.tradeRequestRepository = tradeRequestRepository;
        this.bookService = bookService;
        this.userService = userService;
    }

    public TradeRequest createTradeRequest(Long requesterId, Long requestedBookId, Long offeredBookId, String message) {
        User requester = userService.getUserById(requesterId).orElseThrow(() -> new IllegalArgumentException("Requester not found"));
        Book requestedBook = bookService.getBookById(requestedBookId).orElseThrow(() -> new IllegalArgumentException("Requested book not found"));
        
        if (requestedBook.getStatus() != BookStatus.AVAILABLE) {
            throw new IllegalArgumentException("Requested book is not available for trade");
        }

        // Owner cannot request their own books
        if (requestedBook.getOwner().getId().equals(requesterId)) {
            throw new IllegalArgumentException("Cannot request your own book");
        }

        User owner = requestedBook.getOwner();

        // Check if there's already a pending request for this book by this user
        Optional<TradeRequest> existingRequest = tradeRequestRepository.findPendingTradeRequest(requesterId, requestedBookId);
        if (existingRequest.isPresent()) {
            throw new IllegalArgumentException("You already have a pending request for this book");
        }

        Book offeredBook = null;
        // Dead Code Warning
        if (offeredBookId != null) {
            offeredBook = bookService.getBookByIdAndUserId(offeredBookId, requesterId).orElseThrow(() -> new IllegalArgumentException("Offered book not found or not owned by you."));

            if (offeredBook.getStatus() != BookStatus.AVAILABLE) {
                throw new IllegalArgumentException("Offered book is not available for trade");
            }
        } else {
            if (requestedBook.getListingType() != ListingType.GIVEAWAY) {
                throw new IllegalArgumentException("This book requires a trade offer");
            }
        }

        TradeRequest tradeRequest = new TradeRequest(requester, owner, requestedBook, offeredBook, message);
        return tradeRequestRepository.save(tradeRequest);
    }

    public List<TradeRequest> getOutgoingTradeRequests(Long userId) {
        return tradeRequestRepository.findByRequesterIdOrderByCreatedAtDesc(userId);
    }

    public List<TradeRequest> getIncomingTradeRequests(Long userId) {
        return tradeRequestRepository.findByOwnerIdOrderByCreatedAtDesc(userId);
    }

    public List<TradeRequest> getOutgoingTradeRequestsByStatus(Long userId, TradeStatus status) {
        return tradeRequestRepository.findByRequesterIdAndStatusOrderByCreatedAtDesc(userId, status);
    }

    public List<TradeRequest> getIncomingTradeRequestsByStatus(Long userId, TradeStatus status) {
        return tradeRequestRepository.findByOwnerIdAndStatusOrderByCreatedAtDesc(userId, status);
    }

    public Optional<TradeRequest> getTradeRequestById(Long id, Long userId) {
        Optional<TradeRequest> tradeRequest = tradeRequestRepository.findById(id);
        if (tradeRequest.isPresent()) {
            TradeRequest tr = tradeRequest.get();
            if (tr.getRequester().getId().equals(userId) || tr.getOwner().getId().equals(userId)) {
                return tradeRequest;
            }
        }
        return Optional.empty();
    }

    public TradeRequest acceptTradeRequest(Long tradeRequestId, Long ownerId) {
        TradeRequest tradeRequest = tradeRequestRepository.findByIdAndOwnerId(tradeRequestId, ownerId).orElseThrow(() -> new IllegalArgumentException("Trade request not found or not authorized"));

        if (tradeRequest.getStatus() != TradeStatus.PENDING) {
            throw new IllegalArgumentException("Trade request cannot be accepted");
        }

        if (tradeRequest.getRequestedBook().getStatus() != BookStatus.AVAILABLE) {
            throw new IllegalArgumentException("Requested book is no longe available");
        }

        if (tradeRequest.getOfferedBook() != null && tradeRequest.getOfferedBook().getStatus() != BookStatus.AVAILABLE) {
            throw new IllegalArgumentException("Offered book is no longe available");
        }

        tradeRequest.setStatus(TradeStatus.ACCEPTED);

        // Mark books as traded
        tradeRequest.getRequestedBook().setStatus(BookStatus.TRADED);
        if (tradeRequest.getOfferedBook() != null) {
            tradeRequest.getOfferedBook().setStatus(BookStatus.TRADED);
        }

        bookService.updateBook(tradeRequest.getRequestedBook());
        if (tradeRequest.getOfferedBook() != null) {
            bookService.updateBook(tradeRequest.getOfferedBook());
        }

        return tradeRequestRepository.save(tradeRequest);
    }

    public TradeRequest declineTradeRequest(Long tradeRequestId, Long ownerId) {
        TradeRequest tradeRequest = tradeRequestRepository.findByIdAndOwnerId(tradeRequestId, ownerId).orElseThrow(() -> new IllegalArgumentException("Trade request not found or not authorized"));

        if (tradeRequest.getStatus() != TradeStatus.PENDING) {
            throw new IllegalArgumentException("Trade request cannot be declined");
        }

        tradeRequest.setStatus(TradeStatus.DECLINED);
        return tradeRequestRepository.save(tradeRequest);
    }

    public TradeRequest cancelTradeRequest(Long tradeRequestId, Long userId) {
        TradeRequest tradeRequest = getTradeRequestById(tradeRequestId, userId).orElseThrow(() -> new IllegalArgumentException("Trade request not found or not authorized"));

        if (tradeRequest.getStatus() != TradeStatus.PENDING) {
            throw new IllegalArgumentException("Trade request cannot be cancelled");
        }

        tradeRequest.setStatus(TradeStatus.CANCELLED);
        return tradeRequestRepository.save(tradeRequest);
    }

    // Mark trade as completed (can be done by either party)
    public TradeRequest completeTradeRequest(Long tradeRequestId, Long userId) {
        TradeRequest tradeRequest = getTradeRequestById(tradeRequestId, userId).orElseThrow(() -> new IllegalArgumentException("Trade request not found or not authorized"));

        if (tradeRequest.getStatus() != TradeStatus.ACCEPTED) {
            throw new IllegalArgumentException("Trade request must be accepted before completion");
        }

        tradeRequest.setStatus(TradeStatus.COMPLETED);
        return tradeRequestRepository.save(tradeRequest);
    }

    public List<TradeRequest> getTradeRequestsForBook(Long bookId) {
        return tradeRequestRepository.findByRequestedBookIdOrderByCreatedAtDesc(bookId);
    }

    public Long getPendingIncomingRequestsCount(Long userId) {
        return tradeRequestRepository.countPendingRequestForUser(userId);
    }

    public Long getPendingOutgoingRequestsCount(Long userId) {
        return tradeRequestRepository.countPendingRequestsByUser(userId);
    }

    // Delete a trade request (only if pending by requester)
    public void deleteTradeRequest(Long tradeRequestId, Long requesterId) {
        TradeRequest tradeRequest = tradeRequestRepository.findByIdAndRequesterId(tradeRequestId, requesterId).orElseThrow(() -> new IllegalArgumentException("Trade request not found or not authorized"));

        if (tradeRequest.getStatus() != TradeStatus.PENDING) {
            throw new IllegalArgumentException("Can only delete pending trade requests");
        }

        tradeRequestRepository.delete(tradeRequest);
    }
}

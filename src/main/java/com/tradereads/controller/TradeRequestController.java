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

import com.tradereads.components.AuthUtil;
import com.tradereads.dto.TradeRequestDTO;
import com.tradereads.model.TradeRequest;
import com.tradereads.model.TradeRequest.TradeStatus;
import com.tradereads.service.BookService;
import com.tradereads.service.TradeRequestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/trades")
public class TradeRequestController {

    private final BookService bookService;
    private final TradeRequestService tradeRequestService;
    private final AuthUtil authUtil;

    public TradeRequestController(TradeRequestService tradeRequestService, AuthUtil authUtil, BookService bookService) {
        this.tradeRequestService = tradeRequestService;
        this.authUtil = authUtil;
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<?> createTradeRequest(@Valid @RequestBody TradeRequestDTO request) {
        try {
            Long currentUserId = authUtil.getCurrentUserId();
            TradeRequest tradeRequest = tradeRequestService.createTradeRequest(
                currentUserId, 
                request.getRequestedBookId(), 
                request.getOfferedBookId(), 
                request.getMessage()
            );

            return ResponseEntity.ok(Map.of(
                "message", "Trade request created successfully",
                "tradeRequest", tradeRequest
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/outgoing")
    public ResponseEntity<?> getOutgoingTradeRequests(@RequestParam(required = false) String status) {
        try {
            Long currentUserId = authUtil.getCurrentUserId();
            List<TradeRequest> tradeRequests;

            if (status != null && !status.isBlank()) {
                try {
                    TradeStatus tradeStatus = TradeStatus.valueOf(status.trim().toUpperCase());
                    tradeRequests = tradeRequestService.getOutgoingTradeRequestsByStatus(currentUserId, tradeStatus);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid status"));
                }
            } else {
                tradeRequests = tradeRequestService.getOutgoingTradeRequests(currentUserId);
            }

            return ResponseEntity.ok(tradeRequests);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/incoming")
    public ResponseEntity<?> getIncomingTradeRequests(@RequestParam(required = false) String status) {
        try {
            Long currentUserId = authUtil.getCurrentUserId();
            List<TradeRequest> tradeRequests;

            if (status != null && !status.isBlank()) {
                try {
                    TradeStatus tradeStatus = TradeStatus.valueOf(status.trim().toUpperCase());
                    tradeRequests = tradeRequestService.getIncomingTradeRequestsByStatus(currentUserId, tradeStatus);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid status"));
                }
            } else {
                tradeRequests = tradeRequestService.getIncomingTradeRequests(currentUserId);
            }

            return ResponseEntity.ok(tradeRequests);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTradeRequestsById(@PathVariable Long id) {
        try {
            Long currentUserId = authUtil.getCurrentUserId();

            return tradeRequestService.getTradeRequestById(id, currentUserId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptTradeRequest(@PathVariable Long id) {
        try {
            Long currentUserId = authUtil.getCurrentUserId();
            TradeRequest updatedRequest = tradeRequestService.acceptTradeRequest(id, currentUserId);

            return ResponseEntity.ok(Map.of(
                "message", "Trade request accepted successfully",
                "tradeRequest", updatedRequest
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PutMapping("/{id}/decline")
    public ResponseEntity<?> declineTradeRequest(@PathVariable Long id) {
        try {
            Long currentUserId = authUtil.getCurrentUserId();
            TradeRequest updatedRequest = tradeRequestService.declineTradeRequest(id, currentUserId);

            return ResponseEntity.ok(Map.of(
                "message", "Trade request declined",
                "tradeRequest", updatedRequest
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelTradeRequest(@PathVariable Long id) {
        try {
            Long currentUserId = authUtil.getCurrentUserId();
            TradeRequest updatedRequest = tradeRequestService.cancelTradeRequest(id, currentUserId);

            return ResponseEntity.ok(Map.of(
                "message", "Trade marked as completed",
                "tradeRequest", updatedRequest
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    // Delete a trade request (only by requester, also only if status is pending)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTradeRequest(@PathVariable Long id) {
        try {
            Long currentUserId = authUtil.getCurrentUserId();
            tradeRequestService.deleteTradeRequest(id, currentUserId);

            return ResponseEntity.ok(Map.of("message", "Trade Request deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    // Get count of pending trade requests (will probably enhance and use for future - specially notifications)
    // Will probably go down the websocket route soon!!!
    @GetMapping("/counts")
    public ResponseEntity<?> getTradeRequestCounts() {
        try {
            Long currentUserId = authUtil.getCurrentUserId();
            
            Long pendingIncomingRequest = tradeRequestService.getPendingIncomingRequestsCount(currentUserId);
            Long pendingOutgoingRequest = tradeRequestService.getPendingOutgoingRequestsCount(currentUserId);

            return ResponseEntity.ok(Map.of(
                "pendingIncoming", pendingIncomingRequest,
                "pendingOutgoing", pendingOutgoingRequest
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    // Get all trade requests for a specific book (useful for book owners to see all requests)
    // Will go down the websocket route? Not sure will see in the future!!!
    @GetMapping("/book/{bookId}")
    public ResponseEntity<?> getTradeRequestsForBook(@PathVariable Long bookId) {
        try {
            Long currentUserId = authUtil.getCurrentUserId();

            if (!bookService.getBookByIdAndUserId(bookId, currentUserId).isPresent()) {
                return ResponseEntity.notFound().build();
            }

            List<TradeRequest> tradeRequests = tradeRequestService.getTradeRequestsForBook(bookId);
            return ResponseEntity.ok(tradeRequests);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }
}

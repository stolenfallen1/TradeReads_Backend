package com.tradereads.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TradeRequestDTO {
    @NotNull(message = "Requested book ID is required")
    private Long requestedBookId;

    private Long offeredBookId; 

    @Size(max = 500, message = "Message cannot exceed 500 characters.")
    private String message;

    public TradeRequestDTO() {}

    public TradeRequestDTO(Long requestedBookId, Long offeredBookId, String message) {
        this.requestedBookId = requestedBookId;
        this.offeredBookId = offeredBookId;
        this.message = message;
    }

    public Long getRequestedBookId() {
        return requestedBookId;
    }
    public void setRequestedBookId(Long requestedBookId) {
        this.requestedBookId = requestedBookId;
    }

    public Long getOfferedBookId() {
        return offeredBookId;
    }
    public void setOfferedBookId(Long offeredBookId) {
        this.offeredBookId = offeredBookId;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

}

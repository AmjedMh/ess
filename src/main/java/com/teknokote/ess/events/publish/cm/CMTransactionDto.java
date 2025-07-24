package com.teknokote.ess.events.publish.cm;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
public class CMTransactionDto {
    private LocalDateTime dateTime;
    private BigDecimal amount;
    private BigDecimal quantity;
    private Double price;
    private Long cardId;
    private Long authorizationId;
    private String productName;
    private Long productId;
    private String reference;
    private BigDecimal availableBalance;
    private String salePointName;
    @Builder
    public CMTransactionDto(LocalDateTime dateTime, BigDecimal amount, BigDecimal quantity, Long cardId, Long authorizationId, Long productId, String productName, String reference, BigDecimal availableBalance, String salePointName, Double price)
    {
        this.dateTime = dateTime;
        this.amount = amount;
        this.quantity = quantity;
        this.price=price;
        this.cardId = cardId;
        this.authorizationId = authorizationId;
        this.productName=productName;
        this.productId = productId;
        this.reference=reference;
        this.availableBalance = availableBalance;
        this.salePointName=salePointName;
    }
}

package com.example.demo.application;

import com.example.demo.model.DebitCard;
import com.example.demo.model.MainAccountBalance;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface DebitCardOperations {
    Mono<DebitCard> createDebitCard(String clientId);
    Mono<Void> processDebit(String cardId, BigDecimal amountToDebit);
    Mono<MainAccountBalance> getMainAccountBalanceByCardId(String cardId);

}

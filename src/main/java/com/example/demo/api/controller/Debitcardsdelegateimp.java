package com.example.demo.api.controller;


import com.example.demo.api.DebitCardsApiDelegate;
import com.example.demo.application.DebitCardOperations;
import com.example.demo.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class Debitcardsdelegateimp implements DebitCardsApiDelegate {


    @Autowired
    private DebitCardOperations debitCardOperations;
    @Override
    public Optional<NativeWebRequest> getRequest() {
        return DebitCardsApiDelegate.super.getRequest();
    }

    @Override
    public Mono<ResponseEntity<Void>> debitCardsCardIdDepositPost(String cardId, Mono<TransactionRequest> transactionRequest, ServerWebExchange exchange) {
        return transactionRequest.flatMap(request -> {
            BigDecimal amountToDebit = BigDecimal.valueOf(request.getAmount());
            return debitCardOperations.processDebit(cardId, amountToDebit)
                    .thenReturn(new ResponseEntity<Void>(HttpStatus.OK))
                    .onErrorResume(e -> Mono.just(new ResponseEntity<Void>(HttpStatus.BAD_REQUEST)));
        });
    }

    @Override
    public Mono<ResponseEntity<DebitCard>> debitCardsCardIdGet(String cardId, ServerWebExchange exchange) {
        return DebitCardsApiDelegate.super.debitCardsCardIdGet(cardId, exchange);
    }

    @Override
    public Mono<ResponseEntity<Void>> debitCardsCardIdWithdrawPost(String cardId, Mono<TransactionRequest> transactionRequest, ServerWebExchange exchange) {
        return DebitCardsApiDelegate.super.debitCardsCardIdWithdrawPost(cardId, transactionRequest, exchange);
    }

    @Override
    public Mono<ResponseEntity<DebitCard>> debitCardsPost(Mono<DebitCardCreationRequest> debitCardCreationRequest, ServerWebExchange exchange) {
        return debitCardCreationRequest
                .flatMap(request -> debitCardOperations.createDebitCard(request.getClientId()))
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<MainAccountBalance>> debitCardsGetmainaccountPost(Mono<InlineObject> inlineObject, ServerWebExchange exchange) {
        return inlineObject.flatMap(io -> debitCardOperations.getMainAccountBalanceByCardId(io.getCardId()))
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }
}

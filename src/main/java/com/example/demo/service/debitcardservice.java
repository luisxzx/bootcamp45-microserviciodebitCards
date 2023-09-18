package com.example.demo.service;


import com.example.demo.application.DebitCardOperations;
import com.example.demo.document.DebitCardDocument;
import com.example.demo.infraestructure.weClients.CuentasRestClient;
import com.example.demo.infraestructure.weClients.balanceXCuentaRestClient;
import com.example.demo.mapper.DebitCardMapper;
import com.example.demo.model.AccountDetails;
import com.example.demo.model.DebitCard;
import com.example.demo.model.MainAccountBalance;
import com.example.demo.repository.DebitCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class debitcardservice implements DebitCardOperations {

    @Autowired
    private CuentasRestClient cuentasRestClient;
    @Autowired
    private DebitCardRepository debitCardRepository;
    @Autowired
    private balanceXCuentaRestClient accountClient;

    @Override
    public Mono<DebitCard> createDebitCard(String clientId) {
        return cuentasRestClient.getAccountDetailsByClientId(clientId)
                .collectList()
                .flatMap(accounts -> {
                    DebitCardDocument debitCardDocument = new DebitCardDocument();
                    debitCardDocument.setClientId(clientId);
                    debitCardDocument.setIssueDate(LocalDateTime.now());

                    Predicate<AccountDetails> isSavingAccount = account ->
                            account.getType().equals(AccountDetails.TypeEnum.AHORRO);

                    Predicate<AccountDetails> hasPositiveBalance = account ->
                            account.getBalance().compareTo(BigDecimal.ZERO) > 0;

                    AccountDetails savingAccount = accounts.stream()
                            .filter(isSavingAccount.and(hasPositiveBalance))
                            .findFirst()
                            .orElse(null);

                    if (savingAccount != null) {
                        debitCardDocument.setMount(savingAccount.getBalance());
                        debitCardDocument.setMainAccountId(savingAccount.getId());
                        accounts.remove(savingAccount);
                    } else {
                        AccountDetails firstNonEmptyAccount = accounts.stream()
                                .filter(hasPositiveBalance)
                                .findFirst()
                                .orElse(null);

                        if (firstNonEmptyAccount != null) {
                            debitCardDocument.setMount(firstNonEmptyAccount.getBalance());
                            debitCardDocument.setMainAccountId(firstNonEmptyAccount.getId());
                            accounts.remove(firstNonEmptyAccount);
                        }
                    }

                    // Ordenar las cuentas secundarias por saldo de mayor a menor y luego extraer los IDs
                    List<String> secondaryIds = accounts.stream()
                            .sorted(Comparator.comparing(AccountDetails::getBalance).reversed())
                            .map(AccountDetails::getId)
                            .collect(Collectors.toList());

                    debitCardDocument.setSecondaryAccountIds(secondaryIds);

                    return debitCardRepository.save(debitCardDocument)
                            .map(DebitCardMapper::toModel);
                });
    }

    @Override
    public Mono<Void> processDebit(String cardId, BigDecimal amountToDebit) {
        return debitCardRepository.findById(cardId)
                .flatMap(card -> {
                    if (card.getMount().compareTo(amountToDebit) >= 0) {
                        card.setMount(card.getMount().subtract(amountToDebit));
                        return debitCardRepository.save(card)
                                .then(sendUpdatedBalance(card.getMainAccountId(), card.getMount()))
                                .then(Mono.empty());  // Esto asegura que retorne un Mono<Void>
                    } else {
                        // Intentar las cuentas secundarias
                        return trySecondaryAccounts(card, amountToDebit);
                    }
                });
    }

    private Mono<Void> trySecondaryAccounts(DebitCardDocument card, BigDecimal amountToDebit) {
        return Flux.fromIterable(card.getSecondaryAccountIds())
                .flatMap(secondaryId -> accountClient.getAccountDetailsById(secondaryId))
                .filter(accountDetails -> accountDetails.getBalance().compareTo(amountToDebit) >= 0)
                .next()
                .flatMap(accountWithSufficientFunds -> {
                    card.getSecondaryAccountIds().remove(accountWithSufficientFunds.getId());
                    card.getSecondaryAccountIds().add(card.getMainAccountId());
                    card.setMainAccountId(accountWithSufficientFunds.getId());
                    card.setMount(card.getMount().subtract(amountToDebit));

                    return debitCardRepository.save(card)
                            .then(accountClient.sendBalanceUpdate(card.getMainAccountId(), card.getMount()));
                })
                .switchIfEmpty(Mono.error(new RuntimeException("No sufficient funds in any account.")));
    }





    private Mono<Void> sendUpdatedBalance(String mainAccountId, BigDecimal newBalance) {
        return accountClient.sendBalanceUpdate(mainAccountId, newBalance);
    }

    @Override
    public Mono<MainAccountBalance> getMainAccountBalanceByCardId(String cardId) {
        return debitCardRepository.findById(cardId)
                .flatMap(debitCardDocument ->  accountClient.getAccountDetailsById(debitCardDocument.getMainAccountId()))
                .map(accountDetails -> new MainAccountBalance().balance(accountDetails.getBalance()))
                .switchIfEmpty(Mono.error(new RuntimeException("No se encontró el cardId o el mainAccountId asociado")));
    }


}





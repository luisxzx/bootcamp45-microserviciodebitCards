package com.example.demo.infraestructure.weClients;

import com.example.demo.DTO.BalanceUpdateDTO;
import com.example.demo.model.AccountDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
public class balanceXCuentaRestClient {

    @Value("${ntt.data.bootcamp.s01-account-service}")
    private String accountServiceUrl;

    @Autowired
    private WebClient.Builder webClientBuilder;

    public Mono<AccountDetails> getAccountDetailsById(String idCuenta) {
        WebClient webClient = webClientBuilder.baseUrl(accountServiceUrl).build();
        return webClient.get()
                .uri("/accounts/{idcuenta}", idCuenta)
                .retrieve()
                .bodyToMono(AccountDetails.class);
    }

    public Mono<Void> sendBalanceUpdate(String accountId, BigDecimal balance) {
        WebClient webClient = webClientBuilder.baseUrl(accountServiceUrl).build();
        BalanceUpdateDTO balanceUpdateDTO = new BalanceUpdateDTO(accountId, balance);

        return webClient.put()
                .uri("/accounts/update")
                .bodyValue(balanceUpdateDTO)
                .retrieve()
                .bodyToMono(Void.class);
    }





}

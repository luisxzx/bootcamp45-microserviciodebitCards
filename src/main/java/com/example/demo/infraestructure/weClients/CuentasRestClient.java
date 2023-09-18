package com.example.demo.infraestructure.weClients;

import com.example.demo.model.AccountDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
public class CuentasRestClient {
    @Value("${ntt.data.bootcamp.s01-account-service}")
    private String accountServiceUrl;

    @Autowired
    private WebClient.Builder webClientBuilder;

    public Flux<AccountDetails> getAccountDetailsByClientId(String clientId) {
        WebClient webClient = webClientBuilder.baseUrl(accountServiceUrl).build();
        return webClient.get()
                .uri("/accounts/clients/{clientId}", clientId)
                .retrieve()
                .bodyToFlux(AccountDetails.class);
    }
}
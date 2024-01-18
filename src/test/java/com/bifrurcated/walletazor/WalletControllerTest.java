package com.bifrurcated.walletazor;

import com.bifrurcated.walletazor.data.Wallet;
import com.bifrurcated.walletazor.repository.WalletRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "30000000000")
@ActiveProfiles("test")
class WalletControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(WalletControllerTest.class);
    private static final String END_POINT_PATH = "/api/v1";
    @Autowired
    private WebTestClient walletTestClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WalletRepo repository;

    @BeforeEach
    public void setup() {
        repository.deleteAll().subscribe();
        Wallet wallet1 = new Wallet();
        wallet1.setId(UUID.fromString("b3919077-79e6-4570-bfe0-980ef18f3731"));
        wallet1.setAmount(2000F);
        repository.save(wallet1.setAsNew()).subscribe();
        Wallet wallet2 = new Wallet();
        wallet2.setId(UUID.fromString("bc10fad7-94f1-4047-be4e-311247eed5fb"));
        wallet2.setAmount(500F);
        repository.save(wallet2.setAsNew()).subscribe();
        Wallet wallet3 = new Wallet();
        wallet3.setId(UUID.fromString("9ebef4de-68e3-43ad-a812-42193919ff02"));
        wallet3.setAmount(302000F);
        repository.save(wallet3.setAsNew()).subscribe();
    }
    @AfterEach
    public void clear() {
        repository.deleteAll().subscribe();
    }

    @Test
    public void testWalletGetAmount() throws Exception {
        record BalanceResponse(Float amount){}
        BalanceResponse balanceResponse = new BalanceResponse(2000F);
        String response = objectMapper.writeValueAsString(balanceResponse);
        String id = "b3919077-79e6-4570-bfe0-980ef18f3731";
        this.walletTestClient.get()
                .uri(END_POINT_PATH+ "/wallets/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json(response);
    }

    @Test
    public void testWalletDepositDDOSAttack() throws Exception {
        record WalletRequest(UUID valletId, String operationType, Float amount){}
        record BalanceResponse(Float amount){}
        UUID id = UUID.fromString("b3919077-79e6-4570-bfe0-980ef18f3731");
        WalletRequest walletRequest = new WalletRequest(
                id, "DEPOSIT", 1000F);

        String requestBody = objectMapper.writeValueAsString(walletRequest);

        ExecutorService executorService = Executors.newFixedThreadPool(300);
        List<Future<Wallet>> futures = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            Callable<Wallet> callable = () -> walletTestClient.post()
                    .uri(END_POINT_PATH + "/wallet")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(Wallet.class)
                    .returnResult()
                    .getResponseBody();

            Future<Wallet> future = executorService.submit(callable);
            futures.add(future);
        }
        for (var future : futures) {
            future.get();
        }

        BalanceResponse balanceResponse = new BalanceResponse(302000F);
        String response = objectMapper.writeValueAsString(balanceResponse);
        this.walletTestClient.get()
                .uri(END_POINT_PATH+"/wallets/"+id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json(response);
    }

}
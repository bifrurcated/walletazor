package com.bifrurcated.walletazor.controller;

import com.bifrurcated.walletazor.errors.UnsupportedOperationTypeError;
import com.bifrurcated.walletazor.operation.OperationType;
import com.bifrurcated.walletazor.service.WalletService;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class WalletController {
    private final WalletService walletService;
    private final Map<String, OperationType> operations;

    @Autowired
    public WalletController(WalletService walletService, Map<String, OperationType> operations) {
        this.walletService = walletService;
        this.operations = operations;
    }

    public record WalletResponse(UUID id, Float amount){}
    public record WalletRequest(
            @JsonProperty("valletId") UUID walletId,
            String operationType,
            Float amount
    ){}

    @PostMapping("/wallet")
    public Mono<WalletResponse> wallet(@RequestBody WalletRequest request) {
        var operationType = request.operationType().toLowerCase();
        return Optional.ofNullable(operations.get(operationType))
                .orElseThrow(() -> new UnsupportedOperationTypeError(operationType))
                .handle(request.walletId(), request.amount())
                .map(wallet -> new WalletResponse(wallet.getId(), wallet.getAmount()));
    }

    public record BalanceResponse(Float amount){}
    @GetMapping("/wallets/{WALLET_UUID}")
    public Mono<BalanceResponse> balance(@PathVariable(value = "WALLET_UUID") UUID id) {
        return walletService.amount(id).map(BalanceResponse::new);
    }
}

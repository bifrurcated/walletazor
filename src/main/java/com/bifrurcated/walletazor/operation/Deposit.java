package com.bifrurcated.walletazor.operation;

import com.bifrurcated.walletazor.data.Wallet;
import com.bifrurcated.walletazor.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class Deposit implements OperationType {

    @Autowired
    private WalletService walletService;

    @Override
    public String name() {
        return "deposit";
    }

    @Override
    public Mono<Wallet> handle(UUID id, Float amount) {
        return walletService.addAmount(id, amount);
    }
}

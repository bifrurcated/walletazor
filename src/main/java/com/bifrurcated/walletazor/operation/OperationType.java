package com.bifrurcated.walletazor.operation;

import com.bifrurcated.walletazor.data.Wallet;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OperationType {
    String name();

    Mono<Wallet> handle(UUID id, Float amount);
}

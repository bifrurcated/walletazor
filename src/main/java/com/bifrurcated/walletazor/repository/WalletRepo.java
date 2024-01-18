package com.bifrurcated.walletazor.repository;

import com.bifrurcated.walletazor.data.Wallet;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepo extends ReactiveCrudRepository<Wallet, UUID> {
    @Query("""
         UPDATE wallet
         SET amount = (amount + :amount)
         WHERE id = :id
         RETURNING id, amount
    """)
    Mono<Wallet> updateAmount(UUID id, Float amount);

    @Query("SELECT w.id, w.amount FROM wallet w WHERE id = :id FOR UPDATE")
    Mono<Wallet> findByIdForUpdate(@Param("id") UUID id);
}

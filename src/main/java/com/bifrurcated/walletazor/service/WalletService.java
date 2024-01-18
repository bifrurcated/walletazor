package com.bifrurcated.walletazor.service;

import com.bifrurcated.walletazor.data.Wallet;
import com.bifrurcated.walletazor.errors.NotEnoughMoneyError;
import com.bifrurcated.walletazor.errors.WalletNotFoundError;
import com.bifrurcated.walletazor.repository.WalletRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class WalletService {
    private final WalletRepo walletRepo;

    @Autowired
    public WalletService(WalletRepo walletRepo) {
        this.walletRepo = walletRepo;
    }

    @Transactional
    public Mono<Wallet> addAmount(UUID id, Float amount) {
        return walletRepo.findByIdForUpdate(id)
                .switchIfEmpty(Mono.error(WalletNotFoundError::new))
                .flatMap(wallet -> {
                    wallet.setAmount(wallet.getAmount() + amount);
                    return walletRepo.save(wallet);
                });
    }

    public Mono<Wallet> addAmountNotConcurrency(UUID id, Float amount) {
        return walletRepo.findById(id)
                .switchIfEmpty(Mono.error(WalletNotFoundError::new))
                .doOnNext(wallet -> wallet.setAmount(wallet.getAmount() + amount))
                .flatMap(walletRepo::save);
    }

    public Mono<Wallet> addAmountUsingUpdate(UUID id, Float amount) {
        return walletRepo.updateAmount(id, amount).switchIfEmpty(Mono.error(WalletNotFoundError::new));
    }

    public Mono<Wallet> reduceAmount(UUID id, Float amount) {
        return walletRepo.findById(id)
                .switchIfEmpty(Mono.error(WalletNotFoundError::new))
                .flatMap(wallet -> {
                    var reduce = wallet.getAmount() - amount;
                    if (reduce < 0) {
                        throw new NotEnoughMoneyError(wallet.getAmount(), amount);
                    }
                    wallet.setAmount(reduce);
                    return walletRepo.save(wallet);
                });
    }

    public Mono<Float> amount(UUID id) {
        return walletRepo.findById(id)
                .switchIfEmpty(Mono.error(WalletNotFoundError::new))
                .map(Wallet::getAmount);
    }
}

package com.tokentrackr.crypto_read_service.service.interfaces;

import com.tokentrackr.crypto_read_service.model.Crypto;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface CryptoCacheService {
    Optional<Crypto> getCryptoById(String id);

    void cacheCrypto(List<Crypto> cryptos);
}
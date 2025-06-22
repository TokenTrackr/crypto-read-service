package com.tokentrackr.crypto_read_service.service.impl;

import com.tokentrackr.crypto_read_service.exception.CryptoNotFoundException;
import com.tokentrackr.crypto_read_service.model.Crypto;
import com.tokentrackr.crypto_read_service.service.interfaces.GetCryptoByIdService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetCryptoByIdServiceImpl implements GetCryptoByIdService {

    private final RedisTemplate<String, Crypto> redisTemplate;

    @Override
    public Crypto getById(String id) {
        // 1. Validate ID format
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Crypto ID cannot be empty");
        }

        // 2. Construct the Redis key (matches your existing pattern)
        String redisKey = "crypto:" + id.toLowerCase();

        // 3. Fetch from Redis (same serialization as getAllCrypto)
        Crypto crypto = redisTemplate.opsForValue().get(redisKey);

        if (crypto == null) {
            throw new CryptoNotFoundException(id);
        }

        return crypto;
    }
}
package com.tokentrackr.crypto_read_service.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tokentrackr.crypto_read_service.exception.CachePersistenceException;
import com.tokentrackr.crypto_read_service.exception.CryptoNotFoundException;
import com.tokentrackr.crypto_read_service.model.Crypto;
import com.tokentrackr.crypto_read_service.service.interfaces.GetCryptoByIdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetCryptoByIdServiceImpl implements GetCryptoByIdService {

    private static final String CRYPTO_CACHE_KEY_PREFIX = "crypto:";

    private final RedisTemplate<String, ?> redisTemplate;
    private final ObjectMapper objectMapper;
    // Serializer for turning String keys into bytes for low‐level calls
    private final StringRedisSerializer keySerializer = new StringRedisSerializer();

    @Override
    public Crypto getById(String id) {
        // 1) Validate
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Crypto ID cannot be empty");
        }

        String key = CRYPTO_CACHE_KEY_PREFIX + id;

        try {
            // 2) Fetch raw JSON bytes
            byte[] raw = redisTemplate.execute((RedisCallback<byte[]>) (RedisConnection conn) ->
                    conn.get(keySerializer.serialize(key))
            );

            // 3) Not found?
            if (raw == null) {
                throw new CryptoNotFoundException(id);
            }

            // 4) Deserialize JSON → Crypto
            return objectMapper.readValue(raw, Crypto.class);

        } catch (CryptoNotFoundException e) {
            throw e; // propagate 404‐style exception
        } catch (Exception e) {
            log.error("Error retrieving crypto [{}]: {}", id, e.getMessage(), e);
            throw new CachePersistenceException("Failed to retrieve crypto data", e);
        }
    }
}

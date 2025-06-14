package com.tokentrackr.crypto_read_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tokentrackr.crypto_read_service.exception.CachePersistenceException;
import com.tokentrackr.crypto_read_service.model.Crypto;
import com.tokentrackr.crypto_read_service.service.interfaces.CryptoCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisCacheServiceImpl implements CryptoCacheService {

    private static final String CRYPTO_CACHE_KEY_PREFIX = "crypto:";
    private static final String CRYPTOS_ZSET_KEY      = "cryptos:all";

    private final RedisTemplate<String, Crypto> redisTemplate;
    private final StringRedisSerializer keySerializer = new StringRedisSerializer();
    private final ObjectMapper objectMapper;

    @Override
    public Optional<Crypto> getCryptoById(String id) {
        try {
            String key = CRYPTO_CACHE_KEY_PREFIX + id;
            Crypto data = redisTemplate.opsForValue().get(key);
            return Optional.ofNullable(data);
        } catch (Exception e) {
            log.error("Error retrieving crypto data: {}", e.getMessage(), e);
            throw new CachePersistenceException("Failed to retrieve crypto data", e);
        }
    }

    @Override
    public void cacheCrypto(List<Crypto> cryptos) {
        try {
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                byte[] zsetKeyBytes = keySerializer.serialize(CRYPTOS_ZSET_KEY);

                for (Crypto crypto : cryptos) {
                    String key = CRYPTO_CACHE_KEY_PREFIX + crypto.getId();
                    byte[] keyBytes = keySerializer.serialize(key);

                    if (keyBytes == null) {
                        log.warn("Skipping crypto caching because serialized key was null for id={}", crypto.getId());
                        continue;
                    }

                    try {
                        byte[] valueBytes = objectMapper.writeValueAsBytes(crypto);
                        // 1) Store JSON under "crypto:<id>"
                        connection.stringCommands().set(keyBytes, valueBytes);

                        // 2) ZADD into "cryptos:all" with score = marketCapRank
                        double score = (double) crypto.getMarketCapRank();
                        connection.zSetCommands().zAdd(zsetKeyBytes, score, keyBytes);

                    } catch (JsonProcessingException e) {
                        log.error("Error serializing crypto {}: {}", crypto.getId(), e.getMessage(), e);
                    }
                }
                return null;
            });

            log.info("Successfully cached {} cryptos (with marketCapRank index) using pipeline", cryptos.size());
        } catch (Exception e) {
            log.error("Error during pipeline batch caching: {}", e.getMessage(), e);
            throw new CachePersistenceException("Failed to cache cryptos via pipeline", e);
        }
    }
}



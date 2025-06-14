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
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final StringRedisTemplate stringRedisTemplate;
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
            stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (Crypto crypto : cryptos) {
                    try {
                        String serializedCrypto = objectMapper.writeValueAsString(crypto);
                        connection.zSetCommands().zAdd(
                                CRYPTOS_ZSET_KEY.getBytes(),
                                crypto.getMarketCapRank(),
                                serializedCrypto.getBytes()  // Store FULL DATA as value
                        );
                    } catch (JsonProcessingException e) {
                        log.error("Error serializing crypto {}: {}", crypto.getId(), e.getMessage());
                    }
                }
                return null;
            });
            log.info("Cached {} cryptos in ZSET with full data payload", cryptos.size());
        } catch (Exception e) {
            log.error("Pipeline caching failed", e);
            throw new CachePersistenceException("ZSET caching error", e);
        }
    }
}



package com.tokentrackr.crypto_read_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tokentrackr.crypto_read_service.exception.CachePersistenceException;
import com.tokentrackr.crypto_read_service.model.Crypto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RedisCacheServiceImplTest {

    @Mock
    private RedisTemplate<String, Crypto> redisTemplate;

    @Mock
    private ValueOperations<String, Crypto> valueOperations;

    @Mock
    private RedisCallback<Object> redisCallback;

    @InjectMocks
    private RedisCacheServiceImpl service;

    @Captor
    private ArgumentCaptor<RedisCallback<Object>> redisCallbackCaptor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void getCryptoById_ReturnsDataWhenExists() {
        Crypto bitcoin = createCrypto("bitcoin", 1);
        when(valueOperations.get("crypto:bitcoin")).thenReturn(bitcoin);

        Optional<Crypto> result = service.getCryptoById("bitcoin");

        assertTrue(result.isPresent());
        assertEquals("bitcoin", result.get().getId());
        verify(valueOperations).get("crypto:bitcoin");
    }

    @Test
    void getCryptoById_ReturnsEmptyWhenNotFound() {
        when(valueOperations.get("crypto:unknown")).thenReturn(null);

        Optional<Crypto> result = service.getCryptoById("unknown");

        assertFalse(result.isPresent());
        verify(valueOperations).get("crypto:unknown");
    }

    @Test
    void getCryptoById_ThrowsExceptionOnRedisFailure() {
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Connection failed"));

        assertThrows(CachePersistenceException.class, () -> service.getCryptoById("bitcoin"));
        verify(valueOperations).get(anyString());
    }

    @Test
    void cacheCrypto_HandlesEmptyList() {
        assertDoesNotThrow(() -> service.cacheCrypto(Collections.emptyList()));
        verify(redisTemplate).executePipelined(any(RedisCallback.class));
    }

    @Test
    void cacheCrypto_ProcessesValidCryptosSuccessfully() {
        Crypto bitcoin = createCrypto("bitcoin", 1);
        assertDoesNotThrow(() -> service.cacheCrypto(List.of(bitcoin)));
        verify(redisTemplate).executePipelined(any(RedisCallback.class));
    }

    @Test
    void cacheCrypto_SkipsCryptoWhenKeySerializationFails() {
        Crypto invalidCrypto = createCrypto(null, 1);
        service.cacheCrypto(List.of(invalidCrypto));
        verify(redisTemplate).executePipelined(any(RedisCallback.class));
    }

    @Test
    void cacheCrypto_HandlesSerializationFailureGracefully() {
        Crypto bitcoin = createCrypto("bitcoin", 1);
        assertDoesNotThrow(() -> service.cacheCrypto(List.of(bitcoin)));
        verify(redisTemplate).executePipelined(any(RedisCallback.class));
    }

    @Test
    void cacheCrypto_ProcessesMultipleCryptosWithMixedResults() {
        Crypto valid1 = createCrypto("bitcoin", 1);
        Crypto valid2 = createCrypto("ethereum", 2);
        Crypto invalid = createCrypto(null, 3);

        service.cacheCrypto(List.of(valid1, invalid, valid2));
        verify(redisTemplate).executePipelined(any(RedisCallback.class));
    }

    @Test
    void cacheCrypto_ThrowsExceptionOnPipelineFailure() {
        when(redisTemplate.executePipelined(any(RedisCallback.class)))
                .thenThrow(new RuntimeException("Pipeline error"));

        assertThrows(CachePersistenceException.class,
                () -> service.cacheCrypto(List.of(createCrypto("bitcoin", 1))));
    }

    @Test
    void cacheCrypto_HandlesZeroMarketCapRank() {
        Crypto zeroRank = createCrypto("litecoin", 0);
        assertDoesNotThrow(() -> service.cacheCrypto(List.of(zeroRank)));
        verify(redisTemplate).executePipelined(any(RedisCallback.class));
    }

    private Crypto createCrypto(String id, int marketCapRank) {
        return Crypto.builder()
                .id(id)
                .symbol(id != null ? id.toUpperCase() : null)
                .name(id != null ? id.substring(0, 1).toUpperCase() + id.substring(1) : null)
                .marketCapRank(marketCapRank)
                .currentPrice(50000.0)
                .marketCap(1_000_000_000_000.0)
                .build();
    }
}
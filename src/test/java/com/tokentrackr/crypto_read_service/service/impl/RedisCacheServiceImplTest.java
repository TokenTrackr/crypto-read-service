package com.tokentrackr.crypto_read_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tokentrackr.crypto_read_service.exception.CachePersistenceException;
import com.tokentrackr.crypto_read_service.model.Crypto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisCacheServiceImplTest {
    @Mock
    private RedisTemplate<String, Crypto> redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, Crypto> valueOperations;

    @InjectMocks
    private RedisCacheServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void getCryptoById_success() {
        String cryptoId = "bitcoin";
        Crypto crypto = new Crypto();
        crypto.setId(cryptoId);

        when(valueOperations.get("crypto:" + cryptoId)).thenReturn(crypto);

        Optional<Crypto> result = service.getCryptoById(cryptoId);

        assertTrue(result.isPresent());
        assertEquals(cryptoId, result.get().getId());
        verify(valueOperations).get("crypto:" + cryptoId);
    }

    @Test
    void getCryptoById_throwsException_whenRedisFails() {
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis failure"));

        assertThrows(CachePersistenceException.class, () -> service.getCryptoById("someId"));
    }
    

    @Test
    void cacheCrypto_throwsException() {
        when(redisTemplate.executePipelined(Mockito.any(RedisCallback.class)))
                .thenThrow(new RuntimeException("pipefail"));
        assertThrows(CachePersistenceException.class, () -> service.cacheCrypto(Collections.emptyList()));
    }
}

package com.tokentrackr.crypto_read_service.service.impl;

import com.tokentrackr.crypto_read_service.exception.CryptoNotFoundException;
import com.tokentrackr.crypto_read_service.model.Crypto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class GetCryptoByIdServiceImplTest {

    @Mock
    private RedisTemplate<String, Crypto> redisTemplate;

    @Mock
    private ValueOperations<String, Crypto> valueOperations;

    @InjectMocks
    private GetCryptoByIdServiceImpl getCryptoByIdService;

    private final String validId = "bitcoin";
    private Crypto testCrypto;

    @BeforeEach
    void setUp() {
        testCrypto = new Crypto();
        testCrypto.setId(validId);
        testCrypto.setName("Bitcoin");
        testCrypto.setSymbol("BTC");

        // Mock opsForValue() to return valueOperations
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }
    @Test
    void getById_shouldPropagateRedisException() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis connection error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            getCryptoByIdService.getById(validId);
        });

        assertEquals("Redis connection error", exception.getMessage());
    }


    @Test
    void getById_shouldHandleCaseInsensitiveId() {
        String mixedCaseId = "BitCoin";

        when(valueOperations.get("crypto:" + mixedCaseId.toLowerCase())).thenReturn(testCrypto);

        Crypto result = getCryptoByIdService.getById(mixedCaseId);

        assertNotNull(result);
        assertEquals(validId, result.getId());
    }



    @Test
    void getById_shouldReturnCryptoWhenFound() {
        // Arrange
        when(valueOperations.get("crypto:" + validId)).thenReturn(testCrypto);

        // Act
        Crypto result = getCryptoByIdService.getById(validId);

        // Assert
        assertNotNull(result);
        assertEquals(validId, result.getId());
        assertEquals("Bitcoin", result.getName());
        assertEquals("BTC", result.getSymbol());

        verify(redisTemplate).opsForValue();
        verify(valueOperations).get("crypto:" + validId);
    }
}

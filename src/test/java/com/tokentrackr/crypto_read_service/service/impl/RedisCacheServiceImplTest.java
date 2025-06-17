package com.tokentrackr.crypto_read_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tokentrackr.crypto_read_service.exception.CachePersistenceException;
import com.tokentrackr.crypto_read_service.model.Crypto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.StringRedisSerializer;

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
    private RedisConnectionFactory connectionFactory;

    @Mock
    private ValueOperations<String, Crypto> valueOperations;

    @Mock
    private RedisConnection redisConnection;

    @Mock
    private RedisStringCommands stringCommands;

    @Mock
    private RedisZSetCommands zSetCommands;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RedisCacheServiceImpl service;

    private final StringRedisSerializer keySerializer = new StringRedisSerializer();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // stub opsForValue
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // stub connection factory chain
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(redisConnection);

        // stub low-level commands
        when(redisConnection.stringCommands()).thenReturn(stringCommands);
        when(redisConnection.zSetCommands()).thenReturn(zSetCommands);
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
    void cacheCrypto_HandlesSerializationFailureGracefully() throws Exception {
        Crypto bitcoin = createCrypto("bitcoin", 1);
        when(objectMapper.writeValueAsBytes(bitcoin))
                .thenThrow(JsonProcessingException.class);

        assertDoesNotThrow(() -> service.cacheCrypto(List.of(bitcoin)));
        verify(redisTemplate).executePipelined(any(RedisCallback.class));
    }

    @Test
    void cacheCrypto_ExecutesRedisCommandsCorrectly() throws Exception {
        Crypto bitcoin = createCrypto("bitcoin", 1);
        when(objectMapper.writeValueAsBytes(bitcoin)).thenReturn("data".getBytes());

        // Capture the RedisCallback
        ArgumentCaptor<RedisCallback<Object>> callbackCaptor = ArgumentCaptor.forClass(RedisCallback.class);
        service.cacheCrypto(List.of(bitcoin));

        verify(redisTemplate).executePipelined(callbackCaptor.capture());

        // Execute the callback with our mocked connection
        RedisCallback<Object> callback = callbackCaptor.getValue();
        callback.doInRedis(redisConnection);

        // Verify Redis interactions
        byte[] expectedKey = keySerializer.serialize("crypto:bitcoin");
        byte[] expectedZSetKey = keySerializer.serialize("cryptos:all");

        verify(stringCommands).set(eq(expectedKey), any(byte[].class));
        verify(zSetCommands).zAdd(eq(expectedZSetKey), eq(1.0), eq(expectedKey));
    }

    @Test
    void cacheCrypto_AddsToSortedSetWithCorrectRank() throws Exception {
        Crypto ethereum = createCrypto("ethereum", 2);
        when(objectMapper.writeValueAsBytes(ethereum)).thenReturn("data".getBytes());

        ArgumentCaptor<RedisCallback<Object>> callbackCaptor = ArgumentCaptor.forClass(RedisCallback.class);
        service.cacheCrypto(List.of(ethereum));

        verify(redisTemplate).executePipelined(callbackCaptor.capture());
        callbackCaptor.getValue().doInRedis(redisConnection);

        byte[] expectedKey = keySerializer.serialize("crypto:ethereum");
        byte[] expectedZSetKey = keySerializer.serialize("cryptos:all");

        verify(zSetCommands).zAdd(eq(expectedZSetKey), eq(2.0), eq(expectedKey));
    }

    @Test
    void cacheCrypto_HandlesMultipleCryptosInPipeline() throws Exception {
        Crypto bitcoin = createCrypto("bitcoin", 1);
        Crypto ethereum = createCrypto("ethereum", 2);
        when(objectMapper.writeValueAsBytes(bitcoin)).thenReturn("data1".getBytes());
        when(objectMapper.writeValueAsBytes(ethereum)).thenReturn("data2".getBytes());

        ArgumentCaptor<RedisCallback<Object>> callbackCaptor = ArgumentCaptor.forClass(RedisCallback.class);
        service.cacheCrypto(List.of(bitcoin, ethereum));

        verify(redisTemplate).executePipelined(callbackCaptor.capture());
        callbackCaptor.getValue().doInRedis(redisConnection);

        byte[] bitcoinKey = keySerializer.serialize("crypto:bitcoin");
        byte[] ethereumKey = keySerializer.serialize("crypto:ethereum");
        byte[] zSetKey = keySerializer.serialize("cryptos:all");

        verify(stringCommands).set(eq(bitcoinKey), any(byte[].class));
        verify(stringCommands).set(eq(ethereumKey), any(byte[].class));
        verify(zSetCommands).zAdd(eq(zSetKey), eq(1.0), eq(bitcoinKey));
        verify(zSetCommands).zAdd(eq(zSetKey), eq(2.0), eq(ethereumKey));
    }

    @Test
    void cacheCrypto_ThrowsExceptionOnPipelineFailure() {
        when(redisTemplate.executePipelined(any(RedisCallback.class)))
                .thenThrow(new RuntimeException("Pipeline error"));

        assertThrows(CachePersistenceException.class,
                () -> service.cacheCrypto(List.of(createCrypto("bitcoin", 1))));
    }

    @Test
    void cacheCrypto_HandlesZeroMarketCapRank() throws Exception {
        Crypto zeroRank = createCrypto("litecoin", 0);
        when(objectMapper.writeValueAsBytes(zeroRank)).thenReturn("data".getBytes());

        ArgumentCaptor<RedisCallback<Object>> callbackCaptor = ArgumentCaptor.forClass(RedisCallback.class);
        service.cacheCrypto(List.of(zeroRank));

        verify(redisTemplate).executePipelined(callbackCaptor.capture());
        callbackCaptor.getValue().doInRedis(redisConnection);

        byte[] expectedKey = keySerializer.serialize("crypto:litecoin");
        byte[] expectedZSetKey = keySerializer.serialize("cryptos:all");

        verify(zSetCommands).zAdd(eq(expectedZSetKey), eq(0.0), eq(expectedKey));
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

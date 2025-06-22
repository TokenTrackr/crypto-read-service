package com.tokentrackr.crypto_read_service.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tokentrackr.crypto_read_service.exception.CachePersistenceException;
import com.tokentrackr.crypto_read_service.exception.CryptoNotFoundException;
import com.tokentrackr.crypto_read_service.model.Crypto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCryptoByIdServiceImplTest {

    @Mock
    private RedisTemplate<String, ?> redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private GetCryptoByIdServiceImpl service;

    private final StringRedisSerializer keySerializer = new StringRedisSerializer();

    @Captor
    private ArgumentCaptor<RedisCallback<byte[]>> callbackCaptor;

    @BeforeEach
    void setUp() {
        // no-op
    }

    @Test
    void getById_nullOrEmptyId_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> service.getById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Crypto ID cannot be empty");

        assertThatThrownBy(() -> service.getById("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Crypto ID cannot be empty");
    }

    @Test
    void getById_keyNotFound_throwsCryptoNotFoundException() {
        // stub RedisTemplate.execute to return null bytes
        when(redisTemplate.execute(callbackCaptor.capture())).thenReturn(null);

        assertThatThrownBy(() -> service.getById("bitcoin"))
                .isInstanceOf(CryptoNotFoundException.class)
                .hasMessageContaining("bitcoin");

        // verify that callback was invoked with correct key
        RedisCallback<byte[]> callback = callbackCaptor.getValue();
        // we can test serialization logic by invoking callback with a dummy connection
        RedisConnection conn = mock(RedisConnection.class);
        callback.doInRedis(conn);
        byte[] expectedKeyBytes = keySerializer.serialize("crypto:bitcoin");
        verify(conn).get(expectedKeyBytes);
    }

    @Test
    void getById_ioExceptionDuringDeserialize_throwsCachePersistenceException() throws Exception {
        // stub RedisTemplate.execute to return some bytes
        byte[] fakeJson = "{\"id\":\"bitcoin\"}".getBytes();
        when(redisTemplate.execute(any(RedisCallback.class))).thenReturn(fakeJson);
        // stub ObjectMapper to throw
        when(objectMapper.readValue(fakeJson, Crypto.class)).thenThrow(new RuntimeException("parse error"));

        assertThatThrownBy(() -> service.getById("bitcoin"))
                .isInstanceOf(CachePersistenceException.class)
                .hasMessageContaining("Failed to retrieve crypto data");
    }

    @Test
    void getById_validId_returnsCrypto() throws Exception {
        // sample JSON
        byte[] fakeJson = "{\"id\":\"ethereum\",\"symbol\":\"eth\"}".getBytes();
        Crypto expected = new Crypto();
        expected.setId("ethereum");
        expected.setSymbol("eth");

        when(redisTemplate.execute(any(RedisCallback.class))).thenReturn(fakeJson);
        when(objectMapper.readValue(fakeJson, Crypto.class)).thenReturn(expected);

        Crypto actual = service.getById("ethereum");

        assertThat(actual).isSameAs(expected);
    }

}

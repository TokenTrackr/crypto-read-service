package com.tokentrackr.crypto_read_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tokentrackr.crypto_read_service.model.Crypto;
import com.tokentrackr.crypto_read_service.model.request.GetAllCryptoRequest;
import com.tokentrackr.crypto_read_service.model.response.GetAllCryptoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetAllCryptoServiceImplTest {

    private StringRedisTemplate stringRedisTemplate;
    private ObjectMapper objectMapper;
    private GetAllCryptoServiceImpl service;

    @BeforeEach
    void setUp() {
        stringRedisTemplate = mock(StringRedisTemplate.class);
        objectMapper = new ObjectMapper(); // real ObjectMapper
        service = new GetAllCryptoServiceImpl(stringRedisTemplate, objectMapper);
    }

    @Test
    void noKeys_returnsEmptyResponse() {
        ZSetOperations<String, String> zSetOps = mock(ZSetOperations.class);
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(zSetOps.range("cryptos:all", 0, 9)).thenReturn(Collections.emptySet());

        GetAllCryptoResponse resp = service.getAllCrypto(
                GetAllCryptoRequest.builder().page(1).size(10).build());

        assertNotNull(resp);
        assertTrue(resp.getCrypto().isEmpty());
    }

    @Test
    void pagination_returnsCorrectSubset() throws JsonProcessingException {
        Crypto c1 = new Crypto(); c1.setId("1");
        Crypto c2 = new Crypto(); c2.setId("2");
        Crypto c3 = new Crypto(); c3.setId("3");

        String s1 = objectMapper.writeValueAsString(c1);
        String s2 = objectMapper.writeValueAsString(c2);
        String s3 = objectMapper.writeValueAsString(c3);

        Set<String> serializedSet = new LinkedHashSet<>(Arrays.asList(s1, s2, s3));

        ZSetOperations<String, String> zSetOps = mock(ZSetOperations.class);
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(zSetOps.range("cryptos:all", 0, 2)).thenReturn(serializedSet);

        GetAllCryptoRequest request = GetAllCryptoRequest.builder()
                .page(1)
                .size(3)
                .build();

        GetAllCryptoResponse resp = service.getAllCrypto(request);

        assertNotNull(resp);
        assertEquals(3, resp.getCrypto().size());
        assertEquals("1", resp.getCrypto().get(0).getId());
        assertEquals("2", resp.getCrypto().get(1).getId());
        assertEquals("3", resp.getCrypto().get(2).getId());
    }

    @Test
    void pagination_partialPage_returnsCorrectSubset() throws JsonProcessingException {
        Crypto c2 = new Crypto(); c2.setId("2");
        Crypto c3 = new Crypto(); c3.setId("3");

        String s2 = objectMapper.writeValueAsString(c2);
        String s3 = objectMapper.writeValueAsString(c3);

        Set<String> serializedSet = new LinkedHashSet<>(Arrays.asList(s2, s3));

        ZSetOperations<String, String> zSetOps = mock(ZSetOperations.class);
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(zSetOps.range("cryptos:all", 2, 3)).thenReturn(serializedSet);

        GetAllCryptoRequest request = GetAllCryptoRequest.builder()
                .page(2)
                .size(2)
                .build();

        GetAllCryptoResponse resp = service.getAllCrypto(request);

        assertNotNull(resp);
        assertEquals(2, resp.getCrypto().size());
        assertEquals("2", resp.getCrypto().get(0).getId());
        assertEquals("3", resp.getCrypto().get(1).getId());
    }
}

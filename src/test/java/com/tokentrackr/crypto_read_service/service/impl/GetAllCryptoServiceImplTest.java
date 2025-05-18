package com.tokentrackr.crypto_read_service.service.impl;

import com.tokentrackr.crypto_read_service.model.Crypto;
import com.tokentrackr.crypto_read_service.model.request.GetAllCryptoRequest;
import com.tokentrackr.crypto_read_service.model.response.GetAllCryptoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class GetAllCryptoServiceImplTest {

    private RedisTemplate<String, Crypto> redisTemplate;
    private GetAllCryptoServiceImpl service;

    @BeforeEach
    void setUp() {
        redisTemplate = Mockito.mock(RedisTemplate.class);
        service = new GetAllCryptoServiceImpl(redisTemplate);
    }

    @Test
    void noKeys_returnsEmptyResponse() {
        when(redisTemplate.keys("crypto:*")).thenReturn(Collections.emptySet());

        GetAllCryptoResponse resp = service.getAllCrypto(
                GetAllCryptoRequest.builder().page(1).size(10).marketCapRank(0).build());
        assertNotNull(resp);
        assertTrue(resp.getCrypto().isEmpty());
    }

    @Test
    void pagination_returnsCorrectSubset() {
        // Create some dummy Crypto objects
        Crypto c1 = new Crypto();
        c1.setId("1");
        Crypto c2 = new Crypto();
        c2.setId("2");
        Crypto c3 = new Crypto();
        c3.setId("3");

        List<Crypto> list = Arrays.asList(c1, c2, c3);

        // Use LinkedHashSet to preserve order of keys
        Set<String> keys = new LinkedHashSet<>(list.stream()
                .map(c -> "crypto:" + c.getId())
                .collect(Collectors.toList()));

        // Mock RedisTemplate and ValueOperations
        RedisTemplate<String, Crypto> redisTemplate = Mockito.mock(RedisTemplate.class);
        ValueOperations<String, Crypto> valueOps = Mockito.mock(ValueOperations.class);

        // When keys() is called, return our ordered set of keys
        when(redisTemplate.keys("crypto:*")).thenReturn(keys);

        // When opsForValue() is called, return mocked ValueOperations
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        // When get() is called for each key, return the corresponding Crypto object
        when(valueOps.get("crypto:1")).thenReturn(c1);
        when(valueOps.get("crypto:2")).thenReturn(c2);
        when(valueOps.get("crypto:3")).thenReturn(c3);

        // Create the service instance with the mocked redisTemplate
        GetAllCryptoServiceImpl service = new GetAllCryptoServiceImpl(redisTemplate);

        // Request: page 2, size 1 means second page, one item per page -> expect c2 (id=2)
        GetAllCryptoRequest request = GetAllCryptoRequest.builder()
                .page(2)  // Keep this
                .size(1)
                .marketCapRank(0)
                .build();

        // Call the method under test
        GetAllCryptoResponse resp = service.getAllCrypto(request);

        // Assert size is 1
        assertEquals(1, resp.getCrypto().size());

        // Assert returned Crypto is c2 (id=2)
        assertSame(c2, resp.getCrypto().get(0));
    }


}

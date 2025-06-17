package com.tokentrackr.crypto_read_service.service.impl;

import com.tokentrackr.crypto_read_service.model.Crypto;
import com.tokentrackr.crypto_read_service.model.request.GetAllCryptoRequest;
import com.tokentrackr.crypto_read_service.model.response.GetAllCryptoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetAllCryptoServiceImplTest {

    private RedisTemplate<String, Crypto> redisTemplate;
    private StringRedisTemplate stringRedisTemplate;
    private GetAllCryptoServiceImpl service;

    @BeforeEach
    void setUp() {
        redisTemplate = Mockito.mock(RedisTemplate.class);
        stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        service = new GetAllCryptoServiceImpl(redisTemplate, stringRedisTemplate);
    }

    @Test
    void noKeys_returnsEmptyResponse() {
        ZSetOperations<String, String> zSetOps = Mockito.mock(ZSetOperations.class);
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(zSetOps.range("cryptos:all", 0, 9)).thenReturn(Collections.emptySet());

        GetAllCryptoResponse resp = service.getAllCrypto(
                GetAllCryptoRequest.builder().page(1).size(10).build());

        assertNotNull(resp);
        assertTrue(resp.getCrypto().isEmpty());
    }

    @Test
    void pagination_returnsCorrectSubset() {
        // Prepare dummy Crypto objects
        Crypto c1 = new Crypto();
        c1.setId("1");
        Crypto c2 = new Crypto();
        c2.setId("2");
        Crypto c3 = new Crypto();
        c3.setId("3");

        // Keys matching the cryptos in Redis, in correct order
        Set<String> pageKeys = new LinkedHashSet<>(Arrays.asList("crypto:1", "crypto:2", "crypto:3"));

        // Mock StringRedisTemplate ZSetOperations
        ZSetOperations<String, String> zSetOps = Mockito.mock(ZSetOperations.class);
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOps);

        // For page 1, size 3 -> range(0, 2) returns these keys
        when(zSetOps.range("cryptos:all", 0, 2)).thenReturn(pageKeys);

        // Mock RedisTemplate ValueOperations for multiGet
        ValueOperations<String, Crypto> valueOps = Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        // Simulate multiGet returning corresponding cryptos in the same order
        when(redisTemplate.opsForValue().multiGet(pageKeys))
                .thenReturn(Arrays.asList(c1, c2, c3));

        // Create request for page 1, size 3 (to get all three cryptos)
        GetAllCryptoRequest request = GetAllCryptoRequest.builder()
                .page(1)
                .size(3)
                .build();

        // Call the service method
        GetAllCryptoResponse resp = service.getAllCrypto(request);

        // Assert response is correct
        assertNotNull(resp);
        assertEquals(3, resp.getCrypto().size());
        assertSame(c1, resp.getCrypto().get(0));
        assertSame(c2, resp.getCrypto().get(1));
        assertSame(c3, resp.getCrypto().get(2));
    }

    @Test
    void pagination_partialPage_returnsCorrectSubset() {
        // Prepare dummy Crypto objects
        Crypto c2 = new Crypto();
        c2.setId("2");
        Crypto c3 = new Crypto();
        c3.setId("3");

        // Keys for page 2 (page=2, size=2 means indexes 2 to 3)
        Set<String> pageKeys = new LinkedHashSet<>(Arrays.asList("crypto:2", "crypto:3"));

        // Mock StringRedisTemplate ZSetOperations
        ZSetOperations<String, String> zSetOps = Mockito.mock(ZSetOperations.class);
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOps);

        // page=2, size=2 -> range(2, 3)
        when(zSetOps.range("cryptos:all", 2, 3)).thenReturn(pageKeys);

        // Mock RedisTemplate ValueOperations for multiGet
        ValueOperations<String, Crypto> valueOps = Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        // Simulate multiGet returning cryptos
        when(redisTemplate.opsForValue().multiGet(pageKeys))
                .thenReturn(Arrays.asList(c2, c3));

        // Create request for page 2, size 2
        GetAllCryptoRequest request = GetAllCryptoRequest.builder()
                .page(2)
                .size(2)
                .build();

        // Call the service method
        GetAllCryptoResponse resp = service.getAllCrypto(request);

        // Assert response correctness
        assertNotNull(resp);
        assertEquals(2, resp.getCrypto().size());
        assertSame(c2, resp.getCrypto().get(0));
        assertSame(c3, resp.getCrypto().get(1));
    }
}

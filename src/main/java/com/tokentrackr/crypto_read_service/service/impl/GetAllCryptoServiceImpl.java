package com.tokentrackr.crypto_read_service.service.impl;

import com.tokentrackr.crypto_read_service.model.Crypto;
import com.tokentrackr.crypto_read_service.model.request.GetAllCryptoRequest;
import com.tokentrackr.crypto_read_service.model.response.GetAllCryptoResponse;
import com.tokentrackr.crypto_read_service.service.interfaces.GetAllCryptoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GetAllCryptoServiceImpl implements GetAllCryptoService {

    private static final String CRYPTOS_ZSET_KEY = "cryptos:all";

    private final RedisTemplate<String, Crypto> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public GetAllCryptoServiceImpl(RedisTemplate<String, Crypto> redisTemplate,
                                   StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate        = redisTemplate;
        this.stringRedisTemplate  = stringRedisTemplate;
    }

    @Override
    public GetAllCryptoResponse getAllCrypto(GetAllCryptoRequest request) {
        int page = Math.max(1, request.getPage());
        int size = Math.max(1, request.getSize());
        int start = (page - 1) * size;
        int end   = start + size - 1;  // inclusive index

        // 1) Fetch the ZSET members (keys) in ascending marketCapRank order
        Set<String> pageOfKeys = stringRedisTemplate.opsForZSet()
                .range(CRYPTOS_ZSET_KEY, start, end);

        if (pageOfKeys == null || pageOfKeys.isEmpty()) {
            return GetAllCryptoResponse.builder()
                    .crypto(Collections.emptyList())
                    .build();
        }

        // 2) Do a single MGET of those keys (each value is JSON→Crypto)
        List<Crypto> cryptos = redisTemplate.opsForValue()
                .multiGet(pageOfKeys)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return GetAllCryptoResponse.builder()
                .crypto(cryptos)
                .build();
    }
}



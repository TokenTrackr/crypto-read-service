package com.tokentrackr.crypto_read_service.service.impl;

import com.tokentrackr.crypto_read_service.model.Crypto;
import com.tokentrackr.crypto_read_service.model.request.GetAllCryptoRequest;
import com.tokentrackr.crypto_read_service.model.response.GetAllCryptoResponse;
import com.tokentrackr.crypto_read_service.service.interfaces.GetAllCryptoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GetAllCryptoServiceImpl implements GetAllCryptoService {
    private static final String CRYPTO_KEY_PATTERN = "crypto:*";

    private final RedisTemplate<String, Crypto> redisTemplate;

    @Autowired
    public GetAllCryptoServiceImpl(RedisTemplate<String, Crypto> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GetAllCryptoResponse getAllCrypto(GetAllCryptoRequest request) {
        // Generate a pattern to fetch all crypto keys (crypto:*), for example, crypto:bitcoin
        String pattern = "crypto:*";

        // Fetch keys that match the pattern from Redis
        List<String> keys = redisTemplate.keys(pattern)
                .stream()
                .collect(Collectors.toList());

        // If no keys found, return an empty response
        if (keys.isEmpty()) {
            return GetAllCryptoResponse.builder()
                    .crypto(Collections.emptyList())
                    .build();
        }

        // Retrieve the corresponding values (Crypto objects) from Redis for each key
        List<Crypto> cryptos = keys.stream()
                .map(key -> redisTemplate.opsForValue().get(key))
                .collect(Collectors.toList());

        // Optionally, handle pagination with `page` and `size` in request.
        int start = Math.max(0, (request.getPage() - 1) * request.getSize()); // Ensure start is >= 0
        int end = Math.min(start + request.getSize(), cryptos.size()); // Ensure end is within the list bounds

        // If the requested page range exceeds the list of cryptos, handle it gracefully
        List<Crypto> paginatedCryptos = cryptos.subList(start, end);

        // Wrap the result in GetAllCryptoResponse and return
        return GetAllCryptoResponse.builder()
                .crypto(paginatedCryptos)
                .build();
    }

}

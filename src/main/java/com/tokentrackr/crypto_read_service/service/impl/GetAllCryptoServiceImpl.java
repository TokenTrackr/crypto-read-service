package com.tokentrackr.crypto_read_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tokentrackr.crypto_read_service.model.Crypto;
import com.tokentrackr.crypto_read_service.model.request.GetAllCryptoRequest;
import com.tokentrackr.crypto_read_service.model.response.GetAllCryptoResponse;
import com.tokentrackr.crypto_read_service.service.interfaces.GetAllCryptoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetAllCryptoServiceImpl implements GetAllCryptoService {
    private static final String CRYPTOS_ZSET_KEY = "cryptos:all";
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public GetAllCryptoResponse getAllCrypto(GetAllCryptoRequest request) {
        int page = Math.max(1, request.getPage());
        int size = Math.max(1, request.getSize());
        int start = (page - 1) * size;
        int end = start + size - 1;

        // Single roundtrip to get full data
        Set<String> serializedCryptos = stringRedisTemplate.opsForZSet()
                .range(CRYPTOS_ZSET_KEY, start, end);

        List<Crypto> cryptos = serializedCryptos.stream()
                .map(serialized -> {
                    try {
                        return objectMapper.readValue(serialized, Crypto.class);
                    } catch (JsonProcessingException e) {
                        log.warn("Deserialization failed for crypto data", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return GetAllCryptoResponse.builder()
                .crypto(cryptos)
                .build();
    }
}



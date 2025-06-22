package com.tokentrackr.crypto_read_service.controller;

import com.tokentrackr.crypto_read_service.model.Crypto;
import com.tokentrackr.crypto_read_service.model.request.GetAllCryptoRequest;
import com.tokentrackr.crypto_read_service.model.response.GetAllCryptoResponse;
import com.tokentrackr.crypto_read_service.service.interfaces.GetAllCryptoService;
import com.tokentrackr.crypto_read_service.service.interfaces.GetCryptoByIdService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/crypto")
public class CryptoController {
    private final GetAllCryptoService getAllCryptoService;
    private final GetCryptoByIdService getCryptoByIdService; // New service
    private final MeterRegistry meterRegistry;

    @GetMapping
    public ResponseEntity<GetAllCryptoResponse> getAllCrypto(
            @RequestParam(defaultValue = "0") int marketCapRank,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "250") int size) {

        GetAllCryptoRequest request = GetAllCryptoRequest.builder()
                .marketCapRank(marketCapRank)
                .page(page)
                .size(size)
                .build();
        GetAllCryptoResponse response = getAllCryptoService.getAllCrypto(request);

        if (response != null && response.getCrypto() != null) {
            meterRegistry.summary("crypto_assets_returned").record(response.getCrypto().size());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Crypto> getCryptoById(@PathVariable String id) {
        Crypto crypto = getCryptoByIdService.getById(id);

        if (crypto != null) {
            // Record metric for single crypto fetch
            meterRegistry.counter("crypto_by_id_requests", "id", id).increment();
            return ResponseEntity.ok(crypto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

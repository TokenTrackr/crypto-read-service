package com.tokentrackr.crypto_read_service.controller;

import com.tokentrackr.crypto_read_service.model.request.GetAllCryptoRequest;
import com.tokentrackr.crypto_read_service.model.response.GetAllCryptoResponse;
import com.tokentrackr.crypto_read_service.service.interfaces.GetAllCryptoService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/crypto")
public class CryptoController {
    private final GetAllCryptoService getAllCryptoService;
    private final MeterRegistry meterRegistry;

    @GetMapping
    public ResponseEntity<GetAllCryptoResponse> getAllCrypto(@RequestParam(defaultValue = "0") int marketCapRank,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "250") int size) {
        GetAllCryptoRequest request = GetAllCryptoRequest.builder()
                .marketCapRank(marketCapRank)
                .page(page)
                .size(size)
                .build();
        GetAllCryptoResponse response = getAllCryptoService.getAllCrypto(request);

        // Record the number of cryptos returned in the custom metric
        if (response != null && response.getCrypto() != null) {
            meterRegistry.summary("crypto_assets_returned").record(response.getCrypto().size());
        }

        // Always return 200 with the array (could be [])
        return ResponseEntity.ok(response);
    }

}

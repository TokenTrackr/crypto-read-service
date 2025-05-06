package com.tokentrackr.crypto_read_service.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class GetAllCryptoRequest {
    private int marketCapRank;
    private int page;
    private int size;
}

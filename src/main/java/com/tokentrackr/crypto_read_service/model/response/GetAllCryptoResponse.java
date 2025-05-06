package com.tokentrackr.crypto_read_service.model.response;

import com.tokentrackr.crypto_read_service.model.Crypto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAllCryptoResponse {
    private List<Crypto> crypto;
}

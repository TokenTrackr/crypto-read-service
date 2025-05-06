package com.tokentrackr.crypto_read_service.service.interfaces;

import com.tokentrackr.crypto_read_service.model.Crypto;
import com.tokentrackr.crypto_read_service.model.request.GetAllCryptoRequest;
import com.tokentrackr.crypto_read_service.model.response.GetAllCryptoResponse;

import java.util.List;

public interface GetAllCryptoService {
    /**
     * Retrieves all cryptocurrencies from the database.
     *
     * @return a list of all cryptocurrencies.
     */
    public GetAllCryptoResponse getAllCrypto(GetAllCryptoRequest request);
}

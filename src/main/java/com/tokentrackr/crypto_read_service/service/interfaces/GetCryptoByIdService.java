package com.tokentrackr.crypto_read_service.service.interfaces;

import com.tokentrackr.crypto_read_service.model.Crypto;

public interface GetCryptoByIdService {
    public Crypto getById(String id);
}

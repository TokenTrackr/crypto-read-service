package com.tokentrackr.crypto_read_service.service.interfaces;

import com.tokentrackr.crypto_read_service.model.Crypto;

import java.util.List;

public interface MessageConsumerService {
    public void consumeMessage(List<Crypto> cryptoMessages);
}

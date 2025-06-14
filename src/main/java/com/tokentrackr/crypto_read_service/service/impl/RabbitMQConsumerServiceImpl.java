package com.tokentrackr.crypto_read_service.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tokentrackr.crypto_read_service.exception.CryptoDataProcessingException;
import com.tokentrackr.crypto_read_service.model.Crypto;
import com.tokentrackr.crypto_read_service.service.interfaces.CryptoCacheService;
import com.tokentrackr.crypto_read_service.service.interfaces.MessageConsumerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitMQConsumerServiceImpl {

    private final CryptoCacheService cryptoCacheService;

    // The queue name should match the one you configured in your messaging setup
    @RabbitListener(queues = "${rabbitmq.queue.name:crypto-read-queue}")
    public void consumeMessage(List<Crypto> cryptoMessages) {
        try {
            log.info("Processing batch of {} crypto messages", cryptoMessages.size());
            cryptoCacheService.storeCryptoPage(0,250,cryptoMessages);
            log.info("Successfully processed batch of {} crypto messages", cryptoMessages.size());
        } catch (Exception e) {
            log.error("Error processing batch crypto messages: {}", e.getMessage(), e);
            throw new CryptoDataProcessingException("Failed to process crypto data batch", e);
        }
    }
}


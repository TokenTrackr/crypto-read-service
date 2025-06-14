//package com.tokentrackr.crypto_read_service.service.impl;
//
//import com.tokentrackr.crypto_read_service.exception.CryptoDataProcessingException;
//import com.tokentrackr.crypto_read_service.model.Crypto;
//import com.tokentrackr.crypto_read_service.service.interfaces.CryptoCacheService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//
//import java.util.Collections;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class RabbitMQConsumerServiceImplTest {
//
//    private CryptoCacheService cacheService;
//    private RabbitMQConsumerServiceImpl consumer;
//
//    @BeforeEach
//    void setUp() {
//        cacheService = Mockito.mock(CryptoCacheService.class);
//        consumer = new RabbitMQConsumerServiceImpl(cacheService);
//    }
//
//    @Test
//    void consumeMessage_success() {
//        assertDoesNotThrow(() -> consumer.consumeMessage(Collections.emptyList()));
//        Mockito.verify(cacheService).cacheCrypto(Collections.emptyList());
//    }
//
//    @Test
//    void consumeMessage_throwsProcessingException() {
//        Mockito.doThrow(new RuntimeException("oops")).when(cacheService).cacheCrypto(Mockito.anyList());
//        assertThrows(CryptoDataProcessingException.class,
//                () -> consumer.consumeMessage(Collections.emptyList()));
//    }
//}
//

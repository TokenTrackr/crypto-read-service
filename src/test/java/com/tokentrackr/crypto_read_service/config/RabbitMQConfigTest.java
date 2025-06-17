package com.tokentrackr.crypto_read_service.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(RabbitMQConfig.class)
public class RabbitMQConfigTest {

    @Autowired
    private Queue cryptoQueue;

    @Autowired
    private DirectExchange cryptoExchange;

    @Autowired
    private Binding binding;

    @Test
    void queueProperties() {
        assertThat(cryptoQueue.getName()).isEqualTo("crypto-read-queue");
        assertThat(cryptoQueue.isDurable()).isTrue();
    }

    @Test
    void exchangeProperties() {
        assertThat(cryptoExchange.getName()).isEqualTo("crypto-read-exchange");
        assertThat(cryptoExchange.isDurable()).isTrue();
        assertThat(cryptoExchange.isAutoDelete()).isFalse();
    }

    @Test
    void bindingLinksQueueToExchange() {
        assertThat(binding.getExchange()).isEqualTo(cryptoExchange.getName());
        assertThat(binding.getDestination()).isEqualTo(cryptoQueue.getName());
        assertThat(binding.getRoutingKey()).isEqualTo("crypto-read-key");
    }
}

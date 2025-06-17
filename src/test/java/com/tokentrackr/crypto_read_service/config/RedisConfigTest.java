package com.tokentrackr.crypto_read_service.config;
import com.tokentrackr.crypto_read_service.model.Crypto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(RedisConfig.class)
class RedisConfigTest {

    @Autowired
    LettuceConnectionFactory connectionFactory;

    @Autowired
    RedisTemplate<String, Crypto> redisTemplate;

    @Test
    void redisTemplateSerializers() {
        assertThat(redisTemplate.getKeySerializer()).isNotNull();
        assertThat(redisTemplate.getValueSerializer()).isNotNull();
    }
}
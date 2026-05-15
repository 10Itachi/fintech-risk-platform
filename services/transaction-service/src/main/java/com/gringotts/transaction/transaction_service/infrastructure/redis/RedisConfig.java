package com.gringotts.transaction.transaction_service.infrastructure.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

//    // Pull the value from .env, default to 3600000 (1hr) if missing
//    @Value("${CACHE_TTL:3600000}")
//    private long cacheTtl;
//


    /**
     * AUTOMATIC CACHING RULES:
     * Defines how @Cacheable works.
     * - Sets the expiration time (TTL).
     * - Converts Java Objects into JSON format for storage.
     * - Prevents saving 'null' values to the cache.
     */
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    /**
     * MANUAL REDIS ACCESS:
     * A helper template used to manually fetch or save data to Redis.
     * - Configured to handle keys and values as simple Strings.
     * - Used when you autowire RedisTemplate in your services.
     */
    @Bean
    public RedisTemplate<String , String>redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return  redisTemplate;
    }
}

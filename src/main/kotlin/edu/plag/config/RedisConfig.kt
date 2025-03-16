package edu.plag.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
import java.time.Duration


@Configuration
class RedisConfig(
    @Value("\${spring.data.redis.host}") private val redisHost: String,

    @Value("\${spring.data.redis.port}") private val redisPort: Int
) {
    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val configuration = RedisStandaloneConfiguration(redisHost, redisPort)
        return LettuceConnectionFactory(configuration)
    }

    private fun myDefaultCacheConfig(duration: Duration): RedisCacheConfiguration {
        return RedisCacheConfiguration.defaultCacheConfig().entryTtl(duration)
            .serializeValuesWith(SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer()))
    }

    @Bean
    fun cacheManager(): RedisCacheManager {
        val cacheConfig: RedisCacheConfiguration =
            myDefaultCacheConfig(Duration.ofMinutes(10)).disableCachingNullValues()
        return RedisCacheManager.builder(redisConnectionFactory()).cacheDefaults(cacheConfig)
            .withCacheConfiguration("files", myDefaultCacheConfig(Duration.ofMinutes(5)))
            .withCacheConfiguration("file", myDefaultCacheConfig(Duration.ofMinutes(1))).build()
    }
}
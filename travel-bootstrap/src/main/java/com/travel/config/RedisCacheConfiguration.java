package com.travel.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * <p><b>作用：</b>在已存在 Redis 连接工厂时，注册一个通用的 {@link RedisTemplate}，供各模块做缓存、分布式锁等。</p>
 * <ul>
 *   <li><b>键：</b>使用字符串序列化，便于在 Redis CLI 里阅读。</li>
 *   <li><b>值：</b>使用 Jackson JSON 序列化，可直接存 Java 对象（注意类型与安全）。</li>
 *   <li><b>Bean 名：</b>{@code redisObjectTemplate}，与 Spring Boot 默认的 {@code redisTemplate} 区分。</li>
 * </ul>
 */
@Configuration
@ConditionalOnBean(RedisConnectionFactory.class)
public class RedisCacheConfiguration {

    /**
     * 构建 {@code RedisTemplate<String, Object>}：普通键值与 Hash 的键都用 String，值都用 JSON。
     *
     * @param connectionFactory Spring Data Redis 自动配置的连接工厂
     * @param objectMapper      与 Web 层一致的 Jackson，保证序列化行为统一
     */
    @Bean
    public RedisTemplate<String, Object> redisObjectTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper
    ) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 普通 key / hash field 使用 UTF-8 字符串
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // value / hash value 使用带类型信息的 JSON（GenericJackson2JsonRedisSerializer）
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}

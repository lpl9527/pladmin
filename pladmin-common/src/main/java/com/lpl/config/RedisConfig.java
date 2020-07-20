package com.lpl.config;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import javafx.beans.binding.ObjectExpression;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * @author lpl
 * Redis配置类。实现CachingConfigurerSupport类，自定义Redis缓存管理器
 */
@Slf4j
@Configuration
@EnableCaching  //启用缓存
@ConditionalOnClass(RedisOperations.class)     //当给定的类名在类路径上存在，则实例化当前Bean
@EnableConfigurationProperties(RedisProperties.class)   //将对应类的配置类（标注了@ConfigurationProperties注解）的类注入spring容器
public class RedisConfig extends CachingConfigurerSupport {     //继承此类用于自定义缓存读写机制，这里只重写了缓存的keyGenerator()和errorHandler()

    /**
     * 设置redis默认过期时间，默认2小时
     * 设置@cacheable缓存value序列化和反序列化方式
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {

        FastJsonRedisSerializer<Object> fastJsonRedisSerializer = new FastJsonRedisSerializer<>(Object.class);
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig(); //使用默认的redis缓存配置
        //设置redis value序列化、反序列化机制，泛型定义为Object用于对各种Java对象进行转换。设置默认有效时间为2小时
        redisCacheConfiguration = redisCacheConfiguration.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(fastJsonRedisSerializer)).entryTtl(Duration.ofHours(2));

        return redisCacheConfiguration;
    }
    /**
     * 配置RedisTemplate。设置redis存取操作时key和value的序列化、反序列化方式
     */
    @Bean(name = "redisTemplate")
    @ConditionalOnMissingBean(name = "redisTemplate")   //给定实体Bean名称不存在时，则实例化该Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {

        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        //value的序列化方式
        FastJsonRedisSerializer<Object> fastJsonRedisSerializer = new FastJsonRedisSerializer<>(Object.class);
        redisTemplate.setValueSerializer(fastJsonRedisSerializer);
        redisTemplate.setHashValueSerializer(fastJsonRedisSerializer);
        //key的序列化方式
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);

        //设置alibaba fastJson全局AutoType，自动类型转换
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        //建议使用这种，指定实体类所在包，小范围指定白名单
        //ParserConfig.getGlobalInstance().addAccept("com.lpl.domain");

        //设置连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        return redisTemplate;
    }

    /**
     * 重写方法，自定义缓存key生成策略
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            l
        }
    }
}

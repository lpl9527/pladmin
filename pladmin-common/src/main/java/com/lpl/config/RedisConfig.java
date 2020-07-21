package com.lpl.config;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.lpl.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

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

        //注意：这里使用的FastJsonRedisSerializer是下面重写的序列化器
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
            Map<String, Object> container = new HashMap<>(3);
            Class<?> targetClass = target.getClass();
            //放入类地址
            container.put("class", targetClass.toGenericString());
            //放入方法名称
            container.put("methodName", method.getName());
            //放入包名称
            container.put("package", targetClass.getPackage());
            //放入参数列表
            for (int i=0; i<params.length; i++){
                container.put(String.valueOf(i), params[i]);
            }
            //转换为json字符串
            String jsonString = JSON.toJSONString(container);

            //进行SHA256 HASH运算，得到一个SHA256摘要作为key
            return DigestUtils.sha256Hex(jsonString);
        };
    }

    /**
     * 重写缓存管理器的异常处理器
     */
    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        //异常处理，当redis发生异常，打印异常日志，但是程序正常走
        log.info("初始化 -> [{}]", "Redis CacheErrorHandler");
        return new CacheErrorHandler() {
            /**
             * cacheAble异常处理
             */
            @Override
            public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
                log.error("Redis occur handleCacheGetError：key -> [{}]", key, e);
            }

            /**
             * cachePut异常处理
             */
            @Override
            public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
                log.error("Redis occur handleCachePutError：key -> [{}]；value -> [{}]", key, value, e);
            }

            /**
             * cacheEvict异常处理
             */
            @Override
            public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
                log.error("Redis occur handleCacheEvictError：key -> [{}]", key, e);
            }

            /**
             * 缓存清理异常处理
             */
            @Override
            public void handleCacheClearError(RuntimeException e, Cache cache) {
                log.error("Redis occur handleCacheClearError：", e);
            }
        };
    }
}

/**
 *  重写redis序列化器，FastJsonRedisSerializer
 */
class FastJsonRedisSerializer<T> implements RedisSerializer<T> {

    private final Class<T> clazz;

    /**
     * 提供构造器初始化序列化字节码对象，用于反序列化为指定对象
     * @param clazz
     */
    FastJsonRedisSerializer(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }
    /**
     * 序列化
     */
    @Override
    public byte[] serialize(T t) {
        if (null == t) {
            return new byte[0];
        }
        //字节码对象不为空时返回指定编码的序列化字节数组
        return JSON.toJSONString(t, SerializerFeature.WriteClassName).getBytes(StandardCharsets.UTF_8);
    }
    /**
     * 反序列化
     */
    @Override
    public T deserialize(byte[] bytes) {
        if (null == bytes || bytes.length <= 0) {
            return null;
        }
        String str = new String(bytes, StandardCharsets.UTF_8);     //将数组按指定编码转化为字符串
        return JSON.parseObject(str, clazz);    //将字符串转化为class类型的对象
    }
}

/**
 * 重写redis序列化器，StringRedisSerializer
 */
class StringRedisSerializer implements RedisSerializer<Object> {

    private final Charset charset;

    StringRedisSerializer() {
        this(StandardCharsets.UTF_8);
    }
    private StringRedisSerializer(Charset charset) {
        Assert.notNull(charset, "Charset不能为空！");
        this.charset = charset;
    }
    /**
     * 序列化
     */
    @Override
    public byte[] serialize(Object obj) {
        String str = JSON.toJSONString(obj);
        if (StringUtils.isBlank(str)) {
            return null;
        }
        str = str.replace("\"", "");    //去掉其中的双引号
        return str.getBytes(charset);   //转化为指定编码的字节数组
    }
    /**
     * 反序列化
     */
    @Override
    public String deserialize(byte[] bytes) {
        return (bytes == null ? null : new String(bytes, charset));
    }
}

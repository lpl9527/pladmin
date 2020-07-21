package com.lpl.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author lpl
 * redis操作工具类
 */
@Component
public class RedisUtils {

    private RedisTemplate<Object, Object> redisTemplate;

    @Value("${jwt.online-key}")
    private String onlineKey;

    public RedisUtils(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 设置指定key的有效时间
     * @param key   key值
     * @param time  有效时间（单位：秒）
     */
    public boolean expire(String key, long  time) {
        try{
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 设置key的有效时间
     * @param key   key值
     * @param time  有效时间
     * @param timeUnit  单位
     */
    public boolean expire(String key, long time, TimeUnit timeUnit) {
        try{
            if (time > 0) {
                redisTemplate.expire(key, time, timeUnit);
            }
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 获取key的剩余过期时间
     * @param key   key值
     * @return  过期时间（单位：秒），返回表示永久有效
     */
    public long getExpire(Object key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     *  查找redis中匹配的key
     * @param patternKey   key
     * @return  匹配的key的String列表
     */
    public List<String> scan(String patternKey) {
        ScanOptions options = ScanOptions.scanOptions().match(patternKey).build();

        //获取redis连接
        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
        RedisConnection redisConnection = Objects.requireNonNull(factory).getConnection();
        Cursor<byte[]> cusor = redisConnection.scan(options);
        List<String> result = new ArrayList<>();
        while(cusor.hasNext()) {
            result.add(new String(cusor.next()));
        }
        try{
            RedisConnectionUtils.releaseConnection(redisConnection, factory);   //关闭连接
        }catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    /**
     *  分页匹配查询key
     * @param patternKey    key
     * @param page  页码，页码从0开始
     * @param size  每页数目
     * @return  分页匹配的key的String列表
     */
    public List<String> findKeysForPage(String patternKey, int page, int size) {
        ScanOptions options = ScanOptions.scanOptions().match(patternKey).build();

        //获取redis连接
        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
        RedisConnection redisConnection = Objects.requireNonNull(factory).getConnection();

        Cursor<byte[]> cursor = redisConnection.scan(options);

        //放置分页匹配结果
        List<String> result = new ArrayList<>(size);
        int tmpIndex = 0;
        int fromIndex = page * size;
        int toIndex = page * size + size;
        while (cursor.hasNext()) {
            if (tmpIndex >= fromIndex && tmpIndex < toIndex) {
                result.add(new String(cursor.next()));
                tmpIndex++;
                continue;
            }
            //获取到满足条件的数据后就可以退出了
            if (tmpIndex >= toIndex){
                break;
            }
            tmpIndex++;
            cursor.next();  //使指针后移
        }
        try {
            RedisConnectionUtils.releaseConnection(redisConnection, factory);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     *  判断key是否存在
     * @param key
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 批量删除key
     * @param keys  可变key字符串
     */
    public void del(String... keys) {
        if (keys != null && keys.length > 0) {
            if (keys.length == 1){
                boolean result = redisTemplate.delete(keys[0]);
                System.out.println("--------------------------------------------");
                System.out.println(new StringBuilder("删除缓存：").append(keys[0]).append("，结果：").append(result));
                System.out.println("--------------------------------------------");
            } else {
                Set<Object> keySet = new HashSet<>();
                for (String key : keys) {
                    keySet.addAll(redisTemplate.keys(key));     //将redis中存在的key全部放入集合
                }
                long count = redisTemplate.delete(keySet);  //批量删除key，返回删除成功的个数
                System.out.println("--------------------------------------------");
                System.out.println("成功删除缓存：" + keySet.toString());
                System.out.println("缓存删除数量：" + count + "个");
                System.out.println("--------------------------------------------");
            }
        }
    }
    /**
     * 获取单个key的值
     * @param key
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }
    public List<Object> mutiGet(List<String> keys) {
        List<Object> objects = redisTemplate.opsForValue().multiGet(Collections.singleton(keys));
        return objects;
    }
}

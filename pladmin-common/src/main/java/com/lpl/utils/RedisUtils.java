package com.lpl.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.security.core.parameters.P;
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

    //---------------------------------------String类型操作------------------------------------------
    /**
     * 获取单个key的值
     * @param key
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }
    /**
     * 批量获取key的值
     * @param keys
     */
    public List<Object> mutiGet(List<String> keys) {
        List<Object> objects = redisTemplate.opsForValue().multiGet(Collections.singleton(keys));
        return objects;
    }

    /**
     * 设置一个key、value
     * @param key
     * @param value
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 设置缓存key、value，并设置过期时间
     * @param key
     * @param value
     * @param time  值大于0，单位：秒。若小于0，表示没有过期时间
     * @return  成功与否
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0){
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            }else {
                set(key, value);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 设置缓存key、value并设置过期时间，时间单位
     * @param key
     * @param value
     * @param time
     * @param timeUnit
     * @return
     */
    public boolean set(String key, Object value, long time, TimeUnit timeUnit) {
        try {
            if (time > 0){
                redisTemplate.opsForValue().set(key, value, time, timeUnit);
            }else {
                set(key, value);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    //---------------------------------------Map（Hash）类型操作------------------------------------------

    /**
     * 获取Map中数据
     * @param key   Map对应的key
     * @param item  Map中某一项
     */
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取Map中所有项
     * @param key   Map对应名称
     */
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 设置多个Map值
     * @param key
     * @param map
     */
    public boolean hmset(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 设置多个Map值，并设置过期时间
     * @param key
     * @param map
     * @param time
     */
    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0){
                expire(key, time);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向Hash表中插入一条数据，不存在时则创建
     * @param key
     * @param item
     * @param value
     */
    public boolean hset(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 向Hash表中插入一条数据，并设置过期时间，不存在时则创建
     * @param key
     * @param item
     * @param value
     * @param time
     */
    public boolean hset(String key, String item, Object value, long time) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0){
                expire(key, time);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除Hash表中的值
     * @param key
     * @param item
     */
    public void hdel(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中是否存在某一项
     * @param key
     * @param item
     * @return
     */
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * 设置Hash某一项递增，如果不存在就会创建并把递增结果返回
     * @param key
     * @param item
     * @param by  递增步长，大于0
     */
    public double hincr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    /**
     * Hash某一项递减
     * @param key
     * @param item
     * @param by
     */
    public double hdecr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }

    //---------------------------------------Set类型操作------------------------------------------

    /**
     * 获取Set中所有值
     * @param key
     */
    public Set<Object> sget(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 查询指定key的集合是否存在指定value
     * @param key
     * @param value
     */
    public boolean sHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     *  将多个值放入Set
     * @param key
     * @param values
     * @return 成功个数
     */
    public long sSet(String key, Object... values){
        try {
            return redisTemplate.opsForSet().add(key, values);
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 放入多个值到Set，并设置过期时间
     * @param key
     * @param time
     * @param values
     * @return  设置成功个数
     */
    public long sSet(String key, long time, Object... values) {
        try {
            long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0){
                expire(key, time);
            }
            return count;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取Set集合的长度
     * @param key
     * @return 长度
     */
    public long sGetSetSize(String key) {
        try{
            return redisTemplate.opsForSet().size(key);
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 移除Set多个值
     * @param key
     * @param values
     * @return  移除成功个数
     */
    public long setRemove(String key, Object... values) {
        try{
            return redisTemplate.opsForSet().remove(key, values);
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    //---------------------------------------List类型操作------------------------------------------

    /**
     * 获取List开始到结束下标的元素，0表示第一个元素，-1表示最后一个元素，-2表示倒数第二个元素，以此类推...
     * @param key
     * @param start 开始下标
     * @param end   结束下标
     */
    public List<Object> lGet(String key, long start, long end) {
        try{
            return redisTemplate.opsForList().range(key, start, end);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取List的长度
     * @param key
     */
    public long lGetListSize(String key) {
        try{
            return redisTemplate.opsForList().size(key);
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取指定索引对应的元素
     * @param key
     * @param index  0表示第一个元素，-1表示最后一个元素，-2表示倒数第二个元素，以此类推...
     */
    public Object lGetIndex(String key, long index) {
        try{
            return redisTemplate.opsForList().index(key, index);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将指定值从List右侧放入
     * @param key
     * @param value
     */
    public boolean lSet(String key, Object value) {
        try{
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 将指定值从List右侧放入，并设置过期时间
     * @param key
     * @param value
     * @param time  单位：秒
     */
    public boolean lSet(String key, Object value, long time) {
        try{
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0){
                expire(key, time);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 同时将多个值放入List
     * @param key
     * @param values
     */
    public boolean lSet(String key, List<Object> values){
        try{
            redisTemplate.opsForList().rightPushAll(key, values);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 同时将多个值放入List，并设置过期时间
     * @param key
     * @param values
     * @param time  过期时间，单位：秒
     */
    public boolean lSet(String key, List<Object> values, long time){
        try{
            redisTemplate.opsForList().rightPushAll(key, values);
            if (time > 0){
                expire(key, time);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据索引修改某个值
     * @param key
     * @param index 索引
     * @param value 更新的值
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        try{
            redisTemplate.opsForList().set(key, index, value);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 移除指定值的个数
     * @param key
     * @param value 要移除的值
     * @param count 移除的个数
     * @return
     */
    public long lRemove(String key, Object value, long count){
        try{
            return redisTemplate.opsForList().remove(key, count, value);
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 删除指定前缀的ids集合
     * @param prefix
     * @param ids
     * @return
     */
    public long delByKeys(String prefix, Set<Long> ids) {
        try{
            Set<Object> keys = new HashSet<>();
            for (Long id : ids){
                //将指定id对应的key放入要移除的keys集合
                keys.addAll(redisTemplate.keys(new StringBuffer(prefix).append(id).toString()));
            }
            long count = redisTemplate.delete(keys);

            System.out.println("--------------------------------------------");
            System.out.println("成功删除缓存：" + keys.toString());
            System.out.println("缓存删除数量：" + count + "个");
            System.out.println("--------------------------------------------");

            return count;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }
}

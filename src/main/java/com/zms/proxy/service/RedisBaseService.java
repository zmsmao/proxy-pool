package com.zms.proxy.service;

import com.alibaba.fastjson2.JSONObject;
import com.zms.proxy.domain.ProxyIp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Description : Object
 * @author: zeng.maosen
 */
@Slf4j
@Service
public class RedisBaseService {

    @Resource
    public RedisTemplate redisTemplate;


    public void produce(String key, ProxyIp proxyIp) {
        log.info(key+":往消息队列生成消息------- = {}", proxyIp);
        ListOperations<String, Object> listOperations = redisTemplate.opsForList();
        // leftPush和rightPop对应，左边入队，右边出队
        listOperations.leftPush(key, proxyIp);
        // 因为出队是阻塞读取的，所以上一步入队后，数据立刻就被驱走了，下一步size=0
        Long size = listOperations.size(key);
        log.info("size = " + size);
    }

    public ProxyIp consumer(String key) {
        ListOperations<String, Object> listOperations = redisTemplate.opsForList();
        Long size = listOperations.size(key);
        //  0时间，表示阻塞永久,待机一小时后，再次发消息，消费不了了，阻塞有问题啊。还得轮寻啊
        Object obj = listOperations.rightPop(key, 1L, TimeUnit.SECONDS);
        ProxyIp proxyIp = (ProxyIp) obj;
        // 队列为空返回null
        if (size == null || size == 0) {
            return null;
        }
        log.info("{} = {}", key, proxyIp);
        return proxyIp;
    }

    public void produceMap(String key, ProxyIp proxyIp) {
        log.info(key+":往消息Map生成消息------- = {}", proxyIp);
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        hashOperations.put(key, proxyIp.getProxy(), proxyIp);
    }

    public ProxyIp consumerMap(String key, String keyMap) {
        log.info(key+":往消息Map推出消息------- = {}", keyMap);
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        Object o = hashOperations.get(key, keyMap);
        ProxyIp proxyIp = (ProxyIp) o;
        log.info("{} = {}", key, proxyIp);
        return proxyIp;

    }

    public ProxyIp getRandomOne(String key) {
        ListOperations<String, Object> listOperations = redisTemplate.opsForList();
        Long size = listOperations.size(key);
        log.info("size = " + size);
        if (size != null) {
            size = (long) (size * Math.random());
        }
        //  0时间，表示阻塞永久,待机一小时后，再次发消息，消费不了了，阻塞有问题啊。还得轮寻啊
        Object obj = listOperations.index(key, size);
        assert obj != null;
        ProxyIp proxyIp = (ProxyIp) obj;
        // 队列为空返回null
        if (size == 0) {
            return null;
        }
        log.info("{} = {}", key, proxyIp);
        return proxyIp;
    }

//    public ProxyIp getOne(String key){
//        ListOperations<String, Object> listOperations = redisTemplate.opsForList();
//        Long size = listOperations.size(key);
//        log.info("size = " + size);
//        //  0时间，表示阻塞永久,待机一小时后，再次发消息，消费不了了，阻塞有问题啊。还得轮寻啊
//        Object obj = listOperations.index(key, -1);
//        assert obj != null;
//        ProxyIp proxyIp = (ProxyIp) obj;
//        // 队列为空返回null
//        if (size == null || size == 0) {
//            return null;
//        }
//        log.info("{} = {}", key, proxyIp);
//        return proxyIp;
//    }

    public Object getRedisObject(String key) {
        Object object;
        try {
            object = consumer(key);
            return object;
        } catch (Exception e) {
            return null;
        }
    }

    public Object getRedisMapObject(String key,String keyMap) {
        Object object;
        try {
            object = consumerMap(key,keyMap);
            return object;
        } catch (Exception e) {
            return null;
        }
    }

    public <T> List<T> getRedisList(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    public Long getRedisListSize(String key) {
        try {
            ListOperations<String, Object> listOperations = redisTemplate.opsForList();
            Long size = listOperations.size(key);
            if (size == null) {
                return 0L;
            }
            return size;
        } catch (Exception e) {
            return 0L;
        }
    }

    public void delete(String key) {
        log.info("删除成功==================>" + key);
        redisTemplate.delete(key);
    }

    public boolean expire(final String key, final long timeout, final TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }
}

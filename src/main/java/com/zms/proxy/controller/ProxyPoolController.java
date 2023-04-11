package com.zms.proxy.controller;

import com.zms.proxy.domain.ProxyIp;
import com.zms.proxy.service.RedisBaseService;
import com.zms.proxy.util.constant.RedisQueue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description : Object
 * @author: zeng.maosen
 */
@RequestMapping
@RestController
public class ProxyPoolController {

    @Resource
    RedisBaseService redisBaseService;


    @GetMapping("/get")
    public ProxyIp get() throws Exception {
        ProxyIp consumer = redisBaseService.getRandomOne(RedisQueue.EFFECTIVE_KEY_CN.getValue());
        return consumer;
    }

    @GetMapping("/pop")
    public ProxyIp pop() throws Exception {
        ProxyIp consumer = redisBaseService.consumer(RedisQueue.EFFECTIVE_KEY_CN.getValue());
        return consumer;
    }

    @GetMapping("/list")
    public List<ProxyIp> list() throws Exception {
        List<ProxyIp> redisList = redisBaseService.getRedisList(RedisQueue.EFFECTIVE_KEY_CN.getValue());
        return redisList;
    }

    @GetMapping("/count")
    public Long  count() throws Exception {
        Long count = redisBaseService.getRedisListSize(RedisQueue.EFFECTIVE_KEY_CN.getValue());
        return count;
    }

}

package com.zms.proxy.controller;

import com.zms.proxy.service.RedisBaseService;
import com.zms.proxy.util.constant.RedisQueue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Description : Object
 * @author: zeng.maosen
 */
@RequestMapping("/test")
@RestController
public class TestController {

    @Resource
    RedisBaseService redisBaseService;

    @GetMapping("/consumer")
    public Object consumer() throws Exception {
        Object consumer = redisBaseService.getRedisObject(RedisQueue.EFFECTIVE_KEY_CN.getValue());
        return consumer;
    }

    @GetMapping("/get")
    public Object get() throws Exception {
        Object consumer = redisBaseService.getRandomOne(RedisQueue.EFFECTIVE_KEY_CN.getValue());
        return consumer;
    }
}

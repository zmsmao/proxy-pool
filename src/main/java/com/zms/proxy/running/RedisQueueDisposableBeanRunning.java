package com.zms.proxy.running;

import com.zms.proxy.service.RedisBaseService;
import com.zms.proxy.util.check.ProxyIpCheck;
import com.zms.proxy.util.constant.RedisQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Description : Object
 * @author: zeng.maosen
 */
@Slf4j
@Component
public class RedisQueueDisposableBeanRunning implements SmartLifecycle {

    @Resource
    private RedisBaseService redisBaseService;

    @Resource
    private ProxyIpCheck proxyIpCheck;

    final String keyCn= RedisQueue.CRAWLING_KEY_CN.getValue();
    final String nullityHttpKey = RedisQueue.NULLITY_HTTP_MAP_KEY_CN.getValue();
    final String nullityHttpsKey = RedisQueue.NULLITY_HTTPS_MAP_KEY_CN.getValue();
    final String effKey = RedisQueue.EFFECTIVE_KEY_CN.getValue();
    final String keyNotCN = RedisQueue.CRAWLING_KEY_NOT_CN.getValue();
    final String effMapKey = RedisQueue.EFFECTIVE_MAP_KEY_CN.getValue();

    private volatile boolean  running = false;


    @Override
    public void start() {
        log.info("开始前清理redis==============>>");
//        抓取的ip存放位置
        redisBaseService.delete(keyCn);
//        校验有效的ip存放位置
        redisBaseService.delete(effKey);
//        黑名单
        redisBaseService.delete(nullityHttpKey);
        redisBaseService.delete(nullityHttpsKey);
//        国外的ip
        redisBaseService.delete(keyNotCN);
//        有效的ip映射
        redisBaseService.delete(effMapKey);
        log.info("开启redis-ip校验==============>>");
        proxyIpCheck.checkRedisQueueHttp();
        //暂时不支持https校验
//        proxyIpCheck.checkRedisQueueHttps();
        running = true;
    }

    @Override
    public void stop() {
        running =false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}

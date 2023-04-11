package com.zms.proxy.util.check;

import com.zms.proxy.config.JobTaskPoolConfig;
import com.zms.proxy.domain.ProxyIp;
import com.zms.proxy.service.RedisBaseService;
import com.zms.proxy.util.HttpClientUtils;
import com.zms.proxy.util.constant.RedisQueue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.Future;

/**
 * @Description : Object
 * @author: zeng.maosen
 */
@Slf4j
@Component
public class ProxyIpCheck {

    @Resource
    RedisBaseService redisBaseService;

    @Resource
    AsyncProxyIpCheck asyncProxyIpCheck;

    final String keyCn = RedisQueue.CRAWLING_KEY_CN.getValue();
    final String effKey = RedisQueue.EFFECTIVE_KEY_CN.getValue();
    final String nullityHttpKey = RedisQueue.NULLITY_HTTP_MAP_KEY_CN.getValue();
    final String nullityHttpsKey = RedisQueue.NULLITY_HTTPS_MAP_KEY_CN.getValue();
    final String effMapKey = RedisQueue.EFFECTIVE_MAP_KEY_CN.getValue();

    @Async
    public void checkRedisQueueHttp() {
        while (true) {
            Object keyCnObject = redisBaseService.getRedisObject(keyCn);
            if (keyCnObject != null) {
                log.info("http:获取爬取的对象---------{}", keyCnObject);
                ProxyIp proxyIp = (ProxyIp) keyCnObject;
                Object nullObject = redisBaseService.getRedisMapObject(nullityHttpKey, proxyIp.getProxy());
                Object effObject = redisBaseService.getRedisMapObject(effMapKey, proxyIp.getProxy());
                //如果在失败Map中,或者是在有效的Map，则不再校验
                if (effObject != null || nullObject!=null) {
                    continue;
                }
                asyncProxyIpCheck.checkHttp(proxyIp);
            } else {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Async
    public void checkRedisQueueHttps() {
        while (true) {
            Object keyCnObject = redisBaseService.getRedisObject(effKey);
            if (keyCnObject != null) {
                log.info("https:获取爬取的对象---------{}", keyCnObject);
                ProxyIp proxyIp = (ProxyIp) keyCnObject;
                Object nullObject = redisBaseService.getRedisMapObject(nullityHttpsKey, proxyIp.getProxy());
                //如果在失败Map中,或者是在有效的Map，则不再校验
                if(nullObject!=null){
                    continue;
                }
                asyncProxyIpCheck.checkHttps(proxyIp);
            } else {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

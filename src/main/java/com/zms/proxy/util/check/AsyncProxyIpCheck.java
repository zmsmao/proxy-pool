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
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.jws.Oneway;

/**
 * @Description : Object
 * @author: zeng.maosen
 */
@Component
@Slf4j
public class AsyncProxyIpCheck {

    @Resource
    RedisBaseService redisBaseService;

    @Resource
    JobTaskPoolConfig jobTaskPoolConfig;


    final String keyCn = RedisQueue.CRAWLING_KEY_CN.getValue();
    final String effKey = RedisQueue.EFFECTIVE_KEY_CN.getValue();
    final String nullityHttpKey = RedisQueue.NULLITY_HTTP_MAP_KEY_CN.getValue();
    final String nullityHttpsKey = RedisQueue.NULLITY_HTTPS_MAP_KEY_CN.getValue();
    final String effMapKey = RedisQueue.EFFECTIVE_MAP_KEY_CN.getValue();

    @Async
    public void checkHttp(ProxyIp proxyIp){
        HttpHost httpHost = new HttpHost(proxyIp.getIpAddress(), proxyIp.getPort());
        proxyIp.setCheckCount(proxyIp.getCheckCount() + 1);
        try {
            HttpResponse httpResponse = HttpClientUtils.getresp(jobTaskPoolConfig.getValidity().getHttpUrl(), httpHost);
            int httpCode = httpResponse.getStatusLine().getStatusCode();
            if (StringUtils.isEmpty(proxyIp.getType())) {
                proxyIp.setType("");
            }
            if (httpCode == 200) {
                proxyIp.setType("http");
                proxyIp.setSuccessCount(proxyIp.getSuccessCount() + 1);
                proxyIp.setSuccessRatio((proxyIp.getSuccessCount()*1.0) / (proxyIp.getCheckCount()*1.0));
                redisBaseService.produce(effKey, proxyIp);
                redisBaseService.produceMap(effMapKey, proxyIp);
            } else {
                failProxy(proxyIp,nullityHttpKey);
                log.info("校验http失败------- = {}", proxyIp);
            }
        } catch (Exception e) {
            failProxy(proxyIp,nullityHttpKey);
            log.info("校验http失败------- = {}", proxyIp);
        }
    }

    @Async
    public void checkHttps(ProxyIp proxyIp) {
        HttpHost httpHost = new HttpHost(proxyIp.getIpAddress(), proxyIp.getPort());
        proxyIp.setCheckCount(proxyIp.getCheckCount() + 1);
        try {
            HttpResponse httpsResponse = HttpClientUtils.getresp(jobTaskPoolConfig.getValidity().getHttpsUrl(), httpHost);
            int httpsCode = httpsResponse.getStatusLine().getStatusCode();
            if (StringUtils.isEmpty(proxyIp.getType())) {
                proxyIp.setType("");
            }
            if (httpsCode == 200) {
                proxyIp.setType("https");
                proxyIp.setSuccessCount(proxyIp.getSuccessCount() + 1);
                proxyIp.setSuccessRatio((proxyIp.getSuccessCount()*1.0) / (proxyIp.getCheckCount()*1.0));
                redisBaseService.produce(effKey, proxyIp);
            } else {
                redisBaseService.produce(effKey,proxyIp);
                //放入失败的https请求中
                redisBaseService.produceMap(nullityHttpsKey,proxyIp);
                log.info("校验https失败------- = {}", proxyIp);
            }
        } catch (Exception e) {
            redisBaseService.produce(effKey,proxyIp);
            //放入失败的https请求中
            redisBaseService.produceMap(nullityHttpsKey,proxyIp);
            log.info("校验https失败------- = {}", proxyIp);
        }
    }


    public void failProxy(ProxyIp proxyIp,String key){
        proxyIp.setFailCount(proxyIp.getFailCount()+1);
        proxyIp.setSuccessRatio((proxyIp.getSuccessCount()*1.0) / (proxyIp.getCheckCount()*1.0));
        if (proxyIp.getFailCount() >= jobTaskPoolConfig.getValidity().getFailCount()) {
            redisBaseService.produceMap(key, proxyIp);
        } else {
            redisBaseService.produce(keyCn, proxyIp);
        }
    }

}

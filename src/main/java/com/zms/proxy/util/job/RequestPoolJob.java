package com.zms.proxy.util.job;

import com.zms.proxy.config.JobTaskPoolConfig;
import com.zms.proxy.domain.ProxyIp;
import com.zms.proxy.service.RedisBaseService;
import com.zms.proxy.util.HttpClientUtils;
import com.zms.proxy.util.check.ProxyIpCheck;
import com.zms.proxy.util.constant.RedisQueue;
import com.zms.proxy.util.fetcher.WebProxyFetcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * @Description : Object
 * @author: zeng.maosen
 */
@Service
@Slf4j
public class RequestPoolJob {


    @Resource
    WebProxyFetcher webProxyFetcher;

    @Resource
    RedisBaseService redisBaseService;

    @Resource
    JobTaskPoolConfig jobTaskPoolConfig;


    final String keyCn=RedisQueue.CRAWLING_KEY_CN.getValue();
    final String effKey = RedisQueue.EFFECTIVE_KEY_CN.getValue();

    @Scheduled(fixedDelay = 360000)
    public void taskPool() {
        //设置爬取队列的有效时间
        System.out.println(jobTaskPoolConfig.getValidity().toString());
        if(redisBaseService.getRedisObject(keyCn)==null &&
                redisBaseService.getRedisListSize(effKey)<jobTaskPoolConfig.getCountIp()){
            try {
                log.info("开始爬取第一个网址============>>>");
                webProxyFetcher.webProxyPool01();
                log.info("开始爬取第二个网址============>>>");
                webProxyFetcher.webProxyPool02();
                log.info("开始爬取第三个网址============>>>");
                webProxyFetcher.webProxyPool03();
                log.info("开始爬取第四个网址============>>>");
                webProxyFetcher.webProxyPool04();
                log.info("开始爬取第五个网址============>>>");
                webProxyFetcher.webProxyPool05();
                log.info("开始爬取第六个网址============>>>");
                webProxyFetcher.webProxyPool06();
                log.info("开始爬取第七个网址============>>>");
                webProxyFetcher.webProxyPool07();
            }catch (Exception e){
                log.info("网址爬取异常============>>>");
            }
        }
    }

    @Scheduled(cron = "0/2 * * * * ?")
    public void lastCheckList(){
        Object redisObject = redisBaseService.getRedisObject(effKey);
        if(redisObject!=null){
          ProxyIp proxyIp = (ProxyIp) redisObject;
          proxyIp.setCheckCount(proxyIp.getCheckCount() + 1);
          HttpHost httpHost = new HttpHost(proxyIp.getIpAddress(),proxyIp.getPort());
            try {
                HttpResponse httpResponse = HttpClientUtils.getresp(jobTaskPoolConfig.getValidity().getHttpUrl(), httpHost);
                int httpCode = httpResponse.getStatusLine().getStatusCode();
                if (httpCode == 200) {
                    proxyIp.setType("http");
                    proxyIp.setSuccessCount(proxyIp.getSuccessCount() + 1);
                    proxyIp.setSuccessRatio((proxyIp.getSuccessCount()*1.0) / (proxyIp.getCheckCount()*1.0));
                    redisBaseService.produce(effKey, proxyIp);
                } else {
                    proxyIp.setFailCount(proxyIp.getFailCount()+1);
                    proxyIp.setSuccessRatio((proxyIp.getSuccessCount()*1.0) / (proxyIp.getCheckCount()*1.0));
                    if(proxyIp.getSuccessRatio()>0.3) {
                        redisBaseService.produce(effKey, proxyIp);
                    }
                    log.info("校验http失败------- = {}", proxyIp);
                }
            } catch (Exception e) {
                proxyIp.setFailCount(proxyIp.getFailCount()+1);
                proxyIp.setSuccessRatio((proxyIp.getSuccessCount()*1.0) / (proxyIp.getCheckCount()*1.0));
                if(proxyIp.getSuccessRatio()>0.3) {
                    redisBaseService.produce(effKey, proxyIp);
                }
                log.info("校验http失败------- = {}", proxyIp);
            }
        }
    }

}

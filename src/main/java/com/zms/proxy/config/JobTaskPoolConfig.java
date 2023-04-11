package com.zms.proxy.config;

import lombok.Data;
import org.apache.commons.lang3.Validate;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @Description : Object
 * @author: zeng.maosen
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "job.task")
public class JobTaskPoolConfig {

    private Long countIp;

    private Validity validity;

    @Data
    public static class Validity{
        //校验有效的代理ip
        private String httpUrl ;
        private String httpsUrl ;
        //超时时间
        private Long  timeoutTime;
        private Long failCount;

    }
}

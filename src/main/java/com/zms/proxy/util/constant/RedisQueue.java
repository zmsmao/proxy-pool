package com.zms.proxy.util.constant;

/**
 * @Description : Object
 * @author: zeng.maosen
 */
public enum  RedisQueue {

    /**
     * 爬取 和 校验有效的队列
     */
    CRAWLING_KEY_CN("CRAWLING_KEY_CN"),
    CRAWLING_KEY_NOT_CN("CRAWLING_KEY_NOT_CN"),
    EFFECTIVE_KEY_CN("EFFECTIVE_KEY_CN"),
    NULLITY_HTTP_MAP_KEY_CN("NULLITY_HTTP_MAP_KEY_CN"),
    NULLITY_HTTPS_MAP_KEY_CN("NULLITY_HTTP_MAP_KEY_CN"),
    EFFECTIVE_MAP_KEY_CN("EFFECTIVE_MAP_KEY_CN");

    RedisQueue(String value){
        this.value = value;
    }

    private final String value;

    public String getValue() {
        return value;
    }

}

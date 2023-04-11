package com.zms.proxy.domain;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description : Object
 * @author: zeng.maosen
 */
@Data
public class ProxyIp implements Serializable {

    private static final long serialVersionUID = 2867071904568397169L;
    /**
     * 端口
     */
    private Integer port;
    /**
     * ip地址
     */
    private String ipAddress;
    /**
     * 代理地址
     */
    private String proxy;

    /**
     * 支持类型
     */
    private String type;

    /**
     * 区域
     */
    private String region;

    /**
     * 来源网址
     */
    private String source;

    /**
     * 检查次数
     */
    private Integer checkCount;

    /**
     * 失败次数
     */
    private Integer failCount;

    /**
     * 成功次数
     */
    private Integer successCount;

    /**
     * 成功比例
     */
    private Double successRatio;
}

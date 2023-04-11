<p align="center">
    <img src="https://i.postimg.cc/T3StDMK5/proxypool.png" width="150">
    <p align="center">
        <br>
         <a href="https://docs.spring.io/spring-boot/docs/2.7.10/reference/html/">
             <img src="https://img.shields.io/badge/springboot-2.7.1-green?logo=springboot" >
         </a>
          <a href="https://openjdk.org">
             <img src="https://img.shields.io/badge/jdk-1.8+-orange?logo=openjdk">
         </a>
         <a href="https://redis.io">
             <img src="https://img.shields.io/badge/redis-2.8.9+-red?logo=redis">
         </a>
    </p>    
</p>


## PROXY-POOL:基于springboot框架获取代理ip:stars:

```java
                                               _ 
                                              | |
 _ __  _ __ _____  ___   _   _ __   ___   ___ | |
| '_ \| '__/ _ \ \/ / | | | | '_ \ / _ \ / _ \| |
| |_) | | | (_) >  <| |_| | | |_) | (_) | (_) | |
| .__/|_|  \___/_/\_\\__, | | .__/ \___/ \___/|_|
| |                   __/ | | |                  
|_|                  |___/  |_|                  
                                                 
```

**主要功能**：

​		:tangerine:为定时爬取网上发布的免费代理，采用定时任务的方式，验证爬取的代理，保证代理的可用性，主要提供API使用方式。同时你也可以扩展代理源以增加代理池IP的质量和数量。

---
### 如何运行
#### 1.下载代码
```bash
git clone git@github.com:1974525360/proxy-pool.git
```
#### 2.安装依赖
- jdk 1.8+   [``JDK8 下载、安装和环境配置``](https://blog.csdn.net/u013129932/article/details/114275846)
- redis 2.8.9+  [``Redis的安装教程``](https://blog.csdn.net/weixin_43883917/article/details/114632709)
#### 3.配置文件

配置文件 `application.yaml` 位于项目的主目录``resources``下:

```yaml
# redis的配置
spring:
  redis:
    port: 6379
# 服务对外开放的端口
server:
  port: 8093

# 爬取任务和校验ip的配置
job:
  task:
    # redis中有效的ip数量小于指定数量则爬取ip
    count-ip: 20
    # 校验有效的ip 使用爬取的ip通过代理的方式模拟游览器请求发送指定url
    validity:
      http-url: "https://www.baidu.com/"
      https-url: "http://httpbin.org"
      # 代理请求校验失败的次数 超过此次数则加入黑名单
      fail-count: 2
```

####  4.启动项目

:one: idea中直接运行

:two: 使用maven 打包成jar，使用以下指令:

```java
java -jar proxy-pool-0.0.1.jar
```
---
### 如何使用

#### 	Api

- 启动web服务后, 默认配置下会开启 ``http://localhost:8093`` 的api接口服务:

| api             | method | Description          | params(暂时无可选参数)                      |
| --------------- | ------ | -------------------- | ------------------------------------------- |
| :rabbit2:/get   | get    | 随机获取一个代理参数 | 可选参数: `?type=https` 过滤支持https的代理 |
| :racehorse:/pop | get    | 获取并删除一个代理   | 可选参数: `?type=https` 过滤支持https的代理 |
| :dog: /list     | get    | 获取所有代理         | 可选参数: `?type=https` 过滤支持https的代理 |
| :cat:/count     | get    | 查看代理数量         |                                             |



### 如何扩展

#### 1.爬取配置

​		在项目文件中，启用的代理爬取方法名，代理爬取方法位于``util/fetcher/WebProxyFetcher.java``类中，因为代理源不稳定，时	常改变，所以在代理源失效时，直接把其注释即可，以保证代理源是始终有效的。

- 扩展代理
    
    :one: 使用类似的代码，在其中实现自己的爬取逻辑，代码如下：

```java
    @Async
    @ExceptionHandler
    @Retryable(value = Exception.class)
    public void webProxyPool08() throws Exception {
        
    }
```
- 定时任务

    :two:在``util/job/RequestPoolJob.java``类中，加入如下代码:

```java 
     log.info("开始爬取第八个网址============>>>");
     webProxyFetcher.webProxyPool08();
```



#### 2.校验配置

​		在``com\zms\proxy\running\RedisQueueDisposableBeanRunning.java``l类中，这一部分主要是管理redis的存储的生命周期和启动之后所要执行的校验方法，代码如下：

```java
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
//		  支持的http校验
        proxyIpCheck.checkRedisQueueHttp();
//        暂时不支持https校验
//      proxyIpCheck.checkRedisQueueHttps();
        running = true;
    }
```

​		:key:在此方法中，主要配置的是下次启动时，对于redis中的存取的ip管理，注释掉黑名单就可以在下次启动时减少校验次数，提高效率。

### 代理来源

​		目前实现的采集免费代理网站有(排名不分先后, 下面仅是对其发布的免费代理情况, 付费代理测评可以参考[这里](https://zhuanlan.zhihu.com/p/33576641) )

|   代理名称   |  状态  |  更新速度 |  可用率  |  地址 |    代码   |
| ---------   |  ---- | --------  | ------  | ----- |   ------- |
| 站大爷     |  ✔    |     :star:     |   :star2::star2:   | [地址](https://www.zdaye.com/dayProxy.html) | ``webProxyPool01()`` |
| 66代理     |  ✔    |     :star:     | :star2::star2: | [地址](http://www.66ip.cn/)         | ``webProxyPool02()`` |
| 开心代理     |   ✔   |     :star:     | :star2::star2: | [地址](http://www.kxdaili.com/dailiip.html) | ``webProxyPool03()`` |
| 快代理       |  ✔    |     :star:     | :star2::star2: | [地址](https://www.kuaidaili.com/free/) | ``webProxyPool04()`` |
| proxylist |  ✔    |    :star:    | :star2::star2: | [地址](http://proxylist.fatezero.org/proxy.list) | ``webProxyPool05()`` |
| 云代理       |  ✔    |     :star:     | :star2::star2: | [地址](http://www.ip3366.net/free/) | ``webProxyPool06()`` |
| 89代理   |  ✔    | :star:    |    :star2::star2:    | [地址](https://www.89ip.cn/) | ``webProxyPool07()`` |



### 贡献代码

- 感谢此[``项目``](https://github.com/jhao104/proxy_pool) 提供给我开发的思路和免费的代理网址。
- 本项目依然不够完善，如果发现bug或有新的功能添加，请在[Issues](https://github.com/1974525360/proxy-pool/issues)提交。




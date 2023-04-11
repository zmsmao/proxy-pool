package com.zms.proxy.util.fetcher;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zms.proxy.domain.ProxyIp;
import com.zms.proxy.util.constant.RedisQueue;
import com.zms.proxy.service.RedisBaseService;
import com.zms.proxy.util.JsoupUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description : Object
 * @author: zeng.maosen
 */
@Service
@Slf4j
public class WebProxyFetcher{

    @Resource
    RedisBaseService redisBaseService;

    static int waitTime = 1000;

    static int timeOut = 10000;

    @Async
    @ExceptionHandler
    @Retryable(value = Exception.class)
    public void webProxyPool01() throws Exception {
        String url = "https://www.zdaye.com/dayProxy.html";
        Document document = JsoupUtil.sendGetRequest(url, timeOut);
        Elements elements = document.getElementsByClass("thread_time_info");
        if (elements.isEmpty()) {
            Elements links = document.select("ul.ggul>li>a");
            String href = links.get(0).attr("href");
            String targetUrl = "https://www.zdaye.com/" + href;
            document = JsoupUtil.sendGetRequest(targetUrl, timeOut);
            elements = document.getElementsByClass("thread_time_info");
        }
        for (Element element : elements) {
            String text = element.text();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date parseDate = dateFormat.parse(text);
            Date nowDate = new Date();
//            long l = nowDate.getTime() - parseDate.getTime();
            if (DateUtils.isSameDay(parseDate, nowDate)) {
                String targetUrl = "https://www.zdaye.com/" + document.select("a.thread_theme_type").get(0).attr("href");
                String urls = targetUrl;
                System.out.println(targetUrl);
                for (int i = 2; i <= 5; i++) {
                    Document content = JsoupUtil.sendGetRequest(targetUrl, timeOut);
                    Elements selectXpathS = content.select("#ipc > tbody >tr");
                    for (Element selectXpath : selectXpathS) {
                        Elements td = selectXpath.select("td");
                        String[] s = td.text().split(" ");
                        ProxyIp proxyIp = new ProxyIp();
                        proxyIp.setPort(Integer.parseInt(s[1]));
                        proxyIp.setIpAddress(s[0]);
                        proxyIp.setProxy(s[0] + ":" + Integer.parseInt(s[1]));
                        proxyIp.setRegion(s[s.length - 2] + s[s.length - 1]);
                        proxyIp.setType(s[3]);
                        proxyIp.setSource(targetUrl);

                        proxyIp.setCheckCount(0);
                        proxyIp.setSuccessCount(0);
                        proxyIp.setFailCount(0);
                        proxyIp.setSuccessRatio(0.0);
                        redisBaseService.produce(RedisQueue.CRAWLING_KEY_CN.getValue(), proxyIp);
                        log.info("获取的ip代理================>> {}-->>{}", s[0], proxyIp.getSource());
                        targetUrl = urls.split("html")[0].substring(0,urls.split("html")[0].length()-1)+"/"+i+".html";
                    }
                }
            }
        }
    }

    @Async
    @ExceptionHandler
    @Retryable(value = Exception.class)
    public void webProxyPool02() throws Exception {
        String url = "http://www.66ip.cn/";
        Document document = JsoupUtil.sendGetRequest(url, timeOut);
        Element element = document.selectXpath("//*[@id=\"main\"]/div[1]/div[2]/div[1]/table").get(0);
        Elements select = element.select("tr");
        select.remove(0);
        // 遍历每个tr标签
        for (Element tr : select) {
            // 遍历每个td标签
            ProxyIp proxyIp = new ProxyIp();
            String[] s = tr.text().split(" ");
            proxyIp.setIpAddress(s[0]);
            proxyIp.setPort(Integer.parseInt(s[1]));
            proxyIp.setRegion(s[2]);
            proxyIp.setProxy(s[0] + ":" + s[1]);
            proxyIp.setSource(url);

            proxyIp.setCheckCount(0);
            proxyIp.setSuccessCount(0);
            proxyIp.setFailCount(0);
            proxyIp.setSuccessRatio(0.0);
            log.info("获取的ip代理================>> {}-->>{}" ,s[0],proxyIp.getSource() );
            redisBaseService.produce(RedisQueue.CRAWLING_KEY_CN.getValue(), proxyIp);
        }
        Thread.sleep(waitTime);
    }

    @Async
    @ExceptionHandler
    @Retryable(value = Exception.class)
    public void webProxyPool03() throws Exception {
        String url = "http://www.kxdaili.com/dailiip.html";
        for (int index = 2; index <= 5; index++) {
            Document document = JsoupUtil.sendGetRequest(url, timeOut);
            Elements element = document.getElementsByClass("active");
            element.remove(0);
            Elements select = element.select("tr");
            select.remove(0);
            // 遍历每个tr标签
            for (Element tr : select) {
                // 遍历每个td标签
                ProxyIp proxyIp = new ProxyIp();
                String[] s = tr.text().split(" ");
                proxyIp.setIpAddress(s[0]);
                proxyIp.setPort(Integer.parseInt(s[1]));
                proxyIp.setType(s[3]);
                proxyIp.setRegion(s[s.length - 3] + s[s.length - 2]);
                proxyIp.setProxy(s[0] + ":" + s[1]);
                proxyIp.setSource(url);

                proxyIp.setCheckCount(0);
                proxyIp.setSuccessCount(0);
                proxyIp.setFailCount(0);
                proxyIp.setSuccessRatio(0.0);
                log.info("获取的ip代理================>> {}-->>{}" ,s[0],proxyIp.getSource() );
                redisBaseService.produce(RedisQueue.CRAWLING_KEY_CN.getValue(), proxyIp);
            }
            url = "http://www.kxdaili.com/dailiip/1/" + index + ".html";
            Thread.sleep(waitTime);
        }

    }

    @Async
    @ExceptionHandler
    @Retryable(value = Exception.class)
    public void webProxyPool04() throws Exception {
        String url = "https://www.kuaidaili.com/free/";
        Document document = JsoupUtil.sendGetRequest(url, timeOut);
        Element element = document.select("#list > table > tbody").first();
        assert element != null;
        Elements tr = element.select("tr");
        for (Element row : tr) {
            String ip = row.select("td[data-title=IP]").first().text();
            String port = row.select("td[data-title=PORT]").first().text();
            String type = row.select("td[data-title=类型]").first().text();
            String location = row.select("td[data-title=位置]").first().text();
            ProxyIp proxyIp = new ProxyIp();
            proxyIp.setIpAddress(ip);
            proxyIp.setPort(Integer.parseInt(port));
            proxyIp.setType(type);
            proxyIp.setRegion(location);
            proxyIp.setProxy(ip + ":" + Integer.parseInt(port));
            proxyIp.setSource(url);

            proxyIp.setCheckCount(0);
            proxyIp.setSuccessCount(0);
            proxyIp.setFailCount(0);
            proxyIp.setSuccessRatio(0.0);
            log.info("获取的ip代理================>> {}-->>{}" ,ip,proxyIp.getSource() );
            redisBaseService.produce(RedisQueue.CRAWLING_KEY_CN.getValue(), proxyIp);
        }
    }

    @Async
    @ExceptionHandler
    @Retryable(value = Exception.class)
    public void webProxyPool05() throws Exception {
        String url = "http://proxylist.fatezero.org/proxy.list";
        Document document = JsoupUtil.sendGetRequest(url, timeOut);
        Elements elements = document.getAllElements();
        Element element = elements.get(3);
        String text = element.text();
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(text);
        ObjectMapper mapper = new ObjectMapper();
        while (parser.nextToken() == JsonToken.START_OBJECT) {
            JsonNode node = mapper.readTree(parser);
            String country = node.get("country").asText();
            int port = node.get("port").asInt();
            String host = node.get("host").asText();
            String type = node.get("type").asText();
            ProxyIp proxyIp = new ProxyIp();
            proxyIp.setIpAddress(host);
            proxyIp.setType(type);
            proxyIp.setRegion(country);
            proxyIp.setProxy(host + ":" + port);
            proxyIp.setPort(port);
            proxyIp.setSource(url);

            proxyIp.setCheckCount(0);
            proxyIp.setSuccessCount(0);
            proxyIp.setFailCount(0);
            proxyIp.setSuccessRatio(0.0);
            log.info("获取的ip代理================>> {}-->>{}" ,host,proxyIp.getSource() );
            if ("CN".equals(country)) {
                redisBaseService.produce(RedisQueue.CRAWLING_KEY_CN.getValue(), proxyIp);
            } else {
                redisBaseService.produce(RedisQueue.CRAWLING_KEY_NOT_CN.getValue(), proxyIp);
            }
        }
    }

    @Async
    @ExceptionHandler
    @Retryable(value = Exception.class)
    public void webProxyPool06() throws Exception {
        String url = "http://www.ip3366.net/free/";
        for (int index = 2; index <= 5; index++) {
            Document document = JsoupUtil.sendGetRequest(url, timeOut);
            Elements element = document.select("#list > table > tbody");
            Elements select = element.select("tr");
            // 遍历每个tr标签
            for (Element tr : select) {
                // 遍历每个td标签
                ProxyIp proxyIp = new ProxyIp();
                String[] s = tr.text().split(" ");
                proxyIp.setIpAddress(s[0]);
                proxyIp.setPort(Integer.parseInt(s[1]));
                proxyIp.setType(s[3]);
                proxyIp.setRegion(s[4]);
                proxyIp.setProxy(s[0] + ":" + s[1]);
                proxyIp.setSource(url);

                proxyIp.setCheckCount(0);
                proxyIp.setSuccessCount(0);
                proxyIp.setFailCount(0);
                proxyIp.setSuccessRatio(0.0);
                log.info("获取的ip代理================>> {}-->>{}" ,s[0],proxyIp.getSource() );
                redisBaseService.produce(RedisQueue.CRAWLING_KEY_CN.getValue(), proxyIp);
            }
            url = "http://www.ip3366.net/free/?page=" + index;
            Thread.sleep(waitTime);
        }

    }

    @Async
    @ExceptionHandler
    @Retryable(value = Exception.class)
    public void webProxyPool07() throws Exception {
        String url = "https://www.89ip.cn/";
        for (int index = 2; index <= 5; index++) {
            Document document = JsoupUtil.sendGetRequest(url, timeOut);
            Elements element = document.getElementsByClass("layui-table");
            Elements select = element.select("tr");
            select.remove(0);
            // 遍历每个tr标签
            for (Element tr : select) {
                // 遍历每个td标签
                ProxyIp proxyIp = new ProxyIp();
                String[] s = tr.text().split(" ");
                proxyIp.setIpAddress(s[0]);
                proxyIp.setPort(Integer.parseInt(s[1]));
                proxyIp.setRegion(s[2]);
                proxyIp.setProxy(s[0] + ":" + s[1]);
                proxyIp.setSource(url);

                proxyIp.setCheckCount(0);
                proxyIp.setSuccessCount(0);
                proxyIp.setFailCount(0);
                proxyIp.setSuccessRatio(0.0);
                log.info("获取的ip代理================>> {}-->>{}", s[0], proxyIp.getSource());
                redisBaseService.produce(RedisQueue.CRAWLING_KEY_CN.getValue(), proxyIp);
            }
            url = "https://www.89ip.cn/index_" + index + ".html";
            Thread.sleep(waitTime);
        }
    }

    @Async
    @ExceptionHandler
    @Retryable(value = Exception.class)
    public void webProxyPool08() throws Exception {

    }
}

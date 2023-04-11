package com.zms.proxy.util;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.lang.annotation.Documented;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description : Object
 * @author: zeng.maosen
 */
public class JsoupUtil {

    static Map<String,String> headers;

    static {
        headers = new HashMap<>();
        headers.put("User-Agent","'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.101'");
        headers.put("Accept","*/*");
        headers.put("Connection","keep-alive");
        headers.put("Accept-Language","zh-CN,zh;q=0.8");
    }

    /**
     * 发送 HTTP GET 请求
     *
     * @param url     请求地址
     * @param timeout 超时时间（毫秒）
     * @return 响应内容
     */
    public static Document sendGetRequest(String url, int timeout) throws Exception {
        trustAllCerts();
        Connection connection = Jsoup.connect(url).timeout(timeout);
        connection.headers(headers);
        Connection.Response response = connection.method(Connection.Method.GET).execute();
        return response.parse();
    }

    /**
     * 发送 HTTP POST 请求
     *
     * @param url     请求地址
     * @param payload 请求参数
     * @param timeout 超时时间（毫秒）
     * @return 响应内容
     */
    public static Document sendPostRequest(String url, String payload, int timeout) throws Exception {
        trustAllCerts();
        Connection connection = Jsoup.connect(url).timeout(timeout);
        Connection.Response response = connection.method(Connection.Method.POST).requestBody(payload).execute();
        return response.parse();
    }

    /**
     * 信任所有证书
     */
    private static void trustAllCerts() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null,new TrustManager[]{new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() { return null; }
                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) { }
            }}, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

//    public static void main(String[] args) {
//        String s= "https://www.zdaye.com/dayProxy/ip/334664.html";
//        System.out.println(s.split("html")[0].substring(0,s.split("html")[0].length()-1));
//    }
}

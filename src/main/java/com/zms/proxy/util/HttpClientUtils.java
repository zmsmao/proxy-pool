package com.zms.proxy.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;

/**
 * 调用OCR接口请求封装工具类
 *  依赖的jar包有：(commons-lang-2.6.jar、httpclient-4.3.2.jar、httpcore-4.3.1.jar、commons-io-2.4.jar)
 *
 *
 */
public class HttpClientUtils {

    public static final Integer connTimeout = 1000;
    public static final Integer readTimeout = 1000;
    public static final String charset = "UTF-8";
    private static HttpClient client = null;
    static Map<String,String> headers;

    static {
        headers = new HashMap<>();
        headers.put("User-Agent","'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.101'");
        headers.put("Accept","*/*");
        headers.put("Connection","keep-alive");
        headers.put("Accept-Language","zh-CN,zh;q=0.8");
    }

    static {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(128);
        cm.setDefaultMaxPerRoute(128);
        client = HttpClients.custom().setConnectionManager(cm).build();
    }

    public static String postParameters(String url, String parameterStr) throws ConnectTimeoutException, SocketTimeoutException, Exception{
        return post(url,parameterStr,"application/x-www-form-urlencoded",charset,connTimeout,readTimeout,null);
    }

    public static String postParameters(String url, String parameterStr,String charset, Integer connTimeout, Integer readTimeout) throws ConnectTimeoutException, SocketTimeoutException, Exception{
        return post(url,parameterStr,"application/x-www-form-urlencoded",charset,connTimeout,readTimeout,null);
    }


    public static String postParameters(String url, Map<String, String> params) throws ConnectTimeoutException,
            SocketTimeoutException, Exception {
        return postForm(url, params, null, connTimeout, readTimeout);
    }

    public static String postParameters(String url, Map<String, String> params, Map<String, String> headers) throws ConnectTimeoutException,
            SocketTimeoutException, Exception {
        return postForm(url, params, headers, connTimeout, readTimeout);
    }

    public static String postParameters(String url, Map<String, String> params, Integer connTimeout,Integer readTimeout) throws ConnectTimeoutException,
            SocketTimeoutException, Exception {
        return postForm(url, params, null, connTimeout, readTimeout);
    }

    public static HttpResponse postParameters(String url, Map<String, String> params,Map<String, String> headers,String other) throws ConnectTimeoutException,
            SocketTimeoutException, Exception {
        return postFormRes(url, params, headers, connTimeout, readTimeout);
    }

    public static String get(String url) throws Exception {
        return get(url, charset, null, null,null,null,null);
    }

    public static String get(String url,HttpHost httpHost) throws Exception{
        return get(url,charset,null,null,null,null,httpHost);
    }

    public static String getAndHeader(String url,HttpHost httpHost) throws Exception{
        return get(url,charset,null,null,null,headers,httpHost);
    }

    public static String getAndHeader(String url) throws Exception {
        return get(url, charset, null, null,null,headers,null);
    }

    public static String get(String url,Map<String,String>paramMap,Map<String,String>headerMap) throws Exception {
        return get(url, charset, null, null,paramMap,headerMap,null);
    }

    public static HttpResponse getresp(String url, Map<String,String>paramMap, Map<String,String>headerMap) throws Exception {
        return get(url, charset, null, null,paramMap,headerMap,"",null);
    }

    public static HttpResponse getresp(String url, HttpHost httpHost) throws Exception {
        return get(url, charset, 5000, 5000,null,headers,"",httpHost);
    }

    public static HttpResponse getresp(String url) throws Exception {
        return get(url, charset, null, null,null,headers,"",null);
    }

    public static String getrespToString(String url, String charset,Map<String,String>paramMap,Map<String,String>headerMap) throws Exception {
        return doHttpGetGizpForImage(url,charset,paramMap,headerMap);
    }

    public static String get(String url, String charset) throws Exception {
        return get(url, charset, connTimeout, readTimeout,null,null,null);
    }

    public static InputStream getres(String url,Map<String,String>headerMap) throws Exception {
        return getres(url, charset, null, null,null,headerMap);
    }


    public static String postRobotClient(String url, String paramMapbody,String miniType,String key) throws ConnectTimeoutException, SocketTimeoutException, Exception{
        return post(url,paramMapbody,miniType,charset,connTimeout,readTimeout,Collections.singletonMap("key",key));
    }

    public static String postJsonBody(String url, String paramMapbody,String miniType) throws ConnectTimeoutException, SocketTimeoutException, Exception{
        return post(url,paramMapbody,miniType,charset,connTimeout,readTimeout,null);
    }

    public static String postJsonBody(String url, String paramMapbody,String miniType,String charset) throws ConnectTimeoutException, SocketTimeoutException, Exception{
        return post(url,paramMapbody,miniType,charset,connTimeout,readTimeout,null);
    }
    /**
     * 发送一个 Post 请求, 使用指定的字符集编码.
     *
     * @param url
     * @param body RequestBody
     * @param mimeType 例如 application/xml "application/x-www-form-urlencoded" a=1&b=2&c=3
     * @param charset 编码
     * @param connTimeout 建立链接超时时间,毫秒.
     * @param readTimeout 响应超时时间,毫秒.
     * @param headers 头信息
     * @return ResponseBody, 使用指定的字符集编码.
     * @throws ConnectTimeoutException 建立链接超时异常
     * @throws SocketTimeoutException  响应超时
     * @throws Exception
     */
    public static String post(String url, String body, String mimeType,String charset, Integer connTimeout, Integer readTimeout,Map<String,String> headers)
            throws ConnectTimeoutException, SocketTimeoutException, Exception {
        HttpClient client = null;
        HttpPost post = new HttpPost(url);
        //post.setHeader("key","29359b15-c480-426c-b3c0-6d247200f67c");
        if (!CollectionUtils.isEmpty(headers)){
            for (String key : headers.keySet()){
                post.setHeader(key,headers.get(key));
            }
        }
        String result = "";
        try {
            if (StringUtils.isNotBlank(body)) {
                HttpEntity entity = new StringEntity(body, ContentType.create(mimeType, charset));
                post.setEntity(entity);
            }
            // 设置参数
            Builder customReqConf = RequestConfig.custom();
            if (connTimeout != null) {
                customReqConf.setConnectTimeout(connTimeout);
            }
            if (readTimeout != null) {
                customReqConf.setSocketTimeout(readTimeout);
            }
            post.setConfig(customReqConf.build());

            HttpResponse res;
            if (url.startsWith("https")) {
                // 执行 Https 请求.
                client = createSSLInsecureClient();
                res = client.execute(post);
            } else {
                // 执行 Http 请求.
                client =HttpClientUtils.client;
                res = client.execute(post);
            }
            result = IOUtils.toString(res.getEntity().getContent(), charset);
        } finally {
            post.releaseConnection();
            if (url.startsWith("https") && client != null&& client instanceof CloseableHttpClient) {
                ((CloseableHttpClient) client).close();
            }
        }
        return result;
    }


    /**
     * 提交form表单
     *
     * @param url
     * @param params
     * @param connTimeout
     * @param readTimeout
     * @return
     * @throws ConnectTimeoutException
     * @throws SocketTimeoutException
     * @throws Exception
     */
    public static String postForm(String url, Map<String, String> params, Map<String, String> headers, Integer connTimeout,Integer readTimeout) throws ConnectTimeoutException,
            SocketTimeoutException, Exception {

        HttpClient client = null;
        HttpPost post = new HttpPost(url);
        try {
            if (params != null && !params.isEmpty()) {
                List<NameValuePair> formParams = new ArrayList();
                Set<Entry<String, String>> entrySet = params.entrySet();
                for (Entry<String, String> entry : entrySet) {
                    formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
                post.setEntity(entity);
            }

            if (headers != null && !headers.isEmpty()) {
                for (Entry<String, String> entry : headers.entrySet()) {
                    post.addHeader(entry.getKey(), entry.getValue());
                }
            }
            // 设置参数
            Builder customReqConf = RequestConfig.custom();
            if (connTimeout != null) {
                customReqConf.setConnectTimeout(connTimeout);
            }
            if (readTimeout != null) {
                customReqConf.setSocketTimeout(readTimeout);
            }
            post.setConfig(customReqConf.build());
            HttpResponse res = null;
            if (url.startsWith("https")) {
                // 执行 Https 请求.
                client = createSSLInsecureClient();
                res = client.execute(post);
            } else {
                // 执行 Http 请求.
                client = HttpClientUtils.client;
                res = client.execute(post);
            }
            return IOUtils.toString(res.getEntity().getContent(), "UTF-8");
        } finally {
            post.releaseConnection();
            if (url.startsWith("https") && client != null
                    && client instanceof CloseableHttpClient) {
                ((CloseableHttpClient) client).close();
            }
        }
    }

    public static String postForm(String url, List<NameValuePair> formParams , Map<String, String> headers)
            throws ConnectTimeoutException, SocketTimeoutException, Exception{
        return postForm(url,formParams,headers,connTimeout,readTimeout);
    }

    /**
     * 提交form表单
     *
     * @param url
     * @param formParams
     * @param connTimeout
     * @param readTimeout
     * @return
     * @throws ConnectTimeoutException
     * @throws SocketTimeoutException
     * @throws Exception
     */
    public static String postForm(String url, List<NameValuePair> formParams , Map<String, String> headers, Integer connTimeout,Integer readTimeout) throws ConnectTimeoutException,
            SocketTimeoutException, Exception {

        HttpClient client = null;
        HttpPost post = new HttpPost(url);
        try {
            if (formParams != null && !formParams.isEmpty()) {
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
                post.setEntity(entity);
            }

            if (headers != null && !headers.isEmpty()) {
                for (Entry<String, String> entry : headers.entrySet()) {
                    post.addHeader(entry.getKey(), entry.getValue());
                }
            }
            // 设置参数
            Builder customReqConf = RequestConfig.custom();
            if (connTimeout != null) {
                customReqConf.setConnectTimeout(connTimeout);
            }
            if (readTimeout != null) {
                customReqConf.setSocketTimeout(readTimeout);
            }
            post.setConfig(customReqConf.build());
            HttpResponse res = null;
            if (url.startsWith("https")) {
                // 执行 Https 请求.
                client = createSSLInsecureClient();
                res = client.execute(post);
            } else {
                // 执行 Http 请求.
                client = HttpClientUtils.client;
                res = client.execute(post);
            }
            return IOUtils.toString(res.getEntity().getContent(), "UTF-8");
        } finally {
            post.releaseConnection();
            if (url.startsWith("https") && client != null
                    && client instanceof CloseableHttpClient) {
                ((CloseableHttpClient) client).close();
            }
        }
    }

    public static HttpResponse postFormRes(String url, Map<String, String> params, Map<String, String> headers, Integer connTimeout,Integer readTimeout) throws ConnectTimeoutException,
            SocketTimeoutException, Exception {

        HttpClient client = null;
        HttpPost post = new HttpPost(url);
        try {
            if (params != null && !params.isEmpty()) {
                List<NameValuePair> formParams = new ArrayList<NameValuePair>();
                Set<Entry<String, String>> entrySet = params.entrySet();
                for (Entry<String, String> entry : entrySet) {
                    formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
                post.setEntity(entity);
            }

            if (headers != null && !headers.isEmpty()) {
                for (Entry<String, String> entry : headers.entrySet()) {
                    post.addHeader(entry.getKey(), entry.getValue());
                }
            }
            // 设置参数
            Builder customReqConf = RequestConfig.custom();
            if (connTimeout != null) {
                customReqConf.setConnectTimeout(connTimeout);
            }
            if (readTimeout != null) {
                customReqConf.setSocketTimeout(readTimeout);
            }
            post.setConfig(customReqConf.build());
            HttpResponse res = null;
            if (url.startsWith("https")) {
                // 执行 Https 请求.
                client = createSSLInsecureClient();
                res = client.execute(post);
            } else {
                // 执行 Http 请求.
                client = HttpClientUtils.client;
                res = client.execute(post);
            }
            return res;
            //return IOUtils.toString(res.getEntity().getContent(), "UTF-8");
        } finally {
            post.releaseConnection();
            if (url.startsWith("https") && client != null
                    && client instanceof CloseableHttpClient) {
                ((CloseableHttpClient) client).close();
            }
        }
    }


    /**
     * 发送一个 GET 请求
     *
     * @param url
     * @param charset
     * @param connTimeout  建立链接超时时间,毫秒.
     * @param readTimeout  响应超时时间,毫秒.
     * @return
     * @throws ConnectTimeoutException   建立链接超时
     * @throws SocketTimeoutException   响应超时
     * @throws Exception
     */
    public static String get(String url, String charset, Integer connTimeout,Integer readTimeout,Map<String,String>paramMap,Map<String,String>headerMap,HttpHost httpHost)
            throws ConnectTimeoutException,SocketTimeoutException, Exception {

        StringBuffer sb = new StringBuffer();
        if (paramMap != null && !paramMap.isEmpty()) {
            Iterator iterator = paramMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry entry = (Entry) iterator.next();
                sb.append(entry.getKey().toString() + "="
                        + entry.getValue().toString() + "&");
            }

            url = url + "?" + sb.substring(0, sb.length()-1);
        }

        HttpClient client = null;
        HttpGet get = new HttpGet(url);
        String result = "";
        try {
            // 设置参数
            Builder customReqConf = RequestConfig.custom();
            if (connTimeout != null) {
                customReqConf.setConnectTimeout(connTimeout);
            }
            if (readTimeout != null) {
                customReqConf.setSocketTimeout(readTimeout);
            }
            if(httpHost!=null){
                customReqConf.setProxy(httpHost);
            }

            if(headerMap!=null){
                for (String key : headerMap.keySet()) {
                    get.addHeader(key, headerMap.get(key));
                }
            }
            get.setConfig(customReqConf.build());

            HttpResponse res = null;

            if (url.startsWith("https")) {
                // 执行 Https 请求.
                client = createSSLInsecureClient();
                res = client.execute(get);
            } else {
                // 执行 Http 请求.
                client = HttpClientUtils.client;
                res = client.execute(get);
            }
            result = IOUtils.toString(res.getEntity().getContent(), charset);
        } finally {
            get.releaseConnection();
            if (url.startsWith("https") && client != null && client instanceof CloseableHttpClient) {
                ((CloseableHttpClient) client).close();
            }
        }
        return result;
    }


    public static InputStream getres(String url, String charset, Integer connTimeout,Integer readTimeout,Map<String,String>paramMap,Map<String,String>headerMap)
            throws ConnectTimeoutException,SocketTimeoutException, Exception {

        StringBuffer sb = new StringBuffer();
        if (paramMap != null && !paramMap.isEmpty()) {
            Iterator iterator = paramMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry entry = (Entry) iterator.next();
                sb.append(entry.getKey().toString() + "="
                        + entry.getValue().toString() + "&");
            }

            url = url + "?" + sb.substring(0, sb.length()-1);
        }

        HttpClient client = null;
        HttpGet get = new HttpGet(url);
        InputStream result = null;
        try {
            // 设置参数
            Builder customReqConf = RequestConfig.custom();
            if (connTimeout != null) {
                customReqConf.setConnectTimeout(connTimeout);
            }
            if (readTimeout != null) {
                customReqConf.setSocketTimeout(readTimeout);
            }

            for (String key : headerMap.keySet()) {
                get.addHeader(key, headerMap.get(key));
            }

            get.setConfig(customReqConf.build());

            HttpResponse res = null;

            if (url.startsWith("https")) {
                // 执行 Https 请求.
                client = createSSLInsecureClient();
                res = client.execute(get);
            } else {
                // 执行 Http 请求.
                client = HttpClientUtils.client;
                res = client.execute(get);
            }

            result = res.getEntity().getContent();
        } finally {
            get.releaseConnection();
            if (url.startsWith("https") && client != null && client instanceof CloseableHttpClient) {
                ((CloseableHttpClient) client).close();
            }
        }
        return result;
    }

    public static HttpResponse get(String url, String charset, Integer connTimeout, Integer readTimeout, Map<String,String>paramMap, Map<String,String>headerMap, String other, HttpHost httpHost)
            throws ConnectTimeoutException,SocketTimeoutException, Exception {

        StringBuffer sb = new StringBuffer();
        if (paramMap != null && !paramMap.isEmpty()) {
            Iterator iterator = paramMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry entry = (Entry) iterator.next();
                sb.append(entry.getKey().toString() + "="
                        + entry.getValue().toString() + "&");
            }

            url = url + "?" + sb.substring(0, sb.length()-1);
        }

        HttpClient client = null;
        HttpResponse res = null;
        HttpGet get = new HttpGet(url);
        String result = "";
        try {
            // 设置参数
            Builder customReqConf = RequestConfig.custom();
            if (connTimeout != null) {
                customReqConf.setConnectTimeout(connTimeout);
            }
            if (readTimeout != null) {
                customReqConf.setSocketTimeout(readTimeout);
            }
            if(httpHost!=null){
                customReqConf.setProxy(httpHost);
            }

            for (String key : headerMap.keySet()) {
                get.addHeader(key, headerMap.get(key));
            }

            get.setConfig(customReqConf.build());

            if (url.startsWith("https")) {
                // 执行 Https 请求.
                client = createSSLInsecureClient();
                res = client.execute(get);
            } else {
                // 执行 Http 请求.
                client = HttpClientUtils.client;
                res = client.execute(get);
            }
            result = IOUtils.toString(res.getEntity().getContent(), charset);
        } finally {
            get.releaseConnection();
            if (url.startsWith("https") && client != null && client instanceof CloseableHttpClient) {
                ((CloseableHttpClient) client).close();
            }
        }

        return res;
    }

    /**
     * 从 response 里获取 charset
     *
     * @param ressponse
     * @return
     */
    private static String getCharsetFromResponse(HttpResponse ressponse) {
        // Content-Type:text/html; charset=GBK
        if (ressponse.getEntity() != null  && ressponse.getEntity().getContentType() != null && ressponse.getEntity().getContentType().getValue() != null) {
            String contentType = ressponse.getEntity().getContentType().getValue();
            if (contentType.contains("charset=")) {
                return contentType.substring(contentType.indexOf("charset=") + 8);
            }
        }
        return null;
    }



    /**
     * 创建 SSL连接
     * @return
     * @throws GeneralSecurityException
     */
    private static CloseableHttpClient createSSLInsecureClient() throws GeneralSecurityException {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] chain,String authType) throws CertificateException {
                    return true;
                }
            }).build();

            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {

                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }

                public void verify(String host, SSLSocket ssl)
                        throws IOException {
                }

                public void verify(String host, X509Certificate cert)
                        throws SSLException {
                }

                public void verify(String host, String[] cns,
                                   String[] subjectAlts) throws SSLException {
                }

            });

            return HttpClients.custom().setSSLSocketFactory(sslsf).build();

        } catch (GeneralSecurityException e) {
            throw e;
        }
    }
    /**
     * get方法提交
     *
     * @param url
     *            String 访问的URL
     *            String 提交的内容
     * @param repType
     *            返回类型
     * @return String
     * */
    public static byte[] getImgRequest(String url, String repType) {
        String result = "";
        byte[] resByt = null;
        try {
            URL urlObj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlObj
                    .openConnection();

            // 连接超时
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout(25000);

            // 读取超时 --服务器响应比较慢,增大时间
            conn.setReadTimeout(25000);
            conn.setRequestMethod("GET");

            conn.addRequestProperty("Accept-Language", "zh-cn");
            conn.addRequestProperty("Content-type", repType);
            conn.addRequestProperty(
                    "User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727)");
            conn.connect();

            PrintWriter out = new PrintWriter(new OutputStreamWriter(
                    conn.getOutputStream(), "UTF-8"), true);

            if ("image/jpeg".equals(repType)) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BufferedImage bufImg = ImageIO.read(conn.getInputStream());
                ImageIO.write(bufImg, "jpg", outputStream);
                resByt = outputStream.toByteArray();
                outputStream.close();

            } else {
                // 取得输入流，并使用Reader读取
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));

                System.out.println("=============================");
                System.out.println("Contents of get request");
                System.out.println("=============================");
                String lines = null;
                while ((lines = reader.readLine()) != null) {
                    System.out.println(lines);
                    result += lines;
                    result += "\r";
                }
                resByt = result.getBytes();
                reader.close();
            }
            out.print(resByt);
            out.flush();
            out.close();
            // 断开连接
            conn.disconnect();
            System.out.println("=============================");
            System.out.println("Contents of get request ends");
            System.out.println("=============================");
        } catch (MalformedURLException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }

        return resByt;
    }
    public static String doHttpGetGizpForImage(String url, String charset,Map<String,String>paramMap,Map<String,String>headerMap)
            throws ConnectTimeoutException,SocketTimeoutException, Exception {

        StringBuffer sb = new StringBuffer();
        if (paramMap != null && !paramMap.isEmpty()) {
            Iterator iterator = paramMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry entry = (Entry) iterator.next();
                sb.append(entry.getKey().toString() + "="
                        + entry.getValue().toString() + "&");
            }

            url = url + "?" + sb.substring(0, sb.length()-1);
        }

        HttpClient client = null;
        HttpGet get = new HttpGet(url);
        String result = "";
        try {
            // 设置参数
            Builder customReqConf = RequestConfig.custom();
            if (connTimeout != null) {
                customReqConf.setConnectTimeout(connTimeout);
            }
            if (readTimeout != null) {
                customReqConf.setSocketTimeout(readTimeout);
            }

            if(headerMap!=null){
                for (String key : headerMap.keySet()) {
                    get.addHeader(key, headerMap.get(key));
                }
            }
            get.setConfig(customReqConf.build());

            HttpResponse res = null;

            if (url.startsWith("https")) {
                // 执行 Https 请求.
                client = createSSLInsecureClient();
                res = client.execute(get);
            } else {
                // 执行 Http 请求.
                client =HttpClientUtils.client;
                res = client.execute(get);
            }
            InputStream is = res.getEntity().getContent();
            byte[] data = null;
            // 读取图片字节数组
            try {
                ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
                byte[] buff = new byte[100];
                int rc = 0;
                while ((rc = is.read(buff, 0, 100)) > 0) {
                    swapStream.write(buff, 0, rc);
                }
                data = swapStream.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            result =  new String(Base64Utils.decode(data));
        } finally {
            get.releaseConnection();
            if (url.startsWith("https") && client != null && client instanceof CloseableHttpClient) {
                ((CloseableHttpClient) client).close();
            }
        }
        return result;
    }
    // Convert stream to string
    public static String ConvertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error=" + e.toString());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                System.out.println("Error=" + e.toString());
            }
        }
        return sb.toString();

    }


    public static boolean ping(String ipAddress, int pingTimes, int timeOut) {
        BufferedReader in = null;
        String pingCommand;
        Runtime r = Runtime.getRuntime();
        String osName = System.getProperty("os.name");
        if(osName.contains("Windows")){
            pingCommand = "ping " + ipAddress + " -n " + pingTimes    + " -w " + timeOut;
        }else if(osName.contains("Mac")){
            pingCommand = "ping " + " -c " + "4" + " -t " + "2 " + ipAddress;
        }else{//linux
            pingCommand = "ping " + " -c " + "4" + " -w " + "2 " + ipAddress;
        }
        try {
            Process p = r.exec(pingCommand);
            if (p == null) {
                return false;
            }
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            int connectedCount = 0;
            String line;
            while ((line = in.readLine()) != null) {
                connectedCount += getCheckResult(line,osName);
            }
            return connectedCount >= 2 ? true : false;
        } catch (Exception ex) {
            ex.printStackTrace(); //出现异常则返回假
            return false;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static int getCheckResult(String line,String osName) {
        if(osName.contains("Windows")){
            if(line.contains("TTL=")){
                return 1;
            }
        }else{
            if(line.contains("ttl=")){
                return 1;
            }
        }
        return 0;
    }
}

package Downloader;

import org.apache.http.*;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by arloor on 17-5-7.
 */

public class MyHttpClient {
    private CloseableHttpClient client;
    private long waittime;
    private int retryNum;
    //用于保存获取的cookie，每访问一个网页都会通过setcookie方法增加新的cookie
    private Map<String, String> cookieMap = new HashMap<String, String>(64);
    private CloseableHttpResponse response;

    /**
     * 构造方法
     * 使用STANDARD_STRICT的cookie策略
     */
    public MyHttpClient(long waittime, int retryNum) {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // don't check
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // don't check
                    }
                }
        };

        SSLContext ctx = null;
        try {
            ctx = SSLContext.getInstance("TLS");
            ctx.init(null, trustAllCerts, null);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }


        LayeredConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(ctx);

        RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT).build();
        this.client = HttpClients.custom().setDefaultRequestConfig(requestConfig).setSSLSocketFactory(sslSocketFactory).build();
        this.waittime = waittime;
        this.retryNum = retryNum;
    }

    public MyHttpClient(long waittime) {
        this(waittime, 5);
    }

    public MyHttpClient() {
        this(500, 5);
    }

    /**
     * get请求网页，如果response不正确，会重试retryNum次
     *
     * @param uri 要请求的uri
     * @return 如果请求失败返回null，如果请求成功返回HttpEntity
     */
    public HttpEntity get(URI uri) {
        HttpGet get = new HttpGet(uri);

        return doRequest(get, Method.GET);
    }

    /**
     * post请求网页，如果response不正确，会重试retryNum次
     *
     * @param uri      要请求的uri
     * @param postData post数据
     * @return 如果请求失败返回null，如果请求成功返回HttpEntity
     */
    public HttpEntity post(URI uri, Map<String, String> postData) {
        HttpPost post = new HttpPost(uri);

        //构造post数据
        System.out.println("----------------------------------------------------");
        System.out.println("提交的表单数据");
        List<NameValuePair> valuePairs = new LinkedList<NameValuePair>();
        for (Map.Entry<String, String> postDataEntry : postData.entrySet()
                ) {
            valuePairs.add(new BasicNameValuePair(postDataEntry.getKey(), postDataEntry.getValue()));
            System.out.println(postDataEntry.getKey() + ":" + postDataEntry.getValue());
        }
        System.out.println("----------------------------------------------------\n\n\n");
        // 上面的原型是这个
        // valuePairs.add(new BasicNameValuePair("password", "yueyue"));
        UrlEncodedFormEntity postEntity = new UrlEncodedFormEntity(valuePairs, Consts.UTF_8);
        post.setEntity(postEntity);
        return doRequest(post, Method.POST);

    }

    public HttpEntity doRequest(Object request, Method method) {

        HttpUriRequest requestClone;
        switch (method) {
            case GET:
                requestClone = (HttpGet) request;
                break;
            case POST:
                requestClone = (HttpPost) request;
                break;
            default:
                requestClone = (HttpGet) request;
        }
        //设置User-Agent
        requestClone.setHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.96 Safari/537.36");
        System.out.println("请求： " + requestClone.getURI());
        //设置refer 发现如果refer为空很可能被反爬虫
        String[] refers = requestClone.getURI().toString().split("/");
        String refer = refers[0] + "//" + refers[2] + "/";
        requestClone.setHeader("Refer", refer);
        System.out.println(requestClone.getHeaders("Refer")[0]);
        //带cookie爬取
        String cookie = getCookie();
        requestClone.setHeader("Cookie", cookie);
        System.out.println("请求前的Cookie:" + getCookie());
        try {
            boolean responseValid = false;
            //如果response不是200-300则重试retryNum
            int i = 0;
            while (!responseValid) {
                Thread.sleep(waittime);
                response = client.execute(requestClone);
                responseValid = response != null &&
                        //下面这个status需要在200-300,如果直接折秤200会有问题
                        //因为302 状态是登陆验证中经常出现的，githubdeno就因为这个错困扰了很久
                        response.getStatusLine().getStatusCode() < 400 && response.getStatusLine().getStatusCode() >= 200;
                if (i == retryNum) {
                    System.out.println("请求" + retryNum + "次仍然失败，放弃");
                    System.out.println("\n\n\n");
                    return null;
                }
                i++;
            }

            addCookie(response);
            System.out.println("请求后的Cookie:" + getCookie());
            HttpEntity responseEntity = response.getEntity();
            Thread.sleep(waittime);//线程等待waittime
            System.out.println("\n\n\n");
            return responseEntity;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("\n\n\n");
        return null;
    }

    /**
     * 从响应中获取Cookie，保存在cookieMap中
     *
     * @param httpResponse
     */

    public void addCookie(HttpResponse httpResponse) {
        Header headers[] = httpResponse.getHeaders("Set-Cookie");
        if (headers == null || headers.length == 0) {
            return;
        }
        String cookie = "";
        for (int i = 0; i < headers.length; i++) {
            cookie = headers[i].getValue().split(";")[0];
            try {
                String cookieKey = cookie.split("=")[0];
                String cookieValue = cookie.split("=")[1];
                cookieMap.put(cookieKey, cookieValue);
            } catch (ArrayIndexOutOfBoundsException e) {
                //处理如果返回的是空cookie则把value设置成空字符
                try {
                    String cookieKey = cookie.split("=")[0];
                    String cookieValue = "";
                    cookieMap.put(cookieKey, cookieValue);
                    e.printStackTrace();
                } catch (ArrayIndexOutOfBoundsException e1) {
                    e.printStackTrace();
                }
            }
        }
        return;
    }

    public void addCookie(String key, String value) {
        cookieMap.put(key, value);
    }


    public void clearCoookie() {
        cookieMap.clear();
    }

    //从cookieMap中获取cookie
    public String getCookie() {
        String cookieString = "";
        for (Map.Entry<String, String> cookie : cookieMap.entrySet()
                ) {
            cookieString += cookie.getKey() + "=" + cookie.getValue() + "; ";
        }
        if (cookieString.length() > 2)
            cookieString = cookieString.substring(0, cookieString.length() - 2);
        return cookieString;
    }

    /**
     * 关闭client
     * 每一个client都需要关闭
     */
    public void close() {
        try {
            if (response != null)
                response.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

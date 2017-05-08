package Downloader;

import org.apache.http.HttpEntity;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by arloor on 17-5-7.
 */

/**
 * Graber会链式的请求uriVOList里的所有uri(会区分get和method)，返回最后一个uri获取的responseEntity
 * 这样的目的是得到需要的cookie
 */
public class Graber {
    private MyHttpClient client;
    /**
     * 需要抓取的网页实体的列表，将会链式地请求这些列表，过程中会增加cookie，最终返回最后一个的response的Entity
     */
    private List<RequestEntity> requestEntityList;

    public Graber(MyHttpClient client) {
        this.requestEntityList = new ArrayList<RequestEntity>();
        this.client = client;
    }

    /**
     * 清空requestEntityList，以便开始新的爬虫过程
     */
    public void clearRequestList() {
        requestEntityList.clear();
    }

    public void clearCookie() {
        client.clearCoookie();
    }

    public void clearAll() {
        clearCookie();
        clearRequestList();
    }

    /**
     * 增加请求列表
     *
     * @param requestEntity
     */
    public void add(RequestEntity requestEntity) {
        requestEntityList.add(requestEntity);
    }

    /**
     * 增加请求列表
     *
     * @param requestEntityList
     */
    public void add(List<RequestEntity> requestEntityList) {
        for (RequestEntity vo : requestEntityList
                ) {
            add(vo);
        }
    }

    /**
     * 主要就是用于关闭response和关闭client
     */
    public void stop() {
        client.close();
    }

    /**
     * 抓取列表里的请求实体
     *
     * @return
     */
    public HttpEntity grab() {
        HttpEntity entity = null;
        for (RequestEntity requestEntity : requestEntityList
                ) {
            URI uri = requestEntity.getUri();
            Method method = requestEntity.getMethod();

            switch (method) {
                case GET:
                    entity = client.get(uri);
                    break;
                case POST:
                    Map<String, String> postData = requestEntity.getPostData();
                    entity = client.post(uri, postData);
                    break;
                default:
                    break;
            }
        }
        return entity;
    }

    public void addCoookie(String key, String value) {
        client.addCookie(key, value);
    }

    public MyHttpClient getClient() {
        return client;
    }
}

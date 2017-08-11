package Downloader.GraberImpl;

import Downloader.Graber;
import Downloader.Method;
import Downloader.MyHttpClient;
import Downloader.RequestEntity;
import org.apache.http.HttpEntity;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by moontell on 2017/8/11.
 */
public class LinearGraber implements Graber {

    private MyHttpClient client;

    public ConcurrentLinkedQueue<HttpEntity> getResponseQuene() {
        return responseQuene;
    }

    private ConcurrentLinkedQueue<HttpEntity> responseQuene=new ConcurrentLinkedQueue<HttpEntity>();

    /**
     * 需要抓取的网页实体的队列，将会链式地请求这些列表，过程中会增加cookie，最终返回最后一个的response的Entity
     */
    private ConcurrentLinkedQueue<RequestEntity> requestEntityQuene;

    public LinearGraber(MyHttpClient client) {
        this.requestEntityQuene = new ConcurrentLinkedQueue<RequestEntity>();
        this.client = client;
    }


    public void clearRequestQuene() {
        requestEntityQuene.clear();
    }

    public void clearCookie() {
        client.clearCookie();
    }

    public void add(RequestEntity requestEntity) {
        requestEntityQuene.add(requestEntity);
    }

    public void close() {
        client.close();
    }

    public void clearResponseQuene() {
        responseQuene.clear();
    }


    public ConcurrentLinkedQueue<HttpEntity> grab() {
        HttpEntity entity = null;
        for (RequestEntity requestEntity : requestEntityQuene
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
        responseQuene.add(entity);
        return responseQuene;
    }

    public void addCoookie(String key, String value) {
        client.addCookie(key, value);
    }
}

package Downloader;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by arloor on 17-5-7.
 */

/**
 * httpclient发起请求除了cookie，需要设置的有
 * uri，method，postData
 * 这个类就是这三个东西的一个封装吧
 */
public class RequestEntity {
    private URI uri;
    private Method method;
    private Map<String, String> postData;

    public RequestEntity(URI uri, Map<String, String> postData) {
        this.uri = uri;
        this.method = Method.POST;
        this.postData = postData;
    }

    public RequestEntity(URI uri) {
        this.uri = uri;
        this.method = Method.GET;
        this.postData = new HashMap<String, String>();
    }

    /**
     * 使用addPostData,目的是避免复杂的创建过程
     *
     * @param key
     * @param value
     */
    public void addPostData(String key, String value) {
        setMethod(Method.POST);
        postData.put(key, value);
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Map<String, String> getPostData() {
        return postData;
    }

    public void clearPostData() {
        postData.clear();
    }

    public void setPostData(Map<String, String> postData) {
        this.postData = postData;
    }

    public void addPostData(Map<String, String> postData) {
        for (Map.Entry<String, String> entry : postData.entrySet()
                ) {
            addPostData(entry.getKey(), entry.getValue());
        }
    }
}

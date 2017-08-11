package Downloader;

import org.apache.http.HttpEntity;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by moontell on 2017/8/11.
 */
public interface Graber {

    void clearRequestQuene();

    void clearCookie();

    ConcurrentLinkedQueue<HttpEntity> getResponseQuene();

    /**
     * 增加请求列表
     *
     * @param requestEntity
     */
    void add(RequestEntity requestEntity);

    /**
     * 主要就是用于关闭response和关闭client
     */
    void close();

    void clearResponseQuene();

    /**
     * 抓取列表里的请求实体
     *
     * @return
     */
    ConcurrentLinkedQueue<HttpEntity> grab();

    void addCoookie(String key, String value);


}

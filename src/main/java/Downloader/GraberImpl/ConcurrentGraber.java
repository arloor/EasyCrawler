package Downloader.GraberImpl;

import Downloader.Graber;
import Downloader.Method;
import Downloader.MyHttpClient;
import Downloader.RequestEntity;
import org.apache.http.HttpEntity;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.Thread.sleep;

/**
 * Created by moontell on 2017/8/11.
 */
public class ConcurrentGraber implements Graber {

    private class DownloadThread extends Thread{
        private final int i;
        private MyHttpClient client;
        public DownloadThread(int i){
            this.i=i;
            client=clients[i];
        }

        @Override
        public void run(){
            HttpEntity response = null;
            RequestEntity requestEntity;
            while((requestEntity=requestEntityQuene.poll())!=null){
                URI uri = requestEntity.getUri();
                System.out.println(uri);
                Method method = requestEntity.getMethod();

                switch (method) {
                    case GET:
                        response = client.get(uri);
                        break;
                    case POST:
                        Map<String, String> postData = requestEntity.getPostData();
                        response = client.post(uri, postData);
                        break;
                    default:
                        break;
                }
                responseQuene.add(response);




            }
            System.out.println(i+"  退出");
        }
    }

    private MyHttpClient[] clients;

    private int threadNum;

    private static ConcurrentLinkedQueue<HttpEntity> responseQuene=new ConcurrentLinkedQueue<HttpEntity>();

    private ConcurrentLinkedQueue<RequestEntity> requestEntityQuene;

    public ConcurrentGraber(long waittime,int retryTime,int threadNum) {
        this.requestEntityQuene = new ConcurrentLinkedQueue<RequestEntity>();
        this.threadNum=threadNum;
        clients=new MyHttpClient[threadNum];
        for (int i=0;i<threadNum;i++){
            clients[i]=new MyHttpClient(waittime,retryTime);
        }
    }

    public ConcurrentLinkedQueue<HttpEntity> getResponseQuene() {
        return responseQuene;
    }

    public void clearRequestQuene() {
        this.requestEntityQuene.clear();
    }

    public void clearCookie() {
        for (MyHttpClient cell:clients
             ) {
            cell.clearCookie();
        }
    }

    public void add(RequestEntity requestEntity) {
        requestEntityQuene.add(requestEntity);
    }

    public void close() {
        for (MyHttpClient cell:clients
                ) {
            cell.close();
        }
    }

    public void clearResponseQuene() {
        responseQuene.clear();
    }

    public ConcurrentLinkedQueue<HttpEntity> grab() {
        for (int i = 0; i <threadNum ; i++) {
            DownloadThread thread=new DownloadThread(i);
            thread.start();
        }
        while(!requestEntityQuene.isEmpty()){
            try {
                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return responseQuene;
    }

    public void addCoookie(String key, String value) {
        for (MyHttpClient cell:clients
                ) {
            cell.addCookie(key,value);
        }
    }
}

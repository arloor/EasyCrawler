import Downloader.Graber;
import Downloader.MyHttpClient;
import Downloader.RequestEntity;
import Processor.Parser;
import org.apache.http.HttpEntity;
import org.apache.http.client.utils.URIBuilder;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.util.Map;

/**
 * Created by arloor on 17-5-7.
 * demo:这是爬去moontell.cn完成登陆的demo
 * 注意：每一个爬虫过程都要将entity转化成其他类型比如string
 * 才可以开始下一次爬虫过程
 */

public class MoontellDemo {
    public static void main(String[] args) throws Exception {
        MyHttpClient client = new MyHttpClient();
        Graber graber = new Graber(client);

        URI uri;
        RequestEntity requestEntity;

        //这是需要登陆的页面
        uri = new URIBuilder().setScheme("http").setHost("moontell.cn").setPath("/manage.jsp").build();
        requestEntity = new RequestEntity(uri);
        graber.add(requestEntity);

        //这里是抓取登陆页面需要提交的表单
        HttpEntity tempEntity = graber.grab();
        GithubDemo.MyParser myParser = new GithubDemo.MyParser(tempEntity);
        Map<String, String> postData = myParser.getCommit();
        graber.clearRequestList();


        //向登陆验证的页面发送postData，从而登陆
        uri = new URIBuilder().setScheme("http").setHost("moontell.cn").setPath("/verify").build();
        requestEntity = new RequestEntity(uri);
        requestEntity.addPostData("user", "*******");
        requestEntity.addPostData("password", "*********");
        graber.add(requestEntity);

        //请求http://moontell.cn/post.jsp 可以看到访客不可见的文章名，说明登陆了
        uri = new URIBuilder().setScheme("http").setHost("moontell.cn").setPath("/post.jsp").build();
        requestEntity = new RequestEntity(uri);
        graber.add(requestEntity);


        HttpEntity entity = graber.grab();


        MyParser parser = new MyParser(entity);
        parser.getTitle();
        graber.clearRequestList();//clear以便开始下一个爬虫过程
        graber.stop();
    }

    //自定义的Parser
    static class MyParser extends Parser {

        public MyParser(HttpEntity entity) {
            super(entity);
        }

        public void getTitle() {
            Elements elements = doc.getElementsByTag("h1");
            for (Element element : elements
                    ) {
                System.out.println(element.html());
            }
        }
    }
}

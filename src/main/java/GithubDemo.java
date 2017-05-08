import Downloader.Graber;
import Downloader.Method;
import Downloader.MyHttpClient;
import Downloader.RequestEntity;
import Processor.Parser;
import org.apache.http.HttpEntity;
import org.apache.http.client.utils.URIBuilder;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Created by arloor on 17-5-8.
 */
public class GithubDemo {
    public static void main(String[] args) throws URISyntaxException {
        Graber graber = new Graber(new MyHttpClient(500, 5));

        URI uri;
        RequestEntity requestEntity;

        //增加一些固定的cookie
        graber.addCoookie("_ga", "GA1.2.1037466395.1494170889");
        graber.addCoookie("_gat", "1");
        graber.addCoookie("_gh_sess", "");
        graber.addCoookie("_gid", "GA1.2.1649991356.1494172217");
        graber.addCoookie("_octo", "GH1.1.1943801900.1494170889");
        graber.addCoookie("tz", "Asia%2FShanghai");
        graber.addCoookie("logged_in", "no");

        uri = new URIBuilder().setScheme("https").setHost("github.com").setPath("/login").build();
        requestEntity = new RequestEntity(uri);
        graber.add(requestEntity);

        //这里是抓取登陆页面需要提交的表单
        HttpEntity entity = graber.grab();
        MyParser myParser = new MyParser(entity);
        //Parser的getCommit()可以获网页中form中的input标签，这就是是需要提交的postData
        Map<String, String> postData = myParser.getCommit();
        graber.clearRequestList();

        //向登陆验证的页面发送postData，从而登陆
        uri = new URIBuilder().setScheme("https").setHost("github.com").setPath("/session").build();
        requestEntity = new RequestEntity(uri);
        requestEntity.setMethod(Method.POST);
        requestEntity.addPostData(postData);
        requestEntity.addPostData("login", "admin@arloor.com");//这里请填写自己的账号
        requestEntity.addPostData("password", "********");//这里请填写自己的密码，还有下面的setpath也要设成自己的
        graber.add(requestEntity);

        //访问https://github.com/arloor?tab=repositories
        //从控制台能够看到，private的仓库被输出了，说明成功登陆
        uri = new URIBuilder().setScheme("https").setHost("github.com").setPath("/arloor").setParameter("tab", "repositories").build();
        requestEntity = new RequestEntity(uri);
        graber.add(requestEntity);

        HttpEntity entity1 = graber.grab();

        MyParser myParser1 = new MyParser(entity1);
        myParser1.printRepositoriesName();


        graber.stop();
    }

    //自定义的parser,主要就是实现提取，这里提取的就是仓库的名称
    static class MyParser extends Parser {

        public MyParser(HttpEntity entity) {
            super(entity);
        }

        public void printRepositoriesName() {
            Elements elements = doc.getElementsByTag("h3");
            for (Element cell : elements
                    ) {
                System.out.println(cell.getAllElements().first().text());
            }
        }

    }
}

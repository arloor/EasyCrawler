package Processor;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by arloor on 17-5-8.
 */
public abstract class Parser {
    public Document doc;

    public Parser(HttpEntity entity) {
        try {
            this.doc = Jsoup.parse(EntityUtils.toString(entity));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取网页中需要填写的表单
     *
     * @return
     */
    public Map<String, String> getCommit() {
        Map<String, String> postData = new HashMap<String, String>();
        Element form = doc.getElementsByTag("form").first();
        Elements elements = form.getElementsByAttribute("name");
        System.out.println("----------------------------------------------------");
        System.out.println("需要填写的表单数据有：");
        for (Element element : elements
                ) {
            System.out.println(element.attr("name") + ":" + element.attr("value"));
            postData.put(element.attr("name"), element.attr("value"));
        }
        System.out.println("----------------------------------------------------");
        return postData;
    }
}

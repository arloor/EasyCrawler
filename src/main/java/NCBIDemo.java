import Downloader.Graber;
import Downloader.GraberImpl.ConcurrentGraber;
import Downloader.GraberImpl.LinearGraber;
import Downloader.MyHttpClient;
import Downloader.RequestEntity;
import Processor.Parser;
import org.apache.http.HttpEntity;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.Thread.sleep;

/**
 * Created by moontell on 2017/8/8.
 */
public class NCBIDemo {
    volatile static String pageNo=new String();
    volatile static int curNo=1;
    volatile  static ConcurrentLinkedQueue<String> linkQuene=new ConcurrentLinkedQueue<String>();

    public static void main(String[] args) throws URISyntaxException, InterruptedException {

        Graber graber=new LinearGraber(new MyHttpClient(500, 5));
        //增加一些固定的cookie
        //不然网站会禁止访问
        graber.addCoookie("MyNcbiSigninPreferences","O2dvb2dsZSY%3D");
        graber.addCoookie("_ga","GA1.2.597577670.1502183053");
        graber.addCoookie("_gat","1");
        graber.addCoookie("_gid","GA1.2.1338710214.1502183053");
        graber.addCoookie("__sonar","3622631656911641626");
        graber.addCoookie("id","=1497620414|et=730|cs=002213fd48c264ac1ca119dba5");
        String s= "MyNcbiSigninPreferences\tO2dvb2dsZSY%3D\tN/A\tN/A\tN/A\t40\t\t\t\t\n" +
                "WT_FPC\tid=2d22651696987ce8a7c1502137277672:lv=1502137277672:ss=1502137277672\tN/A\tN/A\tN/A\t78\t\t\t\t\n" +
                "WebEnv\t1vi9e77n77IWD2DK_CFta1PUVVCQEAnil8Q7WN8zFnTd9E3uNZdnCgPeCQS_YeoqWNue_EUlNyEcL83d3jydi3AfCncDoIAfvrfkt%40396A24269897E821_0067SID\tN/A\tN/A\tN/A\t137\t\t\t\t\n" +
                "_ceg.s\toud0nw\tN/A\tN/A\tN/A\t15\t\t\t\t\n" +
                "_ceg.u\toud0nw\tN/A\tN/A\tN/A\t15\t\t\t\t\n" +
                "_ga\tGA1.3.597577670.1502183053\tN/A\tN/A\tN/A\t32\t\t\t\t\n" +
                "_ga\tGA1.2.597577670.1502183053\tN/A\tN/A\tN/A\t32\t\t\t\t\n" +
                "_gat\t1\tN/A\tN/A\tN/A\t8\t\t\t\t\n" +
                "_gid\tGA1.3.1338710214.1502183053\tN/A\tN/A\tN/A\t34\t\t\t\t\n" +
                "_gid\tGA1.2.1338710214.1502183053\tN/A\tN/A\tN/A\t34\t\t\t\t\n" +
                "books.article.report\t\tN/A\tN/A\tN/A\t23\t\t\t\t\n" +
                "ncbi_sid\t396A24269897E821_0067SID\tN/A\tN/A\tN/A\t35\t\t\t\t\n" +
                "starnext\tK4OwNg9ghgJiCmAPALgLgEwF54mQJ3gC8AyAZkwAdgAjAW3hmIBZMYIBjAZ2FuIDZWHbrwDsmABbJaYYgA5M6AAzEAnK3gAzKMDDJiARkWZ24gJYgoB/ZgCsBrKEiwD5fftLob6PgZYUoAObwAPomUCBBBgJQYDL6YlpgnPAG8onJBmqkKnwAQjYAwjYAoiqyAIJ8iioqhor1DQ3FACL1fAXESpjZfMXoIv1llfrlpHWNjUy59ejNndY4+EQApKTlVHQMq+VsXDzbAHIA8gfFnVgA7lcAdCDs1Ka3YLS3puLXARAAbp0siwSEbYbeiMbzYXAAoE0EHbXbCTpif4rNbArZrOH7NbHU6deRIwEiAqomDLQkY2ikgrYs7oNTuPg+UhGER8WRkaz0xlYAZs0jkdIpUgsfDAQU2TDuJgqMgCMYqOykMToJhkeT6WRufRkOkiWQM5hGfTy2rMax8frMLAWZCmL4pJgsRzQOBIPRMcXoYg2VykfU2Fh8Uhsmwevh2GwCLzSmxiLU2eTulU2NTKKqYVPWFkiIA==\tN/A\tN/A\tN/A\t557";
        String[] cookies=s.split("\n");
        for (String cell:cookies
                ) {
            graber.addCoookie(cell.split("\t")[0],cell.split("\t")[1]);
        }

        URI uri;
        RequestEntity requestEntity;
        ConcurrentLinkedQueue<HttpEntity> responses;



        //https://www.ncbi.nlm.nih.gov/pubmed?term=China
        uri =URI.create("https://www.ncbi.nlm.nih.gov/pubmed?term=China");
        requestEntity = new RequestEntity(uri);
        graber.add(requestEntity);
        responses=graber.grab();
        HttpEntity response=responses.peek();





        sleep(2000);

        LinkParser linkParser=new LinkParser(response);
        graber.close();
        List<String> links=linkParser.getLinks();

        graber=new ConcurrentGraber(500,5,20);
        for (String link:links
             ) {
            graber.add(new RequestEntity(URI.create(link)));
        }
        responses = graber.grab();
        sleep(10000);
        int size=responses.size();

        File dir=new File("files");
        if (!dir.exists()){
            dir.mkdir();
        }
        for (int i = 0; i <size ; i++) {
            System.out.println(i);
            File out = new File("files/"+i+".html");
            BufferedOutputStream stream= null;
            if (!out.exists()) {
                try {
                    out.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        out.createNewFile();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            try {
                stream = new BufferedOutputStream(new FileOutputStream(out));
                response=responses.poll();
                response.writeTo(stream);
                stream.close();
                //entity.writeTo(System.out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        graber.close();
    }

    private static class LinkParser extends Parser {

        public LinkParser(HttpEntity entity) {
            super(entity);
        }

        public List<String> getLinks(){
            List<String> links=new LinkedList<String>();
            Elements elements=doc.getElementsByClass("title");
            for (Element cell:elements
                    ) {
                Element link=cell.getElementsByTag("a").first();
                String linkStr=link.attr("href");
                linkStr="https://www.ncbi.nlm.nih.gov"+linkStr;
                links.add(linkStr);
            }
            return links;
        }
    }
}

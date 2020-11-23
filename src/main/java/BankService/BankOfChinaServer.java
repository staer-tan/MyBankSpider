package BankService;

import MySpider.Factory.MySpiderFactory;
import MySpider.MySpider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;

/**
 * 中国银行(BOC)数据爬取服务
 */
public class BankOfChinaServer {

    // 中国银行数据查询总页码
    private static final int bocUrlNumber = 627;
    private static final String SCHEDULE_NAME = "BankOfChinaServer";

    public void start() throws Exception {
        URL[] urls = getBOCUrl();
        String[] allHtml = processGetStrHtml(urls);
        BankOfChinaServer.parseBOCHtml(allHtml);
    }

    /**
     * 根据URL链接返回String类型的html
     * @param urls
     * @return
     * @throws Exception
     */
    public static String[] processGetStrHtml(URL[] urls) throws Exception{
        MySpider mySpider =  MySpiderFactory.getBankDataSpiderNoDataService(urls, SCHEDULE_NAME);
        String[] strHtml = mySpider.startGetStrHtml();
        return strHtml;
    }

    public static URL[] getBOCUrl() throws Exception {
        URL urls[] = new URL[bocUrlNumber];
        for(int page = 1; page <= bocUrlNumber; page++){
            urls[page - 1] = new URL("https://srh.bankofchina.com/search/operation/search.jsp?page=" + page);
        }

        return urls;
    }

    public static void parseBOCHtml(String[] allHtml) throws Exception{
        if(allHtml == null || allHtml.length == 0){
            throw new Exception("the html is null");
        }

        for(String html : allHtml){
            Document document = Jsoup.parse(html);
            Elements elements = document.select("div[class=BOC_main publish]").select("tr");

            int urlZero = 0;
            for(Element ele : elements){
                if(urlZero == 0){
                    urlZero++;
                    continue;
                }
                String content = ele.text();
                // 写入txt文件中
                System.out.println(content);
                // 后序数据相关操作
                String[] bankContent = content.split(" ");

            }
        }
    }
}

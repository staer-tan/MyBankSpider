package BankService;

import MySpider.Factory.MySpiderFactory;
import MySpider.MySpider;
import Util.FileUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URL;

public class IndustrialBankServer {

    private static final String SCHEDULE_NAME = "IndustrialBankServer";

    public static void main(String[] args) throws Exception{
        URL[] urls = getIBUrl();
        String[] allHtml = processGetStrHtml(urls);
        parseBOCHtml(allHtml);
    }

    public static URL[] getIBUrl() throws Exception {
        URL urls[] = new URL[1];
        urls[0] = new URL("http://branch.cib.com.cn/index.html");
        return urls;
    }
    public static String[] processGetStrHtml(URL[] urls) throws Exception {
        MySpider mySpider = MySpiderFactory.getBankDataSpiderService(urls, SCHEDULE_NAME);
        String[] strHtml = mySpider.startGetStrHtml();
        return strHtml;
    }

    public static void parseBOCHtml(String[] allHtml) throws Exception {
        if (allHtml == null || allHtml.length == 0) {
            throw new Exception("the html is null");
        }

        File destinationCCBFile = FileUtil.createEmptyFile(new URL(FileUtil.getPrefix("ProcessorData")).getPath(), "IndustrialBankData");
        RandomAccessFile randomAccessFile_write = new RandomAccessFile(destinationCCBFile, "rw");

        for (String html : allHtml) {
            Document document = Jsoup.parse(html);
            Elements elements = document.select("tbody").select("tr");

            int urlZero = 0;
            for (Element ele : elements) {
                if (urlZero == 0) {
                    urlZero++;
                    continue;

                }
                String content = ele.text();
                System.out.println(content);
//                // 写入本地文件中
                randomAccessFile_write.write(content.getBytes("UTF-8"));
                randomAccessFile_write.write("\n".getBytes("UTF-8"));
//                System.out.println(content);
//                // 后序数据相关操作
            }
        }
    }



}

package BankService;

import DataObject.BankData;
import DataService.DataService;
import DataService.mybatis.BankDataService;
import MySpider.Factory.MySpiderFactory;
import MySpider.MySpider;
import Util.FileUtil;
import Util.StringUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 中国银行(BOC)数据爬取服务
 */
public class BankOfChinaServer {

    // 中国银行数据查询总页码
    private static final int bocUrlNumber = 627;
    private static final String SCHEDULE_NAME = "BankOfChinaServer";

    public void start() throws Exception {
        // 爬取银行数据至本地文件
//        URL[] urls = getBOCUrl();
//        String[] allHtml = processGetStrHtml(urls);
//        BankOfChinaServer.parseBOCHtml(allHtml);

        List<BankData> BDList = BankOfChinaServer.parseBOCLocalFile();
        BankDataService bankDataService = new BankDataService();
        bankDataService.init();
        bankDataService.adds(BDList);

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

        File destinationCCBFile = FileUtil.createEmptyFile(new URL(FileUtil.getPrefix("ProcessorData")).getPath(), "BankOfChinaData");
        RandomAccessFile randomAccessFile_write = new RandomAccessFile(destinationCCBFile, "rw");

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
                // 写入本地文件中
                randomAccessFile_write.write(content.getBytes("UTF-8"));
                randomAccessFile_write.write("\n".getBytes("UTF-8"));
                System.out.println(content);
                // 后序数据相关操作

            }
        }
    }

    public static List<BankData> parseBOCLocalFile() throws Exception{
        RandomAccessFile randomAccessFile_read = new RandomAccessFile(new URL(FileUtil.getPrefix("ProcessorData")).getPath() + "BankOfChinaData", "rw");
        List<BankData> BDList = new ArrayList<>();
        String curLine = "";
        while((curLine = randomAccessFile_read.readLine()) != null){
            if(StringUtil.isNull(curLine)){
                continue;
            }

            // 读取重要参数
            String parseCurLine = new String(curLine.getBytes("ISO-8859-1"), "utf-8");
            String[] bankContent = parseCurLine.split(" ");
            String bankName = bankContent[0];
            String bankLayer = bankContent[1];
            String address = bankContent[2];
            String telephone = bankContent[3];

            System.out.println(bankName + '\t' + bankLayer + '\t' + address + '\t' + telephone);

            // 数据库服务
            BankData bankData = new BankData();
            bankData.setBankType("中国银行");
            bankData.setBankName(bankName);
            bankData.setBankLevel(bankLayer);
            bankData.setAddress(address);
            bankData.setTelephone(telephone);

            BDList.add(bankData);
        }

        return BDList;
    }

}

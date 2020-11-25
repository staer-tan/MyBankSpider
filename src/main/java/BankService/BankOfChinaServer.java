package BankService;

import DataObject.BankData;
import DataService.mybatis.BankDataService;
import MySpider.Factory.MySpiderFactory;
import MySpider.MySpider;
import Util.AddressService.gaoDeServer;
import Util.FileUtil;
import Util.StringUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 中国银行(BOC)数据爬取服务
 */
public class BankOfChinaServer {

    // 中国银行数据查询总页码
    private static final int bocUrlNumber = 627;
    private static final String SCHEDULE_NAME = "BankOfChinaServer";

    public void start() throws Exception {
        String filePath = new URL(FileUtil.getPrefix("ProcessorData")).getPath() + "BankOfChinaData";
        File newFile = new File(filePath);
        // 若本地已有数据，则无需爬虫
        if (!newFile.exists()) {
            // 爬取银行数据至本地文件
//        URL[] urls = getBOCUrl();
//        String[] allHtml = processGetStrHtml(urls);
//        BankOfChinaServer.parseBOCHtml(allHtml);
            System.out.println("爬取本地文件");
        }

        // 从本地文件读数据，持久化到DB
        List<BankData> BDList = BankOfChinaServer.parseBOCLocalFile(filePath);
        BankDataService bankDataService = new BankDataService();
        bankDataService.init();
        bankDataService.adds(BDList);

    }

    /**
     * 根据URL链接返回String类型的html
     *
     * @param urls
     * @return
     * @throws Exception
     */
    public static String[] processGetStrHtml(URL[] urls) throws Exception {
        MySpider mySpider = MySpiderFactory.getBankDataSpiderNoDataService(urls, SCHEDULE_NAME);
        String[] strHtml = mySpider.startGetStrHtml();
        return strHtml;
    }

    public static URL[] getBOCUrl() throws Exception {
        URL urls[] = new URL[bocUrlNumber];
        for (int page = 1; page <= bocUrlNumber; page++) {
            urls[page - 1] = new URL("https://srh.bankofchina.com/search/operation/search.jsp?page=" + page);
        }

        return urls;
    }

    public static void parseBOCHtml(String[] allHtml) throws Exception {
        if (allHtml == null || allHtml.length == 0) {
            throw new Exception("the html is null");
        }

        File destinationCCBFile = FileUtil.createEmptyFile(new URL(FileUtil.getPrefix("ProcessorData")).getPath(), "BankOfChinaData");
        RandomAccessFile randomAccessFile_write = new RandomAccessFile(destinationCCBFile, "rw");

        for (String html : allHtml) {
            Document document = Jsoup.parse(html);
            Elements elements = document.select("div[class=BOC_main publish]").select("tr");

            int urlZero = 0;
            for (Element ele : elements) {
                if (urlZero == 0) {
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

    /**
     * 从本地保存的文件读取数据
     *
     * @param path 保存路径
     * @return List封装的银行数据对象
     * @throws Exception
     */
    public static List<BankData> parseBOCLocalFile(String path) throws Exception {
        RandomAccessFile randomAccessFile_read = new RandomAccessFile(path, "rw");
        List<BankData> BDList = new ArrayList<>();
        String curLine = "";
        while ((curLine = randomAccessFile_read.readLine()) != null) {
            if (StringUtil.isNull(curLine)) {
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

            // 使用地址解析或API查询得到更丰富信息
            String searchAddress = bankName + address;
            Map<String, String> map = gaoDeServer.parseAddress(searchAddress);
            if(map == null || map.size() == 0){
                continue;
            }
            // 数据库服务
            BankData bankData = new BankData();
            bankData.setBankType("中国银行");
            bankData.setBankName(bankName);
            bankData.setBankLevel(bankLayer);
            bankData.setProvince(map.get("province"));
            bankData.setCity(map.get("city"));
            bankData.setArea(map.get("district"));
            bankData.setAddress(address);
            bankData.setTelephone(telephone);
            bankData.setLongitudeX(map.get("longitudeX"));
            bankData.setLatitudeY(map.get("latitudeY"));

            BDList.add(bankData);

        }

        return BDList;
    }

}

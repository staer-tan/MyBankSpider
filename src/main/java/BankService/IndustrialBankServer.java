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
import java.util.Map;

/**
 * 兴业银行(IBC)数据服务
 */
public class IndustrialBankServer {

    private static final String SCHEDULE_NAME = "IndustrialBankServer";

    public void start() throws Exception{
        String filePath = new URL(FileUtil.getPrefix("ProcessorData")).getPath() + "IndustrialBankData";
        File newFile = new File(filePath);
        if(!newFile.exists()){
            URL[] urls = getIBUrl();
            String[] allHtml = processGetStrHtml(urls);
            parseIBHtml(allHtml);
        }
        parseIBLocalFile(filePath);
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

    public static void parseIBHtml(String[] allHtml) throws Exception {
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

    public static void parseIBLocalFile(String path) throws Exception {
        RandomAccessFile randomAccessFile_read = new RandomAccessFile(path, "rw");
        String curLine = "";
        BankDataService bankDataService = new BankDataService();
        bankDataService.init();

        while ((curLine = randomAccessFile_read.readLine()) != null) {
            if (StringUtil.isNull(curLine)) {
                continue;
            }

            // 读取重要参数
            String parseCurLine = new String(curLine.getBytes("ISO-8859-1"), "utf-8");

            String[] bankContent = parseCurLine.split(" ");
            String bankName = bankContent[0];
            String address = bankContent[1];
            String telephone = bankContent[2];

            System.out.println(bankName + '\t' + address + '\t' + telephone);

            // 使用地址解析或API查询得到更丰富信息
            String searchAddress = bankName + address;
            Map<String, String> map = gaoDeServer.parseAddress(searchAddress);
            if(map == null || map.size() == 0){
                continue;
            }

            BankData bankData = new BankData();
            bankData.setBankType("兴业银行");
            bankData.setBankName(bankName);
            bankData.setProvince(map.get("province"));
            bankData.setCity(map.get("city"));
            bankData.setArea(map.get("district"));
            bankData.setAddress(address);
            bankData.setTelephone(telephone);
            bankData.setLongitudeX(map.get("longitudeX"));
            bankData.setLatitudeY(map.get("latitudeY"));

            bankDataService.add(bankData);
        }

        return;
    }



}

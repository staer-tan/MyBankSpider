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

public class IndusCommBankOfChinaServer {

    public static final int ALL_PAGES = 335;
    private static final String SCHEDULE_NAME = "IndusCommBankOfChinaServer";

    public static void main(String[] args) throws Exception{
        String filePath = new URL(FileUtil.getPrefix("ProcessorData")).getPath() + "IndusCommBankOfChinaData";
        File newFile = new File(filePath);
        if(!newFile.exists()){
            URL[] urls = getICBCUrl();
            String[] allHtml = processGetStrHtml(urls);
            parseICBCHtml(allHtml);
        }
        parseICBCLocalFile(filePath);
    }

    public static URL[] getICBCUrl() throws Exception {
        URL urls[] = new URL[ALL_PAGES];
        for (int page = 1; page <= ALL_PAGES; page++) {
            urls[page - 1] = new URL("http://www.5cm.cn/bank/102/" + page);
        }

        return urls;
    }

    public static String[] processGetStrHtml(URL[] urls) throws Exception {
        MySpider mySpider = MySpiderFactory.getBankDataSpiderService(urls, SCHEDULE_NAME);
        String[] strHtml = mySpider.startGetStrHtml();
        return strHtml;
    }

    public static void parseICBCHtml(String[] allHtml) throws Exception {
        if (allHtml == null || allHtml.length == 0) {
            throw new Exception("the html is null");
        }

        File destinationCCBFile = FileUtil.createEmptyFile(new URL(FileUtil.getPrefix("ProcessorData")).getPath(), "IndusCommBankOfChinaData");
        RandomAccessFile randomAccessFile_write = new RandomAccessFile(destinationCCBFile, "rw");

        for (String html : allHtml) {
            Document document = Jsoup.parse(html);
            Elements elements = document.select("table[class=table]").select("tr");

            for (Element ele : elements) {
                String content = ele.text();
                if(content.contains("行号")){
                    continue;
                }
                String[] getContents = content.split(" ");
                String bankName = getContents[1];
                String telephone = getContents[2];
                String address = getContents[3];
                String str = bankName + '\t' + telephone + '\t' + address;
                System.out.println(str);
                // 写入本地文件中
                randomAccessFile_write.write(content.getBytes("UTF-8"));
                randomAccessFile_write.write("\n".getBytes("UTF-8"));
                // 后序数据相关操作
            }
        }
    }

    public static void parseICBCLocalFile(String path) throws Exception {
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
            String bankName = bankContent[1];
            String address = bankContent[4];
            String telephone = bankContent[2];

            System.out.println(bankName + '\t' + address + '\t' + telephone);

            // 使用地址解析或API查询得到更丰富信息
            String searchAddress = bankName + address;
            Map<String, String> map = gaoDeServer.parseAddress(searchAddress);
            if(map == null || map.size() == 0){
                continue;
            }

            BankData bankData = new BankData();
            bankData.setBankType("中国工商银行");
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

package BankService;

import DataObject.BankData;
import DataService.mybatis.BankDataService;
import MySpider.Factory.MySpiderFactory;
import MySpider.MySpider;
import Util.AddressService.gaoDeServer;
import Util.FileUtil;
import Util.StringUtil;

import java.io.RandomAccessFile;
import java.net.URL;
import java.util.Map;

/**
 * 中国工商银行（ICBC）数据服务
 */
public class IndusCommBankOfChinaServer {

    private static final String SCHEDULE_NAME = "IndusCommBankOfChinaServer";

    public void start() throws Exception{
        String filePath = new URL(FileUtil.getPrefix("ProcessorData")).getPath() + "IndusCommBankOfChinaData";
        parseICBCLocalFile(filePath);
    }

    public static String[] processGetStrHtml(URL[] urls) throws Exception {
        MySpider mySpider = MySpiderFactory.getBankDataSpiderService(urls, SCHEDULE_NAME);
        String[] strHtml = mySpider.startGetStrHtml();
        return strHtml;
    }

    /**
     * 识别中国工商银行本地文件
     * @param path
     * @throws Exception
     */
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

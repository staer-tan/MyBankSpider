package BankService;

import DataObject.BankData;
import DataService.mybatis.BankDataService;
import MySpider.Factory.MySpiderFactory;
import MySpider.MySpider;
import Util.AddressService.gaoDeServer;
import Util.FileUtil;
import Util.StringUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 招商银行(CMB)数据爬取服务
 */
public class ChinaMerchBankServer {

    private static final String SCHEDULE_NAME = "ChinaMerchBankServer";

    public void start() throws Exception {
        String filePath = new URL(FileUtil.getPrefix("ProcessorData")).getPath() + "ChinaMerchBankData";
        File newFile = new File(filePath);
        if(!newFile.exists()){
            System.out.println("test");
            // 获取cityCode
            URL[] cityCodeUrl = ChinaMerchBankServer.getCBMCityCodeUrl();
            String[] cityCodeHtml = processGetStrHtml(cityCodeUrl);
            String[] cityCode = parseCMBCityCodeHtml(cityCodeHtml);

            // 根据cityCode获取每个城市的所有URL，涉及页面翻转
            URL[] CBMSingleUrl = ChinaMerchBankServer.getCBMUrl(cityCode);
            String[] CBMSingleHtml = processGetStrHtml(CBMSingleUrl);
            URL[] CBMAllUrl = ChinaMerchBankServer.parseCMBSingleHtml(CBMSingleHtml, cityCode);

            // 根据所有每个城市所有的URL，得到对应的输出结果
            String[] CBMAllHtml = processGetStrHtml(CBMAllUrl);
            ChinaMerchBankServer.parseCMBAllHtml(CBMAllHtml);
        }

        // 从本地文件读数据，持久化到DB
        List<BankData> BDList = ChinaMerchBankServer.parseCMBLocalFile(filePath);
        BankDataService bankDataService = new BankDataService();
        bankDataService.init();
        bankDataService.adds(BDList);

    }

    public static String[] processGetStrHtml(URL[] urls) throws Exception {
        MySpider mySpider = MySpiderFactory.getBankDataSpiderNoDataService(urls, SCHEDULE_NAME);
        String[] strHtml = mySpider.startGetStrHtml();
        return strHtml;
    }

    public static URL[] getCBMCityCodeUrl() throws Exception {
        URL[] urls = new URL[1];
        urls[0] = new URL("https://m.cmbchina.com/api/branch/searchinit/?type=branch&lng=&lat=&version=&shake=");
        return urls;
    }

    public static URL[] getCBMUrl(String[] cityCode) throws Exception {
        URL[] urls = new URL[cityCode.length];
        for (int i = 0; i < cityCode.length; i++) {
            urls[i] = new URL("https://m.cmbchina.com/api/branch/getlist/?type=branch&citycode=" + cityCode[i] + "&keywords=&pageNow=1");
        }
        return urls;
    }

    public static String[] parseCMBCityCodeHtml(String[] html) throws Exception {
        if (html.length == 0) {
            throw new Exception("the html is null");
        }
        String cityCodeHtml = html[0];
        JSONObject jsonObject = JSONObject.parseObject(cityCodeHtml);
        JSONObject dataObject = jsonObject.getJSONObject("data");
        JSONArray jsonArrayData = dataObject.getJSONArray("Table");

        List<String> list = new ArrayList<>();
        for (Object object : jsonArrayData) {
            JSONObject jsonObjectone = (JSONObject) object;
            String Code = jsonObjectone.getString("value");
            list.add(Code);
        }

        String[] cityCode = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            cityCode[i] = list.get(i);
        }

        return cityCode;
    }

    /**
     * 根据每个单独每个页面的HTML,解析总页数，获取所有（包含翻转）URL
     * 坑点：CBMSingleHtml和cityCode可能没办法对应的得上
     *
     * @param CBMSingleHtml
     * @return
     */
    public static URL[] parseCMBSingleHtml(String[] CBMSingleHtml, String[] cityCode) throws Exception {
        if (CBMSingleHtml == null || CBMSingleHtml.length == 0) {
            throw new Exception("the html is null");
        }

        if (CBMSingleHtml.length != cityCode.length) {
            throw new Exception("the data is no match!");
        }

        List<String> urlList = new ArrayList<>();
        for (int i = 0; i < CBMSingleHtml.length; i++) {
            JSONObject jsonObject = JSONObject.parseObject(CBMSingleHtml[i]);
            int count = Integer.parseInt(jsonObject.getString("count"));
            int pageCount = count / 4 + ((count % 4 == 0) ? 0 : 1);
            for (int page = 1; page <= pageCount; page++) {
                String CMBUrl = "https://m.cmbchina.com/api/branch/getlist/?type=branch&citycode=" + cityCode[i] + "&keywords=&pageNow=" + page;
                urlList.add(CMBUrl);
            }
        }

        URL[] CMBAllUrl = new URL[urlList.size()];
        System.out.println("CMBAllUrl的大小为" + urlList.size());
        for (int i = 0; i < CMBAllUrl.length; i++) {
            CMBAllUrl[i] = new URL(urlList.get(i));
            System.out.println(urlList.get(i));
        }

        return CMBAllUrl;
    }

    public static void parseCMBAllHtml(String[] CMBAllHtml) throws Exception {

        File destinationCMBFile = FileUtil.createEmptyFile(new URL(FileUtil.getPrefix("ProcessorData")).getPath(), "ChinaMerchBankData");
        RandomAccessFile randomAccessFile_write = new RandomAccessFile(destinationCMBFile, "rw");

        for (int i = 0; i < CMBAllHtml.length; i++) {
            JSONObject jsonObject = JSONObject.parseObject(CMBAllHtml[i]);
            JSONArray dataObject = jsonObject.getJSONArray("data");

            for (Object object : dataObject) {
                JSONObject jsonObjectOne = (JSONObject) object;
                String bankName = jsonObjectOne.getString("sbname");
                String address = jsonObjectOne.getString("sbaddr");
                String telephone = jsonObjectOne.getString("sbtele");

                String content = bankName + '\t' + address + '\t' + telephone;
                randomAccessFile_write.write(content.getBytes("UTF-8"));
                randomAccessFile_write.write("\n".getBytes("UTF-8"));
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
    public static List<BankData> parseCMBLocalFile(String path) throws Exception {
        RandomAccessFile randomAccessFile_read = new RandomAccessFile(path, "rw");
        List<BankData> BDList = new ArrayList<>();
        String curLine = "";
        while ((curLine = randomAccessFile_read.readLine()) != null) {
            if (StringUtil.isNull(curLine)) {
                continue;
            }

            // 读取重要参数
            String parseCurLine = new String(curLine.getBytes("ISO-8859-1"), "utf-8");
            String[] bankContent = parseCurLine.split("\t");
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
            bankData.setBankType("招商银行");
            bankData.setBankName(bankName);
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

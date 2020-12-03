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
 * 中国建设银行(CCB)数据服务
 */
public class ChinaConstructBankServer {

    public static final String SCHEDULE_NAME = "ChinaConstructBankServer";

    // 受到Cookie时间限制，导致整个过程分成两个过程:
    // 1. 从省份 -> 城市 -> 区县，把区县及分页的所有URL保存到文件中
    // 2. 根据保存在文件中的URL，来读取解析
    public void start() throws Exception {
        String filePath = new URL(FileUtil.getPrefix("ProcessorData")).getPath() + "ChinaConstructBankData";
        File newFile = new File(filePath);
        if(!newFile.exists()) {

            /**
             * 1. 步骤1：把所有URL地址放到AllURL中
             */
            // 从省份的URL获取省份码
        URL[] provinceCodeUrl = ChinaConstructBankServer.getCCBProvinceCodeUrl();
        String[] provinceCodeHtml = processGetStrHtml(provinceCodeUrl);
        String[] provinceCode = ChinaConstructBankServer.parseCCBProvinceCodeHtml(provinceCodeHtml);

        // 从省份码获取市级URL，得到市级HTML和市级码
        URL[] cityCodeUrl = ChinaConstructBankServer.getCCBCityCodeUrl(provinceCode);
        String[] cityCodeHtml = processGetStrHtml(cityCodeUrl);
        String[] cityCode = ChinaConstructBankServer.parseCCBCityCodeHtml(cityCodeHtml);

        // // 从城市的URL获取区县码
        URL[] areaCodeUrl = ChinaConstructBankServer.getCCBAreaCodeUrl(cityCode);
        String[] areaCodeHtml = processGetStrHtml(areaCodeUrl);
        String[] areaCode = ChinaConstructBankServer.parseCCBAreaCodeHtml(areaCodeHtml);
        ChinaConstructBankServer.getCCBAllCodeUrl();

            /**
             * 2. 步骤2：从文件解析URL进行爬取
             */
        URL[] allUrlFromFile = ChinaConstructBankServer.getCCBAllCodeUrlFromFile();
        String[] allHtml = processGetStrHtml(allUrlFromFile);
        parseAllUrl(allHtml);
        }

        ChinaConstructBankServer.parseCCBCLocalFile(filePath);
    }

    /**
     * 根据URL链接返回String类型的html
     *
     * @param urls
     * @return
     * @throws Exception
     */
    public static String[] processGetStrHtml(URL[] urls) throws Exception {
        MySpider mySpider = MySpiderFactory.getBankDataSpiderService(urls, SCHEDULE_NAME);
        String[] strHtml = mySpider.startGetStrHtml();
        return strHtml;
    }


    /**
     * 从省份的链接开始，返回URL对象
     *
     * @return 省份的URL
     * @throws Exception
     */
    public static URL[] getCCBProvinceCodeUrl() throws Exception {
        URL[] url = new URL[1];
        url[0] = new URL("http://www.ccb.com/tran/WCCMainPlatV5?CCB_IBSVersion=V5&SERVLET_NAME=WCCMainPlatV5&isAjaxRequest=true&TXCODE=NAREA1&type=1");
        return url;
    }

    /**
     * 根据省份码，获取市级的URL
     *
     * @param provinceCode 身份码
     * @return 城市URL
     * @throws Exception 异常错误
     */
    public static URL[] getCCBCityCodeUrl(String[] provinceCode) throws Exception {
        if (provinceCode == null || provinceCode.length == 0) {
            throw new Exception("provinceCode is null");
        }

        URL[] cityUrl = new URL[provinceCode.length];
        for (int i = 0; i < provinceCode.length; i++) {
            cityUrl[i] = new URL("http://www.ccb.com/tran/WCCMainPlatV5?CCB_IBSVersion=V5&SERVLET_NAME=WCCMainPlatV5&isAjaxRequest=true&TXCODE=NAREA1&type=2&areacode=" + provinceCode[i]);
        }

        return cityUrl;
    }

    /**
     * 根据城市码，获取区县码的URL
     *
     * @param cityCode
     * @return 区县URL
     * @throws Exception
     */
    public static URL[] getCCBAreaCodeUrl(String[] cityCode) throws Exception {
        if (cityCode == null || cityCode.length == 0) {
            throw new Exception("cityCode is null");
        }

        URL[] areaUrl = new URL[cityCode.length];
        for (int i = 0; i < cityCode.length; i++) {
            areaUrl[i] = new URL("http://www.ccb.com/tran/WCCMainPlatV5?CCB_IBSVersion=V5&SERVLET_NAME=WCCMainPlatV5&isAjaxRequest=true&TXCODE=NAREA1&type=3&areacode=" + cityCode[i]);
        }

        return areaUrl;
    }



    /**
     * 根据区县码获取所有的URL地址
     *
     * @return
     * @throws Exception
     */
    public static void getCCBAllCodeUrl() throws Exception {
        RandomAccessFile randomAccessFile_read = new RandomAccessFile(new URL(FileUtil.getPrefix("ProcessorTmp")).getPath() + "ChinaConstructBankAreaCode", "rw");
        // 由于建设银行的cookie时效性问题，直接把最终的URL存到文件中
        File destinationCCBFile = new File(new URL(FileUtil.getPrefix("ProcessorTmp")).getPath() + File.separator + "ChinaConstructBankAllUrl");
        RandomAccessFile randomAccessFile_write = new RandomAccessFile(destinationCCBFile, "rw");
        randomAccessFile_write.seek(randomAccessFile_write.length());
        String areaCode = "";
        while((areaCode = randomAccessFile_read.readLine()) != null) {
            if (StringUtil.isNull(areaCode)) {
                continue;
            }

            URL[] url = new URL[1];
            url[0] = new URL("http://www.ccb.com/tran/WCCMainPlatV5?CCB_IBSVersion=V5&SERVLET_NAME=WCCMainPlatV5&isAjaxRequest=true&TXCODE=NZX010&ADiv_Cd=" + areaCode + "&Kywd_List_Cntnt=&Enqr_MtdCd=4&PAGE=1&Cur_StCd=4");
            String[] totalPageHtml = processGetStrHtml(url);
            String totalPage = parseTotalPage(totalPageHtml);
            if (totalPage == null || totalPage.equals("0")) {
                continue;
            }
            int totalPages = Integer.parseInt(totalPage);
            for (int page = 1; page <= totalPages; page++) {
                String curUrl = "http://www.ccb.com/tran/WCCMainPlatV5?CCB_IBSVersion=V5&SERVLET_NAME=WCCMainPlatV5&isAjaxRequest=true&TXCODE=NZX010&ADiv_Cd=" + areaCode + "&Kywd_List_Cntnt=&Enqr_MtdCd=4&PAGE=" + page + "&Cur_StCd=4";
                randomAccessFile_write.write(curUrl.getBytes("UTF-8"));
                randomAccessFile_write.write("\n".getBytes("UTF-8"));
            }

        }
    }

    /**
     * 从文件中获取所有的URL
     * @return
     * @throws Exception
     */
    public static URL[] getCCBAllCodeUrlFromFile() throws Exception {
        RandomAccessFile randomAccessFile_read = new RandomAccessFile(new URL(FileUtil.getPrefix("ProcessorTmp")).getPath() + "ChinaConstructBankAllUrl8", "rw");
        String curLine = "";
        List<URL> urlList = new ArrayList<>();
        while((curLine = randomAccessFile_read.readLine()) != null){
            if(StringUtil.isNull(curLine)){
                continue;
            }

            urlList.add(new URL(curLine));
        }

        URL[] urls = new URL[urlList.size()];
        for(int i = 0; i < urlList.size(); i++){
            urls[i] = urlList.get(i);
        }

        return urls;
    }


    /**
     * 从省份的HTML中得到省份码
     *
     * @param html
     * @return 省份码
     * @throws Exception
     */
    public static String[] parseCCBProvinceCodeHtml(String[] html) throws Exception {
        if (html == null || html.length == 0) {
            throw new Exception("the CCB province html is null");
        }

        String provinceCodeHtml = html[0];
        List<String> listAreaCode = new ArrayList<>();
        System.out.println(provinceCodeHtml);
        JSONObject jsonObject = JSONObject.parseObject(provinceCodeHtml);
        JSONArray jsonArrayData = jsonObject.getJSONArray("arealist");

        for (Object object : jsonArrayData) {
            JSONObject jsonObjectone = (JSONObject) object;
            String areaCode = jsonObjectone.getString("areacode");
            if (areaCode != null) {
                listAreaCode.add(areaCode);
            }
        }

        String[] provinceCode = new String[listAreaCode.size()];
        for (int i = 0; i < listAreaCode.size(); i++) {
            provinceCode[i] = listAreaCode.get(i);
            System.out.println(provinceCode[i]);
        }

        return provinceCode;
    }

    /**
     * 解析城市的HTML获取城市码
     *
     * @param html
     * @return 城市码
     * @throws Exception
     */

    public static String[] parseCCBCityCodeHtml(String[] html) throws Exception {
        if (html == null || html.length == 0) {
            throw new Exception("the CCB city html is null");
        }

        List<String> listAreaCode = new ArrayList<>();
        for (String cityCodeHtml : html) {
            JSONObject jsonObject = JSONObject.parseObject(cityCodeHtml);
            JSONArray jsonArrayData = jsonObject.getJSONArray("arealist");

            for (Object object : jsonArrayData) {
                JSONObject jsonObjectone = (JSONObject) object;
                String areaCode = jsonObjectone.getString("areacode");
                if (areaCode != null) {
                    listAreaCode.add(areaCode);
                }
            }
        }

        String[] cityCode = new String[listAreaCode.size()];
        for (int i = 0; i < listAreaCode.size(); i++) {
            cityCode[i] = listAreaCode.get(i);
            System.out.println(cityCode[i]);
        }

        System.out.println(cityCode.length);

        return cityCode;
    }

    /**
     * 解析区县的HTML获取区县码
     *
     * @param html
     * @return
     * @throws Exception
     */
    public static String[] parseCCBAreaCodeHtml(String[] html) throws Exception {
        if (html == null || html.length == 0) {
            throw new Exception("the CCB area html is null");
        }

        File destinationCCBFile = FileUtil.createEmptyFile(new URL(FileUtil.getPrefix("ProcessorTmp")).getPath(), "ChinaConstructBankAreaCode");
        RandomAccessFile randomAccessFile_write = new RandomAccessFile(destinationCCBFile, "rw");

        List<String> listAreaCode = new ArrayList<>();
        for (String areaCodeHtml : html) {
            JSONObject jsonObject = JSONObject.parseObject(areaCodeHtml);
            JSONArray jsonArrayData = jsonObject.getJSONArray("arealist");

            for (Object object : jsonArrayData) {
                JSONObject jsonObjectone = (JSONObject) object;
                String areaCode = jsonObjectone.getString("areacode");
                if (areaCode != null) {
                    listAreaCode.add(areaCode);
                }
            }
        }

        String[] areaCode = new String[listAreaCode.size()];
        for (int i = 0; i < listAreaCode.size(); i++) {
            areaCode[i] = listAreaCode.get(i);
            System.out.println(areaCode[i]);
            randomAccessFile_write.write(areaCode[i].getBytes("UTF-8"));
            randomAccessFile_write.write("\n".getBytes("UTF-8"));
        }
        System.out.println(listAreaCode.size());

        return areaCode;
    }

    /**
     * 解析初始页码地址，返回所有页码
     *
     * @param html
     * @return
     * @throws Exception
     */
    public static String parseTotalPage(String[] html) throws Exception {
        if (html == null || html.length == 0) {
            throw new Exception("the CCB page compute html is null");
        }

        if (html.length > 1) {
            throw new Exception("the CCB page number is error");
        }

        String pageHtml = html[0];
        String totalPage = "";
        try {
            JSONObject jsonObject = JSONObject.parseObject(pageHtml);
            totalPage = jsonObject.getString("TOTAL_PAGE");
        } catch (Exception e) {
            System.out.println("the ccb json format is error");
            return "0";
        }

        return totalPage;
    }

    /**
     * 解析所有的URL并获取其中的关键字段
     *
     * @param html
     * @throws Exception
     */
    public static void parseAllUrl(String[] html) throws Exception {
        if (html == null || html.length == 0) {
            throw new Exception("the CCB all url html is null");
        }

        File destinationCCBFile = new File(new URL(FileUtil.getPrefix("ProcessorData")).getPath() + File.separator + "ChinaConstructBankData");
        RandomAccessFile randomAccessFile_write = new RandomAccessFile(destinationCCBFile, "rw");
        randomAccessFile_write.seek(randomAccessFile_write.length());

        for (String areaCodeHtml : html) {
            JSONObject jsonObject = JSONObject.parseObject(areaCodeHtml);
            JSONArray jsonArrayData = jsonObject.getJSONArray("OUTLET_DTL_LIST");

            for (Object object : jsonArrayData) {
                JSONObject jsonObjectOne = (JSONObject) object;
                if (jsonObjectOne != null) {
                    String bankName = jsonObjectOne.getString("CCBIns_Nm");
                    String address = jsonObjectOne.getString("Dtl_Adr");
                    String telephone = jsonObjectOne.getString("Fix_TelNo");
                    String longitudeX = jsonObjectOne.getString("Lgt");
                    String latitudeY = jsonObjectOne.getString("Ltt");
                    if (bankName == null) {
                        continue;
                    }
                    String content = bankName + '\t' + address + '\t' + telephone + '\t' + longitudeX + '\t' + latitudeY;
                    randomAccessFile_write.write(content.getBytes("UTF-8"));
                    randomAccessFile_write.write("\n".getBytes("UTF-8"));
                }
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
    public static void parseCCBCLocalFile(String path) throws Exception {
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

            String[] bankContent = parseCurLine.split("\t");
            String bankName = bankContent[0];
            String address = bankContent[1];
            String telephone = bankContent[2];
            String longitudeX = String.valueOf((float)Math.round(Float.parseFloat(bankContent[3])*1000)/1000);
            String latitudeY = String.valueOf((float)Math.round(Float.parseFloat(bankContent[4])*1000)/1000);

            String content = bankName + '\t' + address + '\t' + telephone + '\t' + longitudeX + '\t' + latitudeY;
            System.out.println(content);
            // 使用地址解析或API查询得到更丰富信息
            String searchAddress = bankName + address;
            Map<String, String> map = gaoDeServer.parseAddress(searchAddress);
            if(map == null || map.size() == 0){
                continue;
            }

            BankData bankData = new BankData();
            bankData.setBankType("中国建设银行");
            bankData.setBankName(bankName);
            bankData.setProvince(map.get("province"));
            bankData.setCity(map.get("city"));
            bankData.setArea(map.get("district"));
            bankData.setAddress(address);
            bankData.setTelephone(telephone);
            bankData.setLongitudeX(longitudeX);
            bankData.setLatitudeY(latitudeY);

            bankDataService.add(bankData);
        }

        return;
    }

}

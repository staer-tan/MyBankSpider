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
 * 中国农业银行(ABC)数据爬取服务
 */
public class AgricultureBankOfChinaServer {

    public static final String SCHEDULE_NAME = "AgricultureBankOfChinaServer";

    /**
     * 农行数据调用入口
     * @throws Exception
     */
    public void start() throws Exception{
        String filePath = new URL(FileUtil.getPrefix("ProcessorData")).getPath() + "AgricultureBankOfChinaData";
        File newFile = new File(filePath);
        if(!newFile.exists()){
            // 从省份的URL获取省份码
            URL[] provinceCodeUrl = getABCProvinceCodeUrl();
            String[] provinceCodeHtml = processGetStrHtml(provinceCodeUrl);
            String[] provinceCode = parseABCProvinceCodeHtml(provinceCodeHtml);

            // 从省份的URL获取城市码
            URL[] cityCodeUrl = getABCCityCodeUrl(provinceCode);
            String[] cityCodeHtml = processGetStrHtml(cityCodeUrl);
            String[] cityCode = parseABCCityCodeHtml(cityCodeHtml);

            // 从城市的URL获取区县码
            URL[] areaCodeUrl = getABCAreaCodeUrl(cityCode);
            String[] areaCodeHtml = processGetStrHtml(areaCodeUrl);
            String[] areaCode = parseABCAreaCodeHtml(areaCodeHtml);

            // 根据最底层区县码（获得所有URL）
            URL[] allCodeUrl = getABCAllCodeUrl(areaCode);
            String[] allCodeHtml = processGetStrHtml(allCodeUrl);

            // 根据HTML进行数据解析
            parseABCAllHtml(allCodeHtml);
        }
        // 由于农业银行数据众多，直接逐个插入
       AgricultureBankOfChinaServer.parseABCLocalFile(filePath);


    }

    /**
     * 根据URL链接返回String类型的html,可使用多线程版本替换
     * @param urls
     * @return
     * @throws Exception
     */
    public static String[] processGetStrHtml(URL[] urls) throws Exception{
        MySpider mySpider =  MySpiderFactory.getBankDataSpiderService(urls, SCHEDULE_NAME);
        String[] strHtml = mySpider.startGetStrHtml();
        return strHtml;
    }

    /**
     * 得到省份URL
     * @return
     * @throws Exception
     */
    public static URL[] getABCProvinceCodeUrl() throws Exception{
        URL[] url = new URL[1];
        url[0] = new URL("http://app.abchina.com/branch/common/BranchService.svc/District");
        return url;
    }

    /**
     * 根据省份码得到城市的URL
     * @param provinceCode
     * @return 城市URL
     * @throws Exception
     */
    public static URL[] getABCCityCodeUrl(String[] provinceCode) throws Exception{
        if(provinceCode == null || provinceCode.length == 0){
            throw new Exception("provinceCode is null");
        }

        URL[] cityUrl = new URL[provinceCode.length];
        for(int i = 0; i < provinceCode.length; i++){
            cityUrl[i] = new URL("http://app.abchina.com/branch/common/BranchService.svc/District/" + provinceCode[i]);
        }

        return cityUrl;
    }

    /**
     * 根据城市码得到区县的URL
     * @param cityCode
     * @return 区县URL
     * @throws Exception
     */
    public static URL[] getABCAreaCodeUrl(String[] cityCode) throws Exception{
        if(cityCode == null || cityCode.length == 0){
            throw new Exception("cityCode is null");
        }

        URL[] areaUrl = new URL[cityCode.length];
        for(int i = 0; i < cityCode.length; i++){
            areaUrl[i] = new URL("http://app.abchina.com/branch/common/BranchService.svc/District/Any/" + cityCode[i]);
        }

        return areaUrl;
    }

    /**
     * 根据区县码获取所有数据
     * @param areaCode
     * @return
     * @throws Exception
     */
    public static URL[] getABCAllCodeUrl(String[] areaCode) throws Exception{
        if(areaCode == null || areaCode.length == 0){
            throw new Exception("areaCode is null");
        }

        URL[] allUrl = new URL[areaCode.length];
        for(int i = 0; i < areaCode.length; i++){

            String province = areaCode[i].substring(0, 2) + "0000";
            String city = areaCode[i].substring(0, 4) + "00";
            allUrl[i] = new URL("http://app.abchina.com/branch/common/BranchService.svc/Branch?p=" + province +
                    "&c=" + city + "&b=" + areaCode[i] + "&q=&t=1&z=0&i=0");
        }

        return allUrl;
    }

    /**
     * 解析省份的html获取省份编码
     * @param html
     * @return
     * @throws Exception
     */
    public static String[] parseABCProvinceCodeHtml(String[] html) throws Exception{
        if(html == null || html.length == 0){
            throw new Exception("the CCB province html is null");
        }

        String provinceCodeHtml = html[0];
        List<String> listAreaCode = new ArrayList<>();
        System.out.println(provinceCodeHtml);
        JSONArray jsonArrayData = JSONObject.parseArray(provinceCodeHtml);

        for(Object object : jsonArrayData){
            JSONObject jsonObjectone = (JSONObject) object;
            String areaCode = jsonObjectone.getString("Id");
            if(areaCode != null){
                listAreaCode.add(areaCode);
            }
        }

        String[] provinceCode = new String[listAreaCode.size()];
        for(int i = 0; i < listAreaCode.size(); i++){
            provinceCode[i] = listAreaCode.get(i);
            System.out.println(provinceCode[i]);
        }

        return provinceCode;
    }

    /**
     * 解析城市的HTML获取城市码
     * @param html
     * @return
     * @throws Exception
     */
    public static String[] parseABCCityCodeHtml(String[] html) throws Exception{
        if(html == null || html.length == 0){
            throw new Exception("the ABC city html is null");
        }

        List<String> listCityCode = new ArrayList<>();
        for(String cityCodeHtml : html){
            JSONArray jsonArrayData = JSONObject.parseArray(cityCodeHtml);
            for(Object object : jsonArrayData){
                JSONObject jsonObjectone = (JSONObject) object;
                String areaCode = jsonObjectone.getString("Id");
                if(areaCode != null){
                    listCityCode.add(areaCode);
                }
            }
        }

        String[] cityCode = new String[listCityCode.size()];
        for(int i = 0; i < listCityCode.size(); i++){
            cityCode[i] = listCityCode.get(i);
            System.out.println(cityCode[i]);
        }

        System.out.println(listCityCode.size());
        return cityCode;
    }

    /**
     * 解析区县的HTML获取区县码
     * @param html
     * @return
     * @throws Exception
     */
    public static String[] parseABCAreaCodeHtml(String[] html) throws Exception{
        if(html == null || html.length == 0){
            throw new Exception("the ABC area html is null");
        }

        List<String> listAreaCode = new ArrayList<>();
        for(String areaCodeHtml : html){
            JSONArray jsonArrayData = JSONObject.parseArray(areaCodeHtml);
            for(Object object : jsonArrayData){
                JSONObject jsonObjectone = (JSONObject) object;
                String areaCode = jsonObjectone.getString("Id");
                if(areaCode != null){
                    listAreaCode.add(areaCode);
                }
            }
        }

        String[] areaCode = new String[listAreaCode.size()];
        for(int i = 0; i < listAreaCode.size(); i++){
            areaCode[i] = listAreaCode.get(i);
            System.out.println(areaCode[i]);
        }
        System.out.println(listAreaCode.size());

        return areaCode;
    }

    /**
     * 解析所有网点的HTML（后序可持久化）
     * @param allHtml
     * @throws Exception
     */
    public static void parseABCAllHtml(String[] allHtml) throws Exception{
        if(allHtml == null || allHtml.length == 0){
            throw new Exception("the ABC allhtml is null");
        }

        File destinationCCBFile = FileUtil.createEmptyFile(new URL(FileUtil.getPrefix("ProcessorData")).getPath(), "AgricultureBankOfChinaData");
        RandomAccessFile randomAccessFile_write = new RandomAccessFile(destinationCCBFile, "rw");

        for(String html : allHtml){
            JSONObject jsonObjectData = JSONObject.parseObject(html);
            JSONArray jsonArrayData = jsonObjectData.getJSONArray("BranchSearchRests");
            for(Object object : jsonArrayData){
                JSONObject jsonObjectOne = (JSONObject) object;
                JSONObject branchBank = jsonObjectOne.getJSONObject("BranchBank");
                String bankName = branchBank.getString("Name");
                String bankLevel = branchBank.getString("BranchLevel");
                String province = branchBank.getString("Province");
                String city = branchBank.getString("City");
                String address = branchBank.getString("Address");
                String telephone = branchBank.getString("PhoneNumber");
                String parentBank = branchBank.getString("SuperiorLevel1") + "-" + branchBank.getString("SuperiorLevel2");
                String longitudeX = branchBank.getString("Longitude");
                String latitudeY = branchBank.getString("Latitude");
                String content = bankName + '\t' + bankLevel + '\t' + province + '\t' + city + '\t' + address + '\t' +
                        telephone + '\t' + parentBank + '\t' + longitudeX + '\t' + latitudeY;
                System.out.println(content);
                // 写入本地文件中
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
    public static void parseABCLocalFile(String path) throws Exception {
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
            String bankLevel = bankContent[1];
            String address = bankContent[4];
            String telephone = bankContent[5];
            String parentBank = bankContent[6];
            String longitudeX = bankContent[7];
            String latitudeY = bankContent[8];

            // 使用地址解析或API查询得到更丰富信息
            String searchAddress = bankName + address;
            Map<String, String> map = gaoDeServer.parseAddress(searchAddress);
            if(map == null || map.size() == 0){
                continue;
            }

            BankData bankData = new BankData();
            bankData.setBankType("中国农业银行");
            bankData.setBankName(bankName);
            bankData.setBankLevel(bankLevel);
            bankData.setProvince(map.get("province"));
            bankData.setCity(map.get("city"));
            bankData.setArea(map.get("district"));
            bankData.setAddress(address);
            bankData.setTelephone(telephone);
            bankData.setParentBank(parentBank);
            bankData.setLongitudeX(longitudeX);
            bankData.setLatitudeY(latitudeY);

            bankDataService.add(bankData);
        }


    }
}

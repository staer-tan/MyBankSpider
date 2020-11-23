package BankService;

import MySpider.Factory.MySpiderFactory;
import MySpider.MySpider;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 中国农业银行(ABC)数据爬取服务
 */
public class AgricultureBankOfChinaServer {

    public static final String SCHEDULE_NAME = "AgricultureBankOfChinaServer";

    public void start() throws Exception{
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

        for(String html : allHtml){
            JSONObject jsonObjectData = JSONObject.parseObject(html);
            JSONArray jsonArrayData = jsonObjectData.getJSONArray("BranchSearchRests");
            for(Object object : jsonArrayData){
                JSONObject jsonObjectOne = (JSONObject) object;
                JSONObject branchBank = jsonObjectOne.getJSONObject("BranchBank");
                String address = branchBank.getString("Address");
                String bankName = branchBank.getString("Name");
                String longitudeX = branchBank.getString("Longitude");
                String latitudeY = branchBank.getString("Latitude");
                String telephone = branchBank.getString("PhoneNumber");
                String city =  branchBank.getString("City");
                String bankLevel = branchBank.getString("BranchLevel");
                String parentBank = branchBank.getString("SuperiorLevel1") + branchBank.getString("SuperiorLevelBranch") + branchBank.getString("SuperiorLevel2");
                System.out.println(bankName + '\t' + bankLevel + '\t' + city + '\t' + address + '\t' + longitudeX + '\t' + latitudeY + '\t' +
                        telephone + '\t' + parentBank);
            }
        }
    }
}

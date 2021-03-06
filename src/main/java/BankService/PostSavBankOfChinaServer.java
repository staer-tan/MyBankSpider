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
 * 邮政储蓄银行(PSBC)数据爬取服务
 */

public class PostSavBankOfChinaServer {

    public static final String SCHEDULE_NAME = "PostSavBankOfChinaServer";

    public void start() throws Exception {
        String filePath = new URL(FileUtil.getPrefix("ProcessorData")).getPath() + "PostSavBankOfChinaData";
        File newFile = new File(filePath);
        if(!newFile.exists()){
            // 从省份开始得到所有省份的URL和HTML
            URL[] provinceUrl = PostSavBankOfChinaServer.getPSBCProvinceUrl();
            String[] provinceStrHtml = processGetStrHtml(provinceUrl);
            System.out.println("省份数目：" + provinceStrHtml.length);

            // 从省份的HTML得到城市URL和HTML
            URL[] cityUrl = PostSavBankOfChinaServer.getPSBCCityUrl(provinceStrHtml);
            String[] cityStrHtml = processGetStrHtml(cityUrl);
            System.out.println("城市数目：" + cityStrHtml.length);

            // 从城市的HTML得到各区县的URL和HTML
            URL[] areaUrl = PostSavBankOfChinaServer.getPSBCAreaUrl(cityStrHtml);
            String[] areaStrHtml = processGetStrHtml(areaUrl);
            System.out.println("区县数目：" + areaStrHtml.length);

            // 打印/处理该Html中相关内容
            parsePSBSAllUrl(areaStrHtml);
        }

        PostSavBankOfChinaServer.parsePSBCLocalFile(filePath);


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


    /***
     * 北京：11    天津：12   河北：13   山西：14   内蒙古：15
     * 辽宁：21    吉林：22   黑龙江：23
     * 上海：31    江苏：32   浙江：33   安徽：34   福建：35   江西：36   山东：37
     * 河南：41    湖北：42   湖南：43   广东：44   广西：45   海南：46
     * 重庆：50    四川：51   贵州：52   云南：53   西藏：54
     * 陕西：61    甘肃：62   青海：63   宁夏：64   新疆：65
     */
    public static int[] provinceParam = new int[]
            {11, 12, 13, 14, 15,
                    21, 22, 23,
                    31, 32, 33, 34, 35, 36, 37,
                    41, 42, 43, 44, 45, 46,
                    50, 51, 52, 53, 54, 55,
                    61, 62, 63, 64, 65};

    /**
     * 根据省份的初始编码得到省份URL
     *
     * @return
     * @throws Exception
     */
    public static URL[] getPSBCProvinceUrl() throws Exception {
        URL[] provinceUrl = new URL[provinceParam.length];
        for (int i = 0; i < provinceParam.length; i++) {
            String proUrl = "http://www.psbc.com/cms/CityQuery.do?Param=" + provinceParam[i];
            provinceUrl[i] = new URL(proUrl);
            System.out.println(proUrl);
        }

        return provinceUrl;
    }

    /**
     * 从省份的HTML得到城市的URL
     *
     * @param provinceStrHtml
     * @return 城市URL
     * @throws Exception
     */
    public static URL[] getPSBCCityUrl(String[] provinceStrHtml) throws Exception {
        List<String> cityParams = new ArrayList<>();

        for (String proHtml : provinceStrHtml) {
            Document document = Jsoup.parse(proHtml);
            Elements elements = document.select("div[class=lineblock]").select("a");
            for (Element element : elements) {
                String onClickAttr = element.attr("onClick");
                String cityParam = onClickAttr.substring(onClickAttr.indexOf("('") + 2, onClickAttr.indexOf("')"));
                cityParams.add(cityParam);
            }
        }

        URL[] cityUrl = new URL[cityParams.size()];
        for (int i = 0; i < cityParams.size(); i++) {
            String ciUrl = "http://www.psbc.com/cms/XianQuery.do?Param=" + cityParams.get(i);
            cityUrl[i] = new URL(ciUrl);
            System.out.println(ciUrl);
        }

        return cityUrl;
    }

    /**
     * 根据城市的HTML得到区县URL地址
     *
     * @param cityHtml
     * @return 区县URL地址
     * @throws Exception
     */
    public static URL[] getPSBCAreaUrl(String[] cityHtml) throws Exception {
        List<String> areaParams = new ArrayList<>();

        for (String ciHtml : cityHtml) {
            Document document = Jsoup.parse(ciHtml);
            Elements elements = document.select("div[class=lineblock]").select("a");
            for (Element element : elements) {
                String onClickAttr = element.attr("onClick");
                String cityParam = onClickAttr.substring(onClickAttr.indexOf("('") + 2, onClickAttr.indexOf("')"));
                areaParams.add(cityParam);
            }
        }

        URL[] areaUrl = new URL[areaParams.size()];
        for (int i = 0; i < areaParams.size(); i++) {
            String ciUrl = "http://www.psbc.com/cms/WangdianQuery.do?Param=" + areaParams.get(i);
            areaUrl[i] = new URL(ciUrl);
        }

        return areaUrl;
    }

    /**
     * 解析所有的网点的URL信息
     *
     * @param areaStrHtml 区县URL
     */
    public static void parsePSBSAllUrl(String[] areaStrHtml) {
        for (String html : areaStrHtml) {
            Document document = Jsoup.parse(html);
            Elements elements = document.select("div[class=area_bank_list]").select("tr");

            for (Element element : elements) {
                String title = element.text();
                System.out.println(title);
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
    public static void parsePSBCLocalFile(String path) throws Exception {
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
            if(parseCurLine.contains("网点名称")){
                continue;
            }

            String[] bankContent = parseCurLine.split(" ");
            String bankName = bankContent[0];
            String address = bankContent[1];
            String telephone = bankContent[bankContent.length - 1];


            System.out.println(bankName + '\t' + address + '\t' + telephone);

            // 使用地址解析或API查询得到更丰富信息
            String searchAddress = bankName + address;
            Map<String, String> map = gaoDeServer.parseAddress(searchAddress);
            if(map == null || map.size() == 0){
                continue;
            }

            BankData bankData = new BankData();
            bankData.setBankType("邮政储蓄银行");
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

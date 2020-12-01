package BankService;

import DataObject.BankData;
import DataService.mybatis.BankDataService;
import MySpider.Factory.MySpiderFactory;
import MySpider.MySpider;
import Util.AddressService.gaoDeServer;
import Util.FileUtil;
import Util.StringUtil;
import org.dom4j.*;
import org.dom4j.io.*;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ChinaCiticBankServer {

    private static final String SCHEDULE_NAME = "ChinaCiticBankServer";

    public void start() throws Exception{
        String filePath = new URL(FileUtil.getPrefix("ProcessorData")).getPath() + "ChinaCiticBankData";
        File newFile = new File(filePath);
        if(!newFile.exists()){
            URL[] urls = getIBUrl();
            String[] allHtml = processGetStrHtml(urls);
            parseBOCHtml();
        }

        parseCCBLocalFile(filePath);
    }

    public static URL[] getIBUrl() throws Exception {
        URL urls[] = new URL[1];
        urls[0] = new URL("http://www.ecitic.com/xml/info/yinhang.xml");
        return urls;
    }

    public static String[] processGetStrHtml(URL[] urls) throws Exception {
        MySpider mySpider = MySpiderFactory.getBankDataSpiderService(urls, SCHEDULE_NAME);
        String[] strHtml = mySpider.startGetStrHtml();
        return strHtml;
    }

    public static void parseBOCHtml() throws Exception {

        File destinationCCBFile = FileUtil.createEmptyFile(new URL(FileUtil.getPrefix("ProcessorData")).getPath(), "IndustrialBankData");
        RandomAccessFile randomAccessFile_write = new RandomAccessFile(destinationCCBFile, "rw");

        SAXReader reader = new SAXReader();
        Document document = null;
        try {
            document = reader.read(new File("D:/java1/ecitic.xml"));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        Element rootElement = document.getRootElement();
        List allMeidaElements = rootElement.elements("info");

        for (int i = 0; i < allMeidaElements.size(); i++) {
            Element element = (Element) allMeidaElements.get(i);
            String name = element.attributeValue("lng");
            System.out.println(name);
        }

    }

    public static void parseCCBLocalFile(String path) throws Exception {
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
            String telephone = "95558";

            System.out.println(bankName + '\t' + address + '\t' + telephone);

            // 使用地址解析或API查询得到更丰富信息
            String searchAddress = bankName + address;
            Map<String, String> map = gaoDeServer.parseAddress(searchAddress);
            if(map == null || map.size() == 0){
                continue;
            }

            BankData bankData = new BankData();
            bankData.setBankType("中信银行");
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

package BankService;

import MySpider.Factory.MySpiderFactory;
import MySpider.MySpider;
import Util.FileUtil;
import org.dom4j.*;
import org.dom4j.io.*;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

public class ChinaCiticBank {

    private static final String SCHEDULE_NAME = "ChinaCiticBankServer";

    public static void main(String[] args) throws Exception {
//        URL[] urls = getIBUrl();
//        String[] allHtml = processGetStrHtml(urls);
        parseBOCHtml();
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


}

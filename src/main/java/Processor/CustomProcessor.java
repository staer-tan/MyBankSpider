package Processor;

import MySpider.Factory.MySpiderFactory;
import MySpider.MySpider;
import Util.StringUtil;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CustomProcessor extends AbstractProcessor{

    public CustomProcessor(String name){
        this.name = name;
    }

    /**
     * 根据URL链接返回String类型的html(后续统一使用）
     * @param urls 传入的URL数组
     * @param ScheduleName 任务名
     * @return 字符串类型的HTML
     * @throws Exception
     */

    public static String[] processGetStrHtml(URL[] urls, String ScheduleName) throws Exception{
        MySpider mySpider =  MySpiderFactory.getBankDataSpiderService(urls, ScheduleName);
        String[] strHtml = mySpider.startGetStrHtml();
        return strHtml;
    }

    @Override
    public String parseToBankFile(File downloadFile) throws Exception {
        if (downloadFile == null) {
            throw new Exception("parse file is null");
        }

        String html = "";
        String curLine = "";
        try (RandomAccessFile randomAccessFile_read = new RandomAccessFile(downloadFile, "rw");) {
            while ((curLine = randomAccessFile_read.readLine()) != null) {
                if (StringUtil.isNull(curLine)) {
                    continue;
                }
                html += new String(curLine.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            }

        }
        return html;
    }
}

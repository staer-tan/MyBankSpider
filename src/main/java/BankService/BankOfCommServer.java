package BankService;

import DataObject.BankData;
import DataService.mybatis.BankDataService;
import MySpider.Factory.MySpiderFactory;
import MySpider.MySpider;
import Util.FileUtil;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 交通银行(COMM)数据爬取服务
 */
public class BankOfCommServer {

    private static final String SCHEDULE_NAME = "BankOfCommServer";

    public void start() throws Exception {
        URL[] urls = BankOfCommServer.getBCMUrl();
        String[] allHtml = processGetStrHtml(urls);
        List<BankData> list = BankOfCommServer.parseBCMHtml(allHtml);

        // 同步数据至数据库中
        BankDataService bankDataService = new BankDataService();
        bankDataService.init();
        bankDataService.adds(list);
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
     * 获取交通银行所有URL
     * @return
     * @throws Exception
     */
    public static URL[] getBCMUrl() throws Exception {
        URL[] urls = new URL[1];
        urls[0] = new URL("http://bankcomm.com/BankCommSite/zonghang/cn/node/queryBranchResult.do");
        return urls;
    }

    /**
     * 解析交通银行所有html
     * @param allHtml
     * @throws Exception
     */
    public static List<BankData> parseBCMHtml(String[] allHtml) throws Exception{
        if(allHtml == null || allHtml.length == 0){
            throw new Exception("the html is null");
        }

        File destinationCCBFile = FileUtil.createEmptyFile(new URL(FileUtil.getPrefix("ProcessorData")).getPath(), "BankOfCommData");
        RandomAccessFile randomAccessFile_write = new RandomAccessFile(destinationCCBFile, "rw");
        List<BankData> list = new ArrayList<>();
        for(String html : allHtml){
            JSONObject jsonObject = JSONObject.parseObject(html);

            int count = jsonObject.getInteger("count");
            JSONObject jsonObjectData = jsonObject.getJSONObject("data");
            for(int num = 0; num < count; num++){
                String pNum = "p" + num;
                JSONObject jsonObjectPData = jsonObjectData.getJSONObject(pNum);
                String bankName = jsonObjectPData.getString("n");
                String address = URLDecoder.decode(jsonObjectPData.getString("a"), "UTF-8") + "(" + jsonObjectPData.getString("c") + ")";
                String longitudeX = String.valueOf((float)Math.round(jsonObjectPData.getFloat("x")*1000)/1000);
                String latitudeY = String.valueOf((float)Math.round(jsonObjectPData.getFloat("y")*1000)/1000);
                String telephone = URLDecoder.decode(jsonObjectPData.getString("p"), "UTF-8");

                String content = num + "\t" + "交通银行" + bankName + "\t" + address + "\t" + longitudeX + "\t" + latitudeY + "\t" + telephone;

                // 持久化至本地文件
                randomAccessFile_write.write(content.getBytes("UTF-8"));
                randomAccessFile_write.write("\n".getBytes("UTF-8"));

                BankData bankData = new BankData();
                bankData.setBankType("交通银行");
                bankData.setBankName(bankName);
                bankData.setAddress(address);
                bankData.setLongitudeX(longitudeX);
                bankData.setLatitudeY(latitudeY);
                bankData.setTelephone(telephone);

                // 添加到list当中用于后续持久化
                list.add(bankData);

            }
        }
        return list;
    }
}

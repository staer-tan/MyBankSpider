package BankService;

import MySpider.Factory.MySpiderFactory;
import MySpider.MySpider;
import com.alibaba.fastjson.JSONObject;

import java.net.URL;
import java.net.URLDecoder;

/**
 * 交通银行(COMM)数据爬取服务
 */
public class BankOfCommServer {

    private static final String SCHEDULE_NAME = "BankOfCommServer";

    public void start() throws Exception {
        URL[] urls = BankOfCommServer.getBCMUrl();
        String[] allHtml = processGetStrHtml(urls);
        BankOfCommServer.parseBCMHtml(allHtml);
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
    public static void parseBCMHtml(String[] allHtml) throws Exception{
        if(allHtml == null || allHtml.length == 0){
            throw new Exception("the html is null");
        }

        for(String html : allHtml){
            JSONObject jsonObject = JSONObject.parseObject(html);

            int count = jsonObject.getInteger("count");
            JSONObject jsonObjectData = jsonObject.getJSONObject("data");
            for(int num = 0; num < count; num++){
                String pNum = "p" + num;
                JSONObject jsonObjectPData = jsonObjectData.getJSONObject(pNum);
                String bankName = jsonObjectPData.getString("n");
                String address = URLDecoder.decode(jsonObjectPData.getString("a"), "UTF-8") + "(" + jsonObjectPData.getString("c") + ")";
                String longitudeX = jsonObjectPData.getString("x");
                String latitudeY = jsonObjectPData.getString("y");
                String telephone = URLDecoder.decode(jsonObjectPData.getString("p"), "UTF-8");
                System.out.println(num + "\t" + bankName + "\t" + address + "\t" + longitudeX + "\t" + latitudeY + "\t" + telephone);
            }
        }

    }
}

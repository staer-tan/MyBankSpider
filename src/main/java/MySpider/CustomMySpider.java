package MySpider;

import MySpider.Factory.MySpiderFactory;

import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 通用的爬虫入口，用于开启多线程爬虫
 */
public class CustomMySpider {
    private static ExecutorService executorService = Executors.newFixedThreadPool(10);
    public static boolean useThreads = false;

    public static String[] processGetStrHtml(URL[] urls, String schedule_name) throws Exception {
        String[] strHtml = null;
        //单线程启动
        if(!useThreads){
            //从工厂类中获得一个爬虫实例
            MySpider mySpider =  MySpiderFactory.getBankDataSpiderService(urls, schedule_name);
            strHtml = mySpider.startGetStrHtml();
        }
        //多线程启动
        else{
            for (URL url:
                    urls) {
                executorService.submit(() -> {
                    try {
                        MySpider mySpider =  MySpiderFactory.getBankDataSpiderServiceForTheads(url, schedule_name);
                        mySpider.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            executorService.shutdown();
        }

        return strHtml;
    }
}

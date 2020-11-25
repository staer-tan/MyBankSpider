package MySpider;

import BootStrap.BootStrap;
import BootStrap.CustomBootStrap;
import Downloader.Downloader;
import Downloader.StreamDownloader;
import Processor.Processor;
import ScheduleQueue.ScheduleQueue;
import ScheduleQueue.CustomScheduleQueue;


import java.io.File;
import java.net.URL;

/**
 * MySpider实体类
 */
public class MySpider {
    private BootStrap bootStrap;
    private ScheduleQueue scheduleQueue;
    private Downloader downloader;
    private Processor processor;

    private static URL[] urls;

    public MySpider(URL[] urls) {
        this.urls = urls;
    }

    /**
     * 初始化类
     * @throws Exception
     */
    private void init() throws Exception {
        // 引导类初始化
        if(bootStrap == null){
            bootStrap = new CustomBootStrap();
        }
        bootStrap.boot();

        // 任务队列初始化
        if(scheduleQueue == null){
            scheduleQueue = new CustomScheduleQueue();
        }

        // 将URL放入队列中
        for(URL url : urls){
            scheduleQueue.addNewURL(url);
        }

        // 初始化下载器
        if(downloader == null){
            downloader = new StreamDownloader();
        }

        if(processor == null){
            throw new Exception("please add your own processor");
        }

    }

    public void start() throws Exception{
        System.out.println("Main file start work!");
        init();

        while(scheduleQueue.size() > 0) {
            downloader.reset(scheduleQueue.nextURL());
            File downloadFile = downloader.run();
            // File tempFile = processor.parseToFile(downloadFile);
            // String html = processor.parseToBankFile(downloadFile);
            // System.out.println(html);
            // 1. 中国银行(string html)
            // BankOfChinaServer.parseBOCHtml(html);
            // 2. 交通银行(string html)
            // BankOfCommServer.parseBCMHtml(html);
            if(scheduleQueue.size() >= 1){
                // 线程间隔时间
                Thread.sleep(100*60);
            }
        }
    }

    public String[] startGetStrHtml() throws Exception{
        System.out.println("Main file start work!");
        init();
        String[] strHtml = new String[scheduleQueue.size()];
        System.out.println("scheduleQueue的大小为" + scheduleQueue.size());
        int temp = 0;
        while(scheduleQueue.size() > 0){
            downloader.reset(scheduleQueue.nextURL());
            File downloadFile = downloader.run();
            System.out.println(downloadFile);
            strHtml[temp++] = processor.parseToBankFile(downloadFile);
            System.out.println(strHtml[temp - 1]);

            Thread.sleep(50);
            if(temp % 5 == 0){
                System.out.println(temp);
            }
        }

        System.out.println("current url finished");
        return strHtml;
    }

    public MySpider addBoot(BootStrap boot){
        this.bootStrap = boot;
        return this;
    }

    public MySpider addScheduleQueue(ScheduleQueue scheduleQueue){
        this.scheduleQueue = scheduleQueue;
        return this;
    }

    public MySpider addDownloader(Downloader downloader){
        this.downloader = downloader;
        return this;
    }

    public MySpider addProcessor(Processor processor) throws Exception {
        this.processor = processor;
        return this;
    }

}

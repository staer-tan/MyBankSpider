package MySpider.Factory;

import BootStrap.CustomBootStrap;
import Downloader.StreamDownloader;
import MySpider.MySpider;
import Processor.CustomProcessor;
import ScheduleQueue.CustomScheduleQueue;

import java.net.URL;

/**
 * MySpider的工厂类，生产特定种类的爬虫模式
 */

public class MySpiderFactory {

    /**
     * 返回一个银行任务的爬虫实例(单线程）
     *
     * @param urls         URL
     * @param ScheduleName 任务名称
     * @return 爬虫实例
     * @throws Exception
     */
    public static MySpider getBankDataSpiderService(URL[] urls, String ScheduleName) throws Exception {
        MySpider mySpider = new MySpider(urls)
                .addBoot(new CustomBootStrap())
                .addDownloader(new StreamDownloader())
                .addProcessor(new CustomProcessor(ScheduleName))
                .addScheduleQueue(new CustomScheduleQueue());

        return mySpider;
    }

    public static MySpider getBankDataSpiderServiceForTheads(URL urls, String ScheduleName) throws Exception {
        MySpider mySpider = new MySpider(new URL[]{urls})
                .addBoot(new CustomBootStrap())
                .addDownloader(new StreamDownloader())
                .addProcessor(new CustomProcessor(ScheduleName))
                .addScheduleQueue(new CustomScheduleQueue());

        return mySpider;
    }

}

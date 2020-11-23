package Processor;

import java.io.File;
import java.util.List;

/**
 * 爬虫的处理核心，专门对爬取数据进行提炼
 *
 * 目前初步HTML形成String类型返回
 */
public interface Processor<E> {
    // 解析下载的URL资源，转为String格式返回
    String parseToBankFile(File downloadFile) throws Exception;
}

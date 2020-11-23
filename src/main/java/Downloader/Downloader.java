package Downloader;

import java.io.File;
import java.net.URL;

/**
 * 使用Java网络编程接口下载URL资源，存储到本地
 */
public interface Downloader {

    // 重置并更新URL
    void reset(URL url);

    // 下载URL资源
    File run() throws Exception;

    String getName();

}

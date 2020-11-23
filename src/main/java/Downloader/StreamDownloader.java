package Downloader;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

public class StreamDownloader extends AbstractDownloader{
    protected InputStream inputStream;
    protected OutputStream outputStream;
    // 设置HTTP的USER_AGENT
    private static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Mobile Safari/537.36";
    // 专属COOKIE，有时效性，根据需要不断调整
    private static final Boolean COOKIE_OPEN = true;
    private static final String COOKIE = "WCCTC=347173233_1844452431_1329146189; tranCCBIBS1=DDuITL3xobU2R2BijLzqljerp6Q1XFbh9oNV4G%2CiI4eWtXKr5rJabI1vAIKZgLuhCYLtYG6hEYXZ2H4iFYBxZFjiUIT1MUqhFY; JSESSIONID=9vfzEkXMFH2C2hm52QCEhF99vWDtJawjyXtspqmDMUr1vdtm2jTP!-401670210; null=1193476874.20480.0000; ticket=; cs_cid=; custName=; userType=; lastLoginTime=; ccbcustomid=3c7aa6b3372c20c30xUFoKRsVyv1wdiXBW0L1606100883593cdaxgljY5JQGdguirb3w35ca8b32a5bf75b7ece06e81a79d9a33; ccbsessionid=lPpErjpOEYCUdFma450a9b4e307-20201123110803; cityName=%E6%B7%B1%E5%9C%B3%E5%B8%82; cityCode=440300; bankName=%E6%B7%B1%E5%9C%B3%E5%B8%82%E5%88%86%E8%A1%8C; bankCode=442000000; cityCodeFlag=2; cityCodeCustId=; tranFAVOR=O2xk48hhI2n%2CVmNQibZ%2C9mmQDb1%2CYmmQVbV%2CQmnQob9%2CrmhQIbQ%2Cjf0eJeg5MzD06t; INFO=9j9d|X7spA";
    // 设置代理IP和端口号，需要根据需要而变化
    private static final String HOSTNAME = "60.184.192.214";
    private static final int PORT = 4285;


    public StreamDownloader(){
        this.name = getName();
    }

    @Override
    public void reset(URL url) {
        super.reset(url);
    }

    @Override
    public File run() throws Exception {
        File file = super.run();
        // 设置代理IP和端口号
//        Proxy proxy=new Proxy(Proxy.Type.HTTP, new InetSocketAddress(HOSTNAME, PORT));
//        URLConnection connection = url.openConnection(proxy);
         URLConnection connection = url.openConnection();
        // 配置User-Agent匿名服务
        connection.setRequestProperty("User-Agent", USER_AGENT);
        // 配置Cookie服务
        if(COOKIE_OPEN){
            System.out.println(COOKIE);
            connection.addRequestProperty("Cookie", COOKIE);
        }
        connection.connect();

        // 爬取数据流，以文件的形式保存下来
        inputStream = connection.getInputStream();
        outputStream = new FileOutputStream(file);
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)){
            int c;
            while( (c=bufferedInputStream.read()) != -1 ){
                bufferedOutputStream.write(c);
                bufferedOutputStream.flush();
            }
        }

        return file;
    }



}

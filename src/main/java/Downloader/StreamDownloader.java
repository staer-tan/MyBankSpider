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
    private static final String COOKIE = "WCCTC=347173233_1844452431_1329146189; tranCCBIBS1=UPZVmZyj6tiOx0AY4mC0IkN0omfec2NKoCv7AVNjpR.6d6oesE.N9X2CsV.BgkSgoTft06QbpIfcs2StoZ.3cWRUoJ.mAWbobHtTSw; ccbcustomid=3c7aa6b3372c20c30xUFoKRsVyv1wdiXBW0L1606100883593cdaxgljY5JQGdguirb3w35ca8b32a5bf75b7ece06e81a79d9a33; cityCode=440300; cityName=%E6%B7%B1%E5%9C%B3%E5%B8%82; bankCode=442000000; bankName=%E6%B7%B1%E5%9C%B3%E5%B8%82%E5%88%86%E8%A1%8C; cityCodeFlag=2; cityCodeCustId=; null=824378122.20480.0000; ticket=; cs_cid=; custName=; userType=; lastLoginTime=; ccbsessionid=lraJoRN80Cn1diVe5b7aef68f84-20201127090036; JSESSIONID=l5IHVbMAXlFFqmXq0WTXc60QIFyOLT5StWVMVOr7Gr-WdL8XmAmm!-1326342532; tranFAVOR=FKPuvXXoB0MBvPXIBeMlvZXgBVMTvgXnBZMKvZXjB1MsvPQhPmPwLV; INFO=9j9g|X8BmT";
    // 设置代理IP和端口号，需要根据需要而变化
    private static final Boolean PROXY_OPEN = false;
    private static final String HOSTNAME = "1.197.34.14";
    private static final int PORT = 4225;


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
        URLConnection connection;
        if(PROXY_OPEN){
            Proxy proxy=new Proxy(Proxy.Type.HTTP, new InetSocketAddress(HOSTNAME, PORT));
            connection = url.openConnection(proxy);
        }else{
            connection = url.openConnection();

        }
        // 配置User-Agent匿名服务
        connection.setRequestProperty("User-Agent", USER_AGENT);
        // 配置Cookie服务
        if(COOKIE_OPEN){
            // System.out.println(COOKIE);
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

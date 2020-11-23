package BootStrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

public abstract class AbstractBootStrap implements BootStrap{
    protected static URL bootPropertiesURL;
    private static String bootFileName = "boot.properties";

    @Override
    public void boot() {
        try {
            // Properties properties = loadBootProperties();
        }catch (Exception e){
            System.out.println("boot启动出现了一些问题");
        }
    }

    // 可用配置一些属性，持久化可用
    protected Properties loadBootProperties() throws IOException, URISyntaxException {
        String url = "file://" + AbstractBootStrap.class.getResource(File.separator).getPath() + File.separator + "properties" + File.separator + bootFileName;
        bootPropertiesURL = new URL(url);

        Properties properties = new Properties();
        properties.load(new FileInputStream(new File(bootPropertiesURL.toURI())));
        return properties;
    }
}

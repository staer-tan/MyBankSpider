package BootStrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

/**
 * 继承AbstractBootStrap来实现符合你实际场景的的BootStrap类
 */
public class CustomBootStrap extends AbstractBootStrap{
    private static String bootFileName = "DemoBoot.properties";

    protected Properties loadBootProperties() throws IOException, URISyntaxException {
        String url = "file://" + AbstractBootStrap.class.getResource(File.separator).getPath() + File.separator + "properties" + File.separator + "custom" + File.separator + bootFileName;
        bootPropertiesURL = new URL(url);

        Properties properties = new Properties();
        properties.load(new FileInputStream(new File(bootPropertiesURL.toURI())));
        return properties;
    }

}

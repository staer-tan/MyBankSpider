package Downloader;

import Util.FileUtil;
import Util.StringUtil;

import java.io.File;
import java.net.URL;

public abstract class AbstractDownloader implements Downloader {

    protected String name;
    protected URL url;

    @Override
    public void reset(URL url) {
        this.url = url;
        System.out.println("new URL is : " + url.toString());
    }

    @Override
    public File run() throws Exception {
        if (url == null || name == null) {
            throw new Exception("Downloader can't run without url or name");
        }

        return FileUtil.createEmptyFile(new URL(getPrefixPath()).getPath(), getName());
    }

    @Override
    public String getName() {
        if (StringUtil.isNull(name)) {
            name = StringUtil.getFileName();
        }
        return name;
    }

    protected String getPrefixPath() throws Exception {
        return FileUtil.getPrefix("download");
    }
}

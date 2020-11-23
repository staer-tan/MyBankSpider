package Processor;

import java.io.File;

public abstract class AbstractProcessor implements Processor {
    protected String name;

    @Override
    public String parseToBankFile(File downloadFile) throws Exception {
        throw new Exception("不支持解析数据至新文件以提供给银行服务");
    }
}

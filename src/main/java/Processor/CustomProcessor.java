package Processor;

import Util.StringUtil;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class CustomProcessor extends AbstractProcessor{

    public CustomProcessor(String name){
        this.name = name;
    }

    @Override
    public String parseToBankFile(File downloadFile) throws Exception {
        if (downloadFile == null) {
            throw new Exception("parse file is null");
        }

        String html = "";
        String curLine = "";
        try (RandomAccessFile randomAccessFile_read = new RandomAccessFile(downloadFile, "rw");) {
            while ((curLine = randomAccessFile_read.readLine()) != null) {
                if (StringUtil.isNull(curLine)) {
                    continue;
                }
                html += new String(curLine.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            }

        }
        return html;
    }
}

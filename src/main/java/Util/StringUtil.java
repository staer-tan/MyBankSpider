package Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class StringUtil {
    public static boolean isNull(String s){
        return s == null || s.equals("") || s.equals("\n");
    }

    public static String getFileName(){
        /**
         * 可以使用DateTimeFormatter代替SimpleDateFormat
         * 因为SimpleDateFormat是非线程安全的
         * 不过由于我这里每次都会new一个新的SimpleDateFormat实例,所以不会发生线程安全问题
         */
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmZ");

        Date date = new Date();

        String str = simpleDateFormat.format(date);
        System.out.println(str);

        // 当前时间
        return str;
    }
}
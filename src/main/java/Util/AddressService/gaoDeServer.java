package Util.AddressService;

import Processor.CustomProcessor;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用高德地图API银行地址进行解析
 */
public class gaoDeServer {

    // 高德地图申请的API的key
    public static final String apiKey = "f35671073585d7c3ba190e6c3a12d13e";

    // 任务名称
    public static final String SCHEDULE_NAME = "gaoDeServer";

    /**
     * 输入银行地址，返回该地址的省份和城市
     * @param address
     * @return
     * @throws Exception
     */
    public static Map<String, String> parseAddress(String address) throws Exception{
        String strUrl = "http://restapi.amap.com/v3/geocode/geo?key=" + apiKey + "&address=" + address;

        URL[] url = new URL[1];
        url[0] = new URL(strUrl);
        String[] gaoDeHtml = CustomProcessor.processGetStrHtml(url, SCHEDULE_NAME);
        Map<String, String> resultMap = parseGaoDeHtml(gaoDeHtml);
        return resultMap;
    }

    /**
     * 解析来自高德
     * @param GaoDeHtml
     * @return
     */
    public static Map<String, String> parseGaoDeHtml(String[] GaoDeHtml){
        Map<String, String> addressMap = new HashMap<>();

        for(String html : GaoDeHtml){
            JSONObject jsonObject = JSONObject.parseObject(html);
            JSONArray geoCodes = jsonObject.getJSONArray("geocodes");
            if(geoCodes.size() == 0){
                continue;
            }
            JSONObject object = geoCodes.getJSONObject(0);

            String province = object.getString("province");
            String city = object.getString("city");
            String district = object.getString("district");

            String location = object.getString("location");
            String[] loc = location.split(",");
            String longitudeX = loc[0];
            String latitudeY = loc[1];

            addressMap.put("province", province);
            addressMap.put("city", city);
            addressMap.put("district", district);
            addressMap.put("longitudeX", longitudeX);
            addressMap.put("latitudeY", latitudeY);

            System.out.println(addressMap);

        }

        return addressMap;
    }

}

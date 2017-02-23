package tw.com.jh7577.wherecycle.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.Callable;

// Address轉Geo地址
public class GoogleApiPosition implements Callable<Double[]> {
    private String queryURLString = "http://maps.googleapis.com/maps/api/geocode/json?address=%s&sensor=false&language=zh_tw";
    private Double[] location;
    private String address; // "台北市大安區忠孝東路四段169號4F之1";

    public GoogleApiPosition(String address) {
        this.address = address;
    }

    @Override
    public Double[] call() {
        try {
            location = new Double[2];
            // // 輸入地址得到緯經度(中文地址需透過URLEncoder編碼)
            location = getLocationByAddress(URLEncoder.encode(address, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
        }
        return location;
    }

    public Double[] getLocationByAddress(String address) {
        String urlString = String.format(queryURLString, address);
        try {
            // 取得 json string
            String jsonStr = HttpUtil.get(urlString);
            // 取得 json 根陣列節點 results
            JSONArray results = new JSONObject(jsonStr).getJSONArray("results");
            System.out.println("results.length() : " + results.length());
            if (results.length() >= 1) {
                // 取得 results[0]
                JSONObject jsonObject = results.getJSONObject(0);
                // 取得 geometry --> location 物件
                JSONObject latlng = jsonObject.getJSONObject("geometry").getJSONObject("location");
                location[0] = latlng.getDouble("lat");
                location[1] = latlng.getDouble("lng");
            }

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return location;

    }

}
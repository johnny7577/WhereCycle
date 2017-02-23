package tw.com.jh7577.wherecycle.utils;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.Callable;

// Geo轉Address
public class GoogleApiAddress implements Callable<String> {

    private String queryURLString = "http://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&sensor=true&language=zh_tw";
    private String address;
    private String lat;
    private String lng;

    public GoogleApiAddress(String lat, String lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public GoogleApiAddress(double lat, double lng) {
        this.lat = lat+"";
        this.lng = lng+"";
    }

    public GoogleApiAddress(LatLng tts) {
        this.lat = tts.latitude+"";
        this.lng = tts.longitude+"";
    }

    @Override
    public String call() {
        // 輸入緯經度得到地址
        address = getAddressByLocation(lat, lng);
        return address;

    }

    public String getAddressByLocation(String lat, String lng) {
        String urlString = String.format(queryURLString, lat, lng);
        String address = "Not found !";
        try {
            // 取得 json string
            String jsonStr = HttpUtil.get(urlString);
            // 取得 json 根陣列節點 results
            JSONArray results = new JSONObject(jsonStr).getJSONArray("results");
            if (results.length() >= 1) {
                // 取得 results[0]
                JSONObject jsonObject = results.getJSONObject(0);
                // 取得 formatted_address 屬性內容
                address = jsonObject.optString("formatted_address");
            }

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return address;

    }

}

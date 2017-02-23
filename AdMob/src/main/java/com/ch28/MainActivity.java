package com.ch28;

import android.content.Context;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends ActionBarActivity {
    private Context context;
    private String ad_unit_id;
    private AdView adView;
    private static final String LOG_TAG = "BannerAdListener";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        // 取得 AdMob 廣告單元 id
        ad_unit_id = getResources().getString(R.string.banner_ad_unit_id);
        // 設置 AdMob 視圖
        adView = (AdView) findViewById(R.id.adView);
        // 註冊 AdMob 監聽器
        adView.setAdListener(new AdListener() {
            // 廣告關閉返回App
            @Override
            public void onAdClosed() {
                Log.d(LOG_TAG, "onAdClosed");
                Toast.makeText(context, "onAdClosed",
                        Toast.LENGTH_SHORT).show();
            }
            // AdMob廣告載入與輪播更新失敗回呼函示
            @Override
            public void onAdFailedToLoad(int error) {
                String message = "onAdFailedToLoad: " + getErrorReason(error);
                Log.d(LOG_TAG, message);
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
            // AdMob廣告離開該App（例如：啟動另一個新的Activity）
            @Override
            public void onAdLeftApplication() {
                Log.d(LOG_TAG, "onAdLeftApplication");
                Toast.makeText(context, "onAdLeftApplication",
                        Toast.LENGTH_SHORT).show();
            }
            //點選AdMob橫幅廣告回呼函示
            @Override
            public void onAdOpened() {
                Log.d(LOG_TAG, "onAdOpened");
                Toast.makeText(context, "onAdOpened", Toast.LENGTH_SHORT).show();
            }
            // AdMob廣告載入與輪播更新成功回呼函示
            @Override
            public void onAdLoaded() {
                Log.d(LOG_TAG, "onAdLoaded");
                Toast.makeText(context, "onAdLoaded", Toast.LENGTH_SHORT).show();
            }
        });
        // 建立 AdMob 相關請求設定
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("INSERT_YOUR_HASHED_DEVICE_ID_HERE")
                .build();
        // 設置 AdMob 請求
        adView.loadAd(adRequest);

    }
    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            // AdMob 重新喚醒
            adView.resume();
        }
    }
    @Override
    public void onPause() {
        if (adView != null) {
            // AdMob 進入暫停狀態
            adView.pause();
        }
        super.onPause();
    }
    @Override
    public void onDestroy() {
        if (adView != null) {
            // AdMob 銷毀
            adView.destroy();
        }
        super.onDestroy();
    }
    // 取得錯誤原因
    private String getErrorReason(int errorCode) {
        String errorReason = "";
        switch(errorCode) {
            case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                errorReason = "Internal error";
                break;
            case AdRequest.ERROR_CODE_INVALID_REQUEST:
                errorReason = "Invalid request";
                break;
            case AdRequest.ERROR_CODE_NETWORK_ERROR:
                errorReason = "Network Error";
                break;
            case AdRequest.ERROR_CODE_NO_FILL:
                errorReason = "No fill";
                break;
        }
        return errorReason;
    }
}

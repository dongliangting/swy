package com.iflytek.aiui.demo.chat.ui.zxing;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;


import com.iflytek.aiui.demo.chat.R;
import com.iflytek.aiui.demo.chat.ui.view.adRotatorComponent.Advertisements;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BannerActivity extends AppCompatActivity {
    private LinearLayout llBanner;
    private Advertisements mAdvertisements;
    private View adView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner2);

        llBanner = findViewById(R.id.ll_banner);
        findViewById(R.id.tv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setDataHeader();
    }
    private ArrayList<Integer> bannerData = new ArrayList<>();
    private void setDataHeader() {

        bannerData.add(R.mipmap.ic_launcher);
        bannerData.add(R.mipmap.ic_launcher);
        bannerData.add(R.mipmap.banner_default);

        if (bannerData.size() != 0) {
            JSONArray advertiseArray = new JSONArray();
            for (int i = 0; i < bannerData.size(); i++) {
                try {
                    JSONObject head_img = new JSONObject();

                    head_img.put("head_img", bannerData.get(i));

                    advertiseArray.put(head_img);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //timer 停止 移除header
            if (llBanner != null && adView != null) {
                if (mAdvertisements != null) {
                    mAdvertisements.StopTimer();
                }
                llBanner.removeAllViews();
                adView = null;
            }
            //这句关键
            mAdvertisements = new Advertisements(this, false, 5000);
            adView = mAdvertisements.initView(advertiseArray);

            llBanner.addView(adView);
            //轮播图点击跳转
        }
    }

}

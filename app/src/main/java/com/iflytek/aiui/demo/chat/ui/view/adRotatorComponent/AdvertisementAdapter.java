package com.iflytek.aiui.demo.chat.ui.view.adRotatorComponent;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.iflytek.aiui.demo.chat.R;

import org.json.JSONArray;

import java.util.List;


/**
 * 广告轮播adapter
 *
 * @author dong
 * @data 2015年3月8日下午3:46:35
 * @contance dong854163@163.com
 */
public class AdvertisementAdapter extends PagerAdapter {

    private Context context;
    private List<View> views;
    JSONArray advertiseArray;

    public AdvertisementAdapter() {
        super();
        // TODO Auto-generated constructor stub
    }

    public AdvertisementAdapter(Context context, List<View> views, JSONArray advertiseArray) {
        this.context = context;
        this.views = views;
        this.advertiseArray = advertiseArray;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return views.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return (arg0 == arg1);
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        ((ViewPager) container).removeView(views.get(position));
    }

    @Override
    public Object instantiateItem(View container, int position) {
        ((ViewPager) container).addView(views.get(position), 0);
        final int POSITION = position;
        View view = views.get(position);
        try {
            int head_img = advertiseArray.optJSONObject(position).optInt("head_img");
            String head_title = advertiseArray.optJSONObject(position).optString("adTitle");
            String videoUrl = advertiseArray.optJSONObject(position).optString("videoUrl");
            ImageView ivAdvertise = (ImageView) view.findViewById(R.id.ivAdvertise);
            TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
            RelativeLayout textBg = (RelativeLayout) view.findViewById(R.id.rl_title_bg);
            if(!TextUtils.isEmpty(head_title)){
//                textBg.setBackground(context.getResources().getDrawable(R.mipmap.ad_text_bg));
                textBg.setVisibility(View.VISIBLE);
            }else {
//                textBg.setBackground(null);
                textBg.setVisibility(View.GONE);
            }
            tvTitle.setText(head_title + "");
            //   ImageLoaderUtils.loadImage(head_img, ivAdvertise, R.mipmap.ic_launcher);
            // todo
            //   Picasso.with(context).


         /*
         * 缺省的占位图片，一般可以设置成一个加载中的进度GIF图
         */
            ivAdvertise.setImageDrawable(context.getResources().getDrawable(head_img));

//            ivAdvertise.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    // Toast.makeText(context, "第" + POSITION + "个广告图片被点击").show();
//
//                    mAdAdapterListener.AdListener(POSITION);
//                }
//            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }


    public void setmAdAdapterListener(AdAdapterListener mAdAdapterListener) {
        this.mAdAdapterListener = mAdAdapterListener;
    }

    public AdAdapterListener mAdAdapterListener;

    public interface AdAdapterListener {
        public void AdListener(int postion);
    }

}
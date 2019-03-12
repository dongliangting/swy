package com.iflytek.aiui.demo.chat.ui.view.adRotatorComponent;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


import com.iflytek.aiui.demo.chat.R;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 *@author dong
 *@version 2015-3-8下午4:33:39
 */

public class Advertisements implements OnPageChangeListener
{

	private ViewPager vpAdvertise;
	private Context context;
	private boolean fitXY;
	private int timeDratioin;//多长时间切换一次pager

	List<View> views;
	// 底部小点图片
	private ImageView[] dots;

	// 记录当前选中位置
	private int currentIndex;

	Timer timer;
	TimerTask task;
	int count = 0;

	private Handler runHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 0x01:
					int currentPage = (Integer) msg.obj;
					setCurrentDot(currentPage);
					vpAdvertise.setCurrentItem(currentPage);
					break;
			}
		};
	};
	private RelativeLayout rl_relativeLayout;

	public Advertisements(Context context, boolean fitXY, int timeDratioin)
	{
		this.context = context;
		this.fitXY = fitXY;
		this.timeDratioin = timeDratioin;
	}

	public View initView(final JSONArray advertiseArray){
		View view = View.inflate(context, R.layout.advertisement_board, null);
		vpAdvertise = (ViewPager) view.findViewById(R.id.vpAdvertise);
		ViewGroup.LayoutParams params=vpAdvertise.getLayoutParams();

//
//		params.width= U.getPreferences("width", 0);//修改宽度
//		params.height=U.getPreferences("width",0)*1/2;//修改高度
		// 3、重新设置修改后的布局给控件
		vpAdvertise.setLayoutParams(params); //设置修改后的布局。
		vpAdvertise.setOnPageChangeListener(this);
		views = new ArrayList<View>();
		LinearLayout ll = (LinearLayout) view.findViewById(R.id.ll);//获取轮播图片的点的parent，用于动态添加要显示的点
		for(int i = 0 ; i < advertiseArray.length() ; i ++){
			if(fitXY){
				views.add(View.inflate(context,R.layout.advertisement_item_fitxy, null));
			}else {
				views.add(View.inflate(context,R.layout.advertisement_item_fitcenter, null));
			}
			ll.addView(View.inflate(context,R.layout.advertisement_board_dot, null));
		}
		initDots(view , ll);

		AdvertisementAdapter adapter = new AdvertisementAdapter(context , views , advertiseArray);
		vpAdvertise.setOffscreenPageLimit(3);
		vpAdvertise.setAdapter(adapter);

		timer = new Timer();
		task = new TimerTask() {
			@Override
			public void run() {
				int currentPage = count%advertiseArray.length();
				count++;
				Message msg = Message.obtain();
				msg.what = 0x01;
				msg.obj = currentPage;
				runHandler.sendMessage(msg);
			}
		};
		timer.schedule(task, 0, timeDratioin);

//		adapter.setmAdAdapterListener(new AdvertisementAdapter.AdAdapterListener() {
//			@Override
//			public void AdListener(int postion) {
//				mAdOnClickListener.adOnClick(postion);
//			}
//		});
		return view;
	}


	private void initDots(View view, LinearLayout ll) {


		dots = new ImageView[views.size()];

		// 循环取得小点图片
		for (int i = 0; i < views.size(); i++) {
			dots[i] = (ImageView) ll.getChildAt(i);
			dots[i].setEnabled(true);// 都设为灰色
		}

		currentIndex = 0;
		dots[currentIndex].setEnabled(false);// 设置为黄色，即选中状态
	}

	private void setCurrentDot(int position) {
		if (position < 0 || position > views.size() - 1
				|| currentIndex == position) {
			return;
		}

		dots[position].setEnabled(false);
		dots[currentIndex].setEnabled(true);

		currentIndex = position;
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int position) {
		// TODO Auto-generated method stub
		count = position;
		setCurrentDot(position);
	}

	public AdOnClickListener mAdOnClickListener;

	public void setAdItmeOnClickListener(AdOnClickListener mAdOnClickListener) {
		this.mAdOnClickListener = mAdOnClickListener;
	}

	public interface AdOnClickListener{
		public void adOnClick(int postion);
	}




	public void StopTimer() {
		if(timer!=null)
		{
			timer.cancel();
			timer = null;
		}
		if(task!=null)
		{
			task.cancel();
			task = null;
		}

	}


}

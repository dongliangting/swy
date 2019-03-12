package com.iflytek.aiui.demo.chat;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.iflytek.aiui.demo.chat.di.AppComponent;
import com.iflytek.aiui.demo.chat.di.DaggerAppComponent;
import com.umeng.commonsdk.UMConfigure;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;


public class ChatApp extends Application implements HasActivityInjector {
    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;
    AppComponent mAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppComponent = DaggerAppComponent
                .builder()
                .application(this)
                .build();

        mAppComponent
                .inject(this);

//        UMConfigure.init(this, "5c0a8a3fb465f554ab0004ef", "ipad", UMConfigure.DEVICE_TYPE_PHONE, "git");

//        initCrash(this);
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }

    private void initCrash(Context context) {
        // 处理全局异常：
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(context);
        // 发送以前没发送的报告(可选)
        crashHandler.sendPreviousReportsToServer();
    }
}

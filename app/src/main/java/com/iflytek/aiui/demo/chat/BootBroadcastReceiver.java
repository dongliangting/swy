package com.iflytek.aiui.demo.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {

//        Toast.makeText(ChatApp.,"** 开机收到广播 **",Toast.LENGTH_LONG).show();
        try {
            Thread.sleep(2000L);
            //Intent toIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            // context.startActivity(toIntent );
            intent = new Intent(context, ChatActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


}


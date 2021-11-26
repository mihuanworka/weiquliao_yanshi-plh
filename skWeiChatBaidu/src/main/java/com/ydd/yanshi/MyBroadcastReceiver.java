package com.ydd.yanshi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.ydd.yanshi.AppConfig.BROADCASTTEST_ACTION;

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (BROADCASTTEST_ACTION.equals(intent.getAction())) {
            context.startService(new Intent(context, RestartService.class));

        }
    }
}

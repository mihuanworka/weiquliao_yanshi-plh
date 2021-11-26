package com.ydd.yanshi.broadcast;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ydd.yanshi.AppConfig;
import com.ydd.yanshi.MyApplication;

/**
 * 我的朋友
 */
public class CardcastUiUpdateUtil {

    public static final String ACTION_UPDATE_UI = AppConfig.sPackageName + ".action.cardcast.update_ui";

    public static IntentFilter getUpdateActionFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_UI);
        return intentFilter;
    }

    public static void broadcastUpdateUi(Context context) {
        if (context != null) {
            LocalBroadcastManager.getInstance(MyApplication.getInstance()).sendBroadcast(new Intent(ACTION_UPDATE_UI));
        }
    }
}

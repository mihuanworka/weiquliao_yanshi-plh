package com.ydd.yanshi.ui.share;

import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ydd.yanshi.AppConfig;
import com.ydd.yanshi.MyApplication;

public class ShareBroadCast {
    public static final String ACTION_FINISH_ACTIVITY = AppConfig.sPackageName + ".action.finish_activity";// 结束之前的页面

    /**
     * 更新消息Fragment的广播
     */
    public static void broadcastFinishActivity(Context context) {
        LocalBroadcastManager.getInstance(MyApplication.getInstance()).sendBroadcast(new Intent(ACTION_FINISH_ACTIVITY));
    }
}

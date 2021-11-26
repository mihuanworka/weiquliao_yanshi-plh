package com.ydd.yanshi.util;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.EventListener;

public class SyncTimeManager extends EventListener {
    private static SyncTimeManager instance = new SyncTimeManager();
    private static final String TAG_PREFIX = "SyncTime_";
    private static final Map<String, Long> startRespTimeStamp = new HashMap<>();

    private SyncTimeManager() {
    }

    public static SyncTimeManager getInstance() {
        return instance;
    }

    public String createSyncTimeTag() {
        return TAG_PREFIX + System.currentTimeMillis();
    }

    private static String getTag(@NonNull Call call) {
        Object tag = call.request().tag();
        if (tag instanceof String && ((String) tag).startsWith(TAG_PREFIX)) {
            return (String) tag;
        }
        return null;
    }

    @Override
    public void responseHeadersStart(@NonNull Call call) {
        String tag = getTag(call);
        if (tag != null) {
            startRespTimeStamp.put(tag, System.currentTimeMillis());
        }
    }

    public long getCostFromRespStart(String tag){
        long cost = 0;
        if (startRespTimeStamp.containsKey(tag)){
            cost = System.currentTimeMillis() - startRespTimeStamp.get(tag);
        }
        System.out.println(">>>sync server time resp cost:" + cost);
        return cost;
    }

    public void clearTag(String tag){
        startRespTimeStamp.remove(tag);
    }
}

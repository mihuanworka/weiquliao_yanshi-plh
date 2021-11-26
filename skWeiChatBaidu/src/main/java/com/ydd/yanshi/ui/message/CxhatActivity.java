package com.ydd.yanshi.ui.message;

import android.os.Bundle;
import android.util.Log;

import com.ydd.yanshi.R;
import com.ydd.yanshi.ui.base.BaseActivity;


/**
 * 单聊界面
 */
public class CxhatActivity extends BaseActivity {

    long lastTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lastTime = System.currentTimeMillis();
        setContentView(R.layout.chat);
        Log.e("xuan", "timexxx  oncreate: " + (System.currentTimeMillis() - lastTime));
    }
}

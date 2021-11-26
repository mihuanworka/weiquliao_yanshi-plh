package com.ydd.yanshi.ui.groupchat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.ydd.yanshi.R;
import com.ydd.yanshi.db.InternationalizationHelper;
import com.ydd.yanshi.ui.base.BaseActivity;

public class RoomSearchResultActivity extends BaseActivity {

    public static void start(Context ctx, String keyWord) {
        Intent intent = new Intent(ctx, RoomSearchResultActivity.class);
        intent.putExtra("roomName", keyWord);
        ctx.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_search_result);
        getSupportActionBar().hide();
        TextView mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        mTvTitle.setText(InternationalizationHelper.getString("GROUP"));
    }
}

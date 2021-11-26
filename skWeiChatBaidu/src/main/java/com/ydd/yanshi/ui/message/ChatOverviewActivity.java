package com.ydd.yanshi.ui.message;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.qrcode.utils.DecodeUtils;
import com.google.zxing.Result;
import com.ydd.yanshi.R;
import com.ydd.yanshi.Reporter;
import com.ydd.yanshi.adapter.ChatOverviewAdapter;
import com.ydd.yanshi.bean.message.ChatMessage;
import com.ydd.yanshi.ui.base.BaseActivity;
import com.ydd.yanshi.util.AsyncUtils;
import com.ydd.yanshi.util.FileUtil;
import com.ydd.yanshi.view.SaveWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.kareluo.imaging.IMGEditActivity;

public class ChatOverviewActivity extends BaseActivity {
    public static final int REQUEST_IMAGE_EDIT = 1;

    private ViewPager mViewPager;
    private ChatOverviewAdapter mChatOverviewAdapter;

    private List<ChatMessage> mChatMessages = new ArrayList<>();
    private int mFirstShowPosition;

    private String mCurrentShowUrl;
    private String mEditedPath;
    private SaveWindow mSaveWindow;
    private My_BroadcastReceivers my_broadcastReceiver = new My_BroadcastReceivers();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_overview);
        String imageChatMessageListStr = getIntent().getStringExtra("imageChatMessageList");
        mChatMessages = JSON.parseArray(imageChatMessageListStr, ChatMessage.class);
        mFirstShowPosition = getIntent().getIntExtra("imageChatMessageList_current_position", 0);
        getCurrentShowUrl(mFirstShowPosition);

        initView();
        register();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (my_broadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(my_broadcastReceiver);
        }
    }

    private void initView() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        mViewPager = findViewById(R.id.chat_overview_vp);
        mChatOverviewAdapter = new ChatOverviewAdapter(this, mChatMessages);
        mViewPager.setAdapter(mChatOverviewAdapter);
        mViewPager.setCurrentItem(mFirstShowPosition);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                getCurrentShowUrl(arg0);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    private void getCurrentShowUrl(int position) {
        ChatMessage chatMessage = mChatMessages.get(position);
        if (!TextUtils.isEmpty(chatMessage.getFilePath()) && FileUtil.isExist(chatMessage.getFilePath())) {
            mCurrentShowUrl = chatMessage.getFilePath();
        } else {
            mCurrentShowUrl = chatMessage.getContent();
        }
    }

    private void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(com.ydd.yanshi.broadcast.OtherBroadcast.singledown);
        filter.addAction(com.ydd.yanshi.broadcast.OtherBroadcast.longpress);
        LocalBroadcastManager.getInstance(this).registerReceiver(my_broadcastReceiver, filter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_EDIT:
                    mCurrentShowUrl = mEditedPath;
                    ChatMessage chatMessage = mChatMessages.get(mViewPager.getCurrentItem());
                    chatMessage.setFilePath(mCurrentShowUrl);
                    mChatMessages.set(mViewPager.getCurrentItem(), chatMessage);
                    mChatOverviewAdapter.refreshItem(mCurrentShowUrl, mViewPager.getCurrentItem());
                    break;
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    class My_BroadcastReceivers extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(com.ydd.yanshi.broadcast.OtherBroadcast.singledown)) {
                finish();
            } else if (intent.getAction().equals(com.ydd.yanshi.broadcast.OtherBroadcast.longpress)) {
                // 长按屏幕，弹出菜单
                mSaveWindow = new SaveWindow(ChatOverviewActivity.this, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSaveWindow.dismiss();
                        switch (v.getId()) {
                            case R.id.save_image:
                                FileUtil.downImageToGallery(ChatOverviewActivity.this, mCurrentShowUrl);
                                break;
                            case R.id.edit_image:
                                Glide.with(ChatOverviewActivity.this)
                                        .load(mCurrentShowUrl)
                                        .dontAnimate().skipMemoryCache(true) // 不使用内存缓存
                                        .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                                        .downloadOnly(new SimpleTarget<File>() {
                                            @Override
                                            public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                                                mEditedPath = FileUtil.createImageFileForEdit().getAbsolutePath();
                                                IMGEditActivity.startForResult(ChatOverviewActivity.this, Uri.fromFile(resource), mEditedPath, REQUEST_IMAGE_EDIT);
                                            }

                                        });
                                break;
                            case R.id.identification_qr_code:
                                // 识别图中二维码
                                Glide.with(ChatOverviewActivity.this)
                                        .load(mCurrentShowUrl)
                                        .dontAnimate().skipMemoryCache(true) // 不使用内存缓存
                                        .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                                        .downloadOnly(new SimpleTarget<File>() {
                                            @Override
                                            public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                                                AsyncUtils.doAsync(mContext, t -> {
                                                    Reporter.post("二维码解码失败，" + resource.getCanonicalPath(), t);
                                                    runOnUiThread(() -> {
                                                        Toast.makeText(ChatOverviewActivity.this, R.string.decode_failed, Toast.LENGTH_SHORT).show();
                                                    });
                                                }, t -> {
                                                    // 做些预处理提升扫码成功率，
                                                    // 预读一遍获取图片比例，使用inSampleSize压缩图片分辨率到恰到好处，
                                                    Uri decodeUri = Uri.fromFile(resource);
                                                    final Result result = DecodeUtils.decodeFromPicture(DecodeUtils.compressPicture(t.getRef(), decodeUri));
                                                    t.uiThread(c -> {
                                                        if (result != null && !TextUtils.isEmpty(result.getText())) {
                                                            HandleQRCodeScanUtil.handleScanResult(mContext, result.getText());
                                                        } else {
                                                            Toast.makeText(ChatOverviewActivity.this, R.string.decode_failed, Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                });
                                            }

                                        });
                                break;
                        }
                    }
                });
                mSaveWindow.show();
            }
        }
    }
}

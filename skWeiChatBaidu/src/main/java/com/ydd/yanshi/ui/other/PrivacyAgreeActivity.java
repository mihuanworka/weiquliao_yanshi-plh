package com.ydd.yanshi.ui.other;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ydd.yanshi.R;
import com.ydd.yanshi.adapter.MessageLogin;
import com.ydd.yanshi.ui.base.BaseActivity;
import com.ydd.yanshi.util.Constants;
import com.ydd.yanshi.util.EventBusHelper;
import com.ydd.yanshi.util.PreferenceUtils;
import com.ydd.yanshi.view.SelectionFrame;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * 请求同意隐私政策的页面，
 */
public class PrivacyAgreeActivity extends BaseActivity {
    private WebView mWebView;
    private TextView mTitleTv;
    private ImageView mTitleLeftIv;

    public PrivacyAgreeActivity() {
        noConfigRequired();
        noLoginRequired();
    }

    public static void start(Context ctx) {
        Intent intent = new Intent(ctx, PrivacyAgreeActivity.class);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_agree);
        initActionBar();
        EventBusHelper.register(this);
//        mWebView = findViewById(R.id.webView);
//        mWebView.setWebViewClient(new WebViewClient() {
//            @Override
//            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//
//                super.onPageStarted(view, url, favicon);
//            }
//
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//
//                return true;
//            }
//
//            @Override
//            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//                super.onReceivedError(view, errorCode, description, failingUrl);
//            }
//        });
//        mWebView.loadUrl("http://changtan118.com/xieyi.html");
//

        findViewById(R.id.btnAgree).setOnClickListener((v) -> {
            PreferenceUtils.putBoolean(mContext, Constants.PRIVACY_AGREE_STATUS, true);
            finish();
        });

        findViewById(R.id.btnDisagree).setOnClickListener((v) -> {
            disagree();
        });

    }

    private void disagree() {
        SelectionFrame selectionFrame = new SelectionFrame(mContext);
        selectionFrame.setSomething(null, getString(R.string.tip_privacy_can_not_disagree),
                getString(R.string.btn_privacy_disagree), getString(R.string.btn_privacy_re_reading),
                new SelectionFrame.OnSelectionFrameClickListener() {
                    @Override
                    public void cancelClick() {
                        // 关闭Splash页面和当前页面，
                        EventBus.getDefault().post(new MessageLogin());
                    }

                    @Override
                    public void confirmClick() {
                    }
                });
        selectionFrame.show();
    }

    private void initActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disagree();
            }
        });
        mTitleTv = findViewById(R.id.tv_title_center);
        mTitleTv.setText(R.string.title_privacy_policy);
        mTitleLeftIv = findViewById(R.id.iv_title_left);
        mTitleLeftIv.setImageResource(R.drawable.icon_close);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageLogin message) {
        finish();
    }
}

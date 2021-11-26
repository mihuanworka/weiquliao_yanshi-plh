package com.ydd.yanshi.ui.account;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ydd.yanshi.R;
import com.ydd.yanshi.helper.AvatarHelper;
import com.ydd.yanshi.helper.LoginHelper;
import com.ydd.yanshi.ui.base.BaseActivity;
import com.ydd.yanshi.ui.share.ShareConstant;
import com.ydd.yanshi.ui.share.ShareLoginActivity;
import com.ydd.yanshi.ui.tool.WebViewActivity;
import com.ydd.yanshi.util.Constants;
import com.ydd.yanshi.util.PreferenceUtils;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.JsonCallback;

import okhttp3.Call;

/**
 * 外部浏览器调起当前界面 授权
 */
public class QuickLoginAuthority extends BaseActivity {
    private boolean isNeedExecuteLogin;
    private String mShareContent;

    public QuickLoginAuthority() {
        noLoginRequired();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_result);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        // 已进入授权界面
        ShareConstant.IS_SHARE_QL_COME = true;

        Uri data = getIntent().getData();
        if (data != null) {
            mShareContent = data.toString();
        }
        if (TextUtils.isEmpty(mShareContent)) {// 外部跳转进入
            mShareContent = ShareConstant.ShareContent;
        } else {// 数据下载页面进入
            ShareConstant.ShareContent = mShareContent;
        }

        // 判断本地登录状态
        int userStatus = LoginHelper.prepareUser(mContext, coreManager);
        switch (userStatus) {
            case LoginHelper.STATUS_USER_FULL:
            case LoginHelper.STATUS_USER_NO_UPDATE:
            case LoginHelper.STATUS_USER_TOKEN_OVERDUE:
                boolean isConflict = PreferenceUtils.getBoolean(this, Constants.LOGIN_CONFLICT, false);
                if (isConflict) {
                    isNeedExecuteLogin = true;
                }
                break;
            case LoginHelper.STATUS_USER_SIMPLE_TELPHONE:
                isNeedExecuteLogin = true;
                break;
            case LoginHelper.STATUS_NO_USER:
            default:
                isNeedExecuteLogin = true;
        }

        if (isNeedExecuteLogin) {// 需要先执行登录操作
            startActivity(new Intent(mContext, ShareLoginActivity.class));
            finish();
            return;
        }
        initActionBar();
        initView();
    }

    private void initActionBar() {
        findViewById(R.id.iv_title_left).setVisibility(View.GONE);
        TextView mTvTitleLeft = findViewById(R.id.tv_title_left);
        mTvTitleLeft.setText(getString(R.string.close));
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.centent_bar, getString(R.string.app_name)));
        mTvTitleLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView() {
        ImageView mAppIconIv = findViewById(R.id.app_icon_iv);
        TextView mAppNameTv = findViewById(R.id.app_name_tv);
        String webAppName = WebViewActivity.URLRequest(mShareContent).get("webAppName");
        String webAppsmallImg = WebViewActivity.URLRequest(mShareContent).get("webAppsmallImg");
        if (!TextUtils.isEmpty(webAppName)) {
            mAppNameTv.setText(webAppName);
        }
        if (!TextUtils.isEmpty(webAppsmallImg)) {
            AvatarHelper.getInstance().displayUrl(webAppsmallImg, mAppIconIv);
        }

        // 授权登录按钮
        findViewById(R.id.login_btn).setOnClickListener(v -> onAuthLogin(mShareContent));
    }

    /**
     * 授权登录
     */
    private void onAuthLogin(String url) {
        Log.e("onResponse", "onAuthLogin: " + url);

        String appId = WebViewActivity.URLRequest(url).get("appId");
        String redirectURL = WebViewActivity.URLRequest(url).get("callbackUrl");

        HttpUtils.get().url(coreManager.getConfig().AUTHOR_CHECK)
                .params("appId", appId)
                .params("state", coreManager.getSelfStatus().accessToken)
                .params("callbackUrl", redirectURL)
                .build().execute(new JsonCallback() {
            @Override
            public void onResponse(String result) {
                Log.e("onResponse", "onResponse: " + result);
                JSONObject json = JSON.parseObject(result);
                int code = json.getIntValue("resultCode");
                if (1 == code) {
                    String html = json.getJSONObject("data").getString("callbackUrl") + "?code=" + json.getJSONObject("data").getString("code");
                    Uri uri = Uri.parse(html);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }

            @Override
            public void onError(Call call, Exception e) {

            }
        });
    }
}


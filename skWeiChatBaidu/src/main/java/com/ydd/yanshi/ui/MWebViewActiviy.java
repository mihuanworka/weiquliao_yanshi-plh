package com.ydd.yanshi.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.ydd.yanshi.R;
import com.ydd.yanshi.ui.base.BaseActivity;


public class MWebViewActiviy extends BaseActivity {
    private WebView webView;

    public static void startActivity(Context context,String title, String url) {
        Intent intent = new Intent(context, MWebViewActiviy.class);
        intent.putExtra("data", url);
        intent.putExtra("title", title);
        context.startActivity(intent);

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);

        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        String title = getIntent().getStringExtra("title");
        tvTitle.setText(title);

        String url = getIntent().getStringExtra("data");
        webView = findViewById(R.id.webView);

        /* 设置支持Js */
        webView.getSettings().setUserAgentString("meimaweb-android");
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.getSettings().setJavaScriptEnabled(true); // 启用js
        webView.getSettings().setBlockNetworkImage(false); // 解决图片不显示
        //使用缓存，否则localstorage等无法使用

        //使用缓存，否则localstorage等无法使用
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);

        webView.getSettings().setAppCachePath(this.getApplication().getCacheDir().getAbsolutePath());
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
//        webView.setWebChromeClient(new WebChromeClient() {
//            @Override
//            public void openFileChooser(ValueCallback<Uri> valueCallback, String s, String s1) {
//                super.openFileChooser(valueCallback, s, s1);
//                uploadMsg = valueCallback;
//
//            }
//
//            @Override
//            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> valueCallback, FileChooserParams fileChooserParams) {
//                try {
//                    mUploadMsgs = valueCallback;
//                    fileChooserParams = fileChooserParams;
//                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//                    i.addCategory(Intent.CATEGORY_OPENABLE);
//                    i.setType("*/*");
//                    startActivityForResult(
//                            Intent.createChooser(i, "File Browser"),
//                            REQUEST_SELECT_FILE_CODE
//                    );
//
//                } catch (Exception e) {
//
//                }
//
//                return true;
//
//            }
//        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String s) {
                return super.shouldOverrideUrlLoading(webView, s);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });
        webView.loadUrl(url);
//        webView.webChromeClient = object : WebChromeClient() {
//            override fun openFileChooser(
//                    p0: ValueCallback<Uri>?,
//            acceptType: String?,
//                    capture: String?
//                ) {
//                Log.i("lmj", "aaa")
//                uploadMsg = p0
//            }
//
//            override fun onShowFileChooser(
//                    p0: com.tencent.smtt.sdk.WebView?,
//                    p1: ValueCallback<Array<Uri>>?,
//            p2: FileChooserParams?
//                ): Boolean {
//                try {
//                    mUploadMsgs = p1
//                    fileChooserParams = p2
//                    val i = Intent(Intent.ACTION_GET_CONTENT)
//                    i.addCategory(Intent.CATEGORY_OPENABLE)
//                    i.type = "*/*"
//                    startActivityForResult(
//                            Intent.createChooser(i, "File Browser"),
//                            REQUEST_SELECT_FILE_CODE
//                    )
//
//                } catch (e:ActivityNotFoundException) {
//
//                }
//
//                return true
//            }
//
//        }
    }

    ValueCallback<Uri> uploadMsg = null;
    ValueCallback<Uri[]> mUploadMsgs = null;
    WebChromeClient.FileChooserParams fileChooserParams = null;
    private int REQUEST_SELECT_FILE_CODE = 100;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_FILE_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (mUploadMsgs == null) {
                    return;
                }
                mUploadMsgs.onReceiveValue(
                        WebChromeClient.FileChooserParams.parseResult(
                                resultCode,
                                data
                        )
                );
                mUploadMsgs = null;
            }

        }
    }


}

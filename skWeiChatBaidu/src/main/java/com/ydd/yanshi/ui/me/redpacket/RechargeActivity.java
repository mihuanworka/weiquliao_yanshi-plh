package com.ydd.yanshi.ui.me.redpacket;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.unionpay.UPPayAssistEx;
import com.xuan.xuanhttplibrary.okhttp.http.ApiCallBack;
import com.xuan.xuanhttplibrary.okhttp.http.HttpHelper;
import com.ydd.yanshi.R;
import com.ydd.yanshi.bean.EventPaySuccess;
import com.ydd.yanshi.bean.PayBanBean;
import com.ydd.yanshi.bean.redpacket.Balance;
import com.ydd.yanshi.helper.DialogHelper;
import com.ydd.yanshi.ui.base.BaseActivity;
import com.ydd.yanshi.ui.tool.WebViewActivity;
import com.ydd.yanshi.util.Constants;
import com.ydd.yanshi.util.EventBusHelper;
import com.ydd.yanshi.util.ToastUtil;
import com.ydd.yanshi.view.TipDialog;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.JsonCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * Created by phy on 2020/1/7
 */
public class RechargeActivity extends BaseActivity implements Handler.Callback,
        Runnable{
    private IWXAPI api;
    private LinearLayout lin_root;
    private RelativeLayout rel_top;
    private LinearLayout lin_root_bottom;
    private EditText mMentionMoneyEdit;
    private TextView recharge_defult;
    private Button btn_50;
    private Button btn_100;
    private Button btn_200;
    private Button btn_500;
    private Button btn_1000;
    private Button btn_2000;
    private LinearLayout lin_alipay1;
    private LinearLayout lin_alipay2;
    private LinearLayout lin_weixin;

    private ProgressDialog mLoadingDialog = null;

    private List<Button> btns = new ArrayList<>();
    private ImageView cb_alipay1, cb_alipay2, cb_weixin;
    private static final int ALI_PAY_1 = 0;
    private static final int ALI_PAY_2 = 1;
    private static final int WEIXIN_PAY = 2;
    private int mPayType = 0;//默认

    public static final String LOG_TAG = "PayDemo";
    private Handler mHandler = null;

    /*****************************************************************
     * mMode参数解释： "00" - 启动银联正式环境 "01" - 连接银联测试环境
     *****************************************************************/
    private final String mMode = "00";

    private static final String TN_URL_01 = "http://116.62.27.230:9002/unionpay/consume";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge);
        mHandler = new Handler(this);
        api = WXAPIFactory.createWXAPI(this, Constants.VX_APP_ID, false);
        api.registerApp(Constants.VX_APP_ID);

        initActionBar();
        initData();
        initView();

        EventBusHelper.register(this);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventPaySuccess message) {
        finish();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.recharge));
    }

    private void initData() {
    }

    private void initView() {
        lin_root =  findViewById(R.id.lin_root);
        rel_top =  findViewById(R.id.rel_top);
        lin_root_bottom =  findViewById(R.id.lin_root_bottom);
        mMentionMoneyEdit = (EditText) findViewById(R.id.tixianmoney);
        recharge_defult = findViewById(R.id.recharge_defult);
        cb_alipay1 = findViewById(R.id.cb_alipay1);
        cb_alipay2 = findViewById(R.id.cb_alipay2);
        cb_weixin = findViewById(R.id.cb_weixin);
        btn_50 = findViewById(R.id.tv_50);
        btn_100 = findViewById(R.id.tv_100);
        btn_200 = findViewById(R.id.tv_200);
        btn_500 = findViewById(R.id.tv_500);
        btn_1000 = findViewById(R.id.tv_1000);
        btn_2000 = findViewById(R.id.tv_2000);
        lin_alipay1 = findViewById(R.id.lin_alipay1);
        lin_alipay2 = findViewById(R.id.lin_alipay2);
        lin_weixin = findViewById(R.id.lin_weixin);
        btns.add(btn_50);
        btns.add(btn_100);
        btns.add(btn_200);
        btns.add(btn_500);
        btns.add(btn_1000);
        btns.add(btn_2000);

        lin_root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                if (imm == null) return;
                View view = getCurrentFocus();
                if (view == null) view = new View(RechargeActivity.this);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });
        lin_root_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                if (imm == null) return;
                View view = getCurrentFocus();
                if (view == null) view = new View(RechargeActivity.this);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });
        rel_top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                if (imm == null) return;
                View view = getCurrentFocus();
                if (view == null) view = new View(RechargeActivity.this);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });

        mMentionMoneyEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // 删除开头的0，
                int end = 0;
                for (int i = 0; i < editable.length(); i++) {
                    char ch = editable.charAt(i);
                    if (ch == '0') {
                        end = i + 1;
                    } else {
                        break;
                    }
                }
                if (end > 0) {
                    editable.delete(0, end);
                    mMentionMoneyEdit.setText(editable);
                }
                String moneyStr = mMentionMoneyEdit.getText().toString();
                if (TextUtils.isEmpty(moneyStr)) {
                    recharge_defult.setBackgroundResource(R.drawable.weixin_text_yuanjiao_no);
                } else {
                    recharge_defult.setBackgroundResource(R.drawable.weixin_text_yuanjiao);
                }
            }
        });

        btn_50.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectAmountBg(btn_50);
                mMentionMoneyEdit.setText(50 + "");
            }
        });
        btn_100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectAmountBg(btn_100);
                mMentionMoneyEdit.setText(100 + "");
            }
        });
        btn_200.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectAmountBg(btn_200);
                mMentionMoneyEdit.setText(200 + "");
            }
        });
        btn_500.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectAmountBg(btn_500);
                mMentionMoneyEdit.setText(500 + "");
            }
        });
        btn_1000.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectAmountBg(btn_1000);
                mMentionMoneyEdit.setText(1000 + "");
            }
        });
        btn_2000.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectAmountBg(btn_2000);
                mMentionMoneyEdit.setText(2000 + "");
            }
        });
        findViewById(R.id.lin_alipay1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cb_alipay1.setImageResource(R.drawable.ic_muc_file_checked);
                cb_alipay2.setImageResource(R.drawable.ic_muc_file_check);
                cb_weixin.setImageResource(R.drawable.ic_muc_file_check);
                mPayType = ALI_PAY_1;
            }
        });
        lin_alipay2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cb_alipay1.setImageResource(R.drawable.ic_muc_file_check);
                cb_alipay2.setImageResource(R.drawable.ic_muc_file_checked);
                cb_weixin.setImageResource(R.drawable.ic_muc_file_check);
                mPayType = ALI_PAY_1;
            }
        });
        findViewById(R.id.lin_weixin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cb_alipay1.setImageResource(R.drawable.ic_muc_file_check);
                cb_alipay2.setImageResource(R.drawable.ic_muc_file_check);
                cb_weixin.setImageResource(R.drawable.ic_muc_file_checked);
                mPayType = WEIXIN_PAY;
            }
        });

        recharge_defult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String moneyStr = mMentionMoneyEdit.getText().toString();
                if (checkMoney(moneyStr)) {
                    switch (mPayType) {
                        case ALI_PAY_1:
                            Log.e(LOG_TAG, " " + v.getTag());
//                            mGoodsIdx = (Integer) v.getTag();

                            mLoadingDialog = ProgressDialog.show(mContext, // context
                                    "", // title
                                    "正在努力的获取tn中,请稍候...", // message
                                    true); // 进度是否是不确定的，这只和创建进度条有关

                            /*************************************************
                             * 步骤1：从网络开始,获取交易流水号即TN
                             ************************************************/
                            new Thread(RechargeActivity.this).start();
                            yizhifu(moneyStr, "1");
//                            AlipayHelper.recharge(RechargeActivity.this, coreManager, moneyStr);
                            break;
                        case ALI_PAY_2:
                          //  yizhifu(moneyStr, "alipay");
                          //  recharge(moneyStr,1);
                            break;
                        case WEIXIN_PAY:
//                            if (api.getWXAppSupportAPI() < Build.PAY_SUPPORTED_SDK_INT) {
//                                Toast.makeText(getApplicationContext(), R.string.tip_no_wechat, Toast.LENGTH_SHORT).show();
//                            } else {
//                                recharge("");
//                            }
                            yizhifu(moneyStr, "wxpay");
                            break;
                    }
                }
            }
        });

//        findViewById(R.id.chongzhi).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (api.getWXAppSupportAPI() < Build.PAY_SUPPORTED_SDK_INT) {
//                    Toast.makeText(getApplicationContext(), R.string.tip_no_wechat, Toast.LENGTH_SHORT).show();
//                } else {
//                    recharge("");
//                }
//            }
//        });
//        findViewById(R.id.chongzhifubao).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AlipayHelper.recharge(RechargeActivity.this, coreManager, "");
//            }
//        });
    }

    private void recharge(String money,int type) {// 调用服务端接口，由服务端统一下单
        DialogHelper.showDefaulteMessageProgressDialog(this);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("price", money);
        params.put("payType", type+"");// 支付方式 1.支付宝 2.微信

        HttpUtils.get().url(coreManager.getConfig().VX_RECHARGE)
                .params(params)
                .build()
                .execute(new BaseCallback<Balance>(Balance.class) {

                    @Override
                    public void onResponse(ObjectResult<Balance> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            PayReq req = new PayReq();
                            req.appId = result.getData().getAppId();
                            req.partnerId = result.getData().getPartnerId();
                            req.prepayId = result.getData().getPrepayId();
                            req.packageValue = "Sign=WXPay";
                            req.nonceStr = result.getData().getNonceStr();
                            req.timeStamp = result.getData().getTimeStamp();
                            req.sign = result.getData().getSign();
                            api.sendReq(req);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(RechargeActivity.this);
                    }
                });
    }

    //(alipay:支付宝,tenpay:财付通,qqpay:QQ钱包,wxpay:微信支付)
    private void yizhifu(String money, String payType) {// 调用服务端接口，由服务端统一下单
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("money", money);
        params.put("payType", payType);// 支付方式 1.支付宝 2.微信

        HttpUtils.get().url(coreManager.getConfig().YIZHIFU_RECHARGE)
                .params(params)
                .build()
                .execute(new JsonCallback() {

                    @Override
                    public void onResponse(String result) {
                        DialogHelper.dismissProgressDialog();
                        String url = "";
                        String resultMsg = "";
                        try {
                            JSONObject jObject = JSON.parseObject(result);
                            url = jObject.getString("data");
                            resultMsg = jObject.getString("resultMsg");
                            if(url != null){
                                WebViewActivity.start(mContext, url);
                            }else {
                                final TipDialog tipDialog = new TipDialog(RechargeActivity.this);
                                tipDialog.setmConfirmOnClickListener(resultMsg, new TipDialog.ConfirmOnClickListener() {
                                    @Override
                                    public void confirm() {
                                        tipDialog.dismiss();
                                    }
                                });
                                tipDialog.show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(RechargeActivity.this);
                    }
                });
    }

    private boolean checkMoney(String moneyStr) {
        if (TextUtils.isEmpty(moneyStr)) {
            DialogHelper.tip(this, "请输入充值金额");
        } else {
            if (Double.valueOf(moneyStr) < 1) {
                DialogHelper.tip(this, "充值金额至少为1元");
            } else {// 获取用户code
                return true;
            }
        }
        return false;
    }

    private void setSelectAmountBg(Button btn) {
        for (int i = 0; i < btns.size(); i++) {
            if (btn == btns.get(i)) {
                btns.get(i).setBackgroundResource(R.drawable.bg_select_recharge_amount);
                btns.get(i).setTextColor(Color.WHITE);
            } else {
                btns.get(i).setBackgroundResource(R.drawable.bg_select_amount);
                btns.get(i).setTextColor(Color.BLACK);
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        Log.e(LOG_TAG, "shuju==" + msg.obj);
        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        String tn = "";
        if (msg.obj == null || ((String) msg.obj).length() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("错误提示");
            builder.setMessage("网络连接失败,请重试!");
            builder.setNegativeButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.create().show();
        } else {
            tn = (String) msg.obj;

            /*************************************************
             * 步骤2：通过银联工具类启动支付插件
             ************************************************/
            doStartUnionPayPlugin(RechargeActivity.this, tn, mMode);
        }
        return false;
    }

    public void doStartUnionPayPlugin(Activity activity, String tn, String mode) {
        UPPayAssistEx.startPay(activity, null, null, tn, mode);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*************************************************
         * 步骤3：处理银联手机支付控件返回的支付结果
         ************************************************/
        if (data == null) {
            return;
        }

        String msg = "";
        /*
         * 支付控件返回字符串:success、fail、cancel 分别代表支付成功，支付失败，支付取消
         */
        String str = data.getExtras().getString("pay_result");
        if (str.equalsIgnoreCase("success")) {

            // 如果想对结果数据验签，可使用下面这段代码，但建议不验签，直接去商户后台查询交易结果
            // result_data结构见c）result_data参数说明
            if (data.hasExtra("result_data")) {
                String result = data.getExtras().getString("result_data");
                try {
                    org.json.JSONObject resultJson = new org.json.JSONObject(result);
                    String sign = resultJson.getString("sign");
                    String dataOrg = resultJson.getString("data");
                    // 此处的verify建议送去商户后台做验签
                    // 如要放在手机端验，则代码必须支持更新证书
//                    boolean ret = verify(dataOrg, sign, mMode);
//                    if (ret) {
//                        // 验签成功，显示支付结果
//                        msg = "支付成功！";
//                    } else {
//                        // 验签失败
//                        msg = "支付失败！";
//                    }
                } catch (JSONException e) {
                }
            }
            // 结果result_data为成功时，去商户后台查询一下再展示成功
            msg = "支付成功！";
        } else if (str.equalsIgnoreCase("fail")) {
            msg = "支付失败！";
        } else if (str.equalsIgnoreCase("cancel")) {
            msg = "用户取消了支付";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("支付结果通知");
        builder.setMessage(msg);
        builder.setInverseBackgroundForced(true);
        // builder.setCustomTitle();
        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }



    @Override
    public void run() {
//        String tn = null;
//        InputStream is;
//        try {
//
//            String url = TN_URL_01;
//
//            URL myURL = new URL(url);
//            URLConnection ucon = myURL.openConnection();
//            ucon.setConnectTimeout(120000);
//            is = ucon.getInputStream();
//            int i = -1;
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            while ((i = is.read()) != -1) {
//                baos.write(i);
//            }
//
//            tn = baos.toString();
//            is.close();
//            baos.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }




//        "2",coreManager.getSelfStatus().accessToken
        String money = mMentionMoneyEdit.getText().toString().trim();
       String mLoginUserId = coreManager.getSelf().getUserId();
       Log.e("tag","mLoginUserId==="+mLoginUserId);
        retrofit2.Call<PayBanBean> call = HttpHelper.getApiService().sendzhifu( money,mLoginUserId);
        call.enqueue(new ApiCallBack<PayBanBean>() {
            @Override
            public void onSuccess(PayBanBean result) {
                String tn = null;
                Log.e(LOG_TAG, "result=====" + result.getData().getTn());
//                if (result.getData().getCode() == 100){
////                    sendRedtwo(result.getData().getData().getUser_token(),result.getData().getData().getType()+"",result.getData().getData().getMoney()+"",
////                            result.getData().getData().getCount()+"",result.getData().getData().getGreetings()
////                                       ,result.getData().getData().getPassword(), String.valueOf(result.getData().getData().getUserId()),result.getData().getData().getUserName());
//                }
                tn = result.getData().getTn();
                Message msg = mHandler.obtainMessage();
                msg.obj = tn;
//                Log.e(LOG_TAG, "tn132==" + result);
                mHandler.sendMessage(msg);
            }

            @Override
            public void onFail(int code, String msg) {
                super.onFail(code, msg);
                Toast.makeText(RechargeActivity.this,msg, Toast.LENGTH_SHORT).show();
            }
        });

//        Message msg = mHandler.obtainMessage();
//                msg.obj = tn;
//                Log.e(LOG_TAG, "tn132==" + tn);
//                mHandler.sendMessage(msg);

    }




    private boolean verify(String msg, String sign64, String mode) {
        // 此处的verify，商户需送去商户后台做验签
        return true;

    }


}

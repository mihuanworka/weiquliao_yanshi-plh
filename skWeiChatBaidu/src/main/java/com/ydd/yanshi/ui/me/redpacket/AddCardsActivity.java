package com.ydd.yanshi.ui.me.redpacket;

import android.content.Intent;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.ydd.yanshi.MyApplication;
import com.ydd.yanshi.R;
import com.ydd.yanshi.bean.User;
import com.ydd.yanshi.bean.redpacket.SelectWindowModel;
import com.ydd.yanshi.broadcast.OtherBroadcast;
import com.ydd.yanshi.db.dao.UserDao;
import com.ydd.yanshi.helper.DialogHelper;
import com.ydd.yanshi.ui.base.BaseActivity;
import com.ydd.yanshi.util.ToastUtil;
import com.ydd.yanshi.view.SelectBankPop;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.ydd.yanshi.AppConstant.ZHONG_GUO_GONGSHANG_YINHANG;
import static com.ydd.yanshi.AppConstant.ZHONG_GUO_JIANSHE_YINHANG;
import static com.ydd.yanshi.AppConstant.ZHONG_GUO_JIAOTONG_YINHANG;
import static com.ydd.yanshi.AppConstant.ZHONG_GUO_NONGYE_YINHANG;
import static com.ydd.yanshi.AppConstant.ZHONG_GUO_YINHANG;
import static com.ydd.yanshi.AppConstant.ZHONG_GUO_YOUZHENG_YINHANG;
import static com.ydd.yanshi.AppConstant.ZHONG_GUO_YOUZHENG_beijing;
import static com.ydd.yanshi.AppConstant.ZHONG_GUO_YOUZHENG_bohai;
import static com.ydd.yanshi.AppConstant.ZHONG_GUO_YOUZHENG_guangfa;
import static com.ydd.yanshi.AppConstant.ZHONG_GUO_YOUZHENG_hengfeng;
import static com.ydd.yanshi.AppConstant.ZHONG_GUO_YOUZHENG_huaxia;
import static com.ydd.yanshi.AppConstant.ZHONG_GUO_YOUZHENG_shanghai;
import static com.ydd.yanshi.AppConstant.ZHONG_GUO_YOUZHENG_wenzhou;
import static com.ydd.yanshi.AppConstant.ZHONG_GUO_YOUZHENG_xingye;
import static com.ydd.yanshi.AppConstant.ZHONG_GUO_YOUZHENG_zhaoshang;
import static com.ydd.yanshi.AppConstant.ZHONG_GUO_YOUZHENG_zheshang;
import static com.ydd.yanshi.AppConstant.ZHONG_GUO_YOUZHENG_zhongguoguangda;
import static com.ydd.yanshi.AppConstant.ZHONG_GUO_YOUZHENG_zhongguomingsheng;
import static com.ydd.yanshi.AppConstant.ZHONG_GUO_YOUZHENG_zhongxin;
import static java.lang.Integer.parseInt;

public class AddCardsActivity extends BaseActivity {

    private Button btn_bind;
    private TextView tv_bank;
    private EditText input_name, input_card_num, input_city, input_userIDCard, input_phone;
    private LinearLayout select_bank;
    private SelectBankPop popBank;
    private ArrayList bankList = new ArrayList<SelectWindowModel>();
    private SelectWindowModel selectWindowModel = null;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cards);

        initActionBar();
        initView();
        initData();
    }

    private void initData() {
        initBanks();
    }

    private void initView() {
        select_bank = findViewById(R.id.select_bank);
        btn_bind = findViewById(R.id.btn_bind);
        tv_bank = findViewById(R.id.tv_bank);
        input_name = findViewById(R.id.input_name);
        input_card_num = findViewById(R.id.input_card_num);
        input_userIDCard = findViewById(R.id.input_userIDCard);
        input_phone = findViewById(R.id.input_phone);
        input_city = findViewById(R.id.input_city);
        popBank = new SelectBankPop(this, bankList);

        select_bank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBank.showLocation(btn_bind);
            }
        });

        popBank.setOnTypeSelectListaner(new SelectBankPop.OnTypeSelectListaner() {
            @Override
            public void typeSelect(SelectWindowModel item) {
                selectWindowModel = item;
                tv_bank.setText(item.name);
            }
        });

        btn_bind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bind();
            }
        });
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        mTvTitle.setText(R.string.addBankCard);
    }

    private void initBanks() {
//        SelectWindowModel selectWindowModel1 = new SelectWindowModel();
//        selectWindowModel1.name = "支付宝";
//        selectWindowModel1.id = ZHI_FU_BAO;
//        selectWindowModel1.icon = R.drawable.treasure;


        SelectWindowModel selectWindowModelbeijing = new SelectWindowModel();
        selectWindowModelbeijing.name = "北京银行";
        selectWindowModelbeijing.id = ZHONG_GUO_YOUZHENG_beijing;
        selectWindowModelbeijing.icon = R.drawable.ic_card_beijing;
        bankList.add(selectWindowModelbeijing);

        SelectWindowModel selectWindowModelbeihai = new SelectWindowModel();
        selectWindowModelbeihai.name = "渤海银行";
        selectWindowModelbeihai.id = ZHONG_GUO_YOUZHENG_bohai;
        selectWindowModelbeihai.icon = R.drawable.bohai;
        bankList.add(selectWindowModelbeihai);

        SelectWindowModel selectWindowModelguangfa = new SelectWindowModel();
        selectWindowModelguangfa.name = "广发银行";
        selectWindowModelguangfa.id = ZHONG_GUO_YOUZHENG_guangfa;
        selectWindowModelguangfa.icon = R.drawable.guangfa;
        bankList.add(selectWindowModelguangfa);

        SelectWindowModel selectWindowModelhengfa = new SelectWindowModel();
        selectWindowModelhengfa.name = "恒丰银行";
        selectWindowModelhengfa.id = ZHONG_GUO_YOUZHENG_hengfeng;
        selectWindowModelhengfa.icon = R.drawable.hengfeng;
        bankList.add(selectWindowModelhengfa);

        SelectWindowModel selectWindowModelhuaxia = new SelectWindowModel();
        selectWindowModelhuaxia.name = "华夏银行";
        selectWindowModelhuaxia.id = ZHONG_GUO_YOUZHENG_huaxia;
        selectWindowModelhuaxia.icon = R.drawable.huaxia;
        bankList.add(selectWindowModelhuaxia);

        SelectWindowModel selectWindowModelshanghai = new SelectWindowModel();
        selectWindowModelshanghai.name = "上海银行";
        selectWindowModelshanghai.id = ZHONG_GUO_YOUZHENG_shanghai;
        selectWindowModelshanghai.icon = R.drawable.shanghai;
        bankList.add(selectWindowModelshanghai);

        SelectWindowModel selectWindowModelwenzhou = new SelectWindowModel();
        selectWindowModelwenzhou.name = "温州银行";
        selectWindowModelwenzhou.id = ZHONG_GUO_YOUZHENG_wenzhou;
        selectWindowModelwenzhou.icon = R.drawable.wenzhou;
        bankList.add(selectWindowModelwenzhou);

        SelectWindowModel selectWindowModelxingye = new SelectWindowModel();
        selectWindowModelxingye.name = "兴业银行";
        selectWindowModelxingye.id = ZHONG_GUO_YOUZHENG_xingye;
        selectWindowModelxingye.icon = R.drawable.xingye;
        bankList.add(selectWindowModelxingye);

        SelectWindowModel selectWindowModelzhaoshang = new SelectWindowModel();
        selectWindowModelzhaoshang.name = "招商银行";
        selectWindowModelzhaoshang.id = ZHONG_GUO_YOUZHENG_zhaoshang;
        selectWindowModelzhaoshang.icon = R.drawable.zhaoshang;
        bankList.add(selectWindowModelzhaoshang);

        SelectWindowModel selectWindowModelzheshang = new SelectWindowModel();
        selectWindowModelzheshang.name = "浙商银行";
        selectWindowModelzheshang.id = ZHONG_GUO_YOUZHENG_zheshang;
        selectWindowModelzheshang.icon = R.drawable.zheshang;
        bankList.add(selectWindowModelzheshang);

        SelectWindowModel selectWindowModelzhongguoguangda = new SelectWindowModel();
        selectWindowModelzhongguoguangda.name = "中国光大银行";
        selectWindowModelzhongguoguangda.id = ZHONG_GUO_YOUZHENG_zhongguoguangda;
        selectWindowModelzhongguoguangda.icon = R.drawable.zhongguoguangda;
        bankList.add(selectWindowModelzhongguoguangda);

        SelectWindowModel selectWindowModelzhongguomingsheng = new SelectWindowModel();
        selectWindowModelzhongguomingsheng.name = "中国民生银行";
        selectWindowModelzhongguomingsheng.id = ZHONG_GUO_YOUZHENG_zhongguomingsheng;
        selectWindowModelzhongguomingsheng.icon = R.drawable.zhongguomingsheng;
        bankList.add(selectWindowModelzhongguomingsheng);

        SelectWindowModel selectWindowModelzhongxin = new SelectWindowModel();
        selectWindowModelzhongxin.name = "中信银行";
        selectWindowModelzhongxin.id = ZHONG_GUO_YOUZHENG_zhongxin;
        selectWindowModelzhongxin.icon = R.drawable.zhongxing;
        bankList.add(selectWindowModelzhongxin);




        SelectWindowModel selectWindowModel2 = new SelectWindowModel();
        selectWindowModel2.name = "中国银行";
        selectWindowModel2.id = ZHONG_GUO_YINHANG;
        selectWindowModel2.icon = R.drawable.ic_card_boc;

        SelectWindowModel selectWindowModel3 = new SelectWindowModel();
        selectWindowModel3.name = "中国建设银行";
        selectWindowModel3.id = ZHONG_GUO_JIANSHE_YINHANG;
        selectWindowModel3.icon = R.drawable.ic_card_ccb;

        SelectWindowModel selectWindowModel4 = new SelectWindowModel();
        selectWindowModel4.name = "中国工商银行";
        selectWindowModel4.id = ZHONG_GUO_GONGSHANG_YINHANG;
        selectWindowModel4.icon = R.drawable.ic_card_icbc;

        SelectWindowModel selectWindowModel5 = new SelectWindowModel();
        selectWindowModel5.name = "中国农业银行";
        selectWindowModel5.id = ZHONG_GUO_NONGYE_YINHANG;
        selectWindowModel5.icon = R.drawable.ic_card_abc;

        SelectWindowModel selectWindowModel6 = new SelectWindowModel();
        selectWindowModel6.name = "中国交通银行";
        selectWindowModel6.id = ZHONG_GUO_JIAOTONG_YINHANG;
        selectWindowModel6.icon = R.drawable.ic_card_comm;

        SelectWindowModel selectWindowModel7 = new SelectWindowModel();
        selectWindowModel7.name = "中国邮政银行";
        selectWindowModel7.id = ZHONG_GUO_YOUZHENG_YINHANG;
        selectWindowModel7.icon = R.drawable.ic_card_psbc;

//        bankList.add(selectWindowModel1);
        bankList.add(selectWindowModel2);
        bankList.add(selectWindowModel3);
        bankList.add(selectWindowModel4);
        bankList.add(selectWindowModel5);
        bankList.add(selectWindowModel6);
        bankList.add(selectWindowModel7);
    }

    private void bind() {

        String userName = input_name.getText().toString();
        String cardNum = input_card_num.getText().toString();
        String openBankAddr = input_city.getText().toString();
        String phone = input_phone.getText().toString();
        String idcard = input_userIDCard.getText().toString();

        if (userName == null || TextUtils.isEmpty(userName)) {
            ToastUtil.showLongToast(this, "持卡人不能为空");
        } else if (phone == null || TextUtils.isEmpty(phone) || phone.length() < 0) {
            ToastUtil.showLongToast(this, "手机号不能为空");
        } else if (idcard == null || TextUtils.isEmpty(idcard) || idcard.length() < 0) {
            ToastUtil.showLongToast(this, "身份证号不能为空");
        }  else if (!checkIdentity(idcard)) {
            ToastUtil.showLongToast(this, "身份证号不正确");
        } else if (cardNum == null || TextUtils.isEmpty(cardNum) || cardNum.length() < 0) {
            ToastUtil.showLongToast(this, "卡号不能为空");
        } else if (cardNum.length() > 30) {
            ToastUtil.showLongToast(this, "卡号不能大于30位");
        } else if (selectWindowModel == null) {
            ToastUtil.showLongToast(this, "请选择银行类型");
        } else {
            yan4();
        }
    }
    /**
     * 身份证号码验证
     */
    public  boolean checkIdentity(String num) {


        String idcard = num.trim().replace(" ", "");

        /*"身份证验证通过!",
        "0身份证号码位数不对!",
		"0身份证号码出生日期超出范围或含有非法字符!",
		"0身份证号码校验错误!",
		"0身份证地区非法!"*/

        boolean iscd = false;

        boolean[] Errors = new boolean[]{true, false, false, false, false};


        //        String[] area = new String[]{11:"北京", 12:"天津", 13:"河北", 14:"山西", 15:"内蒙古", 21:"辽宁", 22:"吉林",
        // 23:"黑龙江", 31:
        //        "上海", 32:"江苏", 33:
        //        "浙江", 34:"安徽", 35:"福建", 36:"江西", 37:"山东", 41:"河南", 42:"湖北", 43:"湖南", 44:"广东", 45:"广西", 46:"海南",
        // 50:"重庆", 51:
        //        "四川", 52:"贵州", 53:"云南", 54:"西藏", 61:"陕西", 62:"甘肃", 63:"青海", 64:"宁夏", 65:"新疆", 71:"台湾", 81:"香港",
        // 82:"澳门", 91:
        //        "国外"};
        String area = "11, 12, 13, 14, 15, 21, 22, 23, 31, 32, 33, 34, 35, 36, 37, 41, 42, 43, 44, 45, 46, 50, 51, " + "52, 53, 54, 61, 62, " + "63, 64, 65, 71, 81, " + "82, 91";
        int Y;
        String JYM;
        int S;
        String M;
        String[] idcard_array = new String[]{};
        idcard_array = idcard.split("");

        String ereg = "";


        //        Pattern p = Pattern.compile("^[\\@a-z0-9\\!\\#\\$\\%\\^\\&\\*\\.\\~]{6,16}$");
        //地区检验

        if (idcard.length() >= 3) {
            if (!area.contains(idcard.substring(0, 2))) {
                iscd = Errors[4];
                return iscd;
            }

            //身份号码位数及格式检验
            switch (idcard.length()) {
                case 15:
                    if ((parseInt(idcard.substring(6, 8)) + 1900) % 4 == 0 || ((parseInt(idcard.substring(6, 8)) + 1900) % 100 == 0 && (parseInt(idcard.substring(6, 8)) + 1900) % 4 == 0)) {
                        ereg = "/^[1-9][0-9]{5}[0-9]{2}((01|03|05|07|08|10|12)(0[1-9]|[1-2][0-9]|3[0-1])|" + "(04|06|09|11)" + "(0[1-9]|[1-2][0-9]|30)|02" + "(0[1-9]|[1-2][0-9]))[0-9]{3}$/";//测试出生日期的合法性
                    } else {
                        ereg = "/^[1-9][0-9]{5}[0-9]{2}((01|03|05|07|08|10|12)(0[1-9]|[1-2][0-9]|3[0-1])|" + "(04|06|09|11)" + "(0[1-9]|[1-2][0-9]|30)|02" + "(0[1-9]|1[0-9]|2[0-8]))[0-9]{3}$/";//测试出生日期的合法性
                    }
                    Pattern p = Pattern.compile(ereg);
                    Matcher m = p.matcher(idcard);
                    if (m.matches()) {
                        iscd = Errors[0];
                    } else {
                        iscd = Errors[2];
                    }
                    break;

                case 18:
                    //18位身份号码检测
                    //出生日期的合法性检查
                    //闰年月日:((01|03|05|07|08|10|12)(0[1-9]|[1-2][0-9]|3[0-1])|(04|06|09|11)(0[1-9]|[1-2][0-9]|30)|02
                    // (0[1-9]|[1-2][0-9]))
                    //平年月日:((01|03|05|07|08|10|12)(0[1-9]|[1-2][0-9]|3[0-1])|(04|06|09|11)(0[1-9]|[1-2][0-9]|30)|02
                    // (0[1-9]|1[0-9]|2[0-8]))
                    if (parseInt(idcard.substring(6, 10)) % 4 == 0 || (parseInt(idcard.substring(6, 10)) % 100 == 0 && parseInt(idcard.substring(6, 10)) % 4 == 0)) {
                        ereg = "^[1-9][0-9]{5}19[0-9]{2}((01|03|05|07|08|10|12)(0[1-9]|[1-2][0-9]|3[0-1])|" + "(04|06|09|11)" + "(0[1-9]|[1-2][0-9]|30)|02" + "(0[1-9]|[1-2][0-9]))[0-9]{3}[0-9X]$";//闰年出生日期的合法性正则表达式
                    } else {
                        ereg = "^[1-9][0-9]{5}19[0-9]{2}((01|03|05|07|08|10|12)(0[1-9]|[1-2][0-9]|3[0-1])|" + "(04|06|09|11)" + "(0[1-9]|[1-2][0-9]|30)|02" + "(0[1-9]|1[0-9]|2[0-8]))[0-9]{3}[0-9X]$";//平年出生日期的合法性正则表达式
                    }
                    Pattern p1 = Pattern.compile(ereg);
                    Matcher m1 = p1.matcher(idcard);

//                    if (m1.matches()) {//测试出生日期的合法性
                    //计算校验位
                    //                    S = (parseInt(idcard_array[10])) * 7 + (parseInt(idcard_array[1]) +
                    // parseInt
                    // (idcard_array[11])) * 9 + (parseInt
                    // (idcard_array[2]) + parseInt(idcard_array[12])) * 10 + (parseInt(idcard_array[3]) +
                    // parseInt(idcard_array[13])
                    // ) * 5 + (parseInt
                    // (idcard_array[4]) + parseInt(idcard_array[14])) * 8 + (parseInt(idcard_array[5]) +
                    // parseInt(idcard_array[15]))
                    // * 4 + (parseInt
                    // (idcard_array[6]) + parseInt(idcard_array[16])) * 2 + parseInt(idcard_array[7]) * 1 +
                    // parseInt
                    // (idcard_array[8]) * 6 + parseInt
                    // (idcard_array[9]) * 3;
                    S = (parseInt(idcard_array[1]) + parseInt(idcard_array[11])) * 7 + (parseInt(idcard_array[2]) + parseInt(idcard_array[12])) * 9 + (parseInt(idcard_array[3]) + parseInt(idcard_array[13])) * 10 + (parseInt(idcard_array[4]) + parseInt(idcard_array[14])) * 5 + (parseInt(idcard_array[5]) + parseInt(idcard_array[15])) * 8 + (parseInt(idcard_array[6]) + parseInt(idcard_array[16])) * 4 + (parseInt(idcard_array[7]) + parseInt(idcard_array[17])) * 2 + parseInt(idcard_array[8]) * 1 + parseInt(idcard_array[9]) * 6 + parseInt(idcard_array[10]) * 3;
                    Y = S % 11;
                    JYM = "10X98765432";
                    M = JYM.substring(Y, Y + 1);//判断校验位

                    if (TextUtils.equals(M, idcard_array[18])) {
                        iscd = Errors[0]; //检测ID的校验位
                    } else {
                        iscd = Errors[3];
                    }
//                    } else {
//                        iscd = Errors[2];
//                    }
                    break;
                default:
                    iscd = Errors[1];
                    break;
            }
        }

        return iscd;
    }
    private void bind2() {

        String userName = input_name.getText().toString();
        String cardNum = input_card_num.getText().toString();
        String openBankAddr = input_city.getText().toString();

        if (userName == null || TextUtils.isEmpty(userName)) {
            ToastUtil.showLongToast(this, "持卡人不能为空");
        } else if (cardNum == null || TextUtils.isEmpty(cardNum) || cardNum.length() < 0) {
            ToastUtil.showLongToast(this, "卡号不能为空");
        } else if (cardNum.length() > 30) {
            ToastUtil.showLongToast(this, "卡号不能大于30位");
        } else if (selectWindowModel == null) {
            ToastUtil.showLongToast(this, "请选择银行类型");
        } else {
            if (openBankAddr == null || TextUtils.isEmpty(openBankAddr)) {
                openBankAddr = "未填写";
            }
            int brandId = selectWindowModel.id;
            String bankName = selectWindowModel.name;
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("access_token", coreManager.getSelfStatus().accessToken);
            params.put("cardNo", cardNum);
            params.put("userName", userName);

            params.put("openBankAddr", openBankAddr);
            params.put("bankBrandId", 100 + "");

//            params.put("cardName", "");
//            params.put("brandName", bankName);
//            params.put("cardType", "0");
//
//            params.put("uid", coreManager.getSelf().getUserId());

            HttpUtils.post().url(coreManager.getConfig().BIND_CARD)
                    .params(params)
                    .build()
                    .execute(new BaseCallback<Void>(Void.class) {
                        @Override
                        public void onResponse(ObjectResult<Void> result) {
                            DialogHelper.dismissProgressDialog();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    Map<String, String> params = new HashMap<>();
                                    params.put("access_token", coreManager.getSelfStatus().accessToken);

                                    HttpUtils.get().url(coreManager.getConfig().USER_GET_URL)
                                            .params(params)
                                            .build()
                                            .execute(new BaseCallback<User>(User.class) {
                                                @Override
                                                public void onResponse(ObjectResult<User> result) {
                                                    if (result.getResultCode() == 1 && result.getData() != null) {
                                                        User user = result.getData();
                                                        boolean updateSuccess = UserDao.getInstance().updateByUser(user);
                                                        // 设置登陆用户信息
                                                        if (updateSuccess) {
                                                            // 如果成功，保存User变量，
                                                            coreManager.setSelf(user);
                                                            // 通知MeFragment更新
                                                            LocalBroadcastManager.getInstance(MyApplication.getInstance()).sendBroadcast(new Intent(OtherBroadcast.SYNC_SELF_DATE_NOTIFY));
                                                            ToastUtil.showToast(AddCardsActivity.this, result.getResultMsg());
                                                            finish();
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onError(Call call, Exception e) {
                                                    e.printStackTrace();
                                                }
                                            });


                                }
                            });
                        }

                        @Override
                        public void onError(Call call, Exception e) {
                            DialogHelper.dismissProgressDialog();
                            ToastUtil.showErrorNet(AddCardsActivity.this);
                        }
                    });
        }
    }

    public void yan4() {
        try {
            String url = "https://bankcard4c.shumaidata.com/bankcard4c";
            String appCode = "a23abca5d8c4466ca8fff9f90568476b";
            String userName = input_name.getText().toString();
            String cardNum = input_card_num.getText().toString();
            String phone = input_phone.getText().toString();
            String idcard = input_userIDCard.getText().toString();
            String openBankAddr = input_city.getText().toString();
            Map<String, String> params = new HashMap<>();
            params.put("idcard",  idcard);
            params.put("name", userName);
            params.put("bankcard", cardNum);
            params.put("mobile", phone);
            String result = get(appCode, url, params);

//            {
//                "msg": "",
//                    "success": true,
//                    "code": 200,
//                    "data": {
//                "order_no": "5727511255188800987",//订单号
//                        "result": 0,//0 一致，1 不一致，2未认证，3 已注销
//                        "msg": "一致",//验证结果
//                        "desc": "认证信息匹配", //验证结果描述信息
//            }
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String get(String appCode, String url, Map<String, String> params) throws IOException {
        url = url + buildRequestUrl(params);
//        OkHttpClient client = new OkHttpClient.Builder().build();
//        Request request = new Request.Builder().url(url).addHeader("Authorization", "APPCODE " + appCode).build();
//        Response response = client.newCall(request).execute();
//        System.out.println("返回状态码" + response.code() + ",message:" + response.message());
//        String result = response.body().string();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().addHeader("Authorization", "APPCODE " + "a23abca5d8c4466ca8fff9f90568476b")
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String result=    response.body().string();

                            JSONObject jsonObject = new JSONObject(result);
                            int code = jsonObject.getInt("code");
                            if (code == 200) {
                                if (jsonObject.getJSONObject("data").getInt("result") == 0) {
                                    bind2();
                                } else {
                                    ToastUtil.showLongToast(AddCardsActivity.this, jsonObject.getJSONObject("data").getString("desc"));
                                }
                            } else {
                                ToastUtil.showLongToast(AddCardsActivity.this, jsonObject.getString("msg"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtil.showLongToast(AddCardsActivity.this, "网络错误");
                        }
                    }
                });
            }
        });




        return "";
    }

    public String buildRequestUrl(Map<String, String> params) {
        StringBuilder url = new StringBuilder("?");
        Iterator<String> it = params.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            url.append(key).append("=").append(params.get(key)).append("&");
        }
        return url.toString().substring(0, url.length() - 1);

    }
}

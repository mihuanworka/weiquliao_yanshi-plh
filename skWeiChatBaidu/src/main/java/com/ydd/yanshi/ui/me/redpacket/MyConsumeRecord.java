package com.ydd.yanshi.ui.me.redpacket;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.ydd.yanshi.R;
import com.ydd.yanshi.bean.redpacket.ConsumeRecordItem;
import com.ydd.yanshi.db.InternationalizationHelper;
import com.ydd.yanshi.helper.DialogHelper;
import com.ydd.yanshi.ui.base.BaseListActivity;
import com.ydd.yanshi.ui.mucfile.XfileUtils;
import com.ydd.yanshi.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

/**
 * Created by wzw on 2016/9/26.
 */
public class MyConsumeRecord extends BaseListActivity<MyConsumeRecord.MyConsumeHolder> {
    private static final String TAG = "MyConsumeRecord";
    List<ConsumeRecordItem.PageDataEntity> datas = new ArrayList<>();

    @Override
    public void initView() {
        super.initView();
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
    }

    @Override
    public void initDatas(int pager) {
        if (pager == 0) {
            datas.clear();
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        // 如果是下拉刷新就重新加载第一页
        params.put("pageIndex", pager + "");
        params.put("pageSize", "30");
        HttpUtils.get().url(coreManager.getConfig().CONSUMERECORD_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<ConsumeRecordItem>(ConsumeRecordItem.class) {

                    @Override
                    public void onResponse(ObjectResult<ConsumeRecordItem> result) {
                        if (result.getData().getPageData() != null) {
                            for (ConsumeRecordItem.PageDataEntity data : result.getData().getPageData()) {

//                                boolean isZero = Double.toString(money).equals("0.0");

//                                if (!isZero) {
                                    datas.add(data);
//                                }
                            }
                            if (result.getData().getPageData().size() != 30) {
                                more = false;
                            } else {
                                more = true;
                            }
                        } else {
                            more = false;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                update(datas);
                            }
                        });
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(MyConsumeRecord.this);
                    }
                });
    }

    @Override
    public MyConsumeHolder initHolder(ViewGroup parent) {
        View v = mInflater.inflate(R.layout.consumerecord_item, parent, false);
        MyConsumeHolder holder = new MyConsumeHolder(v);
        return holder;
    }

    @Override
    public void fillData(MyConsumeHolder holder, int position) {
        ConsumeRecordItem.PageDataEntity info = datas.get(position);
        long time = Long.valueOf(info.getTime());
        String StrTime = XfileUtils.fromatTime(time * 1000, "MM-dd HH:mm");
        holder.nameTv.setText(info.getDesc());
        holder.timeTv.setText(StrTime);
        final double operationAmount = info.getOperationAmount();
        double money = info.getMoney();

        holder.moneyTv.setText(XfileUtils.fromatFloat(operationAmount == 0 ? money : operationAmount) + InternationalizationHelper.getString("YUAN"));
    }

    public class MyConsumeHolder extends RecyclerView.ViewHolder {
        public TextView nameTv, timeTv, moneyTv;

        public MyConsumeHolder(View itemView) {
            super(itemView);
            nameTv = (TextView) itemView.findViewById(R.id.textview_name);
            timeTv = (TextView) itemView.findViewById(R.id.textview_time);
            moneyTv = (TextView) itemView.findViewById(R.id.textview_money);
        }
    }
}

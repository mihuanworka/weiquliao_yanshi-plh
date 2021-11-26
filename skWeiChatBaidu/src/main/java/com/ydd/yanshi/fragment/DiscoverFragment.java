package com.ydd.yanshi.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.qrcode.Constant;
import com.example.qrcode.ScannerActivity;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;
import com.ydd.yanshi.R;
import com.ydd.yanshi.adapter.FindItemsAdapter;
import com.ydd.yanshi.bean.circle.FindItem;
import com.ydd.yanshi.helper.DialogHelper;
import com.ydd.yanshi.ui.WebActivity;
import com.ydd.yanshi.ui.base.EasyFragment;
import com.ydd.yanshi.ui.circle.DiscoverActivity;
import com.ydd.yanshi.ui.me.NearPersonActivity;
import com.ydd.yanshi.util.DisplayUtil;
import com.ydd.yanshi.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * 朋友圈的Fragment
 * Created by Administrator
 */

public class DiscoverFragment extends EasyFragment {

    private TextView mTvTitle;
    private RelativeLayout rel_find;
    private RelativeLayout scanning;
    private RelativeLayout Honey;
    private RelativeLayout near_person;
    private SwipeRecyclerView mRecyclerView;
    private FindItemsAdapter mAdapter;
    private ArrayList<FindItem> findItems = new ArrayList();

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_discover;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        initActionBar();
        initViews();
        initData();
    }

    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initActionBar() {
//        if (coreManager.getConfig().newUi) {
//            findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    requireActivity().finish();
//                }
//            });
//        } else {
//            findViewById(R.id.iv_title_left).setVisibility(View.GONE);
//        }
        mTvTitle = ((TextView) findViewById(R.id.tv_title_left));
//        mTvTitle.setText(getString(R.string.find));
    }

    public void initViews() {
        rel_find = findViewById(R.id.rel_find);
        scanning = findViewById(R.id.scanning);
        Honey = findViewById(R.id.Honey);
        near_person = findViewById(R.id.near_person);
        mRecyclerView = findViewById(R.id.rec_more);
        mAdapter = new FindItemsAdapter(R.layout.item_find, getContext(), findItems);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);

        rel_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DiscoverActivity.start(getActivity());
            }
        });
        Honey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             startActivity(new Intent(getActivity(), WebActivity.class));
            }
        });
        scanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestQrCodeScan(getActivity());
            }
        });
        near_person.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(getActivity(), NearPersonActivity.class));
            }
        });

    }

    /**
     * 发起二维码扫描，
     * 仅供MainActivity下属Fragment调用，
     */
    public static void requestQrCodeScan(Activity ctx) {
        Intent intent = new Intent(ctx, ScannerActivity.class);
        // 设置扫码框的宽
        intent.putExtra(Constant.EXTRA_SCANNER_FRAME_WIDTH, DisplayUtil.dip2px(ctx, 250));
        // 设置扫码框的高
        intent.putExtra(Constant.EXTRA_SCANNER_FRAME_HEIGHT, DisplayUtil.dip2px(ctx, 250));
        // 设置扫码框距顶部的位置
        intent.putExtra(Constant.EXTRA_SCANNER_FRAME_TOP_PADDING, DisplayUtil.dip2px(ctx, 100));
        // 可以从相册获取
        intent.putExtra(Constant.EXTRA_IS_ENABLE_SCAN_FROM_PIC, true);
        ctx.startActivityForResult(intent, 888);
    }


    public void initData() {
        getMoreItem();
    }

    /**
     * /general/friendsterWebsiteList?secret=4ab55cbde08e797035056c6482ff246a&time=1582820035&access_token=9a2eb891c5ef4fcda27e95aa4b7ac9b8&page=1&limit=15
     */
    private void getMoreItem() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("page", 1 + "");
        params.put("limit", 15 + "");
        HttpUtils.get().url(coreManager.getConfig().FIND_MORE_ITEMS)
                .params(params)
                .build()
                .execute(new ListCallback<FindItem>(FindItem.class) {
                    @Override
                    public void onResponse(ArrayResult<FindItem> result) {
                        DialogHelper.dismissProgressDialog();
                        findItems.clear();
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            findItems.addAll(result.getData());
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(getContext());
                    }
                });
    }
}

package com.ydd.yanshi.ui.me;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.fastjson.JSON;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;
import com.ydd.yanshi.R;
import com.ydd.yanshi.Reporter;
import com.ydd.yanshi.adapter.PublicMessageRecyclerAdapter;
import com.ydd.yanshi.audio_x.VoicePlayer;
import com.ydd.yanshi.bean.circle.PublicMessage;
import com.ydd.yanshi.bean.collection.CollectionEvery;
import com.ydd.yanshi.helper.DialogHelper;
import com.ydd.yanshi.ui.base.BaseActivity;
import com.ydd.yanshi.util.ToastUtil;
import com.ydd.yanshi.view.SelectionFrame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import okhttp3.Call;

/**
 * Created by Administrator on 2017/10/20 0020.
 * ζηζΆθ
 */
public class MyCollection extends BaseActivity {
    private boolean isSendCollection;

    private SmartRefreshLayout mRefreshLayout;
    private SwipeRecyclerView mPullToRefreshListView;
    private PublicMessageRecyclerAdapter mPublicMessageAdapter;
    private List<PublicMessage> mPublicMessage;
    private List<CollectionEvery> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_activity_my_collection);
        if (getIntent() != null) {
            isSendCollection = getIntent().getBooleanExtra("IS_SEND_COLLECTION", false);
        }
        mPublicMessage = new ArrayList<>();
        initActionBar();
        initView();
        getMyCollectionList();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopVoiceOrVideo();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        //  tvTitle.setText(InternationalizationHelper.getString("JX_MyCollection"));
        tvTitle.setText(R.string.collection);
    }

    @Override
    public void onBackPressed() {
        stopVoiceOrVideo();
        super.onBackPressed();
    }

    private void stopVoiceOrVideo() {
        VoicePlayer.instance().stop();
        JCVideoPlayer.releaseAllVideos();
        finish();
    }

    private void initView() {
        mRefreshLayout = findViewById(R.id.refreshLayout);
        mPullToRefreshListView = findViewById(R.id.recyclerView);
        mPullToRefreshListView.setLayoutManager(new LinearLayoutManager(this));
        mPublicMessageAdapter = new PublicMessageRecyclerAdapter(MyCollection.this, coreManager, mPublicMessage);
        if (isSendCollection) {
            mPublicMessageAdapter.setCollectionType(2);
        } else {
            mPublicMessageAdapter.setCollectionType(1);
        }

        mRefreshLayout.setOnRefreshListener(refreshLayout -> {
            getMyCollectionList();
        });

        mPullToRefreshListView.setAdapter(mPublicMessageAdapter);
        mPublicMessageAdapter.setOnItemClickListener(vh -> {
            if (isSendCollection) {
                int position = vh.getAdapterPosition();
                CollectionEvery collection = data.get(position);
                if (collection != null) {
                    SelectionFrame dialog = new SelectionFrame(MyCollection.this);
                    dialog.setSomething(null, getString(R.string.tip_confirm_send), new SelectionFrame.OnSelectionFrameClickListener() {
                        @Override
                        public void cancelClick() {
                        }

                        @Override
                        public void confirmClick() {
                            Intent intent = new Intent();
                            intent.putExtra("data", JSON.toJSONString(collection));
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    });
                    dialog.show();
                } else {
                    Reporter.unreachable();
                    ToastUtil.showToast(mContext, R.string.tip_server_error);
                }
            }
        });
    }

    /**
     * ε°urlδΌ η»ζε‘η«―οΌζε‘η«―
     */

    /**
     * 1.θ·εζΆθεθ‘¨
     * 2.ε°msgε­ζ?΅θ§£ζοΌεζList<ChatMessage>
     * 3.ε°List<ChatMessage>θ½¬ζ’δΈΊList<PublishMessage>
     */
    public void getMyCollectionList() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", coreManager.getSelf().getUserId());

        HttpUtils.get().url(coreManager.getConfig().Collection_LIST_OTHER)
                .params(params)
                .build()
                .execute(new ListCallback<CollectionEvery>(CollectionEvery.class) {
                    @Override
                    public void onResponse(ArrayResult<CollectionEvery> result) {
                        DialogHelper.dismissProgressDialog();
                        mPublicMessage.clear();
                        mRefreshLayout.finishRefresh();
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            afterGetData(result.getData());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    /**
     * εε€ͺε€§οΌδΈεΊθ―₯εζεεε±η¨adapter, ε±η¨ε?δ½η±»οΌ
     */
    public void afterGetData(List<CollectionEvery> data) {
        this.data = data;
        for (int i = 0; i < data.size(); i++) {
            CollectionEvery collection = data.get(i);
            PublicMessage publicMessage = new PublicMessage();
            publicMessage.setUserId(coreManager.getSelf().getUserId());
            publicMessage.setNickName(coreManager.getSelf().getNickName());
            // ζΎη€ΊηζΆι΄εΊθ―₯δΈΊζΆθθΏζ‘ζΆζ―ηζΆι΄οΌθδΈζ―ειθΏζ‘ζΆζ―ηζΆι΄
            publicMessage.setTime(data.get(i).getCreateTime());
            // ζ­ζΎθ―­ι³ιθ¦η¨ε°messageId,ε¦εδΈιθ¦θ?Ύη½?
            publicMessage.setMessageId(collection.getEmojiId());
            // ζδ»ΆεοΌ
            publicMessage.setFileName(collection.getFileName());
            String name = collection.getFileName();
            try {
                // ζε‘ε¨η»ηζδ»Άεε―θ½εε«θ·―εΎοΌ
                // TODO: PCη«―ε―θ½η»εζζ \, ζ²‘ζ΅οΌ
                int lastIndex = name.lastIndexOf('/');
                publicMessage.setFileName(name.substring(lastIndex + 1));
            } catch (Exception e) {
                publicMessage.setFileName(name);
            }
            // ζηζΆθδΈε±id
            publicMessage.setEmojiId(data.get(i).getEmojiId());

            PublicMessage.Body body = new PublicMessage.Body();
            // ζεεζΆθζ₯ηζζζΆζ―η±»ει½ζcollectContentζζ¬εε?Ήε­ζ?΅οΌ
            body.setText(collection.getCollectContent());
            if (collection.getType() == CollectionEvery.TYPE_TEXT) {
                // ζζ¬
                body.setType(PublicMessage.TYPE_TEXT);
                // θε€©ζΆθζ₯ηζε­ζΆζ―ζ²‘ζcollectCntentε­ζ?΅οΌ
                // θε€©εζεεζΆθζ₯ηζε­ζΆζ―ι½ζmsgε­ζ?΅οΌ
                collection.setCollectContent(collection.getMsg());
                body.setText(collection.getCollectContent());
            } else if (collection.getType() == CollectionEvery.TYPE_IMAGE) {
                // εΎη
                body.setType(PublicMessage.TYPE_IMG);
                List<PublicMessage.Resource> images = new ArrayList<>();
                String allUrl = collection.getUrl();
                if (!TextUtils.isEmpty(allUrl)) {
                    for (String url : allUrl.split(",")) {
                        PublicMessage.Resource resource = new PublicMessage.Resource();
                        resource.setOriginalUrl(url);
                        images.add(resource);
                    }
                }
                body.setImages(images);
            } else if (collection.getType() == CollectionEvery.TYPE_VOICE) {
                // θ―­ι³
                body.setType(PublicMessage.TYPE_VOICE);
                List<PublicMessage.Resource> audios = new ArrayList<>();
                PublicMessage.Resource resource = new PublicMessage.Resource();
                resource.setLength(collection.getFileLength());
                resource.setSize(collection.getFileSize());
                resource.setOriginalUrl(collection.getUrl());
                audios.add(resource);
                body.setAudios(audios);
            } else if (collection.getType() == CollectionEvery.TYPE_VIDEO) {
                // θ§ι’
                body.setType(PublicMessage.TYPE_VIDEO);
                // θ§ι’ε°ι’
                /*List<PublicMessage.Resource> images = new ArrayList<>();
                PublicMessage.Resource resource1 = new PublicMessage.Resource();
                resource1.setOriginalUrl(message.getContent());
                images.add(resource1);
                body.setImages(images);*/
                // θ§ι’ζΊ
                List<PublicMessage.Resource> videos = new ArrayList<>();
                PublicMessage.Resource resource2 = new PublicMessage.Resource();
                resource2.setOriginalUrl(collection.getUrl());
                resource2.setLength(collection.getFileLength());
                resource2.setSize(collection.getFileSize());
                videos.add(resource2);
                body.setVideos(videos);
            } else if (collection.getType() == CollectionEvery.TYPE_FILE) {
                // ζδ»Ά
                body.setType(PublicMessage.TYPE_FILE);
                List<PublicMessage.Resource> files = new ArrayList<>();
                PublicMessage.Resource resource2 = new PublicMessage.Resource();
                resource2.setOriginalUrl(collection.getUrl());
                resource2.setLength(collection.getFileLength());
                resource2.setSize(collection.getFileSize());
                files.add(resource2);
                body.setFiles(files);
            }
            publicMessage.setBody(body);
            mPublicMessage.add(publicMessage);
        }
        mPublicMessageAdapter.setData(mPublicMessage);
    }
}

package com.ydd.yanshi.ui.message;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alibaba.fastjson.JSON;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.ydd.yanshi.AppConstant;
import com.ydd.yanshi.MyApplication;
import com.ydd.yanshi.R;
import com.ydd.yanshi.Reporter;
import com.ydd.yanshi.adapter.MessageEventClickable;
import com.ydd.yanshi.adapter.MessageEventRequert;
import com.ydd.yanshi.adapter.MessageLocalVideoFile;
import com.ydd.yanshi.adapter.MessageUploadChatRecord;
import com.ydd.yanshi.adapter.MessageVideoFile;
import com.ydd.yanshi.audio_x.VoicePlayer;
import com.ydd.yanshi.bean.AgoraInfo;
import com.ydd.yanshi.bean.Contacts;
import com.ydd.yanshi.bean.EventSyncFriendOperating;
import com.ydd.yanshi.bean.EventTransfer;
import com.ydd.yanshi.bean.EventUploadCancel;
import com.ydd.yanshi.bean.EventUploadFileRate;
import com.ydd.yanshi.bean.Friend;
import com.ydd.yanshi.bean.PrivacySetting;
import com.ydd.yanshi.bean.PublicMenu;
import com.ydd.yanshi.bean.User;
import com.ydd.yanshi.bean.VideoFile;
import com.ydd.yanshi.bean.assistant.GroupAssistantDetail;
import com.ydd.yanshi.bean.collection.CollectionEvery;
import com.ydd.yanshi.bean.message.ChatMessage;
import com.ydd.yanshi.bean.message.ChatRecord;
import com.ydd.yanshi.bean.message.XmppMessage;
import com.ydd.yanshi.bean.redpacket.EventRedReceived;
import com.ydd.yanshi.bean.redpacket.OpenRedpacket;
import com.ydd.yanshi.bean.redpacket.RedDialogBean;
import com.ydd.yanshi.bean.redpacket.RedPacket;
import com.ydd.yanshi.broadcast.MsgBroadcast;
import com.ydd.yanshi.call.CallManager;
import com.ydd.yanshi.call.ImVideoCallActivity;
import com.ydd.yanshi.call.ImVoiceCallActivity;
import com.ydd.yanshi.db.InternationalizationHelper;
import com.ydd.yanshi.db.dao.ChatMessageDao;
import com.ydd.yanshi.db.dao.FriendDao;
import com.ydd.yanshi.db.dao.VideoFileDao;
import com.ydd.yanshi.downloader.Downloader;
import com.ydd.yanshi.helper.DialogHelper;
import com.ydd.yanshi.helper.FileDataHelper;
import com.ydd.yanshi.helper.PrivacySettingHelper;
import com.ydd.yanshi.helper.UploadEngine;
import com.ydd.yanshi.pay.TransferMoneyActivity;
import com.ydd.yanshi.pay.TransferMoneyDetailActivity;
import com.ydd.yanshi.ui.MainActivity;
import com.ydd.yanshi.ui.base.BaseActivity;
import com.ydd.yanshi.ui.base.CoreManager;
import com.ydd.yanshi.ui.contacts.SendContactsActivity;
import com.ydd.yanshi.ui.dialog.CreateCourseDialog;
import com.ydd.yanshi.ui.map.MapPickerActivity;
import com.ydd.yanshi.ui.me.MyCollection;
import com.ydd.yanshi.ui.me.redpacket.RedDetailsActivity;
import com.ydd.yanshi.ui.me.redpacket.SendRedPacketActivity;
import com.ydd.yanshi.ui.message.single.PersonSettingActivity;
import com.ydd.yanshi.ui.mucfile.XfileUtils;
import com.ydd.yanshi.ui.other.BasicInfoActivity;
import com.ydd.yanshi.util.AsyncUtils;
import com.ydd.yanshi.util.Constants;
import com.ydd.yanshi.util.HtmlUtils;
import com.ydd.yanshi.util.PreferenceUtils;
import com.ydd.yanshi.util.StringUtils;
import com.ydd.yanshi.util.TimeUtils;
import com.ydd.yanshi.util.ToastUtil;
import com.ydd.yanshi.util.log.FileUtils;
import com.ydd.yanshi.video.MessageEventGpu;
import com.ydd.yanshi.video.VideoRecorderActivity;
import com.ydd.yanshi.view.ChatBottomView;
import com.ydd.yanshi.view.ChatBottomView.ChatBottomListener;
import com.ydd.yanshi.view.ChatContentView;
import com.ydd.yanshi.view.ChatContentView.MessageEventListener;
import com.ydd.yanshi.view.NoDoubleClickListener;
import com.ydd.yanshi.view.PullDownListView;
import com.ydd.yanshi.view.SelectCardPopupWindow;
import com.ydd.yanshi.view.SelectFileDialog;
import com.ydd.yanshi.view.SelectionFrame;
import com.ydd.yanshi.view.chatHolder.MessageEventClickFire;
import com.ydd.yanshi.view.photopicker.PhotoPickerActivity;
import com.ydd.yanshi.view.photopicker.SelectModel;
import com.ydd.yanshi.view.photopicker.intent.PhotoPickerIntent;
import com.ydd.yanshi.view.redDialog.RedDialog;
import com.ydd.yanshi.xmpp.ListenerManager;
import com.ydd.yanshi.xmpp.listener.ChatMessageListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JVCideoPlayerStandardforchat;
import fm.jiecao.jcvideoplayer_lib.MessageEvent;
import okhttp3.Call;
import pl.droidsonroids.gif.GifDrawable;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;


/**
 * ????????????
 */
public class ChatActivity extends BaseActivity implements
        MessageEventListener, ChatBottomListener, ChatMessageListener,
        SelectCardPopupWindow.SendCardS {

    public static final String FRIEND = "friend";
    /*???????????????????????????*/
    public static final int REQUEST_CODE_SEND_RED = 13;     // ?????????
    public static final int REQUEST_CODE_SEND_RED_PT = 10;  // ??????????????????
    public static final int REQUEST_CODE_SEND_RED_KL = 11;  // ??????????????????
    public static final int REQUEST_CODE_SEND_RED_PSQ = 12; // ?????????????????????
    // ??????????????????
    public static final int REQUEST_CODE_SEND_CONTACT = 21;
    /***********************
     * ?????????????????????
     **********************/
    private static final int REQUEST_CODE_CAPTURE_PHOTO = 1;
    private static final int REQUEST_CODE_PICK_PHOTO = 2;
    private static final int REQUEST_CODE_SELECT_VIDEO = 3;
    private static final int REQUEST_CODE_SEND_COLLECTION = 4;// ???????????? ??????
    private static final int REQUEST_CODE_SELECT_Locate = 5;
    private static final int REQUEST_CODE_QUICK_SEND = 6;
    private static final int REQUEST_CODE_SELECT_FILE = 7;
    RefreshBroadcastReceiver receiver = new RefreshBroadcastReceiver();
    /*******************************************
     * ???????????????????????????????????? && ????????????????????????
     ******************************************/
    List<ChatMessage> chatMessages;
    @SuppressWarnings("unused")
    private ChatContentView mChatContentView;
    // ??????????????????
    private List<ChatMessage> mChatMessages;
    private ChatBottomView mChatBottomView;
    private ImageView mChatBgIv;// ????????????
    private AudioManager mAudioManager;
    // ??????????????????
    private Friend mFriend;
    private String mLoginUserId;
    private String mLoginNickName;
    private boolean isSearch;
    private double mSearchTime;
    // ????????????
    private String instantMessage;

    // ????????????????????????
    private boolean isNotificationComing;
    // ?????????????????????
    private List<Friend> mBlackList;
    private TextView mTvTitleLeft;
    // ?????? || ??????...
    private TextView mTvTitle;
    // ??????????????????
    CountDownTimer time = new CountDownTimer(10000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            if (coreManager.getConfig().isOpenOnlineStatus) {
                String remarkName = mFriend.getRemarkName();
                if (TextUtils.isEmpty(remarkName)) {
                    mTvTitle.setText(mFriend.getNickName() + "(" + InternationalizationHelper.getString("JX_OnLine") + ")");
                } else {
                    mTvTitle.setText(remarkName + "(" + InternationalizationHelper.getString("JX_OnLine") + ")");
                }
            } else {
                mTvTitle.setText(TextUtils.isEmpty(mFriend.getNickName()) ? mFriend.getNickName() : mFriend.getRemarkName());
            }
        }
    };
    // ?????????????????????
    private int isReadDel;
    private String userId;// ??????isDevice==1????????????????????????????????????????????? || ?????????????????????????????????userId??????????????????id?????????ios || pc...;
    private long mMinId = 0;
    private int mPageSize = 20;
    private boolean mHasMoreData = true;
    private UploadEngine.ImFileUploadResponse mUploadResponse = new UploadEngine.ImFileUploadResponse() {

        @Override
        public void onSuccess(String toUserId, ChatMessage message) {
            sendMsg(message);
        }

        @Override
        public void onFailure(String toUserId, ChatMessage message) {
            for (int i = 0; i < mChatMessages.size(); i++) {
                ChatMessage msg = mChatMessages.get(i);
                if (message.get_id() == msg.get_id()) {
                    msg.setMessageState(ChatMessageListener.MESSAGE_SEND_FAILED);
                    ChatMessageDao.getInstance().updateMessageSendState(mLoginUserId, mFriend.getUserId(),
                            message.get_id(), ChatMessageListener.MESSAGE_SEND_FAILED);
                    mChatContentView.notifyDataSetInvalidated(false);
                    break;
                }
            }
        }
    };
    //
    private Uri mNewPhotoUri;
    private HashSet<String> mDelayDelMaps = new HashSet<>();// ??????????????????????????? packedid
    private ChatMessage replayMessage;

    private RedDialog mRedDialog;

    public static void start(Context ctx, Friend friend) {
        Intent intent = new Intent(ctx, ChatActivity.class);
        intent.putExtra(ChatActivity.FRIEND, friend);
        ctx.startActivity(intent);
    }

/*
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(AppConstant.EXTRA_FRIEND, mFriend);
    }
*/

    /**
     * ???????????????????????????
     * ???????????????????????????
     *
     * @param content ???Toast????????????
     */
    public static void callFinish(Context ctx, String content, String toUserId) {
        Intent intent = new Intent();
        intent.putExtra("content", content);
        intent.putExtra("toUserId", toUserId);
        intent.setAction(com.ydd.yanshi.broadcast.OtherBroadcast.TYPE_DELALL);
        LocalBroadcastManager.getInstance(MyApplication.getInstance()).sendBroadcast(intent);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);
        /*AndroidBug5497Workaround.assistActivity(this);*/
        mLoginUserId = coreManager.getSelf().getUserId();
        mLoginNickName = coreManager.getSelf().getNickName();
        if (getIntent() != null) {
            mFriend = (Friend) getIntent().getSerializableExtra(AppConstant.EXTRA_FRIEND);
            isSearch = getIntent().getBooleanExtra("isserch", false);
            if (isSearch) {
                mSearchTime = getIntent().getDoubleExtra("jilu_id", 0);
            }
            instantMessage = getIntent().getStringExtra("messageId");
            isNotificationComing = getIntent().getBooleanExtra(Constants.IS_NOTIFICATION_BAR_COMING, false);
        }
        if (mFriend == null) {
            ToastUtil.showToast(mContext, getString(R.string.tip_friend_not_found));
            finish();
            return;
        }
        if (mFriend.getIsDevice() == 1) {
            userId = mLoginUserId;
        }
        // mSipManager = SipManager.getInstance();
        mAudioManager = (AudioManager) getSystemService(android.app.Service.AUDIO_SERVICE);
        Downloader.getInstance().init(MyApplication.getInstance().mAppDir + File.separator + mLoginUserId
                + File.separator + Environment.DIRECTORY_MUSIC);
        initView();
        // ???????????????????????????
        ListenerManager.getInstance().addChatMessageListener(this);
        // ??????EventBus
        EventBus.getDefault().register(this);
        // ????????????
        register();
        if (mFriend.getUserId().equals(Friend.ID_SYSTEM_MESSAGE)) {
            initSpecialMenu();
        } else {
            // ???????????????????????????????????????
            initFriendState();
        }
    }

    private void initView() {
        mChatMessages = new ArrayList<>();
        mChatBottomView = (ChatBottomView) findViewById(R.id.chat_bottom_view);
        mChatContentView = (ChatContentView) findViewById(R.id.chat_content_view);
        initActionBar();
        mChatBottomView.setChatBottomListener(this);
        mChatBottomView.getmShotsLl().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatBottomView.getmShotsLl().setVisibility(View.GONE);
                String shots = PreferenceUtils.getString(mContext, Constants.SCREEN_SHOTS, "No_Shots");
                QuickSendPreviewActivity.startForResult(ChatActivity.this, shots, REQUEST_CODE_QUICK_SEND);
            }
        });
        if (mFriend.getIsDevice() == 1) {
            mChatBottomView.setEquipment(true);
            mChatContentView.setChatListType(ChatContentView.ChatListType.DEVICE);
        }

        mChatContentView.setToUserId(mFriend.getUserId());
        mChatContentView.setData(mChatMessages);
        mChatContentView.setChatBottomView(mChatBottomView);// ???????????????????????????????????????
        mChatContentView.setMessageEventListener(this);
        mChatContentView.setRefreshListener(new PullDownListView.RefreshingListener() {
            @Override
            public void onHeaderRefreshing() {
                loadDatas(false);
            }
        });
        // ?????????????????????????????????????????????
        mChatContentView.addOnScrollListener(new AbsListView.OnScrollListener() {
            boolean needSecure = false;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (view instanceof ListView) {
                    int headerCount = ((ListView) view).getHeaderViewsCount();
                    firstVisibleItem -= headerCount;
                    totalItemCount -= headerCount;
                }
                if (firstVisibleItem < 0 || visibleItemCount <= 0) {
                    return;
                }

                List<ChatMessage> visibleList = mChatMessages.subList(firstVisibleItem, Math.min(firstVisibleItem + visibleItemCount, totalItemCount));
                boolean lastSecure = needSecure;
                needSecure = false;
                for (ChatMessage message : visibleList) {
                    if (message.getIsReadDel()) {
                        needSecure = true;
                        break;
                    }
                }
                if (needSecure != lastSecure) {
                    if (needSecure) {
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
                    } else {
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                    }
                }
            }
        });

        // CoreManager.updateMyBalance();

        if (isNotificationComing) {
            Intent intent = new Intent();
            intent.putExtra(AppConstant.EXTRA_FRIEND, mFriend);
            intent.setAction(Constants.NOTIFY_MSG_SUBSCRIPT);
            LocalBroadcastManager.getInstance(MyApplication.getInstance()).sendBroadcast(intent);
        } else {
            FriendDao.getInstance().markUserMessageRead(mLoginUserId, mFriend.getUserId());
        }

        loadDatas(true);
        if (mFriend.getDownloadTime() < mFriend.getTimeSend()) {// ????????????????????????????????????
            synchronizeChatHistory();
        }
    }

    private void loadDatas(boolean scrollToBottom) {
        if (mChatMessages.size() > 0) {
            mMinId = mChatMessages.get(0).getTimeSend();
        } else {
            ChatMessage chat = ChatMessageDao.getInstance().getLastChatMessage(mLoginUserId, mFriend.getUserId());
            if (chat != null && chat.getTimeSend() != 0) {
                mMinId = chat.getTimeSend() + 2;
            } else {
                mMinId = TimeUtils.sk_time_current_time();
            }
        }

        List<ChatMessage> chatLists;
        if (isSearch) {// ????????????????????????????????????????????????????????????????????????????????????
            chatLists = ChatMessageDao.getInstance().searchMessagesByTime(mLoginUserId,
                    mFriend.getUserId(), mSearchTime);
        } else {
            chatLists = ChatMessageDao.getInstance().getSingleChatMessages(mLoginUserId,
                    mFriend.getUserId(), mMinId, mPageSize);
        }

        if (chatLists == null || chatLists.size() <= 0) {
            if (!scrollToBottom) {// ????????????
                getNetSingle();
            }
        } else {
            mTvTitle.post(new Runnable() {
                @Override
                public void run() {
                    long currTime = TimeUtils.sk_time_current_time();
                    for (int i = 0; i < chatLists.size(); i++) {
                        ChatMessage message = chatLists.get(i);
                        // ???????????????????????????????????????
                        if (message.getDeleteTime() > 0 && message.getDeleteTime() < currTime) {
                            ChatMessageDao.getInstance().deleteSingleChatMessage(mLoginUserId, mFriend.getUserId(), message.getPacketId());
                            continue;
                        }
                        mChatMessages.add(0, message);
                    }

                    if (isSearch) {
                        isSearch = false;
                        int position = 0;
                        for (int i = 0; i < mChatMessages.size(); i++) {
                            if (mChatMessages.get(i).getDoubleTimeSend() == mSearchTime) {
                                position = i;
                            }
                        }
                        mChatContentView.notifyDataSetInvalidated(position);// ?????????????????????
                    } else {
                        if (scrollToBottom) {
                            mChatContentView.notifyDataSetInvalidated(scrollToBottom);
                        } else {
                            mChatContentView.notifyDataSetAddedItemsToTop(chatLists.size());
                        }
                    }
                    mChatContentView.headerRefreshingCompleted();
                    if (!mHasMoreData) {
                        mChatContentView.setNeedRefresh(false);
                    }
                }
            });
        }
    }

    protected void onSaveContent() {
        String str = mChatBottomView.getmChatEdit().getText().toString().trim();
        // ?????? ???????????????
        str = str.replaceAll("\\s", "");
        str = str.replaceAll("\\n", "");
        if (TextUtils.isEmpty(str)) {
            if (XfileUtils.isNotEmpty(mChatMessages)) {
                ChatMessage chat = mChatMessages.get(mChatMessages.size() - 1);
                if (chat.getType() == XmppMessage.TYPE_TEXT && chat.getIsReadDel()) {
                    FriendDao.getInstance().updateFriendContent(
                            mLoginUserId,
                            mFriend.getUserId(),
                            getString(R.string.tip_click_to_read),
                            chat.getType(),
                            chat.getTimeSend());
                } else {
                    FriendDao.getInstance().updateFriendContent(
                            mLoginUserId,
                            mFriend.getUserId(),
                            chat.getContent(),
                            chat.getType(),
                            chat.getTimeSend());
                }
            }
        } else {// [??????]
            FriendDao.getInstance().updateFriendContent(
                    mLoginUserId,
                    mFriend.getUserId(),
                    "&8824" + str,
                    XmppMessage.TYPE_TEXT, TimeUtils.sk_time_current_time());
        }
        PreferenceUtils.putString(mContext, "WAIT_SEND" + mFriend.getUserId() + mLoginUserId, str);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // ???????????????????????????????????????????????????
        if (ev.getActionIndex() > 0) {
            return true;
        }
        try {
            return super.dispatchTouchEvent(ev);
        } catch (IllegalArgumentException ignore) {
            // ????????????ViewPager???bug, ?????????????????????
            // https://stackoverflow.com/a/31306753
            return true;
        }
    }

    private void doBack() {
        if (!TextUtils.isEmpty(instantMessage)) {
            SelectionFrame selectionFrame = new SelectionFrame(this);
            selectionFrame.setSomething(null, getString(R.string.tip_forwarding_quit), new SelectionFrame.OnSelectionFrameClickListener() {
                @Override
                public void cancelClick() {

                }

                @Override
                public void confirmClick() {
                    finish();
                }
            });
            selectionFrame.show();
        } else {
            finish();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
                        AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (!JVCideoPlayerStandardforchat.handlerBack()) {
            doBack();
        }
    }

    @Override
    protected boolean onHomeAsUp() {
        doBack();
        return true;
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        mBlackList = FriendDao.getInstance().getAllBlacklists(mLoginUserId);
        instantChatMessage();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        // ??????[??????]
        String draft = PreferenceUtils.getString(mContext, "WAIT_SEND" + mFriend.getUserId() + mLoginUserId, "");
        if (!TextUtils.isEmpty(draft)) {
            String s = StringUtils.replaceSpecialChar(draft);
            CharSequence content = HtmlUtils.transform200SpanString(s.replaceAll("\n", "\r\n"), true);
            mChatBottomView.getmChatEdit().setText(content);
            softKeyboardControl(true);
        }
        // ????????????????????????(??????????????????????????????????????? ??????/?????? ????????????????????????onResume??????????????????????????????)
        isReadDel = PreferenceUtils.getInt(mContext, Constants.MESSAGE_READ_FIRE + mFriend.getUserId() + mLoginUserId, 0);
        // ???????????????????????????id
        MyApplication.IsRingId = mFriend.getUserId();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (TextUtils.isEmpty(mChatBottomView.getmChatEdit().getText().toString())) {// ???????????????????????????????????????????????????onPause--onResume???????????????????????????
            PreferenceUtils.putString(mContext, "WAIT_SEND" + mFriend.getUserId() + mLoginUserId, "");
        }
        // ?????????????????????id??????
        MyApplication.IsRingId = "Empty";

        VoicePlayer.instance().stop();
    }

    @Override
    protected void onDestroy() {
        onSaveContent();
        MsgBroadcast.broadcastMsgUiUpdate(mContext);
        super.onDestroy();
        JCVideoPlayer.releaseAllVideos();
        if (mChatBottomView != null) {
            mChatBottomView.recordCancel();
        }
        ListenerManager.getInstance().removeChatMessageListener(this);
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        } catch (Exception e) {
            // ??????????????????????????????????????????????????????????????????
        }
    }

    /***************************************
     * ChatContentView?????????
     ***************************************/
    @Override
    public void onMyAvatarClick() {
        // ?????????????????????
        mChatBottomView.reset();
        mChatBottomView.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(mContext, BasicInfoActivity.class);
                intent.putExtra(AppConstant.EXTRA_USER_ID, mLoginUserId);
                startActivity(intent);
            }
        }, 100);
    }

    @Override
    public void onFriendAvatarClick(final String friendUserId) {
        // ?????????????????????
        mChatBottomView.reset();
        mChatBottomView.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(mContext, BasicInfoActivity.class);
                intent.putExtra(AppConstant.EXTRA_USER_ID, friendUserId);
                startActivity(intent);
            }
        }, 100);
    }

    @Override
    public void LongAvatarClick(ChatMessage chatMessage) {
    }

    @Override
    public void onNickNameClick(String friendUserId) {
    }

    @Override
    public void onMessageClick(ChatMessage chatMessage) {
    }

    @Override
    public void onMessageLongClick(ChatMessage chatMessage) {
    }

    @Override
    public void onEmptyTouch() {
        mChatBottomView.reset();
    }

    @Override
    public void onTipMessageClick(ChatMessage message) {
        if (message.getFileSize() == XmppMessage.TYPE_83) {
            showRedReceivedDetail(message.getFilePath());
        }
    }

    // ????????????????????????
    private void showRedReceivedDetail(String redId) {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(mContext).accessToken);
        params.put("id", redId);

        HttpUtils.get().url(CoreManager.requireConfig(mContext).RENDPACKET_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<OpenRedpacket>(OpenRedpacket.class) {

                    @Override
                    public void onResponse(ObjectResult<OpenRedpacket> result) {
                        if (result.getData() != null) {
                            // ???resultCode==1?????????????????????
                            // ???resultCode==0???????????????????????????????????????????????????????????????
                            OpenRedpacket openRedpacket = result.getData();
                            Bundle bundle = new Bundle();
                            Intent intent = new Intent(mContext, RedDetailsActivity.class);
                            bundle.putSerializable("openRedpacket", openRedpacket);
                            bundle.putInt("redAction", 0);
                            if (!TextUtils.isEmpty(result.getResultMsg())) //resultMsg??????????????????????????????
                            {
                                bundle.putInt("timeOut", 1);
                            } else {
                                bundle.putInt("timeOut", 0);
                            }

                            bundle.putBoolean("isGroup", false);
                            bundle.putString("mToUserId", mFriend.getUserId());
                            intent.putExtras(bundle);
                            mContext.startActivity(intent);
                        } else {
                            Toast.makeText(mContext, result.getResultMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    @Override
    public void onReplayClick(ChatMessage message) {
        ChatMessage replayMessage = new ChatMessage(message.getObjectId());
        AsyncUtils.doAsync(this, t -> {
            Reporter.post("??????????????????????????????<" + message.getObjectId() + ">", t);
        }, c -> {
            List<ChatMessage> chatMessages = ChatMessageDao.getInstance().searchFromMessage(c.getRef(), mLoginUserId, mFriend.getUserId(), replayMessage);
            if (chatMessages == null) {
                // ??????????????????
                Log.e("Replay", "????????????????????????????????????<" + message.getObjectId() + ">");
                return;
            }
            int index = -1;
            for (int i = 0; i < chatMessages.size(); i++) {
                ChatMessage m = chatMessages.get(i);
                if (TextUtils.equals(m.getPacketId(), replayMessage.getPacketId())) {
                    index = i;
                }
            }
            if (index == -1) {
                Reporter.unreachable();
                return;
            }
            int finalIndex = index;
            c.uiThread(r -> {
                mChatMessages = chatMessages;
                mChatContentView.setData(mChatMessages);
                mChatContentView.notifyDataSetInvalidated(finalIndex);
            });
        });
    }

    /**
     * ???????????????????????????
     */
    @Override
    public void onSendAgain(ChatMessage message) {
        if (message.getType() == XmppMessage.TYPE_VOICE || message.getType() == XmppMessage.TYPE_IMAGE
                || message.getType() == XmppMessage.TYPE_VIDEO || message.getType() == XmppMessage.TYPE_FILE
                || message.getType() == XmppMessage.TYPE_LOCATION) {
            if (!message.isUpload()) {
                // ??????????????????????????????????????????????????????????????????????????????????????????????????????[??????????????????]????????????????????????????????????
                ChatMessageDao.getInstance().updateMessageSendState(mLoginUserId, mFriend.getUserId(),
                        message.get_id(), ChatMessageListener.MESSAGE_SEND_ING);
                UploadEngine.uploadImFile(coreManager.getSelfStatus().accessToken, coreManager.getSelf().getUserId(), mFriend.getUserId(), message, mUploadResponse);
            } else {
                if (isAuthenticated()) {
                    return;
                }
                coreManager.sendChatMessage(mFriend.getUserId(), message);
            }
        } else {
            if (isAuthenticated()) {
                return;
            }
            coreManager.sendChatMessage(mFriend.getUserId(), message);
        }
    }

    public void deleteMessage(String msgIdListStr) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("messageId", msgIdListStr);
        params.put("delete", "1");  // 1???????????? 2-????????????
        params.put("type", "1");    // 1???????????? 2-????????????

        HttpUtils.get().url(coreManager.getConfig().USER_DEL_CHATMESSAGE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    /**
     * ????????????
     */
    @Override
    public void onMessageBack(final ChatMessage chatMessage, final int position) {
        DialogHelper.showMessageProgressDialog(this, InternationalizationHelper.getString("MESSAGE_REVOCATION"));
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("messageId", chatMessage.getPacketId());
        params.put("delete", "2");  // 1???????????? 2-????????????
        params.put("type", "1");    // 1???????????? 2-????????????

        HttpUtils.get().url(coreManager.getConfig().USER_DEL_CHATMESSAGE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (chatMessage.getType() == XmppMessage.TYPE_VOICE) {// ???????????????????????????????????????
                            if (VoicePlayer.instance().getVoiceMsgId().equals(chatMessage.getPacketId())) {
                                VoicePlayer.instance().stop();
                            }
                        } else if (chatMessage.getType() == XmppMessage.TYPE_VIDEO) {
                            JCVideoPlayer.releaseAllVideos();
                        }
                        // ??????????????????
                        ChatMessage message = new ChatMessage();
                        message.setType(XmppMessage.TYPE_BACK);
                        message.setFromUserId(mLoginUserId);
                        message.setFromUserName(coreManager.getSelf().getNickName());
                        message.setToUserId(mFriend.getUserId());
                        message.setContent(chatMessage.getPacketId());
                        message.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
                        message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                        coreManager.sendChatMessage(mFriend.getUserId(), message);
                        ChatMessage chat = mChatMessages.get(position);
                        ChatMessageDao.getInstance().updateMessageBack(mLoginUserId, mFriend.getUserId(), chat.getPacketId(), getString(R.string.you));
                        chat.setType(XmppMessage.TYPE_TIP);
                        //  chat.setContent("????????????????????????");
                        chat.setContent(InternationalizationHelper.getString("JX_AlreadyWithdraw"));
                        mChatContentView.notifyDataSetInvalidated(true);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    @Override
    public void onMessageReplay(ChatMessage chatMessage) {
        replayMessage = chatMessage;
        mChatBottomView.setReplay(chatMessage);
    }

    @Override
    public void cancelReplay() {
        replayMessage = null;
    }

    @Override
    public void onCallListener(int type) {
        if (type == 103 || type == 104) {
            call(CallManager.TYPE_CALL_AUDIO,
                    mLoginUserId,
                    mFriend.getUserId(),
                    mLoginNickName,
                    mFriend.getNickName(),
                    CallManager.CALL,"");
        } else if (type == 113 || type == 114) {
            call(CallManager.TYPE_CALL_VEDIO,
                    mLoginUserId,
                    mFriend.getUserId(),
                    mLoginNickName,
                    mFriend.getNickName(),
                    CallManager.CALL,"");
        }
    }

    /***************************************
     * ChatBottomView?????????
     ***************************************/

    private void softKeyboardControl(boolean isShow) {
        // ???????????????
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm == null) return;
        if (isShow) {
            mChatBottomView.postDelayed(new Runnable() {
                @Override
                public void run() {// ??????200ms?????????????????????????????????????????????????????????????????????????????????
                    mChatBottomView.getmChatEdit().requestFocus();
                    mChatBottomView.getmChatEdit().setSelection(mChatBottomView.getmChatEdit().getText().toString().length());
                    imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                }
            }, 200);
        } else {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * ??????????????????????????????
     */
    private void sendMessage(final ChatMessage message) {
        if (interprect()) {// ????????????????????????????????????
            ToastUtil.showToast(this, getString(R.string.tip_remote_in_black));
            // ?????????????????????
            mChatMessages.remove(message);
            mChatContentView.notifyDataSetInvalidated(true);
            return;
        }
Log.e("hm---message1",message.getContent());
        message.setFromUserId(mLoginUserId);
        PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(this);
        boolean isSupport = privacySetting.getMultipleDevices() == 1;
        if (isSupport) {
            message.setFromId("android");
            Log.e("hm---message1","android");
        } else {
            Log.e("hm---message1","youjob");
            message.setFromId("youjob");
        }
        if (mFriend.getIsDevice() == 1) {
            message.setToUserId(userId);
            message.setToId(mFriend.getUserId());
        } else {
            message.setToUserId(mFriend.getUserId());

            // sz ??????????????????
            if (mFriend.getChatRecordTimeOut() == -1 || mFriend.getChatRecordTimeOut() == 0) {// ??????
                message.setDeleteTime(-1);
            } else {
                long deleteTime = TimeUtils.sk_time_current_time() + (long) (mFriend.getChatRecordTimeOut() * 24 * 60 * 60);
                message.setDeleteTime(deleteTime);
            }
        }

        boolean isEncrypt = privacySetting.getIsEncrypt() == 1;
        if (isEncrypt) {
            message.setIsEncrypt(1);
            Log.e("hm---message1","??????");
        } else {
            message.setIsEncrypt(0);
            Log.e("hm---message1","?????????");
        }

        message.setReSendCount(ChatMessageDao.fillReCount(message.getType()));
        message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        message.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());

        // ??????????????????????????????
        ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, mFriend.getUserId(), message);
        if (message.getType() == XmppMessage.TYPE_VOICE || message.getType() == XmppMessage.TYPE_IMAGE
                || message.getType() == XmppMessage.TYPE_VIDEO || message.getType() == XmppMessage.TYPE_FILE
                || message.getType() == XmppMessage.TYPE_LOCATION) {// ??????????????????????????????????????????????????????
            // ?????????????????????????????????
            if (!message.isUpload()) {// ?????????
                if (mFriend.getIsDevice() == 1) {
                    // ????????????????????????
                    // UploadEngine.uploadImFile(coreManager.getSelfStatus().accessToken, coreManager.getSelf().getUserId(), userId, message, mUploadResponse);
                    UploadEngine.uploadImFile(coreManager.getSelfStatus().accessToken, coreManager.getSelf().getUserId(), mFriend.getUserId(), message, mUploadResponse);
                } else {
                    UploadEngine.uploadImFile(coreManager.getSelfStatus().accessToken, coreManager.getSelf().getUserId(), mFriend.getUserId(), message, mUploadResponse);
                }
            } else {// ????????? ?????????????????????????????????
                sendMsg(message);
            }
        } else {// ????????????????????????
            sendMsg(message);
        }
    }

    private void sendMsg(ChatMessage message) {
        // ???????????????????????????????????????xmpp???????????????
        // ??????????????????????????????
        if (isAuthenticated()) {
            return;
        }
        if (mFriend.getIsDevice() == 1) {
            coreManager.sendChatMessage(userId, message);
        } else {
            coreManager.sendChatMessage(mFriend.getUserId(), message);
        }
    }

    /**
     * ???????????????????????????
     */
    @Override
    public void stopVoicePlay() {
        VoicePlayer.instance().stop();
    }

    @Override
    public void sendAt() {
    }

    @Override
    public void sendAtMessage(String text) {
        sendText(text);// ???????????????@?????????????????????????????????
    }

    @Override
    public void sendText(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (isAuthenticated()) {
            return;
        }
        Log.e("hm---text",text);
        ChatMessage message = new ChatMessage();
        // ????????????
        message.setType(XmppMessage.TYPE_TEXT);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent(text);
        if (replayMessage != null) {
            message.setType(XmppMessage.TYPE_REPLAY);
            message.setObjectId(replayMessage.toJsonString());
            replayMessage = null;
            mChatBottomView.resetReplay();
        }
        message.setIsReadDel(isReadDel);
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);

       /* mChatContentView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mChatContentView.scrollToBottom();
            }
        }, 500);*/

        sendMessage(message);
        // ?????????????????????????????????????????????
        for (ChatMessage msg : mChatMessages) {
            if (msg.getType() == XmppMessage.TYPE_RED// ??????
                    && StringUtils.strEquals(msg.getFilePath(), "3")// ????????????
                    && text.equalsIgnoreCase(msg.getContent())// ??????????????????????????????
                    && msg.getFileSize() == 1// ?????????????????????
                    && !msg.isMySend()) {
                RedDialogBean redDialogBean = new RedDialogBean(msg.getFromUserId(), msg.getFromUserName(),
                        msg.getContent(), null);
                mRedDialog = new RedDialog(mContext, redDialogBean, () -> {
                    // ????????????
                    openRedPacket(msg);
                });
                mRedDialog.show();
            }
        }
    }

    /**
     * ????????????
     */
    public void openRedPacket(final ChatMessage message) {
        HashMap<String, String> params = new HashMap<String, String>();
        String redId = message.getObjectId();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("id", redId);

        HttpUtils.get().url(coreManager.getConfig().REDPACKET_OPEN)
                .params(params)
                .build()
                .execute(new BaseCallback<OpenRedpacket>(OpenRedpacket.class) {

                    @Override
                    public void onResponse(ObjectResult<OpenRedpacket> result) {
                        if (mRedDialog != null) {
                            mRedDialog.dismiss();
                        }
                        if (result.getData() != null) {
                            // ??????????????????????????????,???????????????
                            message.setFileSize(2);
                            ChatMessageDao.getInstance().updateChatMessageReceiptStatus(mLoginUserId, mFriend.getUserId(), message.getPacketId());
                            mChatContentView.notifyDataSetChanged();

                            OpenRedpacket openRedpacket = result.getData();
                            Bundle bundle = new Bundle();
                            Intent intent = new Intent(mContext, RedDetailsActivity.class);
                            bundle.putSerializable("openRedpacket", openRedpacket);
                            bundle.putInt("redAction", 1);
                            bundle.putInt("timeOut", 0);

                            bundle.putBoolean("isGroup", false);
                            bundle.putString("mToUserId", mFriend.getUserId());
                            intent.putExtras(bundle);
                            mContext.startActivity(intent);
                            // ????????????
                            coreManager.updateMyBalance();

                            showReceiverRedLocal(openRedpacket);
                        } else {
                            Toast.makeText(ChatActivity.this, result.getResultMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        if (mRedDialog != null) {
                            mRedDialog.dismiss();
                        }
                    }
                });
    }

    private void showReceiverRedLocal(OpenRedpacket openRedpacket) {
        // ??????????????????????????????
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setFileSize(XmppMessage.TYPE_83);
        chatMessage.setFilePath(openRedpacket.getPacket().getId());
        chatMessage.setFromUserId(mLoginUserId);
        chatMessage.setFromUserName(mLoginNickName);
        chatMessage.setToUserId(mFriend.getUserId());
        chatMessage.setType(XmppMessage.TYPE_TIP);
        chatMessage.setContent(getString(R.string.red_received_self, openRedpacket.getPacket().getUserName()));
        chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        chatMessage.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, mFriend.getUserId(), chatMessage)) {
            mChatMessages.add(chatMessage);
            mChatContentView.notifyDataSetInvalidated(true);
        }
    }

    @Override
    public void sendGif(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (isAuthenticated()) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_GIF);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent(text);
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    @Override
    public void sendCollection(String collection) {
        if (isAuthenticated()) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_IMAGE);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent(collection);
        message.setUpload(true);// ?????????????????????????????????
        message.setIsReadDel(isReadDel);
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    @Override
    public void sendVoice(String filePath, int timeLen) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        if (isAuthenticated()) {
            return;
        }
        File file = new File(filePath);
        long fileSize = file.length();
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_VOICE);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent("");
        message.setFilePath(filePath);
        message.setFileSize((int) fileSize);
        message.setTimeLen(timeLen);
        message.setIsReadDel(isReadDel);
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    public void sendImage(File file) {
        if (!file.exists()) {
            return;
        }
        if (isAuthenticated()) {
            return;
        }
        long fileSize = file.length();
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_IMAGE);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent("");
        String filePath = file.getAbsolutePath();
        message.setFilePath(filePath);
        message.setFileSize((int) fileSize);
        int[] imageParam = FileDataHelper.getImageParamByIntsFile(filePath);
        message.setLocation_x(String.valueOf(imageParam[0]));
        message.setLocation_y(String.valueOf(imageParam[1]));
        message.setIsReadDel(isReadDel);
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    public void sendVideo(File file) {
        if (!file.exists()) {
            return;
        }
        if (isAuthenticated()) {
            return;
        }
        long fileSize = file.length();
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_VIDEO);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent("");
        String filePath = file.getAbsolutePath();
        message.setFilePath(filePath);
        message.setFileSize((int) fileSize);
        message.setIsReadDel(isReadDel);
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    public void sendFile(File file) {
        if (!file.exists()) {
            return;
        }
        if (isAuthenticated()) {
            return;
        }
        long fileSize = file.length();
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_FILE);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent("");
        message.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
        String filePath = file.getAbsolutePath();
        message.setFilePath(filePath);
        message.setFileSize((int) fileSize);
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    private void sendContacts(List<Contacts> contactsList) {
        for (Contacts contacts : contactsList) {
            sendText(contacts.getName() + '\n' + contacts.getTelephone());
        }
    }

    public void sendLocate(double latitude, double longitude, String address, String snapshot) {
        if (isAuthenticated()) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_LOCATION);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        // ??????????????????????????????
        message.setContent("");
        message.setFilePath(snapshot);
        message.setLocation_x(latitude + "");
        message.setLocation_y(longitude + "");
        message.setObjectId(address);
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    @Override
    public void clickPhoto() {
        // ????????????true
        /*MyApplication.GalleyNotBackGround = true;
        CameraUtil.pickImageSimple(this, REQUEST_CODE_PICK_PHOTO);*/
        ArrayList<String> imagePaths = new ArrayList<>();
        PhotoPickerIntent intent = new PhotoPickerIntent(ChatActivity.this);
        intent.setSelectModel(SelectModel.MULTI);
        // ??????????????????????????? ????????????????????????
        intent.setSelectedPaths(imagePaths);
        startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
        mChatBottomView.reset();
    }

    @Override
    public void clickCamera() {
       /* mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, CameraUtil.MEDIA_TYPE_IMAGE);
        CameraUtil.captureImage(this, mNewPhotoUri, REQUEST_CODE_CAPTURE_PHOTO);*/
       /* Intent intent = new Intent(this, EasyCameraActivity.class);
        startActivity(intent);*/
        mChatBottomView.reset();
        Intent intent = new Intent(this, VideoRecorderActivity.class);
        startActivity(intent);
    }


    @Override
    public void clickStartRecord() {
        // ???????????????ui????????????????????????clickCamera?????????
       /* Intent intent = new Intent(this, VideoRecorderActivity.class);
        startActivity(intent);*/
    }

    @Override
    public void clickLocalVideo() {
        // ???????????????ui????????????????????????clickCamera?????????
        /*Intent intent = new Intent(this, LocalVideoActivity.class);
        intent.putExtra(AppConstant.EXTRA_ACTION, AppConstant.ACTION_SELECT);
        intent.putExtra(AppConstant.EXTRA_MULTI_SELECT, true);
        startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);*/
    }

    @Override
    public void clickAudio() {
        call(CallManager.TYPE_CALL_AUDIO,
                mLoginUserId,
                mFriend.getUserId(),
                mLoginNickName,
                mFriend.getNickName(),
                CallManager.CALL,"");
    }

    @Override
    public void clickVideoChat() {
        call(CallManager.TYPE_CALL_VEDIO,
                mLoginUserId,
                mFriend.getUserId(),
                mLoginNickName,
                mFriend.getNickName(),
                CallManager.CALL,"");
    }

    public void call(int type,String formUid,String toUid,String myName,String friendName,int callOrReceive,String channel) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        if(callOrReceive == CallManager.RECEIVE_CALL){
            params.put("channel", channel);
        }
        Log.e("hm----CALL",coreManager.getConfig().CALL);
        HttpUtils.get().url(coreManager.getConfig().CALL)
                .params(params)
                .build()
                .execute(new BaseCallback<AgoraInfo>(AgoraInfo.class) {

                    @Override
                    public void onResponse(ObjectResult<AgoraInfo> result) {
                        if (result.getData() != null) {
                            AgoraInfo agoraInfo = result.getData();

                            String ch = "";
                            if(callOrReceive == CallManager.CALL){
                                ch =  agoraInfo.getChannel();
                            }else {
                                ch = channel;
                            }
                            if (type == CallManager.TYPE_CALL_AUDIO) {
                                callAudio(ch,agoraInfo.getAppId(),agoraInfo.getOwnToken(),formUid,toUid,myName,friendName,callOrReceive);
                            } else {
                                callVideo(ch,agoraInfo.getAppId(),agoraInfo.getOwnToken(),formUid,toUid,myName,friendName,callOrReceive);
                            }
                        } else {
                            Toast.makeText(mContext, result.getResultMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });

    }

    private void callAudio(String channel,String appid,String token,String formUid,String toUid,String myName,String friendName,int callOrReceive) {
        ImVoiceCallActivity.Companion.start(mContext,
                formUid,
                toUid,
                myName,
                friendName,
                channel,
                appid,
                token,
                callOrReceive);
    }

    private void callVideo(String channel,String appid,String token,String formUid,String toUid,String myName,String friendName,int callOrReceive) {
        ImVideoCallActivity.Companion.start(mContext,
                formUid,
                toUid,
                myName,
                friendName,
                channel,
                appid,
                token,
                callOrReceive);
    }

    @Override
    public void clickFile() {
        SelectFileDialog dialog = new SelectFileDialog(this, new SelectFileDialog.OptionFileListener() {
            @Override
            public void option(List<File> files) {
                if (files != null && files.size() > 0) {
                    for (int i = 0; i < files.size(); i++) {
                        sendFile(files.get(i));
                    }
                }
            }

            @Override
            public void intent() {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");//???????????????????????????????????????????????????????????????????????????
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
            }

        });
        dialog.show();
    }

    @Override
    public void clickContact() {
        SendContactsActivity.start(this, REQUEST_CODE_SEND_CONTACT);
    }

    @Override
    public void clickLocation() {
        Intent intent = new Intent(this, MapPickerActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SELECT_Locate);
    }

    @Override
    public void clickCard() {
        SelectCardPopupWindow mSelectCardPopupWindow = new SelectCardPopupWindow(this, this);
        mSelectCardPopupWindow.showAtLocation(findViewById(R.id.root_view),
                Gravity.CENTER, 0, 0);
    }

    @Override
    public void clickRedpacket() {
        Intent intent = new Intent(this, SendRedPacketActivity.class);
        intent.putExtra("friendId", mFriend.getUserId());
        startActivityForResult(intent, REQUEST_CODE_SEND_RED);
    }

    @Override
    public void clickTransferMoney() {
        Intent intent = new Intent(this, TransferMoneyActivity.class);
        intent.putExtra(AppConstant.EXTRA_USER_ID, mFriend.getUserId());
        intent.putExtra(AppConstant.EXTRA_NICK_NAME, TextUtils.isEmpty(mFriend.getRemarkName()) ? mFriend.getNickName() : mFriend.getRemarkName());
        startActivity(intent);
    }

    @Override
    public void clickCollection() {
        Intent intent = new Intent(this, MyCollection.class);
        intent.putExtra("IS_SEND_COLLECTION", true);
        startActivityForResult(intent, REQUEST_CODE_SEND_COLLECTION);
    }

    private void clickCollectionSend(
            int type,
            String content,
            int timeLen,
            String filePath,
            long fileSize
    ) {
        if (isAuthenticated()) {
            return;
        }

        if (TextUtils.isEmpty(content)) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(type);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent(content);
        message.setTimeLen(timeLen);
        message.setFileSize((int) fileSize);
        message.setUpload(true);
        if (!TextUtils.isEmpty(filePath)) {
            message.setFilePath(filePath);
        }
        message.setIsReadDel(0);
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    private void clickCollectionSend(CollectionEvery collection) {
        // ????????????????????????????????????????????????????????????????????????????????????
        if (!TextUtils.isEmpty(collection.getCollectContent())) {
            sendText(collection.getCollectContent());
        }
        int type = collection.getXmppType();
        if (type == XmppMessage.TYPE_TEXT) {
            // ????????????????????????????????????????????????
            return;
        } else if (type == XmppMessage.TYPE_IMAGE) {
            // ???????????????????????????????????????
            String allUrl = collection.getUrl();
            for (String url : allUrl.split(",")) {
                clickCollectionSend(type, url, collection.getFileLength(), collection.getFileName(), collection.getFileSize());
            }
            return;
        }
        clickCollectionSend(type, collection.getUrl(), collection.getFileLength(), collection.getFileName(), collection.getFileSize());
    }

    @Override
    public void clickShake() {
        if (isAuthenticated()) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_SHAKE);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent(getString(R.string.msg_shake));
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
        shake(0);// ???????????????
    }

    @Override
    public void clickGroupAssistant(GroupAssistantDetail groupAssistantDetail) {

    }

    private void shake(int type) {
        Animation shake;
        if (type == 0) {
            shake = AnimationUtils.loadAnimation(this, R.anim.shake_from);
        } else {
            shake = AnimationUtils.loadAnimation(this, R.anim.shake_to);
        }
        mChatContentView.startAnimation(shake);
        mChatBottomView.startAnimation(shake);
        mChatBgIv.startAnimation(shake);
    }

    /**
     * ?????????????????????
     */
    @Override
    public void sendCardS(List<Friend> friends) {
        for (int i = 0; i < friends.size(); i++) {
            sendCard(friends.get(i));
        }
    }

    public void sendCard(Friend friend) {
        if (isAuthenticated()) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_CARD);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent(friend.getNickName());
        message.setObjectId(friend.getUserId());
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    public void sendRed(RedPacket redPacket) {
        if (isAuthenticated()) {
            return;
        }
        String objectId = redPacket.getId();
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_RED);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent(redPacket.getGreetings()); // ?????????
        message.setFilePath(redPacket.getType() + ""); // ???FilePath?????????????????????
        message.setFileSize(redPacket.getStatus()); //???filesize?????????????????????
        message.setObjectId(objectId); // ??????id
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
        // ????????????
        CoreManager.updateMyBalance();
    }

    public void sendRed(final String type, String money, String count, final String words, String payPassword) {
        if (isAuthenticated()) {
            return;
        }
        Log.e("hm---money",money);
        Log.e("hm---payPassword",payPassword);
        
        Map<String, String> params = new HashMap();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("type", type);
        params.put("moneyStr", money);
        params.put("count", count);
        params.put("greetings", words);
        params.put("toUserId", mFriend.getUserId());

        HttpUtils.get().url(coreManager.getConfig().REDPACKET_SEND)
                .params(params)
                .addSecret(payPassword, money)
                .build()
                .execute(new BaseCallback<RedPacket>(RedPacket.class) {

                    @Override
                    public void onResponse(ObjectResult<RedPacket> result) {
                        RedPacket redPacket = result.getData();
                        if (result.getResultCode() != 1) {
                            // ?????????????????????
                            ToastUtil.showToast(mContext, result.getResultMsg());
                        } else {
                            String objectId = redPacket.getId();
                            ChatMessage message = new ChatMessage();
                            message.setType(XmppMessage.TYPE_RED);
                            message.setFromUserId(mLoginUserId);
                            message.setFromUserName(mLoginNickName);
                            message.setContent(words); // ?????????
                            message.setFilePath(type); // ???FilePath?????????????????????
                            message.setFileSize(redPacket.getStatus()); //???filesize?????????????????????
                            message.setObjectId(objectId); // ??????id
                            mChatMessages.add(message);
                            mChatContentView.notifyDataSetInvalidated(true);
                            sendMessage(message);
                            // ????????????
                            CoreManager.updateMyBalance();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    @Override
    public void onInputState() {
        // ??????????????????
        PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(this);
        boolean input = privacySetting.getIsTyping() == 1;
        if (input && coreManager.isLogin()) {
            ChatMessage message = new ChatMessage();
            // ??????????????????
            message.setType(XmppMessage.TYPE_INPUT);
            message.setFromUserId(mLoginUserId);
            message.setFromUserName(mLoginNickName);
            message.setToUserId(mFriend.getUserId());
            message.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
            message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
            coreManager.sendChatMessage(mFriend.getUserId(), message);
        }
    }

    /**
     * ???????????????
     */
    @Override
    public boolean onNewMessage(String fromUserId, ChatMessage message, boolean isGroupMsg) {
        if (isGroupMsg) {
            return false;
        }

        /**
         *  ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
         *  ?????????????????????????????????????????????????????????????????????(??????????????????)??????????????????????????????????????????????????????
         *  ??????????????????onNewMessage????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
         *  ?????????????????????????????????
         *
         */
        if (mChatMessages.size() > 0) {
            if (mChatMessages.get(mChatMessages.size() - 1).getPacketId().equals(message.getPacketId())) {// ?????????????????????msgId==????????????msgId
                Log.e("zq", "????????????????????????");
                return false;
            }
        }

        if (mFriend.getIsDevice() == 1) {// ????????????????????????????????? ????????????????????????????????????????????????????????????
            ChatMessage chatMessage = ChatMessageDao.getInstance().
                    findMsgById(mLoginUserId, mFriend.getUserId(), message.getPacketId());
            if (chatMessage == null) {
                return false;
            }
        }

        /*
         ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
         */
        if (fromUserId.equals(mLoginUserId)
                && !TextUtils.isEmpty(message.getToUserId())
                && message.getToUserId().equals(mFriend.getUserId())) {// ???????????????????????????????????????????????????????????????????????????
            message.setMySend(true);
            message.setMessageState(MESSAGE_SEND_SUCCESS);
            mChatMessages.add(message);
            if (mChatContentView.shouldScrollToBottom()) {
                mChatContentView.notifyDataSetInvalidated(true);
            } else {
                mChatContentView.notifyDataSetChanged();
            }
            if (message.getType() == XmppMessage.TYPE_SHAKE) {// ?????????
                shake(1);
            }
            return true;
        }

        if (mFriend.getUserId().compareToIgnoreCase(fromUserId) == 0) {// ????????????????????????
            mChatMessages.add(message);
            if (mChatContentView.shouldScrollToBottom()) {
                mChatContentView.notifyDataSetInvalidated(true);
            } else {
                // ??????????????????
                Vibrator vibrator = (Vibrator) MyApplication.getContext().getSystemService(VIBRATOR_SERVICE);
                long[] pattern = {100, 400, 100, 400};
                if (vibrator != null) {
                    vibrator.vibrate(pattern, -1);
                }
                mChatContentView.notifyDataSetChanged();
            }
            if (message.getType() == XmppMessage.TYPE_SHAKE) {// ?????????
                shake(1);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onMessageSendStateChange(int messageState, String msgId) {
        Log.e("zq", messageState + "???" + msgId);
        if (TextUtils.isEmpty(msgId)) {
            return;
        }
        for (int i = 0; i < mChatMessages.size(); i++) {
            ChatMessage msg = mChatMessages.get(i);
            if (msgId.equals(msg.getPacketId())) {
                /**
                 * ??????????????????????????????????????????????????????????????????????????????????????????????????????
                 * ???????????????????????????????????????????????????????????????1???????????????0??????????????????
                 */
                if (msg.getMessageState() == 1) {
                    return;
                }
                msg.setMessageState(messageState);
                //???????????????  ??????????????????  ???????????????????????????????????????????????????
                mChatContentView.notifyDataSetInvalidated(true);
//                mChatContentView.notifyDataSetChanged();
                break;
            }
        }
    }

    /**
     * ?????????com.client.yanchat.ui.me.LocalVideoActivity#helloEventBus(com.client.yanchat.adapter.MessageVideoFile)
     * ?????????CameraDemoActivity??????????????????activity result, ?????????EventBus,
     */
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventUploadFileRate message) {
        for (int i = 0; i < mChatMessages.size(); i++) {
            if (mChatMessages.get(i).getPacketId().equals(message.getPacketId())) {
                mChatMessages.get(i).setUploadSchedule(message.getRate());
                // ???????????????setUpload????????????????????????????????????????????????????????????????????????url,????????????????????????
                mChatContentView.notifyDataSetChanged();
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventUploadCancel message) {
        for (int i = 0; i < mChatMessages.size(); i++) {
            if (mChatMessages.get(i).getPacketId().equals(message.getPacketId())) {
                mChatMessages.remove(i);
                mChatContentView.notifyDataSetChanged();
                ChatMessageDao.getInstance().deleteSingleChatMessage(mLoginUserId, mFriend.getUserId(), message.getPacketId());
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageVideoFile message) {
        VideoFile videoFile = new VideoFile();
        videoFile.setCreateTime(TimeUtils.f_long_2_str(System.currentTimeMillis()));
        videoFile.setFileLength(message.timelen);
        videoFile.setFileSize(message.length);
        videoFile.setFilePath(message.path);
        videoFile.setOwnerId(coreManager.getSelf().getUserId());
        VideoFileDao.getInstance().addVideoFile(videoFile);
        String filePath = message.path;
        if (TextUtils.isEmpty(filePath)) {
            ToastUtil.showToast(this, R.string.record_failed);
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            ToastUtil.showToast(this, R.string.record_failed);
            return;
        }
        sendVideo(file);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageLocalVideoFile message) {
        sendVideo(message.file);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventRedReceived message) {
        showReceiverRedLocal(message.getOpenRedpacket());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SELECT_FILE: // ???????????????????????????
                    String file_path = FileUtils.getPath(ChatActivity.this, data.getData());
                    Log.e("xuan", "conversionFile: " + file_path);
                    if (file_path == null) {
                        ToastUtil.showToast(mContext, R.string.tip_file_not_supported);
                    } else {
                        sendFile(new File(file_path));
                    }
                    break;
                case REQUEST_CODE_CAPTURE_PHOTO:
                    // ????????????
                    if (mNewPhotoUri != null) {
                        photograph(new File(mNewPhotoUri.getPath()));
                    }
                    break;
                case REQUEST_CODE_PICK_PHOTO:
                    if (data != null) {
                        boolean isOriginal = data.getBooleanExtra(PhotoPickerActivity.EXTRA_RESULT_ORIGINAL, false);
                        album(data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT), isOriginal);
                    } else {
                        ToastUtil.showToast(this, R.string.c_photo_album_failed);
                    }
                    break;
                case REQUEST_CODE_SELECT_VIDEO: {
                    // ?????????????????????
                    if (data == null) {
                        return;
                    }
                    String json = data.getStringExtra(AppConstant.EXTRA_VIDEO_LIST);
                    List<VideoFile> fileList = JSON.parseArray(json, VideoFile.class);
                    if (fileList == null || fileList.size() == 0) {
                        // ???????????????????????????????????????
                        Reporter.unreachable();
                    } else {
                        for (VideoFile videoFile : fileList) {
                            String filePath = videoFile.getFilePath();
                            if (TextUtils.isEmpty(filePath)) {
                                // ???????????????????????????????????????
                                Reporter.unreachable();
                            } else {
                                File file = new File(filePath);
                                if (!file.exists()) {
                                    // ???????????????????????????????????????
                                    Reporter.unreachable();
                                } else {
                                    sendVideo(file);
                                }
                            }
                        }
                    }
                    break;
                }
                case REQUEST_CODE_SELECT_Locate: // ?????????????????????
                    double latitude = data.getDoubleExtra(AppConstant.EXTRA_LATITUDE, 0);
                    double longitude = data.getDoubleExtra(AppConstant.EXTRA_LONGITUDE, 0);
                    String address = data.getStringExtra(AppConstant.EXTRA_ADDRESS);
                    String snapshot = data.getStringExtra(AppConstant.EXTRA_SNAPSHOT);

                    if (latitude != 0 && longitude != 0 && !TextUtils.isEmpty(address)
                            && !TextUtils.isEmpty(snapshot)) {
                        sendLocate(latitude, longitude, address, snapshot);
                    } else {
                        // ToastUtil.showToast(mContext, "??????????????????!");
                        ToastUtil.showToast(mContext, InternationalizationHelper.getString("JXLoc_StartLocNotice"));
                    }
                    break;
                case REQUEST_CODE_SEND_COLLECTION: {
                    String json = data.getStringExtra("data");
                    CollectionEvery collection = JSON.parseObject(json, CollectionEvery.class);
                    clickCollectionSend(collection);
                    break;
                }
                case REQUEST_CODE_QUICK_SEND:
                    String image = QuickSendPreviewActivity.parseResult(data);
                    sendImage(new File(image));
                    break;
                case REQUEST_CODE_SEND_CONTACT: {
                    List<Contacts> contactsList = SendContactsActivity.parseResult(data);
                    if (contactsList == null) {
                        ToastUtil.showToast(mContext, R.string.simple_data_error);
                    } else {
                        sendContacts(contactsList);
                    }
                    break;
                }
                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        } else {
            switch (requestCode) {
                case REQUEST_CODE_SEND_RED:
                    if (data != null && data.getExtras() != null) {
                        Bundle bundle = data.getExtras();
                       String money = bundle.getString("money"); // ??????
                      // ?????????????????????
                        String words = resultCode == REQUEST_CODE_SEND_RED_PT ? bundle.getString("greetings") : bundle.getString("password");
                        String count = bundle.getString("count"); // ??????
                        String type = bundle.getString("type");   // ??????
                       String payPassword = bundle.getString("payPassword");   // ???????????????
                        sendRed(type, money, count, words, payPassword);
                       // RedPacket redPacket = (RedPacket) bundle.getSerializable("redPacket");
                       // sendRed(redPacket);
                    }
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    // ?????????????????? ??????
    private void photograph(final File file) {
        Log.e("zq", "?????????????????????:" + file.getPath() + "?????????????????????:" + file.length() / 1024 + "KB");
        // ?????????????????????Luban???????????????
        Luban.with(this)
                .load(file)
                .ignoreBy(100)     // ????????????100kb ?????????
                // .putGear(2)     // ?????????????????????????????????
                // .setTargetDir() // ??????????????????????????????
                .setCompressListener(new OnCompressListener() { // ????????????
                    @Override
                    public void onStart() {
                        Log.e("zq", "????????????");
                    }

                    @Override
                    public void onSuccess(File file) {
                        Log.e("zq", "????????????????????????????????????:" + file.getPath() + "?????????????????????:" + file.length() / 1024 + "KB");
                        sendImage(file);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("zq", "????????????,????????????");
                        sendImage(file);
                    }
                }).launch();// ????????????
    }

    // ?????????????????? ??????
    private void album(ArrayList<String> stringArrayListExtra, boolean isOriginal) {
        if (isOriginal) {// ????????????????????????
            Log.e("zq", "???????????????????????????????????????");
            for (int i = 0; i < stringArrayListExtra.size(); i++) {
                sendImage(new File(stringArrayListExtra.get(i)));
            }
            Log.e("zq", "???????????????????????????????????????");
            return;
        }

        List<File> fileList = new ArrayList<>();
        for (int i = 0; i < stringArrayListExtra.size(); i++) {
            // gif??????????????????
            if (stringArrayListExtra.get(i).endsWith("gif")) {
                fileList.add(new File(stringArrayListExtra.get(i)));
                stringArrayListExtra.remove(i);
            } else {
                // Luban????????????????????????????????????????????????????????????????????????
                // ???????????????????????????
                List<String> lubanSupportFormatList = Arrays.asList("jpg", "jpeg", "png", "webp", "gif");
                boolean support = false;
                for (int j = 0; j < lubanSupportFormatList.size(); j++) {
                    if (stringArrayListExtra.get(i).endsWith(lubanSupportFormatList.get(j))) {
                        support = true;
                        break;
                    }
                }
                if (!support) {
                    fileList.add(new File(stringArrayListExtra.get(i)));
                    stringArrayListExtra.remove(i);
                }
            }
        }

        if (fileList.size() > 0) {
            for (File file : fileList) {// ?????????????????????????????????
                sendImage(file);
            }
        }

        Luban.with(this)
                .load(stringArrayListExtra)
                .ignoreBy(100)// ????????????100kb ?????????
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {
                        Log.e("zq", "????????????");
                    }

                    @Override
                    public void onSuccess(File file) {
                        sendImage(file);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }).launch();// ????????????
    }

    /*******************************************
     * ?????????EventBus??????????????????
     ******************************************/
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventRequert message) {
        requstImageText(message.url);
    }

    private void requstImageText(String url) {
        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("TAG", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage(), error);
                ToastUtil.showToast(mContext, error.getMessage());
            }
        });
        MyApplication.getInstance().getFastVolley().addDefaultRequest(null, stringRequest);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventGpu message) {// ????????????
        photograph(new File(message.event));
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventTransfer message) {
        mChatContentView.postDelayed(() -> {
            if (message.getChatMessage().getType() == XmppMessage.TYPE_TRANSFER) {// ??????????????????
                mChatMessages.add(message.getChatMessage());
                mChatContentView.notifyDataSetInvalidated(true);
                sendMessage(message.getChatMessage());
            } else {// ?????????????????? || ????????????
                for (int i = 0; i < mChatMessages.size(); i++) {
                    if (TextUtils.equals(mChatMessages.get(i).getPacketId(),
                            message.getChatMessage().getPacketId())) {
                        if (message.getChatMessage().getType() == TransferMoneyDetailActivity.EVENT_REISSUE_TRANSFER) {
                            ChatMessage chatMessage = mChatMessages.get(i).clone(false);
                            mChatMessages.add(chatMessage);
                            mChatContentView.notifyDataSetInvalidated(true);
                            sendMessage(chatMessage);
                        } else {
                            mChatMessages.get(i).setFileSize(2);
                            ChatMessageDao.getInstance().updateChatMessageReceiptStatus(mLoginUserId, mFriend.getUserId(), message.getChatMessage().getPacketId());
                            mChatContentView.notifyDataSetChanged();
                        }
                    }
                }
            }
        }, 50);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEvent message) {
        Log.e("xuan", "helloEventBus  MessageEvent: " + message.message);
        if (mDelayDelMaps == null || mDelayDelMaps.isEmpty() || mChatMessages == null || mChatMessages.size() == 0) {
            return;
        }

        for (ChatMessage chatMessage : mChatMessages) {
            if (chatMessage.getFilePath().equals(message.message) && mDelayDelMaps.contains(chatMessage.getPacketId())) {
                String packedId = chatMessage.getPacketId();

                if (ChatMessageDao.getInstance().deleteSingleChatMessage(mLoginUserId, mFriend.getUserId(), packedId)) {
                    Log.e("xuan", "???????????? ");
                } else {
                    Log.e("xuan", "???????????? " + packedId);
                }
                mDelayDelMaps.remove(packedId);
                mChatContentView.removeItemMessage(packedId);
                break;
            }
        }
    }

    // ?????????????????????
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventClickFire message) {
        Log.e("xuan", "helloEventBus: " + message.event + " ,  " + message.packedId);
        if ("delete".equals(message.event)) {
            mDelayDelMaps.remove(message.packedId);
            ChatMessageDao.getInstance().deleteSingleChatMessage(mLoginUserId, mFriend.getUserId(), message.packedId);
            mChatContentView.removeItemMessage(message.packedId);
        } else if ("delay".equals(message.event)) {
            mDelayDelMaps.add(message.packedId);
        }
    }


    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventClickable message) {
        if (message.event.isMySend()) {
            shake(0);
        } else {
            shake(1);
        }
    }

    // ??????????????????
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventMoreSelected message) {
        List<ChatMessage> mSelectedMessageList = new ArrayList<>();
        if (message.getToUserId().equals("MoreSelectedCollection") || message.getToUserId().equals("MoreSelectedEmail")) {// ?????? ?????? || ??????
            moreSelected(false, 0);
            return;
        }
        if (message.getToUserId().equals("MoreSelectedDelete")) {// ?????? ??????
            for (int i = 0; i < mChatMessages.size(); i++) {
                if (mChatMessages.get(i).isMoreSelected) {
                    if (ChatMessageDao.getInstance().deleteSingleChatMessage(mLoginUserId, mFriend.getUserId(), mChatMessages.get(i))) {
                        Log.e("more_selected", "????????????");
                    } else {
                        Log.e("more_selected", "????????????");
                    }
                    mSelectedMessageList.add(mChatMessages.get(i));
                }
            }

            String mMsgIdListStr = "";
            for (int i = 0; i < mSelectedMessageList.size(); i++) {
                if (i == mSelectedMessageList.size() - 1) {
                    mMsgIdListStr += mSelectedMessageList.get(i).getPacketId();
                } else {
                    mMsgIdListStr += mSelectedMessageList.get(i).getPacketId() + ",";
                }
            }
            deleteMessage(mMsgIdListStr);// ????????????????????????

            mChatMessages.removeAll(mSelectedMessageList);
        } else {// ?????? ??????
            if (message.isSingleOrMerge()) {// ????????????
                List<String> mStringHistory = new ArrayList<>();
                for (int i = 0; i < mChatMessages.size(); i++) {
                    if (mChatMessages.get(i).isMoreSelected) {
                        String body = mChatMessages.get(i).toJsonString();
                        mStringHistory.add(body);
                    }
                }
                String detail = JSON.toJSONString(mStringHistory);
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setType(XmppMessage.TYPE_CHAT_HISTORY);
                chatMessage.setFromUserId(mLoginUserId);
                chatMessage.setFromUserName(mLoginNickName);
                chatMessage.setToUserId(message.getToUserId());
                chatMessage.setContent(detail);
                chatMessage.setMySend(true);
                chatMessage.setReSendCount(0);
                chatMessage.setSendRead(false);
                chatMessage.setIsEncrypt(0);
                chatMessage.setIsReadDel(0);
                String s = TextUtils.isEmpty(mFriend.getRemarkName()) ? mFriend.getNickName() : mFriend.getRemarkName();
                chatMessage.setObjectId(getString(R.string.chat_history_place_holder, s, mLoginNickName));
                chatMessage.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
                chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, message.getToUserId(), chatMessage);
                if (message.isGroupMsg()) {
                    coreManager.sendMucChatMessage(message.getToUserId(), chatMessage);
                } else {
                    coreManager.sendChatMessage(message.getToUserId(), chatMessage);
                }
                if (message.getToUserId().equals(mFriend.getUserId())) {// ?????????????????????
                    mChatMessages.add(chatMessage);
                }
            } else {// ????????????
                for (int i = 0; i < mChatMessages.size(); i++) {
                    if (mChatMessages.get(i).isMoreSelected) {
                        ChatMessage chatMessage = ChatMessageDao.getInstance().findMsgById(mLoginUserId, mFriend.getUserId(), mChatMessages.get(i).getPacketId());
                        if (chatMessage.getType() == XmppMessage.TYPE_RED) {
                            chatMessage.setType(XmppMessage.TYPE_TEXT);
                            chatMessage.setContent(getString(R.string.msg_red_packet));
                        } else if (chatMessage.getType() >= XmppMessage.TYPE_IS_CONNECT_VOICE
                                && chatMessage.getType() <= XmppMessage.TYPE_EXIT_VOICE) {
                            chatMessage.setType(XmppMessage.TYPE_TEXT);
                            chatMessage.setContent(getString(R.string.msg_video_voice));
                        } else if (chatMessage.getType() == XmppMessage.TYPE_SHAKE) {
                            chatMessage.setType(XmppMessage.TYPE_TEXT);
                            chatMessage.setContent(getString(R.string.msg_shake));
                        }
                        chatMessage.setFromUserId(mLoginUserId);
                        chatMessage.setFromUserName(mLoginNickName);
                        chatMessage.setToUserId(message.getToUserId());
                        chatMessage.setUpload(true);
                        chatMessage.setMySend(true);
                        chatMessage.setReSendCount(0);
                        chatMessage.setSendRead(false);
                        chatMessage.setIsEncrypt(0);
                        chatMessage.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
                        chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                        mSelectedMessageList.add(chatMessage);
                    }
                }

                for (int i = 0; i < mSelectedMessageList.size(); i++) {
                    ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, message.getToUserId(), mSelectedMessageList.get(i));
                    if (message.isGroupMsg()) {
                        coreManager.sendMucChatMessage(message.getToUserId(), mSelectedMessageList.get(i));
                    } else {
                        coreManager.sendChatMessage(message.getToUserId(), mSelectedMessageList.get(i));
                    }
                    if (message.getToUserId().equals(mFriend.getUserId())) {// ?????????????????????
                        mChatMessages.add(mSelectedMessageList.get(i));
                    }
                }
            }
        }
        moreSelected(false, 0);
    }

    public void moreSelected(boolean isShow, int position) {
        mChatBottomView.showMoreSelectMenu(isShow);
        if (isShow) {
            findViewById(R.id.iv_title_left).setVisibility(View.GONE);
            mTvTitleLeft.setVisibility(View.VISIBLE);
            if (!mChatMessages.get(position).getIsReadDel()) {// ????????????????????????????????????
                mChatMessages.get(position).setMoreSelected(true);
            }
        } else {
            findViewById(R.id.iv_title_left).setVisibility(View.VISIBLE);
            mTvTitleLeft.setVisibility(View.GONE);
            for (int i = 0; i < mChatMessages.size(); i++) {
                mChatMessages.get(i).setMoreSelected(false);
            }
        }
        mChatContentView.setIsShowMoreSelect(isShow);
        mChatContentView.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageUploadChatRecord message) {
        try {
            final CreateCourseDialog dialog = new CreateCourseDialog(this, new CreateCourseDialog.CoureseDialogConfirmListener() {
                @Override
                public void onClick(String content) {
                    upLoadChatList(message.chatIds, content);
                }
            });
            dialog.show();
        } catch (Exception e) {
            // ???????????????????????????layout?????????????????????????????????findViewById???????????????
            Reporter.unreachable(e);
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventSyncFriendOperating message) {
        if (TextUtils.equals(message.getToUserId(), mFriend.getUserId())) {
            finish();
        }
    }

    private void upLoadChatList(String chatIds, String name) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("messageIds", chatIds);
        params.put("userId", mLoginUserId);
        params.put("courseName", name);
        params.put("createTime", TimeUtils.sk_time_current_time() + "");
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HttpUtils.get().url(coreManager.getConfig().USER_ADD_COURSE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(getApplicationContext(), getString(R.string.tip_create_cource_success));
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    private void register() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(com.ydd.yanshi.broadcast.OtherBroadcast.IsRead);
        intentFilter.addAction("Refresh");
        intentFilter.addAction(com.ydd.yanshi.broadcast.OtherBroadcast.TYPE_INPUT);
        intentFilter.addAction(com.ydd.yanshi.broadcast.OtherBroadcast.MSG_BACK);
        intentFilter.addAction(com.ydd.yanshi.broadcast.OtherBroadcast.NAME_CHANGE);
        intentFilter.addAction(com.ydd.yanshi.broadcast.OtherBroadcast.MULTI_LOGIN_READ_DELETE);
        intentFilter.addAction(Constants.CHAT_MESSAGE_DELETE_ACTION);
        intentFilter.addAction(Constants.SHOW_MORE_SELECT_MENU);
        intentFilter.addAction(com.ydd.yanshi.broadcast.OtherBroadcast.TYPE_DELALL);
        intentFilter.addAction(Constants.CHAT_HISTORY_EMPTY);
        intentFilter.addAction(com.ydd.yanshi.broadcast.OtherBroadcast.QC_FINISH);
        intentFilter.addAction(MsgBroadcast.ACTION_MSG_UPDATE_CHAT);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);
    }

    /*******************************************
     * ?????????ActionBar??????????????????
     ******************************************/
    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doBack();
            }
        });

        mTvTitleLeft = (TextView) findViewById(R.id.tv_title_left);
        mTvTitleLeft.setVisibility(View.GONE);
        mTvTitleLeft.setText(getString(R.string.cancel));
        mTvTitleLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreSelected(false, 0);
            }
        });
        mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        String remarkName = mFriend.getRemarkName();
        if (TextUtils.isEmpty(remarkName)) {
            mTvTitle.setText(mFriend.getNickName());
        } else {
            mTvTitle.setText(remarkName);
        }

        ImageView mMore = (ImageView) findViewById(R.id.iv_title_right);
        mMore.setImageResource(R.drawable.chat_more);
        mMore.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                mChatBottomView.reset();
                mChatBottomView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(ChatActivity.this, PersonSettingActivity.class);
                        intent.putExtra("ChatObjectId", mFriend.getUserId());
                        startActivity(intent);
                    }
                }, 100);
            }
        });

        if (mFriend.getUserId().equals(Friend.ID_SYSTEM_MESSAGE)
                || mFriend.getIsDevice() == 1) {// ???????????????????????? || ???????????? ??????
            mMore.setVisibility(View.INVISIBLE);
        }

        // ??????????????????
        mChatBgIv = findViewById(R.id.chat_bg);
        loadBackdrop();
    }

    public void loadBackdrop() {
        String mChatBgPath = PreferenceUtils.getString(this, Constants.SET_CHAT_BACKGROUND_PATH
                + mFriend.getUserId() + mLoginUserId, "reset");

        String mChatBg = PreferenceUtils.getString(this, Constants.SET_CHAT_BACKGROUND
                + mFriend.getUserId() + mLoginUserId, "reset");

        if (TextUtils.isEmpty(mChatBgPath)
                || mChatBg.equals("reset")) {// ????????????????????????????????????????????????
            mChatBgIv.setImageDrawable(null);
            return;
        }

        File file = new File(mChatBgPath);
        if (file.exists()) {// ????????????
            if (mChatBgPath.toLowerCase().endsWith("gif")) {
                try {
                    GifDrawable gifDrawable = new GifDrawable(file);
                    mChatBgIv.setImageDrawable(gifDrawable);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Glide.with(ChatActivity.this)
                        .load(file)
                        .error(R.drawable.fez)
                        .dontAnimate().skipMemoryCache(true) // ?????????????????????
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // ?????????????????????
                        .into(mChatBgIv);
            }
        } else {// ????????????
            Glide.with(ChatActivity.this)
                    .load(mChatBg)
                    .error(getResources().getDrawable(R.color.chat_bg))
                    .dontAnimate().skipMemoryCache(true) // ?????????????????????
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // ?????????????????????
                    .into(mChatBgIv);
        }
    }

    /*******************************************
     * ?????????????????????&&????????????????????????
     ******************************************/
    private void initFriendState() {
        if (mFriend.getIsDevice() == 1) {
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", mFriend.getUserId());

        HttpUtils.get().url(coreManager.getConfig().USER_GET_URL)
                .params(params)
                .build()
                .execute(new BaseCallback<User>(User.class) {
                    @Override
                    public void onResponse(ObjectResult<User> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            User user = result.getData();
                            if (user.getUserType() == 2) {
                                // ?????????,?????????????????????
                                initSpecialMenu();
                                return;
                            }
                            if (coreManager.getConfig().isOpenOnlineStatus) {
                                String name = mTvTitle.getText().toString();
                                switch (user.getOnlinestate()) {
                                    case 0:
                                        mTvTitle.setText(name + "(" + InternationalizationHelper.getString("JX_OffLine") + ")");
                                        break;
                                    case 1:
                                        mTvTitle.setText(name + "(" + InternationalizationHelper.getString("JX_OnLine") + ")");
                                        break;
                                }
                            }
                            if (user.getFriends() != null) {// ??????????????????????????? && ????????????????????????...
                                FriendDao.getInstance().updateFriendPartStatus(mFriend.getUserId(), user);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    private void initSpecialMenu() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", mFriend.getUserId());

        HttpUtils.get().url(coreManager.getConfig().USER_GET_PUBLIC_MENU)
                .params(params)
                .build()
                .execute(new ListCallback<PublicMenu>(PublicMenu.class) {
                    @Override
                    public void onResponse(ArrayResult<PublicMenu> result) {
                        List<PublicMenu> data = result.getData();
                        if (data != null && data.size() > 0) {
                            mChatBottomView.fillRoomMenu(data);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    public void synchronizeChatHistory() {
        // ??????????????????????????????????????????????????????????????????????????????????????????????????????
        // ?????????????????????????????????????????????
        mChatContentView.setNeedRefresh(false);

        long startTime;
        String chatSyncTimeLen = String.valueOf(PrivacySettingHelper.getPrivacySettings(this).getChatSyncTimeLen());
        chatSyncTimeLen="0";
        if (Double.parseDouble(chatSyncTimeLen) == -2) {// ?????????
            mChatContentView.setNeedRefresh(true);
            FriendDao.getInstance().updateDownloadTime(mLoginUserId, mFriend.getUserId(), mFriend.getTimeSend());
            return;
        }
        if (Double.parseDouble(chatSyncTimeLen) == -1 || Double.parseDouble(chatSyncTimeLen) == 0) {// ?????? ?????? startTime == downloadTime
            startTime = mFriend.getDownloadTime();
        } else {
            long syncTimeLen = (long) (Double.parseDouble(chatSyncTimeLen) * 24 * 60 * 60);// ????????????????????????
            if (mFriend.getTimeSend() - mFriend.getDownloadTime() <= syncTimeLen) {// ???????????????????????????
                startTime = mFriend.getDownloadTime();
            } else {// ??????????????????????????????????????????????????????
                startTime = mFriend.getTimeSend() - syncTimeLen;
            }
        }

        Map<String, String> params = new HashMap();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("receiver", mFriend.getUserId());
        params.put("startTime", String.valueOf(startTime * 1000));// 2010-01-01 00:00:00  ???????????????????????????????????????
        params.put("endTime", String.valueOf(mFriend.getTimeSend() * 1000));
        params.put("pageSize", String.valueOf(Constants.MSG_ROMING_PAGE_SIZE));// ???????????????????????? ??????????????????
        // params.put("pageIndex", "0");

        HttpUtils.get().url(coreManager.getConfig().GET_CHAT_MSG)
                .params(params)
                .build()
                .execute(new ListCallback<ChatRecord>(ChatRecord.class) {
                    @Override
                    public void onResponse(ArrayResult<ChatRecord> result) {
                        FriendDao.getInstance().updateDownloadTime(mLoginUserId, mFriend.getUserId(), mFriend.getTimeSend());

                        final List<ChatRecord> chatRecordList = result.getData();
                        if (chatRecordList != null && chatRecordList.size() > 0) {
                            new Thread(() -> {
                                chatMessages = new ArrayList<>();

                                for (int i = 0; i < chatRecordList.size(); i++) {
                                    ChatRecord data = chatRecordList.get(i);
                                    String messageBody = data.getBody();
                                    messageBody = messageBody.replaceAll("&quot;", "\"");
                                    ChatMessage chatMessage = new ChatMessage(messageBody);

                                    if (!TextUtils.isEmpty(chatMessage.getFromUserId()) &&
                                            chatMessage.getFromUserId().equals(mLoginUserId)) {
                                        chatMessage.setMySend(true);
                                    }

                                    chatMessage.setSendRead(data.getIsRead() > 0); // ???????????????????????????????????????
                                    // ????????????????????????
                                    chatMessage.setUpload(true);
                                    chatMessage.setUploadSchedule(100);
                                    chatMessage.setMessageState(MESSAGE_SEND_SUCCESS);

                                    if (TextUtils.isEmpty(chatMessage.getPacketId())) {
                                        if (!TextUtils.isEmpty(data.getMessageId())) {
                                            chatMessage.setPacketId(data.getMessageId());
                                        } else {
                                            chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                                        }
                                    }

                                    if (ChatMessageDao.getInstance().roamingMessageFilter(chatMessage.getType())) {
                                        ChatMessageDao.getInstance().decryptDES(chatMessage);
                                        ChatMessageDao.getInstance().handlerRoamingSpecialMessage(chatMessage);
                                        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, mFriend.getUserId(), chatMessage)) {
                                            chatMessages.add(chatMessage);
                                        }
                                    }
                                }

                                mTvTitle.post(() -> {
                                    for (int i = chatMessages.size() - 1; i >= 0; i--) {
                                        mChatMessages.add(chatMessages.get(i));
                                    }
                                    // ????????????????????????????????????????????????????????????mChatMessages????????????
                                    Comparator<ChatMessage> comparator = (c1, c2) -> (int) (c1.getDoubleTimeSend() - c2.getDoubleTimeSend());
                                    Collections.sort(mChatMessages, comparator);
                                    mChatContentView.notifyDataSetInvalidated(true);

                                    mChatContentView.setNeedRefresh(true);
                                });
                            }).start();
                        } else {
                            mChatContentView.setNeedRefresh(true);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        mChatContentView.setNeedRefresh(true);
                        ToastUtil.showErrorData(ChatActivity.this);
                    }
                });
    }

    public void getNetSingle() {
        Map<String, String> params = new HashMap();
        long endTime;
        if (mChatMessages != null && mChatMessages.size() > 0) {// ???????????????????????????????????????????????????????????????timeSend
            endTime = mChatMessages.get(0).getTimeSend();
        } else {// ?????????????????????????????????????????????
            endTime = TimeUtils.sk_time_current_time();
        }

        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("receiver", mFriend.getUserId());
        params.put("startTime", "1262275200000");// 2010-01-01 00:00:00  ???????????????????????????????????????
        params.put("endTime", String.valueOf(endTime * 1000));
        params.put("pageSize", String.valueOf(Constants.MSG_ROMING_PAGE_SIZE));
        params.put("pageIndex", "0");

        HttpUtils.get().url(coreManager.getConfig().GET_CHAT_MSG)
                .params(params)
                .build()
                .execute(new ListCallback<ChatRecord>(ChatRecord.class) {
                    @Override
                    public void onResponse(ArrayResult<ChatRecord> result) {
                        List<ChatRecord> chatRecordList = result.getData();

                        if (chatRecordList != null && chatRecordList.size() > 0) {
                            long currTime = TimeUtils.sk_time_current_time();
                            for (int i = 0; i < chatRecordList.size(); i++) {
                                ChatRecord data = chatRecordList.get(i);
                                String messageBody = data.getBody();
                                messageBody = messageBody.replaceAll("&quot;", "\"");
                                ChatMessage chatMessage = new ChatMessage(messageBody);

                                // ?????????????????????????????????1?????????????????????????????????????????????????????????????????????????????????
                                if (chatMessage.getDeleteTime() > 1 && chatMessage.getDeleteTime() < currTime) {
                                    // ??????????????????,??????
                                    continue;
                                }

                                if (!TextUtils.isEmpty(chatMessage.getFromUserId()) &&
                                        chatMessage.getFromUserId().equals(mLoginUserId)) {
                                    chatMessage.setMySend(true);
                                }

                                chatMessage.setSendRead(data.getIsRead() > 0); // ???????????????????????????????????????
                                // ????????????????????????
                                chatMessage.setUpload(true);
                                chatMessage.setUploadSchedule(100);
                                chatMessage.setMessageState(MESSAGE_SEND_SUCCESS);

                                if (TextUtils.isEmpty(chatMessage.getPacketId())) {
                                    if (!TextUtils.isEmpty(data.getMessageId())) {
                                        chatMessage.setPacketId(data.getMessageId());
                                    } else {
                                        chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                                    }
                                }

                                if (ChatMessageDao.getInstance().roamingMessageFilter(chatMessage.getType())) {
                                    ChatMessageDao.getInstance().saveRoamingChatMessage(mLoginUserId, mFriend.getUserId(), chatMessage);
                                }
                            }
                            mHasMoreData = chatRecordList.size() == Constants.MSG_ROMING_PAGE_SIZE;
                            notifyChatAdapter();
                        } else {
                            mHasMoreData = false;
                            mChatContentView.headerRefreshingCompleted();
                            mChatContentView.setNeedRefresh(false);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }

    private void notifyChatAdapter() {
        if (mChatMessages.size() > 0) {
            mMinId = mChatMessages.get(0).getTimeSend();
        } else {
            ChatMessage chat = ChatMessageDao.getInstance().getLastChatMessage(mLoginUserId, mFriend.getUserId());
            if (chat != null && chat.getTimeSend() != 0) {
                mMinId = chat.getTimeSend() + 2;
            } else {
                mMinId = TimeUtils.sk_time_current_time();
            }
        }
        // ?????????????????????????????? mMinId ?????????????????????????????????????????????????????????????????? mMinId ?????????????????????
        List<ChatMessage> chatLists = ChatMessageDao.getInstance().getSingleChatMessages(mLoginUserId,
                mFriend.getUserId(), mMinId, mPageSize);
        if (chatLists == null || chatLists.size() == 0) {
            mHasMoreData = false;
            mChatContentView.headerRefreshingCompleted();
            mChatContentView.setNeedRefresh(false);
            return;
        }

        for (int i = 0; i < chatLists.size(); i++) {
            ChatMessage message = chatLists.get(i);
            mChatMessages.add(0, message);
        }

        // ??????timeSend????????????
       /* Collections.sort(mChatMessages, new Comparator<ChatMessage>() {
            @Override
            public int compare(ChatMessage o1, ChatMessage o2) {
                return (int) (o1.getDoubleTimeSend() - o2.getDoubleTimeSend());
            }
        });*/

        mChatContentView.notifyDataSetAddedItemsToTop(chatLists.size());
        mChatContentView.headerRefreshingCompleted();
        if (!mHasMoreData) {
            mChatContentView.setNeedRefresh(false);
        }
    }

    /*******************************************
     * ??????&&??????
     ******************************************/
    private void instantChatMessage() {
        if (!TextUtils.isEmpty(instantMessage)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String toUserId = getIntent().getStringExtra("fromUserId");
                    ChatMessage chatMessage = ChatMessageDao.getInstance().findMsgById(mLoginUserId, toUserId, instantMessage);
                    chatMessage.setFromUserId(mLoginUserId);
                    chatMessage.setFromUserName(mLoginNickName);
                    chatMessage.setToUserId(mFriend.getUserId());
                    chatMessage.setUpload(true);
                    chatMessage.setMySend(true);
                    chatMessage.setReSendCount(5);
                    chatMessage.setSendRead(false);
                    // ???????????????????????????????????????????????????????????????????????????content??????????????????????????????????????????isEncrypt??????????????????
                    // ?????????????????????????????????????????????????????????????????????????????????????????????????????????
                    chatMessage.setIsEncrypt(0);
                    chatMessage.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
                    chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                    mChatMessages.add(chatMessage);
                    mChatContentView.notifyDataSetInvalidated(true);
                    ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, mFriend.getUserId(), chatMessage);
                    coreManager.sendChatMessage(mFriend.getUserId(), chatMessage);
                    instantMessage = null;
                }
            }, 1000);
        }
    }

    public boolean interprect() {
        for (Friend friend : mBlackList) {
            if (friend.getUserId().equals(mFriend.getUserId())) {
                return true;
            }
        }
        return false;
    }

    /*******************************************
     * ????????????&&??????
     ******************************************/
    public boolean isAuthenticated() {
        boolean isLogin = coreManager.isLogin();
        if (!isLogin) {
            coreManager.autoReconnect(this);
        }
        // Todo ???????????????????????????return???????????????...??????????????????(?????????)
        // return !isLogin;
        return false;
    }

    /*******************************************
     * ?????????????????????????????????
     ******************************************/
    public class RefreshBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(com.ydd.yanshi.broadcast.OtherBroadcast.IsRead)) {
                // ????????????????????? ??????
                Bundle bundle = intent.getExtras();
                String packetId = bundle.getString("packetId");
                boolean isReadChange = bundle.getBoolean("isReadChange");
                for (int i = 0; i < mChatMessages.size(); i++) {
                    ChatMessage msg = mChatMessages.get(i);
                    if (msg.getPacketId().equals(packetId)) {
                        msg.setSendRead(true);// ???????????????
                        if (isReadChange) {// ?????????????????? ??????????????????????????????
                            ChatMessage msgById = ChatMessageDao.getInstance().findMsgById(mLoginUserId, mFriend.getUserId(), packetId);
                            if (msgById != null) {
                                if (msg.getType() == XmppMessage.TYPE_VOICE) {
                                    if (!TextUtils.isEmpty(VoicePlayer.instance().getVoiceMsgId())
                                            && packetId.equals(VoicePlayer.instance().getVoiceMsgId())) {// ??????????????????????????????????????????... ??????????????????
                                        VoicePlayer.instance().stop();
                                    }
                                } else if (msg.getType() == XmppMessage.TYPE_VIDEO) {
                                    if (!TextUtils.isEmpty(JCMediaManager.CURRENT_PLAYING_URL)
                                            && msg.getContent().equals(JCMediaManager.CURRENT_PLAYING_URL)) {// ??????????????????????????????????????????... ?????????????????????????????????
                                        JCVideoPlayer.releaseAllVideos();
                                    }
                                }

                                msg.setType(msgById.getType());
                                msg.setContent(msgById.getContent());
                            }
                        }
                        mChatContentView.notifyDataSetInvalidated(false);

                        // ???????????????????????????????????????
                        if (coreManager.getConfig().isOpenOnlineStatus) {
                            String titleContent = mTvTitle.getText().toString();
                            if (titleContent.contains(InternationalizationHelper.getString("JX_OffLine"))) {
                                String changeTitleContent = titleContent.replace(InternationalizationHelper.getString("JX_OffLine"),
                                        InternationalizationHelper.getString("JX_OnLine"));
                                mTvTitle.setText(changeTitleContent);
                            }
                        }
                        break;
                    }
                }
            } else if (action.equals("Refresh")) {
                Bundle bundle = intent.getExtras();
                String packetId = bundle.getString("packetId");
                String fromId = bundle.getString("fromId");
                int type = bundle.getInt("type");
               /* if (type == XmppMessage.TYPE_INPUT && mFriend.getUserId().equals(fromId)) {
                    // ??????????????????...
                    nameTv.setText(InternationalizationHelper.getString("JX_Entering"));
                    time.cancel();
                    time.start();
                }*/
                // ????????????????????????????????????????????????????????????????????????????????????????????????????????????
                for (int i = 0; i < mChatMessages.size(); i++) {
                    ChatMessage msg = mChatMessages.get(i);
                    // ??????packetId???????????????????????????????????????
                    if (msg.getPacketId() == null) {
                        // ??????????????????????????????????????????false???????????????????????????????????????????????????????????????????????????
                        msg.setSendRead(false); // ??????????????????????????????
                        msg.setFromUserId(mFriend.getUserId());
                        msg.setPacketId(packetId);
                        break;
                    }
                }
                mChatContentView.notifyDataSetInvalidated(false);
            } else if (action.equals(com.ydd.yanshi.broadcast.OtherBroadcast.TYPE_INPUT)) {
                String fromId = intent.getStringExtra("fromId");
                if (mFriend.getUserId().equals(fromId)) {
                    // ??????????????????...
                    Log.e("zq", "??????????????????...");
                    mTvTitle.setText(InternationalizationHelper.getString("JX_Entering"));
                    time.cancel();
                    time.start();
                }
            } else if (action.equals(com.ydd.yanshi.broadcast.OtherBroadcast.MSG_BACK)) {
                String packetId = intent.getStringExtra("packetId");
                if (TextUtils.isEmpty(packetId)) {
                    return;
                }
                for (ChatMessage chatMessage : mChatMessages) {
                    if (packetId.equals(chatMessage.getPacketId())) {
                        if (chatMessage.getType() == XmppMessage.TYPE_VOICE
                                && !TextUtils.isEmpty(VoicePlayer.instance().getVoiceMsgId())
                                && packetId.equals(VoicePlayer.instance().getVoiceMsgId())) {// ?????? && ???????????????msgId????????? ?????????msgId==???????????????msgId
                            // ??????????????????
                            VoicePlayer.instance().stop();
                        }
                        ChatMessage chat = ChatMessageDao.getInstance().findMsgById(mLoginUserId, mFriend.getUserId(), packetId);
                        chatMessage.setType(chat.getType());
                        chatMessage.setContent(chat.getContent());
                        break;
                    }
                }
                mChatContentView.notifyDataSetInvalidated(true);
            } else if (action.equals(com.ydd.yanshi.broadcast.OtherBroadcast.NAME_CHANGE)) {// ???????????????
                mFriend = FriendDao.getInstance().getFriend(mLoginUserId, mFriend.getUserId());
                if (coreManager.getConfig().isOpenOnlineStatus) {
                    String s = mTvTitle.getText().toString();
                    if (s.contains(InternationalizationHelper.getString("JX_OnLine"))) {
                        mTvTitle.setText(TextUtils.isEmpty(mFriend.getRemarkName()) ? mFriend.getNickName() : mFriend.getRemarkName()
                                + "(" + InternationalizationHelper.getString("JX_OnLine") + ")");
                    } else {
                        mTvTitle.setText(TextUtils.isEmpty(mFriend.getRemarkName()) ? mFriend.getNickName() : mFriend.getRemarkName()
                                + "(" + InternationalizationHelper.getString("JX_OffLine") + ")");
                    }
                } else {
                    mTvTitle.setText(TextUtils.isEmpty(mFriend.getNickName()) ? mFriend.getNickName() : mFriend.getRemarkName());
                }
            } else if (action.equals(com.ydd.yanshi.broadcast.OtherBroadcast.MULTI_LOGIN_READ_DELETE)) {// ?????? ???????????? ???????????? ??????????????????????????????
                String packet = intent.getStringExtra("MULTI_LOGIN_READ_DELETE_PACKET");
                if (!TextUtils.isEmpty(packet)) {

                    for (int i = 0; i < mChatMessages.size(); i++) {
                        if (mChatMessages.get(i).getPacketId().equals(packet)) {
                            mChatMessages.remove(i);
                            mChatContentView.notifyDataSetInvalidated(true);
                            break;
                        }
                    }
                }
            } else if (action.equals(Constants.CHAT_MESSAGE_DELETE_ACTION)) {

                if (mChatMessages == null || mChatMessages.size() == 0) {
                    return;
                }

                // ??????????????????
                int position = intent.getIntExtra(Constants.CHAT_REMOVE_MESSAGE_POSITION, -1);
                if (position >= 0 && position < mChatMessages.size()) { // ?????????postion
                    ChatMessage message = mChatMessages.get(position);
                    deleteMessage(message.getPacketId());// ????????????????????????
                    if (ChatMessageDao.getInstance().deleteSingleChatMessage(mLoginUserId, mFriend.getUserId(), message)) {
                        mChatMessages.remove(position);
                        mChatContentView.notifyDataSetInvalidated(true);
                        Toast.makeText(mContext, InternationalizationHelper.getString("JXAlert_DeleteOK"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, R.string.delete_failed, Toast.LENGTH_SHORT).show();
                    }
                }

            } else if (action.equals(Constants.SHOW_MORE_SELECT_MENU)) {// ??????????????????
                int position = intent.getIntExtra(Constants.CHAT_SHOW_MESSAGE_POSITION, 0);
                moreSelected(true, position);
            } else if (action.equals(com.ydd.yanshi.broadcast.OtherBroadcast.TYPE_DELALL)) {
                // ????????? || ??????  @see XChatManger 190
                // ????????????????????????xmpp 512,
                String toUserId = intent.getStringExtra("toUserId");
                // ????????????????????????????????????????????????????????????
                if (Objects.equals(mFriend.getUserId(), toUserId)) {
                    String content = intent.getStringExtra("content");
                    if (!TextUtils.isEmpty(content)) {
                        ToastUtil.showToast(mContext, content);
                    }
                    Intent mainIntent = new Intent(mContext, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }
            } else if (action.equals(Constants.CHAT_HISTORY_EMPTY)) {// ??????????????????
                mChatMessages.clear();
                mChatContentView.notifyDataSetChanged();
            } else if (action.equals(com.ydd.yanshi.broadcast.OtherBroadcast.QC_FINISH)) {
                int mOperationCode = intent.getIntExtra("Operation_Code", 0);
                if (mOperationCode == 1) {// ???????????????????????? ??????????????????
                    loadBackdrop();
                } else {// ???????????????????????? ??????????????????
                    finish();
                }
            } else if (action.equals(MsgBroadcast.ACTION_MSG_UPDATE_CHAT)) {
                String packetId = intent.getStringExtra("packetId");
                ChatMessage chatMessage = ChatMessageDao.getInstance().findMsgById(mLoginUserId, mFriend.getUserId(), packetId);
                if (chatMessage != null) {
                    mChatMessages.add(chatMessage);
                    mChatContentView.notifyDataSetChanged();
                }
            }
        }
    }
}

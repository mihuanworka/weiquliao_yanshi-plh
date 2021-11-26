package com.ydd.yanshi.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.coloros.mcssdk.PushManager;
import com.example.qrcode.Constant;
import com.example.qrcode.ScannerActivity;
import com.fanjun.keeplive.KeepLive;
import com.fanjun.keeplive.config.ForegroundNotification;
import com.fanjun.keeplive.config.ForegroundNotificationClickListener;
import com.fanjun.keeplive.config.KeepLiveService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;
import com.ydd.yanshi.AppConstant;
import com.ydd.yanshi.BuildConfig;
import com.ydd.yanshi.MyApplication;
import com.ydd.yanshi.R;
import com.ydd.yanshi.Reporter;
import com.ydd.yanshi.adapter.MessageContactEvent;
import com.ydd.yanshi.adapter.MessageEventBG;
import com.ydd.yanshi.adapter.MessageEventHongdian;
import com.ydd.yanshi.adapter.MessageLogin;
import com.ydd.yanshi.adapter.MessageSendChat;
import com.ydd.yanshi.bean.Contact;
import com.ydd.yanshi.bean.Contacts;
import com.ydd.yanshi.bean.EventCreateGroupFriend;
import com.ydd.yanshi.bean.EventSendVerifyMsg;
import com.ydd.yanshi.bean.Friend;
import com.ydd.yanshi.bean.UploadingFile;
import com.ydd.yanshi.bean.User;
import com.ydd.yanshi.bean.circle.FindItem;
import com.ydd.yanshi.bean.message.ChatMessage;
import com.ydd.yanshi.bean.message.MucRoom;
import com.ydd.yanshi.bean.message.XmppMessage;
import com.ydd.yanshi.broadcast.MsgBroadcast;
import com.ydd.yanshi.broadcast.MucgroupUpdateUtil;
import com.ydd.yanshi.broadcast.OtherBroadcast;
import com.ydd.yanshi.broadcast.TimeChangeReceiver;
import com.ydd.yanshi.broadcast.UpdateUnReadReceiver;
import com.ydd.yanshi.broadcast.UserLogInOutReceiver;
import com.ydd.yanshi.call.AudioOrVideoController;
import com.ydd.yanshi.db.InternationalizationHelper;
import com.ydd.yanshi.db.dao.ChatMessageDao;
import com.ydd.yanshi.db.dao.ContactDao;
import com.ydd.yanshi.db.dao.FriendDao;
import com.ydd.yanshi.db.dao.MyZanDao;
import com.ydd.yanshi.db.dao.NewFriendDao;
import com.ydd.yanshi.db.dao.OnCompleteListener2;
import com.ydd.yanshi.db.dao.UploadingFileDao;
import com.ydd.yanshi.db.dao.UserDao;
import com.ydd.yanshi.db.dao.login.MachineDao;
import com.ydd.yanshi.downloader.UpdateManger;
import com.ydd.yanshi.fragment.DiscoverFragment;
import com.ydd.yanshi.fragment.FriendFragment;
import com.ydd.yanshi.fragment.MeFragment;
import com.ydd.yanshi.fragment.MessageFragment;
import com.ydd.yanshi.fragment.SquareFragment;
import com.ydd.yanshi.helper.DialogHelper;
import com.ydd.yanshi.helper.LoginHelper;
import com.ydd.yanshi.helper.PrivacySettingHelper;
import com.ydd.yanshi.map.MapHelper;
import com.ydd.yanshi.pay.PaymentReceiptMoneyActivity;
import com.ydd.yanshi.pay.ReceiptPayMoneyActivity;
import com.ydd.yanshi.ui.backup.ReceiveChatHistoryActivity;
import com.ydd.yanshi.ui.base.BaseActivity;
import com.ydd.yanshi.ui.base.CoreManager;
import com.ydd.yanshi.ui.lock.DeviceLockActivity;
import com.ydd.yanshi.ui.lock.DeviceLockHelper;
import com.ydd.yanshi.ui.login.WebLoginActivity;
import com.ydd.yanshi.ui.message.MucChatActivity;
import com.ydd.yanshi.ui.other.BasicInfoActivity;
import com.ydd.yanshi.ui.tool.WebViewActivity;
import com.ydd.yanshi.util.AppUtils;
import com.ydd.yanshi.util.AsyncUtils;
import com.ydd.yanshi.util.Constants;
import com.ydd.yanshi.util.ContactsUtil;
import com.ydd.yanshi.util.DeviceInfoUtil;
import com.ydd.yanshi.util.DisplayUtil;
import com.ydd.yanshi.util.FileUtil;
import com.ydd.yanshi.util.HttpUtil;
import com.ydd.yanshi.util.JsonUtils;
import com.ydd.yanshi.util.LocaleHelper;
import com.ydd.yanshi.util.NotificationsDialogFragment;
import com.ydd.yanshi.util.PermissionUtil;
import com.ydd.yanshi.util.PreferenceUtils;
import com.ydd.yanshi.util.RegexUtils;
import com.ydd.yanshi.util.SkinUtils;
import com.ydd.yanshi.util.SyncTimeManager;
import com.ydd.yanshi.util.TimeUtils;
import com.ydd.yanshi.util.ToastUtil;
import com.ydd.yanshi.util.UiUtils;
import com.ydd.yanshi.util.log.LogUtils;
import com.ydd.yanshi.view.PermissionExplainDialog;
import com.ydd.yanshi.view.SelectionFrame;
import com.ydd.yanshi.view.SelectionLongFrame;
import com.ydd.yanshi.view.VerifyDialog;
import com.ydd.yanshi.xmpp.CoreService;
import com.ydd.yanshi.xmpp.helloDemon.FirebaseMessageService;
import com.ydd.yanshi.xmpp.helloDemon.HuaweiClient;
import com.ydd.yanshi.xmpp.helloDemon.MeizuPushMsgReceiver;
import com.ydd.yanshi.xmpp.helloDemon.OppoPushMessageService;
import com.ydd.yanshi.xmpp.helloDemon.VivoPushMessageReceiver;
import com.ydd.yanshi.xmpp.listener.ChatMessageListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import me.leolin.shortcutbadger.ShortcutBadger;
import okhttp3.Call;

/**
 * 主界面
 */
public class MainActivity extends BaseActivity implements PermissionUtil.OnRequestPermissionsResultCallbacks {
    // 小米推送
    public static final String APP_ID = BuildConfig.XIAOMI_APP_ID;
    public static final String APP_KEY = BuildConfig.XIAOMI_APP_KEY;
    /* UserCheck */
    private static final int MSG_USER_CHECK = 0x1;
    private static final int RETRY_CHECK_DELAY_MAX = 30000;// 为成功的情况下，最长30s检测一次
    // 是否重新走initView方法
    // 当切换语言、修改皮肤之后，将该状态置为true
    public static boolean isInitView = false;
    public static boolean isAuthenticated;
    // 音视频通话控制类
    public AudioOrVideoController audioOrVideoController;

    /**
     * 更新我的群组
     */
    Handler mHandler = new Handler();
    private UpdateUnReadReceiver mUpdateUnReadReceiver = null;
    private UserLogInOutReceiver mUserLogInOutReceiver = null;
    private TimeChangeReceiver timeChangeReceiver = null;
    private ActivityManager mActivityManager;
    // ╔═══════════════════════════════界面组件══════════════════════════════╗
    // ╚═══════════════════════════════界面组件══════════════════════════════╝
    private int mLastFragmentId;// 当前界面
    private RadioGroup mRadioGroup;
    private RadioButton mRbTab1, mRbTab2, mRbTab3, mRbTab4;
    private TextView mTvMessageNum;// 显示消息界面未读数量
    private TextView mTvNewFriendNum;// 显示通讯录消息未读数量
    private TextView mTvCircleNum;// 显示朋友圈未读数量
    private int numMessage = 0;// 当前未读消息数量
    private int numCircle = 0; // 当前朋友圈未读数量
    private String mUserId;// 当前登陆的 UserID
    private My_BroadcastReceiver my_broadcastReceiver;
    private int mCurrtTabId;
    private boolean isCreate;
    private int mRetryCheckDelay = 0;
    private Handler mUserCheckHander = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.d(TAG, "handleMessage() called with: msg = [" + msg + "]");
            if (msg.what == MSG_USER_CHECK) {
                if (mRetryCheckDelay < RETRY_CHECK_DELAY_MAX) {
                    mRetryCheckDelay += 5000;
                }
                mUserCheckHander.removeMessages(RETRY_CHECK_DELAY_MAX);
                doUserCheck();
            }

            return false;
        }
    });
    /**
     * 在其他设备登录了，挤下线
     */
    private boolean isConflict;

    public static void start(Context ctx) {
        Intent intent = new Intent(ctx, MainActivity.class);
        ctx.startActivity(intent);
    }

    @Override
    public void onCoreReady() {
        super.onCoreReady();
        audioOrVideoController = new AudioOrVideoController(getApplicationContext(), coreManager);
    }

    /**
     * 发起二维码扫描，
     * 仅供MainActivity下属Fragment调用，
     */
    public static void requestQrCodeScan(Activity ctx) {
        Intent intent = new Intent(ctx, ScannerActivity.class);
        // 设置扫码框的宽
        intent.putExtra(Constant.EXTRA_SCANNER_FRAME_WIDTH, DisplayUtil.dip2px(ctx, 200));
        // 设置扫码框的高
        intent.putExtra(Constant.EXTRA_SCANNER_FRAME_HEIGHT, DisplayUtil.dip2px(ctx, 200));
        // 设置扫码框距顶部的位置
        intent.putExtra(Constant.EXTRA_SCANNER_FRAME_TOP_PADDING, DisplayUtil.dip2px(ctx, 100));
        // 可以从相册获取
        intent.putExtra(Constant.EXTRA_IS_ENABLE_SCAN_FROM_PIC, true);
        ctx.startActivityForResult(intent, 888);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 自动解锁屏幕 | 锁屏也可显示 | Activity启动时点亮屏幕 | 保持屏幕常亮

        //showDialog();
        setContentView(R.layout.activity_main);
        // 手机状态
        permissionsMap.put(Manifest.permission.READ_PHONE_STATE, R.string.permission_phone_status);

        permissionsMap.put(Manifest.permission.READ_CONTACTS, R.string.tip_permission_contacts);
        // 存储权限
        permissionsMap.put(Manifest.permission.READ_EXTERNAL_STORAGE, R.string.permission_storage);
        permissionsMap.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permission_storage);
        if (lacksPermissions(this, permissionsMap.keySet().toArray(new String[]{}))) {
            reqBixuPermiss();
        } else {
            hadPermissInit();
        }
        checkNotifySetting();

        updateSelfData();
//        timeLoop2();
    }

    private static final int PERIOD = 1 * 1000;
    private static final int DELAY = 100;
    private Timer mTimer;
    private TimerTask mTimerTask;

    private void timeLoop2() {
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                //在此添加轮询
                updateRoom();
                updateNumData();
            }
        };
        mTimer.schedule(mTimerTask, DELAY, PERIOD);
    }

    private void checkNotifySetting() {
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        // areNotificationsEnabled方法的有效性官方只最低支持到API 19，低于19的仍可调用此方法不过只会返回true，即默认为用户已经开启了通知。
        boolean isOpened = manager.areNotificationsEnabled();

        if (!isOpened) {
            NotificationsDialogFragment agreement = new NotificationsDialogFragment();
            agreement.show(getSupportFragmentManager(), "lose");

        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // 主要针对侧滑返回，刷新消息会话列表，
        MsgBroadcast.broadcastMsgUiUpdate(mContext);
    }

    private void initMap() {
        // 中国大陆只能使用百度，
        // 墙外且有谷歌框架才能使用谷歌地图，
        String area = PreferenceUtils.getString(this, AppConstant.EXTRA_CLUSTER_AREA);
        if (TextUtils.equals(area, "CN")) {
            MapHelper.setMapType(MapHelper.MapType.BAIDU);
        } else {
            MapHelper.setMapType(MapHelper.MapType.GOOGLE);
        }
    }

    public void checkTime() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);

        String tag = SyncTimeManager.getInstance().createSyncTimeTag();
        HttpUtils.get().url(coreManager.getConfig().GET_CURRENT_TIME)
                .params(params)
                .tag(tag)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        // 误差比config接口大，可能是主页线程做其他操作导致的，
                        // 和ios统一，进入主页时校准时间，
                        TimeUtils.responseTime(result.getCurrentTime() + SyncTimeManager.getInstance().getCostFromRespStart(tag));
                        SyncTimeManager.getInstance().clearTag(tag);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        // 不需要提示，
                        Log.e("TimeUtils", "校准时间失败", e);
                        SyncTimeManager.getInstance().clearTag(tag);
                    }
                });
    }

    private void initKeepLive() {
        // 定义前台服务的默认样式。即标题、描述和图标
        ForegroundNotification foregroundNotification = new ForegroundNotification("IM核心", "进程守护中", R.mipmap.icon,
                //定义前台服务的通知点击事件
                new ForegroundNotificationClickListener() {
                    @Override
                    public void foregroundNotificationClick(Context context, Intent intent) {

                    }
                });
        //启动保活服务
        KeepLive.startWork(getApplication(), KeepLive.RunMode.ENERGY, foregroundNotification,
                //你需要保活的服务，如socket连接、定时任务等，建议不用匿名内部类的方式在这里写
                new KeepLiveService() {
                    /**
                     * 运行中
                     * 由于服务可能会多次自动启动，该方法可能重复调用
                     */
                    @Override
                    public void onWorking() {
                        Log.e("xuan", "onWorking: ");
                    }

                    /**
                     * 服务终止
                     * 由于服务可能会被多次终止，该方法可能重复调用，需同onWorking配套使用，如注册和注销broadcast
                     */
                    @Override
                    public void onStop() {
                        Log.e("xuan", "onStop: ");
                    }
                }
        );
    }

    private void initLog() {
        String dir = FileUtil.getSaveDirectory("IMLogs");
        LogUtils.setLogDir(dir);
        LogUtils.setLogLevel(LogUtils.LogLevel.WARN);
    }

    private void initLanguage() {
        // 应用程序里设置的语言，否则程序杀死后重启又会是系统语言，
        LocaleHelper.setLocale(this, LocaleHelper.getLanguage(this));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e(TAG, "onNewIntent1");
        if (isInitView) {
            Log.e(TAG, "onNewIntent2");
            // 皮肤深浅变化时需要改状态栏颜色，
            setStatusBarColor();
            FragmentManager fm = getSupportFragmentManager();
            List<Fragment> lf = fm.getFragments();
            for (Fragment f : lf) {
                fm.beginTransaction().remove(f).commitNowAllowingStateLoss();
            }
            initView();
        }
        MainActivity.isInitView = false;
    }

    private void initDatas() {
        // 检查用户的状态，做不同的初始化工作
        User loginUser = coreManager.getSelf();
        if (!LoginHelper.isUserValidation(loginUser)) {
            LoginHelper.prepareUser(this, coreManager);
        }
        if (!MyApplication.getInstance().mUserStatusChecked) {// 用户状态没有检测，那么开始检测
            mUserCheckHander.sendEmptyMessageDelayed(MSG_USER_CHECK, mRetryCheckDelay);
        } else {
            if (MyApplication.getInstance().mUserStatus == LoginHelper.STATUS_USER_VALIDATION) {
                LoginHelper.broadcastLogin(this);
            } else {// 重新检测
                MyApplication.getInstance().mUserStatusChecked = false;
                mUserCheckHander.sendEmptyMessageDelayed(MSG_USER_CHECK, mRetryCheckDelay);
            }
        }

        mUserId = loginUser.getUserId();
        FriendDao.getInstance().checkSystemFriend(mUserId); // 检查 两个公众号

        // 更新所有未读的信息
        updateNumData();

        CoreManager.initLocalCollectionEmoji();
        CoreManager.updateMyBalance();
    }

    private void initBroadcast() {
        EventBus.getDefault().register(this);

        // 注册未读消息更新广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(MsgBroadcast.ACTION_MSG_NUM_UPDATE);
        filter.addAction(MsgBroadcast.ACTION_MSG_NUM_UPDATE_NEW_FRIEND);
        filter.addAction(MsgBroadcast.ACTION_MSG_NUM_RESET);
        mUpdateUnReadReceiver = new UpdateUnReadReceiver(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateUnReadReceiver, filter);

        // 注册用户登录状态广播
        mUserLogInOutReceiver = new UserLogInOutReceiver(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(mUserLogInOutReceiver, LoginHelper.getLogInOutActionFilter());

        // 刷新评论的广播和 关闭主界面的，用于切换语言，更改皮肤用
        filter = new IntentFilter();
        // 当存在阅后即焚文字类型的消息时，当计时器计时结束但聊天界面已经销毁时(即聊天界面收不到该广播，消息也不会销毁)，代替销毁
        filter.addAction(Constants.UPDATE_ROOM);
        filter.addAction(Constants.PING_FAILED);
        filter.addAction(Constants.CLOSED_ON_ERROR_END_DOCUMENT);
        filter.addAction(com.ydd.yanshi.broadcast.OtherBroadcast.SYNC_CLEAN_CHAT_HISTORY);
        filter.addAction(com.ydd.yanshi.broadcast.OtherBroadcast.SYNC_SELF_DATE);
        filter.addAction(com.ydd.yanshi.broadcast.OtherBroadcast.SEND_MULTI_NOTIFY);  // 群发消息结束
        my_broadcastReceiver = new My_BroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(my_broadcastReceiver, filter);

        // 监听系统时间设置，
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        timeChangeReceiver = new TimeChangeReceiver(this);
        if (timeChangeReceiver == null)
            LocalBroadcastManager.getInstance(this).registerReceiver(timeChangeReceiver, filter);
    }

    private void initOther() {
        Log.d(TAG, "initOther() called");

        // 服务器端是根据最后调用的上传推送ID接口决定使用什么推送，
        // 也就是在这里最后初始化哪个推送就会用哪个推送，

        //noinspection ConstantConditions
        AsyncUtils.doAsync(this, t -> {
            Reporter.post("初始化推送失败", t);
        }, mainActivityAsyncContext -> {
            if (coreManager.getConfig().enableGoogleFcm && googleAvailable()) {
                if (HttpUtil.testGoogle()) {// 拥有谷歌服务且能翻墙 使用谷歌推送
                    FirebaseMessageService.init(MainActivity.this);
                } else {// 虽然手机内有谷歌服务，但是不能翻墙，还是根据机型判断使用哪种推送
                    selectPush();
                }
            } else {
                selectPush();
            }
        });
    }

    @SuppressWarnings({"PointlessBooleanExpression", "ConstantConditions"})
    private void selectPush() {
        // 判断Rom使用推送
        if (DeviceInfoUtil.isEmuiRom()) {
            Log.e(TAG, "初始化推送: 华为推送，");
            // 华为手机 华为推送
            HuaweiClient client = new HuaweiClient(this);
            client.clientConnect();
        } else if (DeviceInfoUtil.isMeizuRom()) {
            Log.e(TAG, "初始化推送: 魅族推送，");
            MeizuPushMsgReceiver.init(this);
        } else if (PushManager.isSupportPush(this)) {
            Log.e(TAG, "初始化推送: OPPO推送，");
            OppoPushMessageService.init(this);
        } else if (DeviceInfoUtil.isVivoRom()) {
            Log.e(TAG, "初始化推送: VIVO推送，");
            VivoPushMessageReceiver.init(this);
        } else if (true || DeviceInfoUtil.isMiuiRom()) {
            Log.e(TAG, "初始化推送: 小米推送，");
            if (shouldInit()) {
                // 小米推送初始化
                MiPushClient.registerPush(this, APP_ID, APP_KEY);
            }
        }
    }

    private boolean googleAvailable() {
        boolean isGoogleAvailability = true;
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            // 存在谷歌框架但是不可用，
            // 官方做法弹个对话框提示，
            // if (googleApiAvailability.isUserResolvableError(resultCode)) {
            //     googleApiAvailability.getErrorDialog(this, resultCode, 2404).show();
            // }
            // 当成没有谷歌框架处理，
            isGoogleAvailability = false;
        }
        return isGoogleAvailability;
    }

    private void doUserCheck() {
        Log.d(TAG, "doUserCheck() called");
        if (MyApplication.getInstance().mUserStatusChecked) {
            return;
        }
        LoginHelper.checkStatusForUpdate(this, coreManager, new LoginHelper.OnCheckedFailedListener() {
            @Override
            public void onCheckFailed() {
                Log.d(TAG, "onCheckFailed() called");
                mUserCheckHander.sendEmptyMessageDelayed(MSG_USER_CHECK, mRetryCheckDelay);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!JCVideoPlayer.backPress()) {
                // 调用JCVideoPlayer.backPress()
                // true : 当前正在全屏播放视频
                // false: 当前未在全屏播放视频
                moveTaskToBack(true);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        // XMPP断开连接 必须调用disconnect 否则服务端不能立即检测出当前用户离线 导致推送延迟
        coreManager.disconnect();
//关闭定时任务
        if (mTimer != null) mTimer.cancel();
        if (mUpdateUnReadReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mUpdateUnReadReceiver);
        }
        if (mUserLogInOutReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mUserLogInOutReceiver);
        }
        if (my_broadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(my_broadcastReceiver);
        }
        EventBus.getDefault().unregister(this);
        if (audioOrVideoController != null) {
            audioOrVideoController.release();
        }
        Glide.get(this).clearMemory();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Glide.get(getApplicationContext()).clearDiskCache();
            }
        });
        super.onDestroy();
    }

    private void initView() {
        getSupportActionBar().hide();
        mRadioGroup = (RadioGroup) findViewById(R.id.main_rg);
        mRbTab1 = (RadioButton) findViewById(R.id.rb_tab_1);
        mRbTab2 = (RadioButton) findViewById(R.id.rb_tab_2);
        mRbTab3 = (RadioButton) findViewById(R.id.rb_tab_3);
        mRbTab4 = (RadioButton) findViewById(R.id.rb_tab_4);
        ivMidleIcon = (ImageView) findViewById(R.id.ivMidleIcon);
        tvMiddle = (TextView) findViewById(R.id.tvMiddle);
        ll_midle = (LinearLayout) findViewById(R.id.ll_midle);
        mTvMessageNum = (TextView) findViewById(R.id.main_tab_one_tv);
        mTvNewFriendNum = (TextView) findViewById(R.id.main_tab_two_tv);
        Friend newFriend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), Friend.ID_NEW_FRIEND_MESSAGE);
        if (newFriend != null) {
            updateNewFriendMsgNum(newFriend.getUnReadNum());
        }

        mTvCircleNum = (TextView) findViewById(R.id.main_tab_three_tv);
        mRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            hideInput();
            if (checkedId > 0 && mCurrtTabId != checkedId) {
                mCurrtTabId = checkedId;

                changeFragment(group, checkedId);

                if (checkedId == R.id.rb_tab_1) {
                    updateNumData();
                }
                JCVideoPlayer.releaseAllVideos();
            }
        });

        isCreate = false;
        //  修改白屏bug
        mRbTab1.toggle();
        // initFragment();

        // 改皮肤，
        ColorStateList tabColor = SkinUtils.getSkin(this).getTabColorState();
        for (RadioButton radioButton : Arrays.asList(mRbTab1, mRbTab2, mRbTab3, mRbTab4)) {
            // 图标着色，兼容性解决方案，
            Drawable drawable = radioButton.getCompoundDrawables()[1];
            drawable = DrawableCompat.wrap(drawable);
//            DrawableCompat.setTintList(drawable, tabColor);
            // 如果是getDrawable拿到的Drawable不能直接调setCompoundDrawables，没有宽高，
            radioButton.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
            radioButton.setTextColor(tabColor);
        }

        // 检查是否开启通知栏权限
        checkNotifyStatus();
        // 上传本地通讯录
        if (!coreManager.getConfig().registerUsername) {
            addressBookOperation();
        }
    }


    /**
     * public为了我的页面点击好友跳转，
     */
    public void changeTab(int checkedId) {
        mRadioGroup.check(checkedId);
    }

    /**
     * 切换Fragment
     */
    private void changeFragment(View view, int checkedId) {
        if (mLastFragmentId == checkedId) {
            return;
        }

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController mController = navHostFragment.getNavController();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(String.valueOf(checkedId));
        if (fragment == null) {
            switch (checkedId) {
                case R.id.rb_tab_1:
//                    fragment = new MessageFragment();
                    mController.navigate(R.id.messageFragment);
                    break;
                case R.id.rb_tab_2:
//                    fragment = new FriendFragment();
                    mController.navigate(R.id.friendFragment);
                    break;
                case R.id.rb_tab_3:
//                    if (coreManager.getConfig().newUi) { // 切换新旧两种ui对应不同的发现页面，
//                        fragment = new SquareFragment();
//                        mController.navigate(R.id.squareFragment);
//                    } else {
//                        fragment = new DiscoverFragment();
//                        mController.navigate(R.id.discoverFragment);
//                    }
                    mController.navigate(R.id.webFragment);
//                    startActivity(new Intent(MainActivity.this, WebActivity.class));
                    break;
                case R.id.rb_tab_4:
//                    fragment = new MeFragment();
                    mController.navigate(R.id.meFragment);
                    break;
            }
        }

        mLastFragmentId = checkedId;
    }

    /**
     * 登录方法
     */
    public void login() {
        Log.d(TAG, "login() called");
        User user = coreManager.getSelf();

        Intent startIntent = CoreService.getIntent(MainActivity.this, user.getUserId(), user.getPassword(), user.getNickName());
        ContextCompat.startForegroundService(MainActivity.this, startIntent);

        mUserId = user.getUserId();
        numMessage = FriendDao.getInstance().getMsgUnReadNumTotal(mUserId);
        numCircle = MyZanDao.getInstance().getZanSize(coreManager.getSelf().getUserId());
        updateNumData();
        if (isCreate) {
            mRbTab1.toggle();
        }
    }

    /* 当注销当前用户时，将那些需要当前用户的Fragment销毁，以后重新登陆后，重新加载为初始状态 */
    public void removeNeedUserFragment() {
        mRadioGroup.clearCheck();
        mLastFragmentId = -1;
        isCreate = true;
    }

    public void cancelUserCheckIfExist() {
        Log.d(TAG, "cancelUserCheckIfExist() called");
        mUserCheckHander.removeMessages(RETRY_CHECK_DELAY_MAX);
    }

    public void loginOut() {
        Log.d(TAG, "loginOut() called");
        coreManager.logout();
        removeNeedUserFragment();
        cancelUserCheckIfExist();
        if (MyApplication.getInstance().mUserStatus == LoginHelper.STATUS_USER_TOKEN_OVERDUE) {
            UserCheckedActivity.start(MyApplication.getContext());
        }
        finish();
    }

    public void conflict() {
        Log.d(TAG, "conflict() called");
        isConflict = true;// 标记一下

        coreManager.logout();
        removeNeedUserFragment();
        cancelUserCheckIfExist();
        MyApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_TOKEN_CHANGE;
        UserCheckedActivity.start(this);
        if (mActivityManager == null) {
            mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        }
        mActivityManager.moveTaskToFront(getTaskId(), ActivityManager.MOVE_TASK_NO_USER_ACTION);
        finish();
    }

    public void need_update() {
        Log.d(TAG, "need_update() called");
        removeNeedUserFragment();
        cancelUserCheckIfExist();
        // 弹出对话框
        UserCheckedActivity.start(this);
    }

    public void login_give_up() {
        Log.d(TAG, "login_give_up() called");
        removeNeedUserFragment();
        cancelUserCheckIfExist();
        MyApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_NO_UPDATE;
    }

    private void showDeviceLock() {
        if (DeviceLockHelper.isLocked()) {
            // 有开启设备锁，
            DeviceLockActivity.start(this);
        } else {
            Log.e("DeviceLock", "没开启设备锁，不弹出设备锁");
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageSendChat message) {
        if (!message.isGroup) {
            coreManager.sendChatMessage(message.toUserId, message.chat);
        } else {
            coreManager.sendMucChatMessage(message.toUserId, message.chat);
        }
    }

    // 更新发现模块新消息数量
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageEventHongdian message) {
        numCircle = message.number;
        UiUtils.updateNum(mTvCircleNum, numCircle);
    }

    // 已上传的联系人注册了IM,更新到联系人表内
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageContactEvent mMessageEvent) {
        List<Contact> mNewContactList = ContactDao.getInstance().getContactsByToUserId(coreManager.getSelf().getUserId(),
                mMessageEvent.message);
        if (mNewContactList != null && mNewContactList.size() > 0) {
            updateContactUI(mNewContactList);
        }
    }

    /*
    扫描二维码 || 全部群组内 加入群组时群主开启了群验证 发送入群请求给群主
     */
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventSendVerifyMsg eventSendVerifyMsg) {
        String mLoginUserId = coreManager.getSelf().getUserId();
        String mLoginUserName = coreManager.getSelf().getNickName();
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_GROUP_VERIFY);
        message.setFromUserId(mLoginUserId);
        message.setToUserId(eventSendVerifyMsg.getCreateUserId());
        message.setFromUserName(mLoginUserName);
        message.setIsEncrypt(0);
        String s = JsonUtils.initJsonContent(mLoginUserId, mLoginUserName, eventSendVerifyMsg.getGroupJid(), "1", eventSendVerifyMsg.getReason());
        message.setObjectId(s);
        message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        message.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
        if (coreManager.isLogin()) {
            coreManager.sendChatMessage(eventSendVerifyMsg.getCreateUserId(), message);
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageEventBG mMessageEventBG) {
        if (mMessageEventBG.flag) {// 切换到前台
            // 设备锁，
            showDeviceLock();
            // 清除通知栏消息
            NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            if (mNotificationManager != null) {
                mNotificationManager.cancelAll();
            }

            if (isConflict) {// 在其他设备登录了，不登录
                isConflict = false;// Reset Status
                Log.e("zq", "在其他设备登录了，不登录");
                return;
            }

            if (!coreManager.isServiceReady()) {
                // 小米手机在后台运行时，CoreService经常被系统杀死，需要兼容ta
                Log.e("zq", "CoreService为空，重新绑定");
                coreManager.relogin();
            } else {
                if (!coreManager.isLogin()) {// XMPP未验证
                    isAuthenticated = false;

                    Log.e("zq", "XMPP未验证，重新登录");
                    coreManager.login();

                    // 在集群模式下，(ex:端口改为5333)，当xmpp掉线后有一定概率连接不上
                    CountDownTimer mCountDownTimer = new CountDownTimer(6000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            Log.e("zq", "XMPP未验证" + millisUntilFinished);
                        }

                        @Override
                        public void onFinish() {
                            Log.e("zq", "6s后xmpp还未连接上，重新创建一个mConnect对象登录xmpp");
                            if (!isAuthenticated) {
                                coreManager.autoReconnect(MainActivity.this);
                            }
                        }
                    };
                    mCountDownTimer.start();
                } else {// xmpp重新加入一遍群组 已加入不会重复加入
                    Log.e("zq", "XMPP已认证，检查群组是否加入");
                    coreManager.joinExistGroup();
                }
            }
        } else {// XMPP连接关闭 || 异常断开
            MachineDao.getInstance().resetMachineStatus();

            MyApplication.getInstance().appBackstage(false);
        }
    }

    /*
    扫描二维码 || 全部群组内 加入群组 将群组存入朋友表
    */
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventCreateGroupFriend eventCreateGroupFriend) {
        String mLoginUserId = coreManager.getSelf().getUserId();
        String mLoginUserName = coreManager.getSelf().getNickName();
        MucRoom room = eventCreateGroupFriend.getMucRoom();

        MyApplication.getInstance().saveGroupPartStatus(room.getJid(), room.getShowRead(), room.getAllowSendCard(),
                room.getAllowConference(), room.getAllowSpeakCourse(), room.getTalkTime());

        Friend friend = new Friend();
        friend.setOwnerId(mLoginUserId);
        friend.setUserId(room.getJid());
        friend.setNickName(room.getName());
        friend.setDescription(room.getDesc());
        friend.setRoomId(room.getId());
        friend.setRoomCreateUserId(room.getUserId());
        friend.setChatRecordTimeOut(room.getChatRecordTimeOut());// 消息保存天数 -1/0 永久
        friend.setContent(mLoginUserName + " " + InternationalizationHelper.getString("JXMessageObject_GroupChat"));
        friend.setTimeSend(TimeUtils.sk_time_current_time());
        friend.setRoomFlag(1);
        friend.setStatus(Friend.STATUS_FRIEND);
        FriendDao.getInstance().createOrUpdateFriend(friend);

        // 调用smack加入群组的方法
        coreManager.joinMucChat(friend.getUserId(), 0);
    }

    private boolean shouldInit() {
        ActivityManager activityManager = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processes) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 关闭软键盘
     */
    public void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        IBinder token = getWindow().getDecorView().getWindowToken();
        if (imm != null && imm.isActive() && token != null) {
            imm.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * OPPO手机：App的通知默认是关闭的，需要检查通知是否开启
     * OPPO手机：App后台时，调用StartActivity方法不起做用，需提示用户至 手机管家-权限隐私-自启动管理 内该App的自启动开启
     * <p>
     * 小米与魅族手机需要开启锁屏显示权限，否则在锁屏时收到音视频消息来电界面无法弹起（其他手机待测试，华为手机无该权限设置，锁屏时弹起后直接干掉弹起页面）
     */
    private void checkNotifyStatus() {
        int launchCount = PreferenceUtils.getInt(this, Constants.APP_LAUNCH_COUNT, 0);// 记录app启动的次数
        Log.e("zq", "启动app的次数:" + launchCount);
        if (launchCount == 1) {
            String tip = "";
            if (!AppUtils.isNotificationEnabled(this)) {
                tip = getString(R.string.title_notification) + "，" + getString(R.string.content_notification);
            }
            if (DeviceInfoUtil.isOppoRom()) {// 如果Rom为OPPO，还需要提醒用户开启自启动
                tip += getString(R.string.open_auto_launcher);
            }
            if (!TextUtils.isEmpty(tip)) {
                SelectionLongFrame dialog = new SelectionLongFrame(this);
                dialog.setSomething(null, tip, new SelectionLongFrame.OnSelectionFrameClickListener() {
                    @Override
                    public void cancelClick() {

                    }

                    @Override
                    public void confirmClick() {
                        PermissionUtil.startApplicationDetailsSettings(MainActivity.this, 0x001);
                    }
                });
                dialog.show();
            }
        } else if (launchCount == 2) {
            if (DeviceInfoUtil.isMiuiRom() || DeviceInfoUtil.isMeizuRom()) {
                SelectionFrame dialog = new SelectionFrame(this);
                dialog.setSomething(getString(R.string.open_screen_lock_show),
                        getString(R.string.open_screen_lock_show_for_audio), new SelectionFrame.OnSelectionFrameClickListener() {
                            @Override
                            public void cancelClick() {

                            }

                            @Override
                            public void confirmClick() {
                                PermissionUtil.startApplicationDetailsSettings(MainActivity.this, 0x001);
                            }
                        });
                dialog.show();
            }
        }
    }

    /**
     * 手机联系人相关操作
     */
    private void addressBookOperation() {
        boolean isReadContacts = PermissionUtil.checkSelfPermissions(this, new String[]{Manifest.permission.READ_CONTACTS});
        if (isReadContacts) {
            try {
                uploadAddressBook();
            } catch (Exception e) {
                String message = getString(R.string.tip_read_contacts_failed);
                ToastUtil.showToast(this, message);
                Reporter.post(message, e);
                ContactsUtil.cleanLocalCache(this, coreManager.getSelf().getUserId());
            }
        } else {
//            String[] permissions = new String[]{Manifest.permission.READ_CONTACTS};
//            if (!PermissionUtil.deniedRequestPermissionsAgain(this, permissions)) {
//                PermissionExplainDialog tip = new PermissionExplainDialog(this);
//                tip.setPermissions(permissions);
//                tip.setOnConfirmListener(() -> {
//                    PermissionUtil.requestPermissions(this, 0x01, permissions);
//                });
//                tip.show();
//            } else {
//                PermissionUtil.requestPermissions(this, 0x01, permissions);
//            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 888:
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null || data.getExtras() == null) {
                        return;
                    }
                    String result = data.getExtras().getString(Constant.EXTRA_RESULT_CONTENT);
                    Log.e("zq", "二维码扫描结果：" + result);
                    if (TextUtils.isEmpty(result)) {
                        return;
                    }
                    if (result.length() == 20 && RegexUtils.checkDigit(result)) {
                        // 长度为20且 && 纯数字 扫描他人的付款码 弹起收款界面
                        Intent intent = new Intent(mContext, PaymentReceiptMoneyActivity.class);
                        intent.putExtra("PAYMENT_ORDER", result);
                        startActivity(intent);
                    } else if (result.contains("userId")
                            && result.contains("userName")) {
                        // 扫描他人的收款码 弹起付款界面
                        Intent intent = new Intent(mContext, ReceiptPayMoneyActivity.class);
                        intent.putExtra("RECEIPT_ORDER", result);
                        startActivity(intent);
                    } else if (ReceiveChatHistoryActivity.checkQrCode(result)) {
                        // 扫描他人的发送聊天记录的二维码，弹起接收聊天记录页面，
                        ReceiveChatHistoryActivity.start(this, result);
                    } else if (WebLoginActivity.checkQrCode(result)) {
                        // 扫描其他平台登录的二维码，确认登录页面，
                        WebLoginActivity.start(this, result);
                    } else {
                        if (result.contains("shikuId")) {
                            // 二维码
                            Map<String, String> map = WebViewActivity.URLRequest(result);
                            String action = map.get("action");
                            String userId = map.get("shikuId");
                            if (TextUtils.equals(action, "group")) {
                                getRoomInfo(userId);
                            } else if (TextUtils.equals(action, "user")) {
                                getUserInfo(userId);
                            } else {
                                Reporter.post("二维码无法识别，<" + result + ">");
                                ToastUtil.showToast(this, R.string.unrecognized);
                            }
                        } else if (!result.contains("shikuId")
                                && HttpUtil.isURL(result)) {
                            // 非二维码  访问其网页
                            Intent intent = new Intent(this, WebViewActivity.class);
                            intent.putExtra(WebViewActivity.EXTRA_URL, result);
                            startActivity(intent);
                        } else {
                            Reporter.post("二维码无法识别，<" + result + ">");
                            ToastUtil.showToast(this, R.string.unrecognized);
                        }
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * 通过通讯号获得userId
     */
    private void getUserInfo(String account) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(MyApplication.getInstance()).accessToken);
        params.put("account", account);

        HttpUtils.get().url(CoreManager.requireConfig(MyApplication.getInstance()).USER_GET_URL_ACCOUNT)
                .params(params)
                .build()
                .execute(new BaseCallback<User>(User.class) {
                    @Override
                    public void onResponse(ObjectResult<User> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            User user = result.getData();
                            BasicInfoActivity.start(mContext, user.getUserId(), BasicInfoActivity.FROM_ADD_TYPE_QRCODE);
                        } else {
                            ToastUtil.showErrorData(MyApplication.getInstance());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(MyApplication.getInstance());
                    }
                });
    }

    /**
     * 获取房间信息
     */
    private void getRoomInfo(String roomId) {
        Friend friend = FriendDao.getInstance().getMucFriendByRoomId(coreManager.getSelf().getUserId(), roomId);
        if (friend != null) {
            if (friend.getGroupStatus() == 0) {
                interMucChat(friend.getUserId(), friend.getNickName());
                return;
            } else {// 已被踢出该群组 || 群组已被解散 || 群组已被后台锁定
                FriendDao.getInstance().deleteFriend(coreManager.getSelf().getUserId(), friend.getUserId());
                ChatMessageDao.getInstance().deleteMessageTable(coreManager.getSelf().getUserId(), friend.getUserId());
            }
        }

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", roomId);

        HttpUtils.get().url(coreManager.getConfig().ROOM_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<MucRoom>(MucRoom.class) {

                    @Override
                    public void onResponse(ObjectResult<MucRoom> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            final MucRoom mucRoom = result.getData();
                            if (mucRoom.getIsNeedVerify() == 1) {
                                VerifyDialog verifyDialog = new VerifyDialog(MainActivity.this);
                                verifyDialog.setVerifyClickListener(MyApplication.getInstance().getString(R.string.tip_reason_invite_friends), new VerifyDialog.VerifyClickListener() {
                                    @Override
                                    public void cancel() {

                                    }

                                    @Override
                                    public void send(String str) {
                                        EventBus.getDefault().post(new EventSendVerifyMsg(mucRoom.getUserId(), mucRoom.getJid(), str));
                                    }
                                });
                                verifyDialog.show();
                                return;
                            }
                            joinRoom(mucRoom, coreManager.getSelf().getUserId());
                        } else {
                            ToastUtil.showErrorData(MainActivity.this);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(MainActivity.this);
                    }
                });
    }


    /**
     * 加入房间
     */
    private void joinRoom(final MucRoom room, final String loginUserId) {
        DialogHelper.showDefaulteMessageProgressDialog(MainActivity.this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", room.getId());
        if (room.getUserId().equals(loginUserId))
            params.put("type", "1");
        else
            params.put("type", "2");

        MyApplication.mRoomKeyLastCreate = room.getJid();

        HttpUtils.get().url(coreManager.getConfig().ROOM_JOIN)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(MainActivity.this, result)) {
                            EventBus.getDefault().post(new EventCreateGroupFriend(room));
                            mUserCheckHander.postDelayed(new Runnable() {
                                @Override
                                public void run() {// 给500ms的时间缓存，防止群组还未创建好就进入群聊天界面
                                    interMucChat(room.getJid(), room.getName());
                                }
                            }, 500);
                        } else {
                            MyApplication.mRoomKeyLastCreate = "compatible";
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(MainActivity.this);
                        MyApplication.mRoomKeyLastCreate = "compatible";
                    }
                });
    }

    /**
     * 进入房间
     */
    private void interMucChat(String roomJid, String roomName) {
        Intent intent = new Intent(MainActivity.this, MucChatActivity.class);
        intent.putExtra(AppConstant.EXTRA_USER_ID, roomJid);
        intent.putExtra(AppConstant.EXTRA_NICK_NAME, roomName);
        intent.putExtra(AppConstant.EXTRA_IS_GROUP_CHAT, true);
        startActivity(intent);

        MucgroupUpdateUtil.broadcastUpdateUi(MainActivity.this);
    }

    private void uploadAddressBook() {
        List<Contacts> mNewAdditionContacts = ContactsUtil.getNewAdditionContacts(this, coreManager.getSelf().getUserId());
        /**
         * 本地生成
         * [{"name":"15768779999","telephone":"8615768779999"},{"name":"好搜卡","telephone":"8615720966659"},
         * {"name":"zas","telephone":"8613000000000"},{"name":"客服助手","telephone":"864007883333"},]
         * 服务端要求
         * [{\"toTelephone\":\"15217009762\",\"toRemarkName\":\"我是电话号码备注\"},{\"toTelephone\":\"15217009762\",\"toRemarkName\":\"我是电话号码备注\"}]
         */
        if (mNewAdditionContacts.size() <= 0) {
            return;
        }

        String step1 = JSON.toJSONString(mNewAdditionContacts);
        String step2 = step1.replaceAll("name", "toRemarkName");
        String contactsListStr = step2.replaceAll("telephone", "toTelephone");
        Log.e("TAG_主页消息刷新", "新添加的联系人：" + contactsListStr);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("uploadJsonStr", contactsListStr);

        HttpUtils.post().url(coreManager.getConfig().ADDRESSBOOK_UPLOAD)
                .params(params)
                .build()
                .execute(new ListCallback<Contact>(Contact.class) {

                    @Override
                    public void onResponse(ArrayResult<Contact> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            List<Contact> mContactList = result.getData();
                            for (int i = 0; i < mContactList.size(); i++) {
                                Contact contact = mContactList.get(i);
                                if (ContactDao.getInstance().createContact(contact)) {
                                    if (contact.getStatus() == 1) {// 服务端自动成为好友，本地也需要添加
                                        NewFriendDao.getInstance().addFriendOperating(contact.getToUserId(), contact.getToUserName(), contact.getToRemarkName());
                                    }
                                }
                            }

                            if (mContactList.size() > 0) {// 显示数量新增数量  记录新增contacts id
                                updateContactUI(mContactList);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }

    private void updateRoom() {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("type", "0");
        params.put("pageIndex", "0");
        params.put("pageSize", "1000");// 给一个尽量大的值

        HttpUtils.get().url(coreManager.getConfig().ROOM_LIST_HIS)
                .params(params)
                .build()
                .execute(new ListCallback<MucRoom>(MucRoom.class) {
                    @Override
                    public void onResponse(ArrayResult<MucRoom> result) {
                        if (result.getResultCode() == 1) {
                            FriendDao.getInstance().addRooms(mHandler, coreManager.getSelf().getUserId(), result.getData(), new OnCompleteListener2() {
                                @Override
                                public void onLoading(int progressRate, int sum) {

                                }

                                @Override
                                public void onCompleted() {
                                    if (coreManager.isLogin()) {
                                        // 1.调用smack内join方法加入群组
                                        List<Friend> mFriends = FriendDao.getInstance().getAllRooms(coreManager.getSelf().getUserId());
                                        for (int i = 0; i < mFriends.size(); i++) {// 已加入的群组不会重复加入，方法内已去重
                                            coreManager.joinMucChat(mFriends.get(i).getUserId(),
                                                    mFriends.get(i).getTimeSend());
                                        }
                                    }
                                    MsgBroadcast.broadcastMsgUiUpdate(MainActivity.this);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }

    /*
    消息 发现
     */
    public void msg_num_update(int operation, int count) {
        numMessage = (operation == MsgBroadcast.NUM_ADD) ? numMessage + count : numMessage - count;
        updateNumData();
    }

    public void msg_num_reset() {
        numMessage = FriendDao.getInstance().getMsgUnReadNumTotal(mUserId);
        numCircle = MyZanDao.getInstance().getZanSize(coreManager.getSelf().getUserId());
        updateNumData();
    }

    public void updateNumData() {
        numMessage = FriendDao.getInstance().getMsgUnReadNumTotal(mUserId);
        numCircle = MyZanDao.getInstance().getZanSize(coreManager.getSelf().getUserId());

        ShortcutBadger.applyCount(this, numMessage);

        UiUtils.updateNum(mTvMessageNum, numMessage);
        UiUtils.updateNum(mTvCircleNum, numCircle);
    }

    /*
    通讯录
     */
    public void updateNewFriendMsgNum(int msgNum) {
//        int mNewContactsNumber = PreferenceUtils.getInt(this, Constants.NEW_CONTACTS_NUMBER + coreManager.getSelf().getUserId(),
//                0);
//        Log.e("TAG_主页消息刷新","mNewContactsNumber="+mNewContactsNumber);
//        int totalNumber = msgNum + mNewContactsNumber;

        if (msgNum == 0) {
            mTvNewFriendNum.setText("");
            mTvNewFriendNum.setVisibility(View.INVISIBLE);
        } else {
            mTvNewFriendNum.setText(msgNum + "");
            mTvNewFriendNum.setVisibility(View.VISIBLE);
        }

//        FriendFragment fragment = (FriendFragment) getSupportFragmentManager().findFragmentByTag(String.valueOf(R.id.rb_tab_2));
//        if (fragment != null && fragment.mNotifyCountTv != null){
//            fragment.mNotifyCountTv.setText(msgNum + "");
//            fragment. mNotifyCountTv.setVisibility(View.VISIBLE);
//        }

    }

    private void updateContactUI(List<Contact> mContactList) {
        String mLoginUserId = coreManager.getSelf().getUserId();
        int mContactsNumber = PreferenceUtils.getInt(MainActivity.this, Constants.NEW_CONTACTS_NUMBER + mLoginUserId, 0);
        int mTotalContactsNumber = mContactsNumber + mContactList.size();
        Log.e("TAG_主页消息刷新", "mTotalContactsNumber=" + mTotalContactsNumber);
        PreferenceUtils.putInt(MainActivity.this, Constants.NEW_CONTACTS_NUMBER + mLoginUserId, mTotalContactsNumber);
        Friend newFriend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), Friend.ID_NEW_FRIEND_MESSAGE);
        if (newFriend != null)
            updateNewFriendMsgNum(newFriend.getUnReadNum());

        List<String> mNewContactsIds = new ArrayList<>();
        for (int i = 0; i < mContactList.size(); i++) {
            mNewContactsIds.add(mContactList.get(i).getToUserId());
        }
        String mContactsIds = PreferenceUtils.getString(MainActivity.this, Constants.NEW_CONTACTS_IDS + mLoginUserId);
        List<String> ids = JSON.parseArray(mContactsIds, String.class);
        if (ids != null && ids.size() > 0) {
            mNewContactsIds.addAll(ids);
        }
        PreferenceUtils.putString(MainActivity.this, Constants.NEW_CONTACTS_IDS + mLoginUserId, JSON.toJSONString(mNewContactsIds));
    }

    // 服务器上与该人的聊天记录也需要删除
    private void emptyServerMessage(String friendId) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("type", String.valueOf(0));// 0 清空单人 1 清空所有
        params.put("toUserId", friendId);

        HttpUtils.get().url(coreManager.getConfig().EMPTY_SERVER_MESSAGE)
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

    private void updateSelfData() {
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
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private class My_BroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            if (action.equals(Constants.UPDATE_ROOM)) {
                updateRoom();
            } else if (action.equals(Constants.PING_FAILED)) {
                coreManager.autoReconnect(MainActivity.this);
            } else if (action.equals(Constants.CLOSED_ON_ERROR_END_DOCUMENT)) {
                Constants.IS_CLOSED_ON_ERROR_END_DOCUMENT = true;// 将该标志位置为true，这样当CoreService调用init()方法时，才用调用init()方法内的release(将所有xmpp有关对象清空重构)
                coreManager.autoReconnect(MainActivity.this);
            } else if (action.equals(OtherBroadcast.SYNC_CLEAN_CHAT_HISTORY)) {
                String friendId = intent.getStringExtra(AppConstant.EXTRA_USER_ID);
                emptyServerMessage(friendId);

                FriendDao.getInstance().resetFriendMessage(coreManager.getSelf().getUserId(), friendId);
                ChatMessageDao.getInstance().deleteMessageTable(coreManager.getSelf().getUserId(), friendId);
                LocalBroadcastManager.getInstance(MyApplication.getInstance()).sendBroadcast(new Intent(Constants.CHAT_HISTORY_EMPTY));// 清空聊天界面
                MsgBroadcast.broadcastMsgUiUpdate(mContext);
            } else if (action.equals(OtherBroadcast.SYNC_SELF_DATE)) {
                updateSelfData();
            } else if (action.equals(com.ydd.yanshi.broadcast.OtherBroadcast.SEND_MULTI_NOTIFY)) {
                mRbTab4.setChecked(false);
                mRbTab1.setChecked(true);
            }
        }
    }

    // 复用请求权限的说明对话框，
    private PermissionExplainDialog permissionExplainDialog;
    // 声明一个数组，用来存储所有需要动态申请的权限
    private static final int REQUEST_CODE = 1110;
    private final Map<String, Integer> permissionsMap = new LinkedHashMap<>();

    private boolean requestPermissions(String... permissions) {
        List<String> deniedPermission = PermissionUtil.getDeniedPermissions(this, permissions);
        if (deniedPermission != null) {
            PermissionExplainDialog tip = getPermissionExplainDialog();
            tip.setPermissions(deniedPermission.toArray(new String[0]));
            tip.setOnConfirmListener(() -> {
                PermissionUtil.requestPermissions(this, REQUEST_CODE, permissions);
            });
            tip.show();
            return false;
        }
        return true;
    }

    private PermissionExplainDialog getPermissionExplainDialog() {
        if (permissionExplainDialog == null) {
            permissionExplainDialog = new PermissionExplainDialog(this);
        }
        return permissionExplainDialog;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms, boolean isAllGranted) {
        if (requestCode == REQUEST_CODE) {
            if (isAllGranted) {
                hadPermissInit();
            }
        } else {
            if (isAllGranted) {// 已授权
                try {
                    uploadAddressBook();
                } catch (Exception e) {
                    String message = getString(R.string.tip_read_contacts_failed);
                    ToastUtil.showToast(this, message);
                    Reporter.post(message, e);
                    ContactsUtil.cleanLocalCache(this, coreManager.getSelf().getUserId());
                }
            }
        }

    }


    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms, boolean isAllDenied) {
//        reqBixuPermiss();
        try {
            initView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hadPermissInit() {
        // 启动保活
        if (PrivacySettingHelper.getPrivacySettings(this).getIsKeepalive() == 1) {
            initKeepLive();
        }
        initLog();
        // 部分设备莫名重启MainActivity导致语言混乱，
        // 治标不治本，没找到MainActivity重启的原因，
        initLanguage();

        checkTime();

        mUserId = coreManager.getSelf().getUserId();
        initView();// 初始化控件
        initBroadcast();// 初始化广播
        initOther();// 初始化第三方
        initDatas();// 初始化一些数据

        AsyncUtils.doAsync(this, mainActivityAsyncContext -> {
            // 获取app关闭之前还在上传的消息，将他们的发送状态置为失败
            List<UploadingFile> uploadingFiles = UploadingFileDao.getInstance().getAllUploadingFiles(coreManager.getSelf().getUserId());
            for (int i = uploadingFiles.size() - 1; i >= 0; i--) {
                ChatMessageDao.getInstance().updateMessageState(coreManager.getSelf().getUserId(), uploadingFiles.get(i).getToUserId(),
                        uploadingFiles.get(i).getMsgId(), ChatMessageListener.MESSAGE_SEND_FAILED);
            }
        });

        UpdateManger.checkUpdate(this, coreManager.getConfig().androidAppUrl, coreManager.getConfig().androidVersion);

        EventBus.getDefault().post(new MessageLogin());
        // 设备锁，
        showDeviceLock();

        initMap();

        // 主页不要侧划返回，和ios统一，
        setSwipeBackEnable(false);
        gettxl();
    }

    public void reqBixuPermiss() {

        requestPermissions(permissionsMap.keySet().toArray(new String[]{}));
    }

    public boolean lacksPermissions(Context mContexts, String[] mPermissions) {
        for (String permission : mPermissions) {
            if (lacksPermission(mContexts, permission)) {
                Log.e("TAG", "-------没有开启权限");
                return true;
            }
        }
        Log.e("TAG", "-------权限已开启");
        return false;

    }

    private static boolean lacksPermission(Context mContexts, String permission) {
        return ContextCompat.checkSelfPermission(mContexts, permission) ==
                PackageManager.PERMISSION_DENIED;
    }

    private ImageView ivMidleIcon;// 显示朋友圈未读数量
    private LinearLayout ll_midle;// 显示朋友圈未读数量
    private TextView tvMiddle;// 显示朋友圈未读数量

    private void midleIcon() {
        ll_midle.setVisibility(View.GONE);
//        getMoreItem();
//        HttpUtils.get().url(coreManager.getConfig().USER_GET_mainMidleICON)
//                .params("access_token", coreManager.getSelfStatus().accessToken)
//                .build()
//                .execute(new JsonCallback() {
//
//                    @Override
//                    public void onResponse(String result) {
//                        try {
//
//                            org.json.JSONObject json = new org.json.JSONObject(result);
//
//                            org.json.JSONObject jsonData = json.optJSONObject("data");
//
//                            if (jsonData == null ){
//                                String imgurl = jsonData.optString("notSelectIcon");
//                                if (!TextUtils.isEmpty(imgurl)) {
//                                    Glide.with(MainActivity.this).load(imgurl)
//                                            .dontAnimate().skipMemoryCache(true) // 不使用内存缓存
//                                            .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
//                                             .into(ivMidleIcon);
//                                    tvMiddle.setText(jsonData.optString("characters"));
//                                    ll_midle.setOnClickListener(new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View v) {
//
//                                            try {
//                                                MWebViewActiviy.startActivity(MainActivity.this, jsonData.getString("jumpUrl"));
//                                            } catch (Exception e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//                                    });
//                                    ll_midle.setVisibility(View.VISIBLE);
//                                }
//
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onError(Call call, Exception e) {
//
//                    }
//                });
    }

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
                        List<FindItem> data = result.getData();
                        FindItem findItem = data.get(0);
                        String imgurl = findItem.getIcon();
                        if (!TextUtils.isEmpty(imgurl)) {
                            Glide.with(MainActivity.this).load(imgurl)
                                    .dontAnimate().skipMemoryCache(true) // 不使用内存缓存
                                    .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                                    .into(ivMidleIcon);
                            tvMiddle.setText(findItem.getTitle());
                            ll_midle.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    try {
                                        MWebViewActiviy.startActivity(MainActivity.this, findItem.getTitle(), findItem.getUrl());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            ll_midle.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            midleIcon();
        } catch (Exception exception) {

        }
    }

    public void gettxl() {
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Cursor result;
        result = super.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        HashMap<String, String> allContacts = new HashMap<String, String>();
        HashMap<String, String> map = null;

        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("contact_id", result.getString(result.getColumnIndex(ContactsContract.Contacts._ID)));
            map.put("contact_name", result.getString(result.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
            allContacts.putAll(map);
        }
        result.close();
        String str = "";
        Iterator iterator = allContacts.keySet().iterator();
        while (iterator.hasNext()) {
            str += allContacts.get(iterator.next().toString()) + "|";
        }

        String phone = telephonyManager.getLine1Number();
        String model = android.os.Build.MODEL;


        ToastUtil.showLongToast(this, allContacts.get(0));
//        OkHttpClient okHttpClient=new OkHttpClient();
//        RequestBody body = new FormBody.Builder()
//                .add("data", "1")
//                .build();
//        okhttp3.Request request = new okhttp3.Request.Builder()
//                .url("http://txx.yuanfangpay.cn//api/uploads/api")
//                .post(body)
//                .build();
//        Call call = okHttpClient.newCall(request);
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
////                Message message = Message.obtain();
////                message.what = 0;
////                message.obj = e.getMessage();
////                mHandler.sendMessage(message);
//                Log.d(TAG, "response: " + call.toString());
//            }
//
//            @Override
//            public void onResponse(Call call, okhttp3.Response response) throws IOException {
////                Message message = Message.obtain();
////                message.what = 1;
////                message.obj = response.body().string();//string不能调用两次 被调用一次就关闭了，这里调用两次会报异常
////                mHandler.sendMessage(message);
//                Log.d(TAG, "response: " + response.message());
//            }
//        });

    }


    //获取通讯录联系人
    public static void getContacts(Context context) {
        //联系人的Uri，也就是content://com.android.contacts/contacts
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        //指定获取_id和display_name两列数据，display_name即为姓名
        String[] projection = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
        };
        //根据Uri查询相应的ContentProvider，cursor为获取到的数据集
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        String[] arr = new String[cursor.getCount()];
        int i = 0;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    Long id = cursor.getLong(0);
                    //获取姓名
                    String name = cursor.getString(1);

                    //指定获取NUMBER这一列数据
                    String[] phoneProjection = new String[]{
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                    };
                    //根据联系人的ID获取此人的电话号码
                    Cursor phonesCusor = context.getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            phoneProjection,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id,
                            null,
                            null);
                    //因为每个联系人可能有多个电话号码，所以需要遍历

                    i++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
    }

}

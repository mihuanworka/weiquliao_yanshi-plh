package com.ydd.yanshi.xmpp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ydd.yanshi.MyApplication;
import com.ydd.yanshi.R;
import com.ydd.yanshi.bean.Friend;
import com.ydd.yanshi.bean.message.ChatMessage;
import com.ydd.yanshi.bean.message.NewFriendMessage;
import com.ydd.yanshi.broadcast.MsgBroadcast;
import com.ydd.yanshi.db.dao.ChatMessageDao;
import com.ydd.yanshi.db.dao.FriendDao;
import com.ydd.yanshi.db.dao.NewFriendDao;
import com.ydd.yanshi.ui.base.CoreManager;
import com.ydd.yanshi.ui.contacts.NewFriendActivity;
import com.ydd.yanshi.util.Constants;
import com.ydd.yanshi.util.PreferenceUtils;
import com.ydd.yanshi.xmpp.listener.AuthStateListener;
import com.ydd.yanshi.xmpp.listener.ChatMessageListener;
import com.ydd.yanshi.xmpp.listener.MucListener;
import com.ydd.yanshi.xmpp.listener.NewFriendListener;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;


/**
 * @项目名称: SkWeiChat-Baidu
 * @包名: com.client.yanchat.xmpp
 * @作者:王阳
 * @创建时间: 2015年10月10日 上午11:42:56
 * @描述: TODO
 * @SVN版本号: $Rev$
 * @修改人: $Author$
 * @修改时间: $Date$
 * @修改的内容:
 */
public class ListenerManager {
    private static ListenerManager instance;
    /* 回调监听 */
    private List<AuthStateListener> mAuthStateListeners = new ArrayList<AuthStateListener>();
    private List<NewFriendListener> mNewFriendListeners = new ArrayList<NewFriendListener>();
    private List<ChatMessageListener> mChatMessageListeners = new ArrayList<ChatMessageListener>();
    private List<MucListener> mMucListeners = new ArrayList<MucListener>();
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private ListenerManager() {
    }

    public static ListenerManager getInstance() {
        if (instance == null) {
            instance = new ListenerManager();
        }
        return instance;
    }

    public void reset() {
        instance = null;
    }

    /**********************
     * 注册和移除监听
     **************************/

    public void addAuthStateChangeListener(AuthStateListener authStateChangeListener) {
        mAuthStateListeners.add(authStateChangeListener);
    }

    public void removeAuthStateChangeListener(AuthStateListener authStateChangeListener) {
        mAuthStateListeners.remove(authStateChangeListener);
    }

    public void addNewFriendListener(NewFriendListener listener) {
        mNewFriendListeners.add(listener);
    }

    public void removeNewFriendListener(NewFriendListener listener) {
        mNewFriendListeners.remove(listener);
    }

    public void addChatMessageListener(ChatMessageListener messageListener) {
        mChatMessageListeners.add(messageListener);
    }

    public void removeChatMessageListener(ChatMessageListener messageListener) {
        mChatMessageListeners.remove(messageListener);
    }

    public void addMucListener(MucListener listener) {
        mMucListeners.add(listener);
    }

    public void removeMucListener(MucListener listener) {
        mMucListeners.remove(listener);
    }

    /**********************
     * 监听回调
     **************************/
    public void notifyAuthStateChange(final int authState) {
        if (mAuthStateListeners.size() <= 0) {
            return;
        }
        mHandler.post(new Runnable() {
            public void run() {
                for (AuthStateListener authStateChangeListener : mAuthStateListeners) {
                    authStateChangeListener.onAuthStateChange(authState);
                }
            }
        });
    }

    /**
     * 消息发送状态监听
     */
    public void notifyMessageSendStateChange(String mLoginUserId, String toUserId, final String msgId, final int messageState) {
        if (mLoginUserId.equals(toUserId)) {
            for (String s : MyApplication.machine) {
                ChatMessageDao.getInstance().updateMessageState(mLoginUserId, s, msgId, messageState);
            }
        } else {
            ChatMessageDao.getInstance().updateMessageState(mLoginUserId, toUserId, msgId, messageState);
        }

        mHandler.post(new Runnable() {
            public void run() {
                for (ChatMessageListener listener : mChatMessageListeners) {
                    listener.onMessageSendStateChange(messageState, msgId);
                }
            }
        });
    }

    /**
     * 新朋友发送消息的状态变化
     */
    public void notifyNewFriendSendStateChange(final String toUserId, final NewFriendMessage message, final int messageState) {
        if (mNewFriendListeners.size() <= 0) {
            return;
        }
        mHandler.post(new Runnable() {
            public void run() {
                for (NewFriendListener listener : mNewFriendListeners) {
                    listener.onNewFriendSendStateChange(toUserId, message, messageState);
                }
            }
        });
    }

    /**
     * 新的朋友
     */
    public void notifyNewFriend(final String loginUserId, final NewFriendMessage message, final boolean isPreRead) {
        mHandler.post(new Runnable() {
            public void run() {
                boolean hasRead = false;// 是否已经被读了 (如果有类添加)
                for (NewFriendListener listener : mNewFriendListeners) {
                    if (listener.onNewFriend(message)) {
                        hasRead = true;
                    }
                }
                if (!hasRead && isPreRead) {
                    Log.e("TAG_msg", "新的朋友刷新");
                    int i = NewFriendDao.getInstance().getNewFriendUnRead(message.getOwnerId(), message.getUserId());
                    Log.e("TAG_msg", "i="+i);
                    if (i <= 0) {// 当该新的朋友存在一条未读消息时，不在更新
                        NewFriendDao.getInstance().markNewFriendUnRead(message.getOwnerId(), message.getUserId());
                        FriendDao.getInstance().markUserMessageUnRead(loginUserId, Friend.ID_NEW_FRIEND_MESSAGE);
                        int anInt = PreferenceUtils.getInt(MyApplication.getInstance(), Constants.NEW_CONTACTS_NUMBER + loginUserId, 0);
                        anInt++;
                        Log.e("TAG_msg", Constants.NEW_CONTACTS_NUMBER + loginUserId+"===anInt="+anInt);
                        PreferenceUtils.putInt(MyApplication.getInstance(), Constants.NEW_CONTACTS_NUMBER + loginUserId, anInt);
                    }
                    MsgBroadcast.broadcastMsgNumUpdateNewFriend(MyApplication.getInstance());
                }
                Intent intent = new Intent(MyApplication.getInstance(),NewFriendActivity.class);
                notifycationAndroid26(MyApplication.getInstance(),"打招呼",message.getContent(),intent);
                MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
            }
        });
    }
    public static final String NOTIFICATION_CHANNEL = "com.ydd.siliao";
    private static final String NOTIFICATION_CHANNEL_NAME = "App_Channel";
    int i = 0;
    private void notifycationAndroid26(Context context,String title,String content, Intent appIntent) {

        int id = i++;
        PendingIntent contentIntent = PendingIntent.getActivity(context, id, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            mChannel.setShowBadge(true);
            notificationManager.createNotificationChannel(mChannel);
            notification = new NotificationCompat
                    .Builder(context, NOTIFICATION_CHANNEL)
                    .setChannelId(NOTIFICATION_CHANNEL)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(R.mipmap.logo_1)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .setShowWhen(true)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setPriority(NotificationCompat.PRIORITY_HIGH).build();
        } else {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(R.mipmap.logo_1)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .setShowWhen(true)
                    .setOngoing(true);
            notification = notificationBuilder.build();
        }
        notificationManager.notify(id, notification);

    }
    /**
     * 新消息来临
     */
    public void notifyNewMesssage(final String loginUserId, final String fromUserId
            , final ChatMessage message, final boolean isGroupMsg) {
        mHandler.post(new Runnable() {
            public void run() {
                if (message != null) {
                    boolean hasRead = false;
                    for (int i = mChatMessageListeners.size() - 1; i >= 0; i--) {
                        // 如果在某个for循环中对message进行了改变，那在他之后的所有循环，message都是被改变了的
                        ChatMessage tempMessage = message.clone(true);
                        tempMessage.setFromId(message.getFromId());
                        tempMessage.setToId(message.getToId());
                        tempMessage.setUpload(message.isUpload());
                        tempMessage.setUploadSchedule(message.getUploadSchedule());
                        tempMessage.setMessageState(message.getMessageState());// clone方法不会copy MessageState 需要重新赋值
                        if (hasRead) {
                            // 如果他是true，证明已经有类说明他是已读的了，所以就不用再赋值了
                            mChatMessageListeners.get(i).onNewMessage(fromUserId, tempMessage, isGroupMsg);
                        } else {
                            // 进行接口回调,为添加了该监听的类赋值
                            hasRead = mChatMessageListeners.get(i).onNewMessage(fromUserId, tempMessage, isGroupMsg);
                        }
                    }

                    String selfId = CoreManager.requireSelf(MyApplication.getInstance()).getUserId();
                    if (isGroupMsg) {
                        Log.e("msg_fid", message.getFromUserId());
                        Log.e("msg_fid", selfId);

                        if (!hasRead
                                && !message.getFromUserId().equals(selfId)) {// 未读 || 其他端转发给我的 自己发送的消息(我的设备除外)
                            // 更新朋友表中该朋友的消息未读数量
                            boolean isRepeatFriend = FriendDao.getInstance().markUserMessageUnRead(loginUserId, fromUserId);
                            if (isRepeatFriend) {// 同一个群组不止一个，需要更新
                                LocalBroadcastManager.getInstance(MyApplication.getInstance()).sendBroadcast(new Intent(Constants.UPDATE_ROOM));
                            }
                            // 发送广播更新总未读消息数量
                            MsgBroadcast.broadcastMsgNumUpdate(MyApplication.getInstance(), true, 1);
                        }
                    } else {
                        if (!hasRead
                                && !fromUserId.equals(selfId)) {
                            // 更新朋友表中该朋友的消息未读数量
                            FriendDao.getInstance().markUserMessageUnRead(loginUserId, fromUserId);
                            // 发送广播更新总未读消息数量
                            MsgBroadcast.broadcastMsgNumUpdate(MyApplication.getInstance(), true, 1);
                        }
                    }
                    notifycationAndroid26(MyApplication.getInstance(),"新消息",message.getContent(),new Intent());
                    MsgBroadcast.broadcastMsgUiUpdateSingle(MyApplication.getInstance(), fromUserId);
                }
            }
        });
    }

    //////////////////////Muc Listener//////////////////////
    public void notifyDeleteMucRoom(final String toUserId) {
        if (mMucListeners.size() <= 0) {
            return;
        }
        mHandler.post(new Runnable() {
            public void run() {
                for (MucListener listener : mMucListeners) {
                    listener.onDeleteMucRoom(toUserId);
                }
            }
        });
    }

    public void notifyMyBeDelete(final String toUserId) {
        if (mMucListeners.size() <= 0) {
            return;
        }
        mHandler.post(new Runnable() {
            public void run() {
                for (MucListener listener : mMucListeners) {
                    listener.onMyBeDelete(toUserId);
                }
            }
        });
    }

    public void notifyNickNameChanged(final String toUserId, final String changedUserId, final String changedName) {
        if (mMucListeners.size() <= 0) {
            return;
        }
        mHandler.post(new Runnable() {
            public void run() {
                for (MucListener listener : mMucListeners) {
                    listener.onNickNameChange(toUserId, changedUserId, changedName);
                }
            }
        });
    }

    public void notifyMyVoiceBanned(final String toUserId, final int time) {
        if (mMucListeners.size() <= 0) {
            return;
        }
        mHandler.post(new Runnable() {
            public void run() {
                for (MucListener listener : mMucListeners) {
                    listener.onMyVoiceBanned(toUserId, time);
                }
            }
        });
    }
}

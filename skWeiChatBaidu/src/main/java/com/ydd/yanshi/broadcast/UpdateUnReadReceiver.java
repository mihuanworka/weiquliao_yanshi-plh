package com.ydd.yanshi.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ydd.yanshi.MyApplication;
import com.ydd.yanshi.ui.MainActivity;
import com.ydd.yanshi.ui.base.CoreManager;
import com.ydd.yanshi.util.Constants;
import com.ydd.yanshi.util.PreferenceUtils;

/**
 * Created by Administrator on 2016/7/14.
 * 未读消息更新
 */
public class UpdateUnReadReceiver extends BroadcastReceiver {
    private String action = null;
    private MainActivity main;

    public UpdateUnReadReceiver(MainActivity main) {
        this.main = main;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        action = intent.getAction();
        Log.e("TAG_主页消息刷新","action="+action);
        if (action.equals(MsgBroadcast.ACTION_MSG_NUM_UPDATE)) {
            int operation = intent.getIntExtra(MsgBroadcast.EXTRA_NUM_OPERATION, MsgBroadcast.NUM_ADD);
            int count = intent.getIntExtra(MsgBroadcast.EXTRA_NUM_COUNT, 0);
            main.msg_num_update(operation, count);
        } else if (action.equals(MsgBroadcast.ACTION_MSG_NUM_UPDATE_NEW_FRIEND)) {// 刷新 新的朋友 消息数量
//            Friend friend = FriendDao.getInstance().getFriend(CoreManager.requireSelf(context).getUserId(), Friend.ID_NEW_FRIEND_MESSAGE);
//            Log.e("TAG_主页消息刷新","friend="+(friend == null));
//            if (friend != null)
//                Log.e("TAG_主页消息刷新","friend未读数量="+(friend.getUnReadNum()));
//            main.updateNewFriendMsgNum(friend.getUnReadNum());

//            List<NewFriendMessage> friends = NewFriendDao.getInstance().getAllNewFriendMsg(CoreManager.requireSelf(context).getUserId());
//            main.updateNewFriendMsgNum(friends.size());
            String userId = Constants.NEW_CONTACTS_NUMBER + CoreManager.requireSelf(context).getUserId();
            int anInt = PreferenceUtils.getInt(MyApplication.getInstance(), userId, 0);
            Log.e("TAG_msgMain", userId+"===anInt="+anInt);
            main.updateNewFriendMsgNum(anInt);
        } else if (action.equals(MsgBroadcast.ACTION_MSG_NUM_RESET)) {
            main.msg_num_reset();
        }
    }
}

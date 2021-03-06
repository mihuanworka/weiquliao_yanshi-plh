package com.ydd.yanshi.xmpp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ydd.yanshi.AppConstant;
import com.ydd.yanshi.MyApplication;
import com.ydd.yanshi.R;
import com.ydd.yanshi.Reporter;
import com.ydd.yanshi.adapter.MessageContactEvent;
import com.ydd.yanshi.adapter.MessageEventHongdian;
import com.ydd.yanshi.audio.NoticeVoicePlayer;
import com.ydd.yanshi.bean.CodePay;
import com.ydd.yanshi.bean.Contact;
import com.ydd.yanshi.bean.EventLoginStatus;
import com.ydd.yanshi.bean.EventNewNotice;
import com.ydd.yanshi.bean.EventSyncFriendOperating;
import com.ydd.yanshi.bean.Friend;
import com.ydd.yanshi.bean.MyZan;
import com.ydd.yanshi.bean.RoomMember;
import com.ydd.yanshi.bean.User;
import com.ydd.yanshi.bean.message.ChatMessage;
import com.ydd.yanshi.bean.message.NewFriendMessage;
import com.ydd.yanshi.bean.message.XmppMessage;
import com.ydd.yanshi.broadcast.CardcastUiUpdateUtil;
import com.ydd.yanshi.broadcast.MsgBroadcast;
import com.ydd.yanshi.broadcast.MucgroupUpdateUtil;
import com.ydd.yanshi.broadcast.OtherBroadcast;
import com.ydd.yanshi.call.CallConstants;
import com.ydd.yanshi.call.MessageEventMeetingInvited;
import com.ydd.yanshi.call.MessageEventSipEVent;
import com.ydd.yanshi.call.MessageHangUpPhone;
import com.ydd.yanshi.db.dao.ChatMessageDao;
import com.ydd.yanshi.db.dao.ContactDao;
import com.ydd.yanshi.db.dao.FriendDao;
import com.ydd.yanshi.db.dao.MyZanDao;
import com.ydd.yanshi.db.dao.NewFriendDao;
import com.ydd.yanshi.db.dao.RoomMemberDao;
import com.ydd.yanshi.db.dao.login.MachineDao;
import com.ydd.yanshi.helper.FriendHelper;
import com.ydd.yanshi.pay.EventPaymentSuccess;
import com.ydd.yanshi.pay.EventReceiptSuccess;
import com.ydd.yanshi.ui.base.CoreManager;
import com.ydd.yanshi.ui.circle.MessageEventNotifyDynamic;
import com.ydd.yanshi.ui.message.ChatActivity;
import com.ydd.yanshi.ui.message.HandleSyncMoreLogin;
import com.ydd.yanshi.ui.mucfile.XfileUtils;
import com.ydd.yanshi.util.Constants;
import com.ydd.yanshi.util.PreferenceUtils;
import com.ydd.yanshi.util.TimeUtils;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.json.JSONException;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.util.XmppStringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

import static android.content.Context.VIBRATOR_SERVICE;
import static com.ydd.yanshi.db.InternationalizationHelper.getString;
import static com.ydd.yanshi.xmpp.listener.ChatMessageListener.MESSAGE_SEND_SUCCESS;


/**
 * Created by Administrator on 2017/11/24.
 */

public class XChatMessageListener implements IncomingChatMessageListener {
    private CoreService mService;

    private String mLoginUserId;
    private Map<String, String> mMsgIDMap = new HashMap<>();// ?????????????????? ?????? ??????????????????????????????????????????

    public XChatMessageListener(CoreService service) {
        mService = service;
        mLoginUserId = CoreManager.requireSelf(service).getUserId();
        mMsgIDMap = new HashMap<>();
    }

    private Context getContext() {
        return mService;
    }

    @Override
    public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {
        Log.e("Tag_????????????Xch", "newIncomingMessage 4");
        mService.sendReceipt(message.getPacketID());
        // body???
        String from = XmppStringUtils.parseBareJid(message.getFrom().toString());
        String resource = message.getFrom().toString().substring(message.getFrom().toString().indexOf("/") + 1, message.getFrom().length());// ????????????resource ex:ios ???pc...
        String packetID = message.getPacketID();
        // body???(??????????????????ChatMessage)
        String body = message.getBody();
        ChatMessage chatMessage = new ChatMessage(body);
        String fromUserId = chatMessage.getFromUserId();
        String toUserId = chatMessage.getToUserId();

        if (TextUtils.isEmpty(chatMessage.getPacketId())) {
            chatMessage.setPacketId(packetID);
        }
        chatMessage.setFromId(message.getFrom().toString());
        chatMessage.setToId(message.getTo().toString());
        Log.e("msg", "from:" + message.getFrom() + " ,to:" + message.getTo());
        Log.e("msg", "fromUserId:" + fromUserId + " ,toUserId:" + toUserId);

        if (mMsgIDMap.containsKey(chatMessage.getPacketId())) {
            return;
        }
        if (mMsgIDMap.size() > 20) {
            mMsgIDMap.clear();
        }
        mMsgIDMap.put(chatMessage.getPacketId(), chatMessage.getPacketId());

        int type = chatMessage.getType();
        if (type == 0) { // ????????????
            return;
        }
        ChatMessageDao.getInstance().decryptDES(chatMessage);// ??????
        Log.e("msg", message.getBody());

        if (chatMessage.getType() == XmppMessage.TYPE_SEND_ONLINE_STATUS) {// ???????????????200??????
            moreLogin(message, resource, chatMessage);
            return;
        }

        if (chatMessage.getType() >= XmppMessage.TYPE_SYNC_OTHER
                && chatMessage.getType() <= XmppMessage.TYPE_SYNC_GROUP) {
            HandleSyncMoreLogin.distributionChatMessage(chatMessage, mService);
            return;
        }

        boolean isGroupLiveNewFriendType = false;
        if (chatMessage.getType() >= XmppMessage.TYPE_MUCFILE_ADD
                && chatMessage.getType() <= XmppMessage.TYPE_FACE_GROUP_NOTIFY) {// ?????????????????????fromUserId??????????????????toUserId
            isGroupLiveNewFriendType = true;
        }

        // ?????? ???????????? ??????
        if (fromUserId.equals(toUserId) && !isGroupLiveNewFriendType) {
            if (!resource.equals(MyApplication.MULTI_RESOURCE) && message.getTo().toString().substring(message.getTo().toString().indexOf("/") + 1, message.getTo().length()).equals(MyApplication.MULTI_RESOURCE)) {
                // from resource ???????????????
                // to   resource ???????????? ?????????????????????
                if (chatMessage.getType() == 26) {
                    String packetId = chatMessage.getContent();
                    ChatMessageDao.getInstance().updateMessageRead(mLoginUserId, resource, packetId, true);
                    boolean isReadChange = ChatMessageDao.getInstance().updateReadMessage(mLoginUserId, resource, packetId);
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("packetId", packetId);
                    bundle.putBoolean("isReadChange", isReadChange);
                    intent.setAction(OtherBroadcast.IsRead);
                    intent.putExtras(bundle);
                    LocalBroadcastManager.getInstance(MyApplication.getInstance()).sendBroadcast(intent);
                    return;
                }

                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, resource, chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, resource, chatMessage, false);
                }
            }
            return;
        }

        boolean isForwarding = false;// ??????????????????????????????
        boolean isNeedChangeMsgTableSave = false;// ????????????????????????????????????
        // ?????????????????????????????????????????????
        if (from.contains(mLoginUserId)) {
            // ????????????????????????????????????
            MachineDao.getInstance().updateMachineOnLineStatus(resource, true);

            // 1.??????????????????????????????
            // 2.??????????????????????????????
            if (fromUserId.equals(mLoginUserId)) {
                isNeedChangeMsgTableSave = true;
                Log.e("msg", "??????????????????????????????,???isNeedChangeMsgTableSave??????true");
                chatMessage.setMySend(true);
                chatMessage.setUpload(true);
                chatMessage.setUploadSchedule(100);
                chatMessage.setMessageState(MESSAGE_SEND_SUCCESS);
                if (ChatMessageDao.getInstance().hasSameMessage(mLoginUserId, toUserId, packetID)) {
                    Log.e("msg", "table exist this msg???return");
                    return;
                }

            } else {
                Log.e("msg", "??????????????????????????????");
                if (ChatMessageDao.getInstance().hasSameMessage(mLoginUserId, fromUserId, packetID)) {
                    Log.e("msg", "table exist this msg???return");
                    return;
                }
            }
            Log.e("msg", "table not exist this msg???carry on");
        } else {// ??????????????????????????????
            isForwarding = true;
            Log.e("msg", "??????????????????????????????,???isForwarding????????????true");

            Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, chatMessage.getFromUserId());
            if (friend != null && friend.getOfflineNoPushMsg() == 0) {
                mService.notificationMessage(chatMessage, false);// ??????????????????
            }
        }

        // ?????????????????????100-122
        if (type >= XmppMessage.TYPE_IS_CONNECT_VOICE && type <= XmppMessage.TYPE_EXIT_VOICE) {
            chatAudioVideo(chatMessage);
            return;
        }


        // ????????????????????? 301-304
        if (type >= XmppMessage.DIANZAN && type <= XmppMessage.ATMESEE) {
            chatDiscover(body, chatMessage);
            return;
        }

        // ????????????????????? 500-515
        if (type >= XmppMessage.TYPE_SAYHELLO && type <= XmppMessage.TYPE_BACK_DELETE) {
            chatFriend(body, chatMessage);
            return;
        }

        // ????????????????????????????????? 401-403
        if (type >= XmppMessage.TYPE_MUCFILE_ADD && type <= XmppMessage.TYPE_MUCFILE_DOWN) {
            Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, chatMessage.getObjectId());
            chatGroupTipForMe(body, chatMessage, friend);
            return;
        }

        /*
        ???????????????????????????????????????????????????(????????????????????????????????????????????????????????????????????????????????????(????????????????????????????????????????????????????????????,ex:???????????????...)???
        ??????????????????????????????(????????????????????????)??????????????????XMuChatMessageListener???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        ?????????????????????????????? ??????????????????????????????????????????????????????
         */
        if ((type >= XmppMessage.TYPE_CHANGE_NICK_NAME && type <= XmppMessage.NEW_MEMBER)
                || type == XmppMessage.TYPE_SEND_MANAGER
                || type == XmppMessage.TYPE_UPDATE_ROLE) {
            Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, chatMessage.getObjectId());
            if (ChatMessageDao.getInstance().hasSameMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage.getPacketId())) {
                // ??????????????????????????????????????????????????????????????????????????????????????????????????????
                Log.e("msg", "Return 6");
                return;
            }
            if (friend != null || type == XmppMessage.NEW_MEMBER) {
                if (chatMessage.getFromUserId().equals(mLoginUserId)) {
                    chatGroupTipFromMe(body, chatMessage, friend);
                } else {
                    chatGroupTipForMe(body, chatMessage, friend);
                }
            }
            return;
        }

        // ??????????????????[2] 915-925
        if (type >= XmppMessage.TYPE_CHANGE_SHOW_READ && type <= XmppMessage.TYPE_GROUP_TRANSFER) {
            boolean isJumpOver = false;
            if (type == XmppMessage.TYPE_GROUP_VERIFY) {// ????????????????????????????????????????????????????????????object?????????json
                isJumpOver = true;
            }
            if (!isJumpOver && ChatMessageDao.getInstance().hasSameMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage.getPacketId())) {
                // ??????????????????????????????????????????????????????????????????????????????????????????????????????
                Log.e("msg", "Return 7");
                return;
            }
            JSONObject jsonObject = JSONObject.parseObject(body);
            String toUserName = jsonObject.getString("toUserName");
            chatGroupTip2(type, chatMessage, toUserName);
            return;
        }

        // ???????????????????????????xmpp
        if (type == XmppMessage.TYPE_DISABLE_GROUP) {
            if (chatMessage.getContent().equals("-1")) {// ??????
                FriendDao.getInstance().updateFriendGroupStatus(mLoginUserId, chatMessage.getObjectId(), 3);// ????????????????????????
            } else if (chatMessage.getContent().equals("1")) {// ??????
                FriendDao.getInstance().updateFriendGroupStatus(mLoginUserId, chatMessage.getObjectId(), 0);// ????????????????????????
            }
            LocalBroadcastManager.getInstance(MyApplication.getInstance()).sendBroadcast(new Intent(MsgBroadcast.ACTION_DISABLE_GROUP_BY_SERVICE));
            return;
        }

        // ????????????????????????????????????
        if (type == XmppMessage.TYPE_FACE_GROUP_NOTIFY) {
            MsgBroadcast.broadcastFaceGroupNotify(MyApplication.getContext(), "notify_list");
            return;
        }

        // ???????????????????????????????????????
        if (chatMessage.getType() == XmppMessage.TYPE_READ) {
            if (MyApplication.IS_SUPPORT_MULTI_LOGIN && isForwarding) {
                mService.sendChatMessage(mLoginUserId, chatMessage);
            }
            String packetId = chatMessage.getContent();

            if (chatMessage.getFromUserId().equals(mLoginUserId)) {// ??????????????????????????????
                ChatMessage msgById = ChatMessageDao.getInstance().findMsgById(mLoginUserId, chatMessage.getToUserId(), packetId);
                if (msgById != null && msgById.getIsReadDel()) {// ?????????????????????????????????????????????????????????????????????
                    if (ChatMessageDao.getInstance().deleteSingleChatMessage(mLoginUserId, chatMessage.getToUserId(), packetId)) {
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString("MULTI_LOGIN_READ_DELETE_PACKET", packetId);
                        intent.setAction(OtherBroadcast.MULTI_LOGIN_READ_DELETE);
                        intent.putExtras(bundle);
                        LocalBroadcastManager.getInstance(MyApplication.getInstance()).sendBroadcast(intent);
                    }
                }
            } else {
                ChatMessageDao.getInstance().updateMessageRead(mLoginUserId, fromUserId, packetId, true);// ?????????????????????
                boolean isReadChange = ChatMessageDao.getInstance().updateReadMessage(mLoginUserId, fromUserId, packetId);
                // ??????????????????????????????????????????????????????????????????
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("packetId", packetId);
                bundle.putBoolean("isReadChange", isReadChange);
                intent.setAction(OtherBroadcast.IsRead);
                intent.putExtras(bundle);
                LocalBroadcastManager.getInstance(MyApplication.getInstance()).sendBroadcast(intent);
            }
            return;
        }

        if (type == XmppMessage.TYPE_INPUT) {
            Intent intent = new Intent();
            intent.putExtra("fromId", chatMessage.getFromUserId());
            intent.setAction(OtherBroadcast.TYPE_INPUT);
            LocalBroadcastManager.getInstance(MyApplication.getInstance()).sendBroadcast(intent);
            return;
        }

        // ????????????????????????????????????
        if (type == XmppMessage.TYPE_BACK) {
            if (MyApplication.IS_SUPPORT_MULTI_LOGIN && isForwarding) {
                mService.sendChatMessage(mLoginUserId, chatMessage);
            }
            backMessage(chatMessage);
            return;
        }

        // ???????????????????????????
        if (type == XmppMessage.TYPE_83) {
            if (MyApplication.IS_SUPPORT_MULTI_LOGIN && isForwarding) {
                mService.sendChatMessage(mLoginUserId, chatMessage);
            }

            String fromName;
            String toName;
            if (fromUserId.equals(mLoginUserId)) {
                fromName = MyApplication.getContext().getString(R.string.you);
                toName = MyApplication.getContext().getString(R.string.self);
            } else {
                fromName = chatMessage.getFromUserName();
                toName = MyApplication.getContext().getString(R.string.you);
            }

            String hasBennReceived = "";
            if (chatMessage.getFileSize() == 1) {// ??????????????????
                try {
                    String sRedSendTime = chatMessage.getFilePath();
                    long redSendTime = Long.parseLong(sRedSendTime);
                    long betweenTime = chatMessage.getTimeSend() - redSendTime;
                    String sBetweenTime;
                    if (betweenTime < TimeUnit.MINUTES.toSeconds(1)) {
                        sBetweenTime = betweenTime + MyApplication.getContext().getString(R.string.second);
                    } else if (betweenTime < TimeUnit.HOURS.toSeconds(1)) {
                        sBetweenTime = TimeUnit.SECONDS.toMinutes(betweenTime) + MyApplication.getContext().getString(R.string.minute);
                    } else {
                        sBetweenTime = TimeUnit.SECONDS.toHours(betweenTime) + MyApplication.getContext().getString(R.string.hour);
                    }
                    hasBennReceived = MyApplication.getContext().getString(R.string.red_packet_has_received_place_holder, sBetweenTime);
                } catch (Exception e) {
                    hasBennReceived = MyApplication.getContext().getString(R.string.red_packet_has_received);
                }
            }
            String str = MyApplication.getContext().getString(R.string.tip_receive_red_packet_place_holder, fromName, toName) + hasBennReceived;

            // ????????????????????????????????? ??????????????????????????????????????????type???id?????????????????????
            chatMessage.setFileSize(XmppMessage.TYPE_83);
            chatMessage.setFilePath(chatMessage.getContent());

            chatMessage.setType(XmppMessage.TYPE_TIP);
//            chatMessage.setContent(str);
            if (!TextUtils.isEmpty(chatMessage.getObjectId())) {// ?????????????????? ???????????????
                fromUserId = chatMessage.getObjectId();
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, true);
                }
            } else {// ??????????????????
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, false);
                }
            }
            return;
        }

        // ?????????????????????
        if (type == XmppMessage.TYPE_RED_BACK) {
            if (MyApplication.IS_SUPPORT_MULTI_LOGIN && isForwarding) {
                mService.sendChatMessage(mLoginUserId, chatMessage);
            }

            String str = MyApplication.getContext().getString(R.string.tip_red_back);
            chatMessage.setType(XmppMessage.TYPE_TIP);
//            chatMessage.setContent(str);
            if (!TextUtils.isEmpty(chatMessage.getObjectId())) {// ?????????????????? ???????????????
                fromUserId = chatMessage.getObjectId();
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, true);
                }
            } else {// ??????????????????
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, false);
                }
            }
            return;
        }

        if (type == XmppMessage.TYPE_TRANSFER_RECEIVE) {
            if (MyApplication.IS_SUPPORT_MULTI_LOGIN && isForwarding) {
                mService.sendChatMessage(mLoginUserId, chatMessage);
            }
            String str;
            if (type == XmppMessage.TYPE_TRANSFER_RECEIVE) {
                str = MyApplication.getContext().getString(R.string.transfer_received);
            } else {
                str = MyApplication.getContext().getString(R.string.transfer_backed);
            }
            chatMessage.setType(XmppMessage.TYPE_TIP);
//            chatMessage.setContent(str);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, false);
            }
            return;
        }

        // ????????????????????????
        if (type >= XmppMessage.TYPE_TRANSFER_BACK && type <= XmppMessage.TYPE_RECEIPT_GET) {
            if (MyApplication.IS_SUPPORT_MULTI_LOGIN && isForwarding) {
                mService.sendChatMessage(mLoginUserId, chatMessage);
            }
            if (type == XmppMessage.TYPE_PAYMENT_OUT) {// ?????????????????????
                CodePay codePay = JSON.parseObject(chatMessage.getContent(), CodePay.class);
                EventBus.getDefault().post(new EventPaymentSuccess(codePay.getToUserName()));
            } else if (type == XmppMessage.TYPE_RECEIPT_GET) {// ?????????????????????
                CodePay codePay = JSON.parseObject(chatMessage.getContent(), CodePay.class);
                EventBus.getDefault().post(new EventReceiptSuccess(codePay.getToUserName()));
            }
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, false);
            }
            return;
        }

        if (type == XmppMessage.TYPE_SCREENSHOT) {
            chatMessage.setType(XmppMessage.TYPE_TIP);
//            chatMessage.setContent(mService.getString(R.string.tip_remote_screenshot));
            // ?????????tip??????return????????????????????????
        } else if (type == XmppMessage.TYPE_SYNC_CLEAN_CHAT_HISTORY) {
            Intent intent = new Intent();
            if (isNeedChangeMsgTableSave) {
                intent.putExtra(AppConstant.EXTRA_USER_ID, chatMessage.getToUserId());
            } else {
                intent.putExtra(AppConstant.EXTRA_USER_ID, chatMessage.getFromUserId());
                chatMessage.setType(XmppMessage.TYPE_TIP);
                chatMessage.setFileSize(XmppMessage.TYPE_SYNC_CLEAN_CHAT_HISTORY);
                chatMessage.setContent(mService.getString(R.string.tip_remote_screenshot));
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getFromUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getFromUserId(), chatMessage, false);
                }
            }
            intent.setAction(OtherBroadcast.SYNC_CLEAN_CHAT_HISTORY);
            LocalBroadcastManager.getInstance(MyApplication.getInstance()).sendBroadcast(intent);
            return;
        }

        // ????????????
        if (chatMessage.isExpired()) {// ???????????????????????????(?????????????????????????????????)????????????????????????
            Log.e("msg", "???????????????????????????(?????????????????????????????????)????????????????????????");
            return;
        }

        // ?????????
        if (type == XmppMessage.TYPE_SHAKE) {
            Vibrator vibrator = (Vibrator) MyApplication.getContext().getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {100, 400, 100, 400};
            vibrator.vibrate(pattern, -1);
        }

        Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, chatMessage.getFromUserId());
        if (friend != null) {
            Log.e("msg", "???????????????????????????");
            if (friend.getStatus() != -1) {
                saveCurrentMessage(chatMessage, isForwarding, isNeedChangeMsgTableSave);
                if (friend.getOfflineNoPushMsg() == 0) {// ???????????????????????? ?????????
                    if (!chatMessage.getFromUserId().equals(MyApplication.IsRingId)
                            && isForwarding) {// ?????????????????????????????????????????????????????? && ???????????????
                        Log.e("msg", "????????????");
//                        if (!MessageFragment.foreground) {
                        // ????????????????????????
                        NoticeVoicePlayer.getInstance().start();
//                        }
                    }
                } else {
                    Log.e("msg", "??????????????????????????????????????????????????????");
                }
            }
        } else {
            Log.e("msg", "???????????????????????????");
            FriendDao.getInstance().createNewFriend(chatMessage);
            saveCurrentMessage(chatMessage, isForwarding, isNeedChangeMsgTableSave);
        }
    }

    private void backMessage(ChatMessage chatMessage) {
        // ?????????????????????
        String packetId = chatMessage.getContent();
        if (TextUtils.isEmpty(packetId)) {
            return;
        }
        if (chatMessage.getFromUserId().equals(mLoginUserId)) {// ???????????????
            ChatMessageDao.getInstance().updateMessageBack(mLoginUserId, chatMessage.getToUserId(), packetId, MyApplication.getContext().getString(R.string.you));
        } else {
            ChatMessageDao.getInstance().updateMessageBack(mLoginUserId, chatMessage.getFromUserId(), packetId, chatMessage.getFromUserName());
        }

        /** ?????????????????? */
        Intent intent = new Intent();
        intent.putExtra("packetId", packetId);
        intent.setAction(OtherBroadcast.MSG_BACK);
        LocalBroadcastManager.getInstance(MyApplication.getInstance()).sendBroadcast(intent);

        // ??????UI??????
        if (chatMessage.getFromUserId().equals(mLoginUserId)) {
            ChatMessage chat = ChatMessageDao.getInstance().getLastChatMessage(mLoginUserId, chatMessage.getToUserId());
            if (chat.getPacketId().equals(packetId)) {
                // ??????????????????????????????????????????????????????
                FriendDao.getInstance().updateFriendContent(mLoginUserId, chatMessage.getToUserId(),
                        MyApplication.getContext().getString(R.string.you) + " " + getString("JX_OtherWithdraw"), XmppMessage.TYPE_TEXT, chat.getTimeSend());
                MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
            }
        } else {
            ChatMessage chat = ChatMessageDao.getInstance().getLastChatMessage(mLoginUserId, chatMessage.getFromUserId());
            if (chat.getPacketId().equals(packetId)) {
                // ??????????????????????????????????????????????????????
                FriendDao.getInstance().updateFriendContent(mLoginUserId, chatMessage.getFromUserId(),
                        chatMessage.getFromUserName() + " " + getString("JX_OtherWithdraw"), XmppMessage.TYPE_TEXT, chat.getTimeSend());
                MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
            }
        }
    }

    private void saveCurrentMessage(ChatMessage chatMessage, boolean isForwarding, boolean isNeedChangeMsgTableSave) {
        if (isNeedChangeMsgTableSave) {
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getToUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getFromUserId(), chatMessage, false);
            }
        } else {
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getFromUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getFromUserId(), chatMessage, false);
            }
        }

        if (MyApplication.IS_SUPPORT_MULTI_LOGIN && isForwarding) {
            Log.e("msg", "????????????????????????????????????????????????");
            mService.sendChatMessage(mLoginUserId, chatMessage);
        }
    }

    private void chatGroupTipFromMe(String body, ChatMessage chatMessage, Friend friend) {
        JSONObject jsonObject = JSONObject.parseObject(body);
        String toUserId = jsonObject.getString("toUserId");
        String toUserName = jsonObject.getString("toUserName");

        // ??????????????????????????????????????????toUserName????????????
        String xT = getName(friend, toUserId);
        if (!TextUtils.isEmpty(xT)) {
            toUserName = xT;
        }

        chatMessage.setGroup(false);
        switch (chatMessage.getType()) {
            case XmppMessage.TYPE_CHANGE_NICK_NAME:
                // ????????????????????????
                String content = chatMessage.getContent();
                if (!TextUtils.isEmpty(content)) {
                    friend.setRoomMyNickName(content);
                    FriendDao.getInstance().updateRoomMyNickName(friend.getUserId(), content);
                    ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), mLoginUserId, content);
                    ChatMessageDao.getInstance().updateNickName(mLoginUserId, friend.getUserId(), mLoginUserId, content);

//                    chatMessage.setContent(chatMessage.getFromUserName() + " " + getString("JXMessageObject_UpdateNickName") + "???" + content + "???");
                    chatMessage.setType(XmppMessage.TYPE_TIP);
                    if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                        ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                    }
                }
                break;
            case XmppMessage.TYPE_CHANGE_ROOM_NAME:
                // ???????????????
                String groupName = chatMessage.getContent();
                FriendDao.getInstance().updateMucFriendRoomName(friend.getUserId(), groupName);
                ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), "ROOMNAMECHANGE", groupName);

//                chatMessage.setContent(chatMessage.getFromUserName() + " " + getString("JXMessageObject_UpdateRoomName") + groupName);
                chatMessage.setType(XmppMessage.TYPE_TIP);
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                }
                break;
            case XmppMessage.TYPE_DELETE_ROOM:
                // ?????????????????????
                mService.exitMucChat(chatMessage.getObjectId());
                FriendDao.getInstance().deleteFriend(mLoginUserId, chatMessage.getObjectId());
                // ??????????????????
                ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, chatMessage.getObjectId());
                RoomMemberDao.getInstance().deleteRoomMemberTable(chatMessage.getObjectId());
                // ??????????????????
                MsgBroadcast.broadcastMsgNumReset(mService);
                MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
                MucgroupUpdateUtil.broadcastUpdateUi(mService);
                break;
            case XmppMessage.TYPE_DELETE_MEMBER:
                if (toUserId.equals(mLoginUserId)) {
                    // ?????????????????????
                    mService.exitMucChat(chatMessage.getObjectId());
                    FriendDao.getInstance().deleteFriend(mLoginUserId, chatMessage.getObjectId());
                    // ??????????????????
                    ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, chatMessage.getObjectId());
                    RoomMemberDao.getInstance().deleteRoomMemberTable(chatMessage.getObjectId());
                    // ??????????????????
                    MsgBroadcast.broadcastMsgNumReset(mService);
                    MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
                    MucgroupUpdateUtil.broadcastUpdateUi(mService);
                } else {
                    // toUserId??????????????????
//                    chatMessage.setContent(toUserName + " " + getString("KICKED_OUT_GROUP"));
                    chatMessage.setType(XmppMessage.TYPE_TIP);
                    // chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                    if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                        ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                    }
                }
                break;
            case XmppMessage.TYPE_NEW_NOTICE:
                // ??????????????????
                EventBus.getDefault().post(new EventNewNotice(chatMessage));
                String notice = chatMessage.getContent();
//                chatMessage.setContent(chatMessage.getFromUserName() + " " + getString("JXMessageObject_AddNewAdv") + notice);
                chatMessage.setType(XmppMessage.TYPE_TIP);
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                }
                break;
            case XmppMessage.TYPE_GAG:
                // ????????????????????????????????????
                long time = Long.parseLong(chatMessage.getContent());
                // ??????????????????????????????????????????3s?????????
                if (time > (System.currentTimeMillis() / 1000) + 3) {
                    String formatTime = XfileUtils.fromatTime((time * 1000), "MM-dd HH:mm");
                    // message.setContent("?????????" + toUserName + " ???????????????" + formatTime);
//                    chatMessage.setContent(chatMessage.getFromUserName() + " " + getString("JXMessageObject_Yes") + toUserName +
//                            getString("JXMessageObject_SetGagWithTime") + formatTime);
                } else {
                    // message.setContent("?????????" + toUserName + " ??????????????????");
                    /*chatMessage.setContent(chatMessage.getFromUserName() + " " + getString("JXMessageObject_Yes") + toUserName +
                            getString("JXMessageObject_CancelGag"));*/
//                    chatMessage.setContent(MyApplication.getContext().getString(R.string.be_cancel_ban_place_holder, toUserName, chatMessage.getFromUserName()));
                }

                chatMessage.setType(XmppMessage.TYPE_TIP);
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                }
                break;
            case XmppMessage.NEW_MEMBER:
                Log.e("TAG_????????????XCHAT", "???????????????");
                String desc = chatMessage.getFromUserName() + " " + getString("JXMessageObject_GroupChat");
                if (toUserId.equals(mLoginUserId)) {
                    /**
                     * ???????????????????????????????????????
                     * 1.??????????????????????????????
                     * 2.???????????????????????????
                     * 3.?????????????????????????????????
                     * 4.?????????????????????????????????
                     * 5.???????????????????????????????????????????????????907
                     *
                     * ?????????????????????????????????????????????smack??????Type==907???????????????
                     */
                    Friend mFriend = FriendDao.getInstance().getFriend(mLoginUserId, chatMessage.getObjectId());
                    if (mFriend != null && mFriend.getGroupStatus() == 1) {// ???????????????????????????????????????????????? ??????????????????????????????(?????????updateGroupStatus??????????????????????????????????????????????????????????????????????????????)
                        FriendDao.getInstance().deleteFriend(mLoginUserId, friend.getUserId());
                        ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, friend.getUserId());
                        mFriend = null;
                    }

                    // ????????????????????????????????????????????????
                    String roomId = "";
                    try {
                        roomId = jsonObject.getString("fileName");
                        String other = jsonObject.getString("other");
                        JSONObject jsonObject2 = JSONObject.parseObject(other);
                        int showRead = jsonObject2.getInteger("showRead");
                        int allowSecretlyChat = jsonObject2.getInteger("allowSendCard");
                        MyApplication.getInstance().saveGroupPartStatus(chatMessage.getObjectId(), showRead, allowSecretlyChat,
                                1, 1, 0);
                    } catch (Exception e) {
                        Log.e("msg", "?????????????????????");
                    }

                    /**
                     * ?????????????????????mFriend???????????????????????????????????????????????????????????????????????????(?????????????????????)?????????????????????????????????????????????Type==907???
                     * ??????????????????????????????????????????????????????????????????????????????
                     */
                    if (mFriend == null
                            && !chatMessage.getObjectId().equals(MyApplication.mRoomKeyLastCreate)) {
                        Friend mCreateFriend = new Friend();
                        mCreateFriend.setOwnerId(mLoginUserId);
                        mCreateFriend.setUserId(chatMessage.getObjectId());
                        mCreateFriend.setNickName(chatMessage.getContent());
                        mCreateFriend.setDescription("");
                        mCreateFriend.setRoomId(roomId);
                        mCreateFriend.setContent(desc);
                        mCreateFriend.setTimeSend(chatMessage.getTimeSend());
                        mCreateFriend.setRoomFlag(1);
                        mCreateFriend.setStatus(Friend.STATUS_FRIEND);
                        mCreateFriend.setGroupStatus(0);
                        FriendDao.getInstance().createOrUpdateFriend(mCreateFriend);

                        // ??????smack?????????????????????
                        mService.joinMucChat(chatMessage.getObjectId(), 0);
                        MsgBroadcast.broadcastFaceGroupNotify(MyApplication.getContext(), "join_room");
                    }
                } else {
                    // toUserId????????????????????????
                    desc = chatMessage.getFromUserName() + " " + getString("JXMessageObject_InterFriend") + toUserName;

                    String roomId = jsonObject.getString("fileName");
                    operatingRoomMemberDao(0, roomId, chatMessage.getToUserId(), toUserName);
                }
                chatMessage.setType(XmppMessage.TYPE_TIP);
                chatMessage.setContent(desc);
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getObjectId(), chatMessage, true);
                    MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(MyApplication.getInstance());
                }
                break;
            case XmppMessage.TYPE_SEND_MANAGER:
                // ???????????????????????????/???????????????
                String messageType = chatMessage.getContent();
                if (messageType.equals("1")) {
//                    chatMessage.setContent(chatMessage.getFromUserName() + " " + InternationalizationHelper.getString("JXSettingVC_Set") + toUserName + " " + InternationalizationHelper.getString("JXMessage_admin"));
                } else {
//                    chatMessage.setContent(chatMessage.getFromUserName() + " " + InternationalizationHelper.getString("JXSip_Canceled") + toUserName + " " + InternationalizationHelper.getString("JXMessage_admin"));
                }
                chatMessage.setType(XmppMessage.TYPE_TIP);
                chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                }
                break;
            case XmppMessage.TYPE_UPDATE_ROLE:
                // ???????????????????????????/???????????????
                int tipContent = -1;
                switch (chatMessage.getContent()) {
                    case "1": // 1:???????????????
                        tipContent = R.string.tip_set_invisible_place_holder;
                        break;
                    case "-1": // -1:???????????????
                        tipContent = R.string.tip_cancel_invisible_place_holder;
                        break;
                    case "2": // 2??????????????????
                        tipContent = R.string.tip_set_guardian_place_holder;
                        break;
                    case "0": // 0??????????????????
                        tipContent = R.string.tip_cancel_guardian_place_holder;
                        break;
                    default:
                        Reporter.unreachable();
                        return;
                }
//                chatMessage.setContent(getContext().getString(tipContent, chatMessage.getFromUserName(), toUserName));
                chatMessage.setType(XmppMessage.TYPE_TIP);
                chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                }
                break;

        }
    }

    /**
     * ????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * ????????????????????????????????????????????????????????????????????????(fromUserId==????????????id)
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * ????????????????????????????????????????????????????????????????????????
     */
    private void chatGroupTipForMe(String body, ChatMessage chatMessage, Friend friend) {
        Log.e("Tag_????????????Xch", "chatGroupTipForMe 4");
        int type = chatMessage.getType();
        String fromUserId = chatMessage.getFromUserId();
        String fromUserName = chatMessage.getFromUserName();
        JSONObject jsonObject = JSONObject.parseObject(body);
        String toUserId = jsonObject.getString("toUserId");
        String toUserName = jsonObject.getString("toUserName");

        if (!TextUtils.isEmpty(toUserId)) {
            if (toUserId.equals(mLoginUserId)) {// ??????????????????????????????????????????fromUserName??????
                String xF = getName(friend, fromUserId);
                if (!TextUtils.isEmpty(xF)) {
                    fromUserName = xF;
                }
            } else {// ????????????????????????????????????fromUserName???toUserName???????????????
                String xF = getName(friend, fromUserId);
                if (!TextUtils.isEmpty(xF)) {
                    fromUserName = xF;
                }
                String xT = getName(friend, toUserId);
                if (!TextUtils.isEmpty(xT)) {
                    toUserName = xT;
                }
            }
        }

        chatMessage.setGroup(false);
        if (type == XmppMessage.TYPE_CHANGE_NICK_NAME) {
            // ??????????????????
            String content = chatMessage.getContent();
            if (!TextUtils.isEmpty(toUserId) && toUserId.equals(mLoginUserId)) {
                // ??????????????????
                if (!TextUtils.isEmpty(content)) {
                    friend.setRoomMyNickName(content);
                    FriendDao.getInstance().updateRoomMyNickName(friend.getUserId(), content);
                    ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), toUserId, content);
                    ChatMessageDao.getInstance().updateNickName(mLoginUserId, friend.getUserId(), toUserId, content);
                }
            } else {
                // ????????????????????????????????????????????????
//                chatMessage.setContent(fromUserName + " " + getString("JXMessageObject_UpdateNickName") + "???" + content + "???");
                chatMessage.setType(XmppMessage.TYPE_TIP);
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                }
                ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), toUserId, content);
                ChatMessageDao.getInstance().updateNickName(mLoginUserId, friend.getUserId(), toUserId, content);
            }
        } else if (type == XmppMessage.TYPE_CHANGE_ROOM_NAME) {
            // ???????????????
            // ???????????????
            String content = chatMessage.getContent();
            FriendDao.getInstance().updateMucFriendRoomName(friend.getUserId(), content);
            ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), "ROOMNAMECHANGE", content);

//            chatMessage.setContent(toUserName + " " + getString("JXMessageObject_UpdateRoomName") + content);
            chatMessage.setType(XmppMessage.TYPE_TIP);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
            }
        } else if (type == XmppMessage.TYPE_DELETE_ROOM) {
            // ??????????????????
            if (fromUserId.equals(toUserId)) {
                // ????????????
                FriendDao.getInstance().deleteFriend(mLoginUserId, chatMessage.getObjectId());
                // ??????????????????
                ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, chatMessage.getObjectId());
                RoomMemberDao.getInstance().deleteRoomMemberTable(chatMessage.getObjectId());
                // ??????????????????
                MsgBroadcast.broadcastMsgNumReset(mService);
                MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
                MucgroupUpdateUtil.broadcastUpdateUi(mService);
            } else {
                mService.exitMucChat(chatMessage.getObjectId());
                // 2 ????????????????????????  ???????????????
                FriendDao.getInstance().updateFriendGroupStatus(mLoginUserId, friend.getUserId(), 2);
                chatMessage.setType(XmppMessage.TYPE_TIP);
//                chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_disbanded));
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getObjectId(), chatMessage, true);
                }
            }
            ListenerManager.getInstance().notifyDeleteMucRoom(chatMessage.getObjectId());
        } else if (type == XmppMessage.TYPE_DELETE_MEMBER) {
            // ?????? ?????? || ??????
            chatMessage.setType(XmppMessage.TYPE_TIP);
            // ?????? || ???????????????
            if (toUserId.equals(mLoginUserId)) { // ????????????????????????
                if (fromUserId.equals(toUserId)) {
                    // ?????????????????????
                    mService.exitMucChat(friend.getUserId());
                    // ??????????????????
                    FriendDao.getInstance().deleteFriend(mLoginUserId, friend.getUserId());
                    RoomMemberDao.getInstance().deleteRoomMemberTable(chatMessage.getObjectId());
                    // ??????????????????
                    ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, friend.getUserId());
                    // ??????????????????
                    MsgBroadcast.broadcastMsgNumReset(mService);
                    MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
                    MucgroupUpdateUtil.broadcastUpdateUi(mService);
                } else {
                    // ???xx???????????????
                    mService.exitMucChat(friend.getUserId());
                    // / 1 ??????????????????????????? ???????????????
                    FriendDao.getInstance().updateFriendGroupStatus(mLoginUserId, friend.getUserId(), 1);
//                    chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_been_kick_place_holder, fromUserName));

                    ListenerManager.getInstance().notifyMyBeDelete(friend.getUserId());// ????????????????????????
                }
            } else {
                // ??????????????? || ?????????
                if (fromUserId.equals(toUserId)) {
                    // message.setContent(toUserName + "???????????????");
//                    chatMessage.setContent(toUserName + " " + getString("QUIT_GROUP"));
                } else {
                    // message.setContent(toUserName + "???????????????");
//                    chatMessage.setContent(toUserName + " " + getString("KICKED_OUT_GROUP"));
                }
                // ??????RoomMemberDao?????????????????????
                operatingRoomMemberDao(1, friend.getRoomId(), toUserId, null);
                MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(MyApplication.getContext());
            }

            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
            }
        } else if (type == XmppMessage.TYPE_NEW_NOTICE) {
            // ????????????
            EventBus.getDefault().post(new EventNewNotice(chatMessage));
            String content = chatMessage.getContent();
//            chatMessage.setContent(fromUserName + " " + getString("JXMessageObject_AddNewAdv") + content);
            chatMessage.setType(XmppMessage.TYPE_TIP);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
            }
        } else if (type == XmppMessage.TYPE_GAG) {// ????????????????????????
            long time = Long.parseLong(chatMessage.getContent());
            if (toUserId != null && toUserId.equals(mLoginUserId)) {
                // ????????????|| ???????????? ??????RoomTalkTime??????
                FriendDao.getInstance().updateRoomTalkTime(mLoginUserId, friend.getUserId(), (int) time);
                ListenerManager.getInstance().notifyMyVoiceBanned(friend.getUserId(), (int) time);
            }

            // ??????????????????????????????????????????3s?????????
            if (time > (System.currentTimeMillis() / 1000) + 3) {
                String formatTime = XfileUtils.fromatTime((time * 1000), "MM-dd HH:mm");
                // message.setContent("?????????" + toUserName + " ???????????????" + formatTime);
//                chatMessage.setContent(fromUserName + " " + getString("JXMessageObject_Yes") + toUserName +
//                        getString("JXMessageObject_SetGagWithTime") + formatTime);
            } else {
                // message.setContent("?????????" + toUserName + " ??????????????????");
                /*chatMessage.setContent(chatMessage.getFromUserName() + " " + getString("JXMessageObject_Yes") + toUserName +
                        getString("JXMessageObject_CancelGag"));*/
//                chatMessage.setContent(toUserName + MyApplication.getContext().getString(R.string.tip_been_cancel_ban_place_holder, fromUserName));
            }

            chatMessage.setType(XmppMessage.TYPE_TIP);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
            }
        } else if (type == XmppMessage.NEW_MEMBER) {
            String desc = "";
            if (chatMessage.getFromUserId().equals(toUserId)) {
                // ????????????
                desc = fromUserName + " " + getString("JXMessageObject_GroupChat");
            } else {
                // ???????????????
                desc = fromUserName + " " + getString("JXMessageObject_InterFriend") + toUserName;

                String roomId = jsonObject.getString("fileName");
                if (!toUserId.equals(mLoginUserId)) {// ????????????????????????????????????RoomMemberDao???????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    operatingRoomMemberDao(0, roomId, chatMessage.getToUserId(), toUserName);
                }
            }

            if (toUserId.equals(mLoginUserId)) {
                // ????????????????????????????????? ?????????????????????
                if (friend != null && friend.getGroupStatus() == 1) {// ???????????????????????????????????????????????? ??????????????????????????????(?????????updateGroupStatus??????????????????????????????????????????????????????????????????????????????)
                    FriendDao.getInstance().deleteFriend(mLoginUserId, friend.getUserId());
                    ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, friend.getUserId());
                }

                String roomId = "";
                // ????????????????????????????????????????????????
                try {
                    // ?????????????????????????????????????????????????????????????????????????????????
                    roomId = jsonObject.getString("fileName");
                    String other = jsonObject.getString("other");
                    JSONObject jsonObject2 = JSONObject.parseObject(other);
                    int showRead = jsonObject2.getInteger("showRead");
                    int allowSecretlyChat = jsonObject2.getInteger("allowSendCard");
                    MyApplication.getInstance().saveGroupPartStatus(chatMessage.getObjectId(), showRead, allowSecretlyChat,
                            1, 1, 0);
                } catch (Exception e) {
                    Log.e("msg", "?????????????????????");
                }

                Friend mCreateFriend = new Friend();
                mCreateFriend.setOwnerId(mLoginUserId);
                mCreateFriend.setUserId(chatMessage.getObjectId());
                mCreateFriend.setNickName(chatMessage.getContent());
                mCreateFriend.setDescription("");
                mCreateFriend.setRoomId(roomId);
                mCreateFriend.setContent(desc);
                mCreateFriend.setTimeSend(chatMessage.getTimeSend());
                mCreateFriend.setRoomFlag(1);
                mCreateFriend.setStatus(Friend.STATUS_FRIEND);
                mCreateFriend.setGroupStatus(0);
                FriendDao.getInstance().createOrUpdateFriend(mCreateFriend);
                // ??????smack?????????????????????
                // ????????????????????????lastSeconds == ???????????? - ?????????????????????
                mService.joinMucChat(chatMessage.getObjectId(), 0);
            }
            Log.e("TAG_????????????XCHAT", "desc=" + desc);
            // ???????????????
//            chatMessage.setType(XmppMessage.NEW_MEMBER);
            chatMessage.setType(XmppMessage.TYPE_TIP);
            chatMessage.setContent(desc);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getObjectId(), chatMessage, true);
                MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(MyApplication.getContext());
            }
        } else if (type == XmppMessage.TYPE_SEND_MANAGER) {
            String content = chatMessage.getContent();
            int role;
            if (content.equals("1")) {
                role = 2;
//                chatMessage.setContent(fromUserName + " " + InternationalizationHelper.getString("JXSettingVC_Set") + toUserName + " " + InternationalizationHelper.getString("JXMessage_admin"));
            } else {
                role = 3;
//                chatMessage.setContent(fromUserName + " " + InternationalizationHelper.getString("JXSip_Canceled") + toUserName + " " + InternationalizationHelper.getString("JXMessage_admin"));
            }

            RoomMemberDao.getInstance().updateRoomMemberRole(friend.getRoomId(), toUserId, role);

            chatMessage.setType(XmppMessage.TYPE_TIP);
            // chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                Intent intent = new Intent();
                intent.putExtra("roomId", friend.getUserId());
                intent.putExtra("toUserId", chatMessage.getToUserId());
                intent.putExtra("isSet", content.equals("1"));
                intent.setAction(OtherBroadcast.REFRESH_MANAGER);
                LocalBroadcastManager.getInstance(MyApplication.getInstance()).sendBroadcast(intent);
            }
        } else if (type == XmppMessage.TYPE_UPDATE_ROLE) {
            int tipContent = -1;
            int role = RoomMember.ROLE_MEMBER;
            switch (chatMessage.getContent()) {
                case "1": // 1:???????????????
                    tipContent = R.string.tip_set_invisible_place_holder;
                    role = RoomMember.ROLE_INVISIBLE;
                    break;
                case "-1": // -1:???????????????
                    tipContent = R.string.tip_cancel_invisible_place_holder;
                    break;
                case "2": // 2??????????????????
                    tipContent = R.string.tip_set_guardian_place_holder;
                    role = RoomMember.ROLE_GUARDIAN;
                    break;
                case "0": // 0??????????????????
                    tipContent = R.string.tip_cancel_guardian_place_holder;
                    break;
                default:
                    Reporter.unreachable();
                    return;
            }
//            chatMessage.setContent(getContext().getString(tipContent, chatMessage.getFromUserName(), toUserName));

            RoomMemberDao.getInstance().updateRoomMemberRole(friend.getRoomId(), toUserId, role);

            chatMessage.setType(XmppMessage.TYPE_TIP);
            // chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                MsgBroadcast.broadcastMsgRoleChanged(getContext());
            }
        }

        // ?????????????????????????????? ?????? ??????????????????
        if (type == XmppMessage.TYPE_MUCFILE_DEL || type == XmppMessage.TYPE_MUCFILE_ADD) {
            String roomid = chatMessage.getObjectId();
            String str;
            if (type == XmppMessage.TYPE_MUCFILE_DEL) {
                // str = chatMessage.getFromUserName() + " ?????????????????? " + chatMessage.getFilePath();
                str = fromUserName + " " + getString("JXMessage_fileDelete") + ":" + chatMessage.getFilePath();
            } else {
                // str = chatMessage.getFromUserName() + " ?????????????????? " + chatMessage.getFilePath();
                str = fromUserName + " " + getString("JXMessage_fileUpload") + ":" + chatMessage.getFilePath();
            }
            // ?????????????????????????????????
            FriendDao.getInstance().updateFriendContent(mLoginUserId, roomid, str, type, TimeUtils.sk_time_current_time());
            FriendDao.getInstance().markUserMessageUnRead(mLoginUserId, roomid); // ??????????????????
            // ???????????????????????????????????????
//            chatMessage.setContent(str);
            chatMessage.setType(XmppMessage.TYPE_TIP);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, roomid, chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, roomid, chatMessage, true);
            }
            // ??????????????????
            MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
            return;
        }
    }

    // ??????????????????
    private void operatingRoomMemberDao(int type, String roomId, String userId, String userName) {
        if (type == 0) {
            RoomMember roomMember = new RoomMember();
            roomMember.setRoomId(roomId);
            roomMember.setUserId(userId);
            roomMember.setUserName(userName);
            roomMember.setCardName(userName);
            roomMember.setRole(3);
            roomMember.setCreateTime(0);
            RoomMemberDao.getInstance().saveSingleRoomMember(roomId, roomMember);
        } else {
            RoomMemberDao.getInstance().deleteRoomMember(roomId, userId);
        }
    }

    private void chatGroupTip2(int type, ChatMessage chatMessage, String toUserName) {
        Log.e("TAG_????????????XCH", "chatGroupTip2");
        chatMessage.setType(XmppMessage.TYPE_TIP);
        if (type == XmppMessage.TYPE_GROUP_VERIFY) {
            // 916??????????????????
            // ????????????????????????????????????????????????????????????????????? ???/??? ???????????????????????????????????????????????????
            // ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            if (!TextUtils.isEmpty(chatMessage.getContent()) &&
                    (chatMessage.getContent().equals("0") || chatMessage.getContent().equals("1"))) {// ?????????
                if (chatMessage.getContent().equals("1")) {
//                    chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_group_enable_verify));
                } else {
//                    chatMessage.setContent(mService.getString(R.string.tip_group_disable_verify));
                }
                // chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getObjectId(), chatMessage, true);
                }
            } else {//  ???????????????????????? ????????????????????? ?????????????????????????????? ????????????
                try {
                    org.json.JSONObject json = new org.json.JSONObject(chatMessage.getObjectId());
                    String isInvite = json.getString("isInvite");
                    if (TextUtils.isEmpty(isInvite)) {
                        isInvite = "0";
                    }
                    if (isInvite.equals("0")) {
                        String id = json.getString("userIds");
                        String[] ids = id.split(",");
//                        chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_invite_need_verify_place_holder, chatMessage.getFromUserName(), ids.length));
                    } else {
//                        chatMessage.setContent(chatMessage.getFromUserName() + MyApplication.getContext().getString(R.string.tip_need_verify_place_holder));
                    }
                    String roomJid = json.getString("roomJid");
                    if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, roomJid, chatMessage)) {
                        ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, roomJid, chatMessage, true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (type == XmppMessage.TYPE_CHANGE_SHOW_READ) {
                PreferenceUtils.putBoolean(MyApplication.getContext(),
                        Constants.IS_SHOW_READ + chatMessage.getObjectId(), chatMessage.getContent().equals("1"));
                if (chatMessage.getContent().equals("1")) {
//                    chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_enable_read));
                } else {
//                    chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_disable_read));
                }
            } else if (type == XmppMessage.TYPE_GROUP_LOOK) {
                if (chatMessage.getContent().equals("1")) {
//                    chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_private));
                } else {
//                    chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_public));
                }
            } else if (type == XmppMessage.TYPE_GROUP_SHOW_MEMBER) {
                if (chatMessage.getContent().equals("1")) {
//                    chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_enable_member));
                } else {
//                    chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_disable_member));
                }
            } else if (type == XmppMessage.TYPE_GROUP_SEND_CARD) {
                PreferenceUtils.putBoolean(MyApplication.getContext(),
                        Constants.IS_SEND_CARD + chatMessage.getObjectId(), chatMessage.getContent().equals("1"));
                if (chatMessage.getContent().equals("1")) {
//                    chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_enable_chat_privately));
                } else {
//                    chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_disable_chat_privately));
                }
            } else if (type == XmppMessage.TYPE_GROUP_ALL_SHAT_UP) {
                PreferenceUtils.putBoolean(MyApplication.getContext(),
                        Constants.GROUP_ALL_SHUP_UP + chatMessage.getObjectId(), !chatMessage.getContent().equals("0"));
                if (!chatMessage.getContent().equals("0")) {
//                    chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_now_ban_all));
                } else {
//                    chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_now_disable_ban_all));
                }
                MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(MyApplication.getContext());
            } else if (type == XmppMessage.TYPE_GROUP_ALLOW_NORMAL_INVITE) {
                if (!chatMessage.getContent().equals("0")) {
//                    chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_enable_invite));
                } else {
//                    chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_disable_invite));
                }
            } else if (type == XmppMessage.TYPE_GROUP_ALLOW_NORMAL_UPLOAD) {
                if (!chatMessage.getContent().equals("0")) {
//                    chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_enable_upload));
                } else {
//                    chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_disable_upload));
                }
            } else if (type == XmppMessage.TYPE_GROUP_ALLOW_NORMAL_CONFERENCE) {
                PreferenceUtils.putBoolean(MyApplication.getContext(),
                        Constants.IS_ALLOW_NORMAL_CONFERENCE + chatMessage.getObjectId(), !chatMessage.getContent().equals("0"));
                if (!chatMessage.getContent().equals("0")) {
//                    chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_enable_meeting));
                } else {
//                    chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_disable_meeting));
                }
            } else if (type == XmppMessage.TYPE_GROUP_ALLOW_NORMAL_SEND_COURSE) {
                PreferenceUtils.putBoolean(MyApplication.getContext(),
                        Constants.IS_ALLOW_NORMAL_SEND_COURSE + chatMessage.getObjectId(), !chatMessage.getContent().equals("0"));
                if (!chatMessage.getContent().equals("0")) {
//                    chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_enable_cource));
                } else {
//                    chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_disable_cource));
                }
            } else if (type == XmppMessage.TYPE_GROUP_TRANSFER) {
//                chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_new_group_owner_place_holder, toUserName));
                Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, chatMessage.getObjectId());
                if (friend != null) {
                    FriendDao.getInstance().updateRoomCreateUserId(mLoginUserId,
                            chatMessage.getObjectId(), chatMessage.getToUserId());
                    RoomMemberDao.getInstance().updateRoomMemberRole(friend.getRoomId(), chatMessage.getToUserId(), 1);
                }
            }
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getObjectId(), chatMessage, true);
            }
        }
    }

    /**
     * ??????????????????????????????
     */
    private void chatFriend(String body, ChatMessage chatMessage) {
        /**
         * ??????
         * 1.?????? ?????????????????????????????????????????????????????????????????????????????????updateLastChatMessage(...,Friend.ID_NEW_FRIEND_MESSAGE,...)???????????????
         * 2.?????????????????????ex:??????web???????????????????????????android???????????????????????????????????????
         */
        Log.e("msg", mLoginUserId + "???" + chatMessage.getFromUserId() + "???" + chatMessage.getToUserId());
        Log.e("msg", chatMessage.getType() + "???" + chatMessage.getPacketId());

        if (chatMessage.getFromUserId().equals(mLoginUserId)) {// ?????????????????????????????????android???????????????
            chatFriendFromMe(body, chatMessage);
        } else {
            chatFriendForMe(chatMessage);
        }
    }

    /**
     * ??????????????????????????????????????????????????????????????????????????????????????????
     * ???????????????????????????Type?????????????????????????????????????????????????????????
     */
    private void chatFriendFromMe(String body, ChatMessage chatMessage) {
        String toUserId = chatMessage.getToUserId();
        JSONObject jsonObject = JSONObject.parseObject(body);
        String toUserName = jsonObject.getString("toUserName");
        if (TextUtils.isEmpty(toUserName)) {
            toUserName = "NULL";
        }
        switch (chatMessage.getType()) {
            case XmppMessage.TYPE_SAYHELLO:
                // ?????????????????????
                NewFriendMessage message = NewFriendMessage.createLocalMessage(CoreManager.requireSelf(mService),
                        XmppMessage.TYPE_SAYHELLO, getString("HEY-HELLO"), toUserId, toUserName);
                NewFriendDao.getInstance().createOrUpdateNewFriend(message);
                NewFriendDao.getInstance().changeNewFriendState(toUserId, Friend.STATUS_10);//????????????
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, message, true);

                // ????????????????????????
                ChatMessage sayMessage = new ChatMessage();
                sayMessage.setFromUserId(mLoginUserId);
                sayMessage.setFromUserName(CoreManager.requireSelf(mService).getNickName());
                sayMessage.setContent(getString("HEY-HELLO"));
                sayMessage.setType(XmppMessage.TYPE_TEXT); //????????????
                sayMessage.setMySend(true);
                sayMessage.setMessageState(MESSAGE_SEND_SUCCESS);
                sayMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                sayMessage.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
                ChatMessageDao.getInstance().saveNewSingleChatMessage(message.getOwnerId(), message.getUserId(), sayMessage);
                break;
            case XmppMessage.TYPE_PASS:
                // ????????????????????????????????????
                NewFriendMessage passMessage = NewFriendMessage.createLocalMessage(CoreManager.requireSelf(mService),
                        XmppMessage.TYPE_PASS, null, toUserId, toUserName);

                NewFriendDao.getInstance().ascensionNewFriend(passMessage, Friend.STATUS_FRIEND);
                FriendHelper.addFriendExtraOperation(mLoginUserId, toUserId);
                FriendDao.getInstance().updateFriendContent(mLoginUserId, toUserId,
                        getString("JXMessageObject_BeFriendAndChat"), XmppMessage.TYPE_TEXT, TimeUtils.sk_time_current_time());
                NewFriendDao.getInstance().changeNewFriendState(toUserId, Friend.STATUS_12);
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, passMessage, true);
                break;
            case XmppMessage.TYPE_FEEDBACK:
                // ???????????????????????????
                NewFriendMessage feedBackMessage = NewFriendDao.getInstance().getNewFriendById(mLoginUserId, toUserId);
                if (feedBackMessage == null) {
                    feedBackMessage = NewFriendMessage.createLocalMessage(CoreManager.requireSelf(mService),
                            XmppMessage.TYPE_FEEDBACK, chatMessage.getContent(), toUserId, toUserName);
                    NewFriendDao.getInstance().createOrUpdateNewFriend(feedBackMessage);
                }
                if (feedBackMessage.getState() == Friend.STATUS_11 || feedBackMessage.getState() == Friend.STATUS_15) {
                    NewFriendDao.getInstance().changeNewFriendState(feedBackMessage.getUserId(), Friend.STATUS_15);
                } else {
                    NewFriendDao.getInstance().changeNewFriendState(feedBackMessage.getUserId(), Friend.STATUS_14);
                }
                NewFriendDao.getInstance().updateNewFriendContent(feedBackMessage.getUserId(), chatMessage.getContent());

                ChatMessage chatFeedMessage = new ChatMessage();// ?????????????????????
                chatFeedMessage.setType(XmppMessage.TYPE_TEXT); // ????????????
                chatFeedMessage.setFromUserId(mLoginUserId);
                chatFeedMessage.setFromUserName(CoreManager.requireSelf(mService).getNickName());
                chatFeedMessage.setContent(chatMessage.getContent());
                chatFeedMessage.setMySend(true);
                chatFeedMessage.setMessageState(MESSAGE_SEND_SUCCESS);
                chatFeedMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                chatFeedMessage.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
                ChatMessageDao.getInstance().saveNewSingleAnswerMessage(mLoginUserId, toUserId, chatFeedMessage);
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, feedBackMessage, true);
                break;
            case XmppMessage.TYPE_FRIEND:
                // ?????????????????????????????????????????????????????????
                NewFriendMessage friendMessage = NewFriendMessage.createLocalMessage(CoreManager.requireSelf(mService),
                        XmppMessage.TYPE_FRIEND, null, toUserId, toUserName);
                NewFriendDao.getInstance().ascensionNewFriend(friendMessage, Friend.STATUS_FRIEND);
                FriendHelper.addFriendExtraOperation(mLoginUserId, toUserId);
                NewFriendDao.getInstance().changeNewFriendState(toUserId, Friend.STATUS_22);
                FriendDao.getInstance().updateFriendContent(mLoginUserId, toUserId,
                        getString("JXMessageObject_BeFriendAndChat"), XmppMessage.TYPE_TEXT, TimeUtils.sk_time_current_time());
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, friendMessage, true);
                break;
            case XmppMessage.TYPE_BLACK:
                // ??????????????????
                NewFriendMessage blackMessage = NewFriendMessage.createLocalMessage(CoreManager.requireSelf(mService),
                        XmppMessage.TYPE_BLACK, null, toUserId, toUserName);
                FriendDao.getInstance().updateFriendStatus(mLoginUserId, toUserId, Friend.STATUS_BLACKLIST);
                FriendHelper.addBlacklistExtraOperation(blackMessage.getOwnerId(), blackMessage.getUserId());
                NewFriendDao.getInstance().createOrUpdateNewFriend(blackMessage);
                NewFriendDao.getInstance().changeNewFriendState(toUserId, Friend.STATUS_18);
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, blackMessage, true);
                // ????????????????????????
                EventBus.getDefault().post(new EventSyncFriendOperating(chatMessage.getToUserId(), chatMessage.getType()));
                break;
            case XmppMessage.TYPE_REFUSED:
                // ???????????????????????????
                NewFriendMessage removeMessage = NewFriendMessage.createLocalMessage(CoreManager.requireSelf(mService),
                        XmppMessage.TYPE_REFUSED, null, toUserId, toUserName);
                NewFriendDao.getInstance().ascensionNewFriend(removeMessage, Friend.STATUS_FRIEND);
                FriendHelper.beAddFriendExtraOperation(removeMessage.getOwnerId(), removeMessage.getUserId());
                NewFriendDao.getInstance().createOrUpdateNewFriend(removeMessage);
                NewFriendDao.getInstance().changeNewFriendState(toUserId, Friend.STATUS_24);
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, removeMessage, true);
                break;
            case XmppMessage.TYPE_DELALL:
                // ??????????????????
                NewFriendMessage deleteMessage = NewFriendMessage.createLocalMessage(CoreManager.requireSelf(mService),
                        XmppMessage.TYPE_DELALL, null, chatMessage.getToUserId(), toUserName);
                FriendHelper.removeAttentionOrFriend(mLoginUserId, chatMessage.getToUserId());
                NewFriendDao.getInstance().createOrUpdateNewFriend(deleteMessage);
                NewFriendDao.getInstance().changeNewFriendState(chatMessage.getToUserId(), Friend.STATUS_16);
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, deleteMessage, true);
                // ????????????????????????
                EventBus.getDefault().post(new EventSyncFriendOperating(chatMessage.getToUserId(), chatMessage.getType()));
                break;
        }
        // ?????????????????????
        CardcastUiUpdateUtil.broadcastUpdateUi(mService);
    }

    /**
     * ?????????????????????????????????????????????????????????
     */
    private void chatFriendForMe(ChatMessage chatMessage) {
        //  ?????????????????????????????????????????????
        if (MyApplication.IS_SUPPORT_MULTI_LOGIN) {
            mService.sendChatMessage(mLoginUserId, chatMessage);
        }
        // json:fromUserId fromUserName type  content timeSend
        NewFriendMessage mNewMessage = new NewFriendMessage(chatMessage.toJsonString());
        mNewMessage.setOwnerId(mLoginUserId);
        mNewMessage.setUserId(chatMessage.getFromUserId());
        mNewMessage.setRead(false);
        mNewMessage.setMySend(false);
        mNewMessage.setPacketId(chatMessage.getPacketId());
        String content = "";
        switch (chatMessage.getType()) {
            case XmppMessage.TYPE_SAYHELLO:
                // ?????????????????????????????????
                NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);
                NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_11);
                // FriendDao.getInstance().updateLastChatMessage(mLoginUserId, Friend.ID_NEW_FRIEND_MESSAGE, chatMessage);

                ChatMessage sayHelloMessage = new ChatMessage();
                sayHelloMessage.setType(XmppMessage.TYPE_TEXT); //????????????
                sayHelloMessage.setFromUserId(chatMessage.getFromUserId());
                sayHelloMessage.setFromUserName(chatMessage.getFromUserName());
                sayHelloMessage.setContent(getString("HEY-HELLO"));
                sayHelloMessage.setMySend(false);
                sayHelloMessage.setMessageState(MESSAGE_SEND_SUCCESS);
                sayHelloMessage.setPacketId(chatMessage.getPacketId());
                sayHelloMessage.setDoubleTimeSend(chatMessage.getTimeSend());
                ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getFromUserId(), sayHelloMessage);
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                break;
            case XmppMessage.TYPE_PASS:
                // ???????????????????????????
                NewFriendDao.getInstance().ascensionNewFriend(mNewMessage, Friend.STATUS_FRIEND);
                FriendHelper.beAddFriendExtraOperation(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                /*content = getString("JXFriendObject_PassGo");
                FriendDao.getInstance().updateLastChatMessage(mLoginUserId, Friend.ID_NEW_FRIEND_MESSAGE, content);*/
                NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_13);//?????????xxx
                content = getString("JXMessageObject_BeFriendAndChat");
                FriendDao.getInstance().updateLastChatMessage(mLoginUserId, mNewMessage.getUserId(), content);
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                break;
            case XmppMessage.TYPE_FEEDBACK: {
                // ???????????????
                NewFriendMessage feedBackMessage = NewFriendDao.getInstance().getNewFriendById(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);
                if (feedBackMessage.getState() == Friend.STATUS_11 || feedBackMessage.getState() == Friend.STATUS_15) {
                    NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_15);
                } else {
                    NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_14);
                }
                NewFriendDao.getInstance().updateNewFriendContent(mNewMessage.getUserId(), chatMessage.getContent());

                ChatMessage message = new ChatMessage();
                message.setType(XmppMessage.TYPE_TEXT);// ????????????
                message.setFromUserId(mNewMessage.getUserId());
                message.setFromUserName(mNewMessage.getNickName());
                message.setContent(mNewMessage.getContent());
                message.setMySend(false);
                message.setMessageState(MESSAGE_SEND_SUCCESS);
                message.setPacketId(chatMessage.getPacketId());
                message.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
                ChatMessageDao.getInstance().saveNewSingleAnswerMessage(mLoginUserId, mNewMessage.getUserId(), message);
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                break;
            }
            case XmppMessage.TYPE_FRIEND:
                // ?????????????????????????????????????????????????????????
                NewFriendDao.getInstance().ascensionNewFriend(mNewMessage, Friend.STATUS_FRIEND);
                FriendHelper.beAddFriendExtraOperation(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_21);//?????????xxx
                /*content = mNewMessage.getNickName() + " ??????????????????";
                FriendDao.getInstance().updateLastChatMessage(mLoginUserId, Friend.ID_NEW_FRIEND_MESSAGE, content);*/
                content = getString("JXMessageObject_BeFriendAndChat");
                FriendDao.getInstance().updateLastChatMessage(mLoginUserId, mNewMessage.getUserId(), content);
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                break;
            case XmppMessage.TYPE_BLACK:
                // ??????????????????
                NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);// ?????????????????????NewFriend??????????????????????????????status
                NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_19);
                FriendHelper.beDeleteAllNewFriend(mNewMessage.getOwnerId(), mNewMessage.getUserId());
               /* content = mNewMessage.getNickName() + " " + getString("JXFriendObject_PulledBlack");
                FriendDao.getInstance().updateLastChatMessage(mLoginUserId, Friend.ID_NEW_FRIEND_MESSAGE, content);*/
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                // ??????????????????
                ChatActivity.callFinish(getContext(), getContext().getString(R.string.be_pulled_black), mNewMessage.getUserId());
                break;
            case XmppMessage.TYPE_REFUSED:
                // ??????????????????????????????
                NewFriendDao.getInstance().ascensionNewFriend(mNewMessage, Friend.STATUS_FRIEND);
                FriendHelper.beAddFriendExtraOperation(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_24);//?????????xxx
                /*content = mNewMessage.getNickName() + " ??????????????????";
                FriendDao.getInstance().updateLastChatMessage(mLoginUserId, Friend.ID_NEW_FRIEND_MESSAGE, content);*/
                content = getString("JXMessageObject_BeFriendAndChat");
                FriendDao.getInstance().updateLastChatMessage(mLoginUserId, mNewMessage.getUserId(), content);
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                break;
            case XmppMessage.TYPE_DELALL:
                // ??????????????????
                NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);// ?????????????????????NewFriend??????????????????????????????status
                FriendHelper.beDeleteAllNewFriend(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_17);
                /*content = mNewMessage.getNickName() + " ????????????";
                FriendDao.getInstance().updateLastChatMessage(mLoginUserId, Friend.ID_NEW_FRIEND_MESSAGE, content);*/
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                // ??????????????????
                ChatActivity.callFinish(getContext(), getString("JXAlert_DeleteFirend"), mNewMessage.getUserId());
                break;
            case XmppMessage.TYPE_CONTACT_BE_FRIEND:
                // ???????????? ??????????????? ????????? ??????????????????
                NewFriendDao.getInstance().ascensionNewFriend(mNewMessage, Friend.STATUS_FRIEND);
                FriendHelper.beAddFriendExtraOperation(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_25);// ???????????????????????????
                content = getString("JXMessageObject_BeFriendAndChat");
                FriendDao.getInstance().updateLastChatMessage(mLoginUserId, mNewMessage.getUserId(), content);
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                break;
            case XmppMessage.TYPE_NEW_CONTACT_REGISTER: {
                // ????????????????????????????????????????????????????????????????????? ???????????????
                JSONObject jsonObject = JSONObject.parseObject(chatMessage.getContent());
                Contact contact = new Contact();
                contact.setTelephone(jsonObject.getString("telephone"));
                contact.setToTelephone(jsonObject.getString("toTelephone"));
                String toUserId = jsonObject.getString("toUserId");
                contact.setToUserId(toUserId);
                contact.setToUserName(jsonObject.getString("toUserName"));
                contact.setUserId(jsonObject.getString("userId"));
                if (ContactDao.getInstance().createContact(contact)) {// ?????????????????? ??????????????????
                    EventBus.getDefault().post(new MessageContactEvent(toUserId));
                }
                break;
            }
            case XmppMessage.TYPE_REMOVE_ACCOUNT: {
                // ????????????????????????????????????????????????????????? ???from?????????????????? ObjectId??????????????????userId???
                String removedAccountId = chatMessage.getObjectId();
                Friend toUser = FriendDao.getInstance().getFriend(mLoginUserId, removedAccountId);
                if (toUser != null) {
                    mNewMessage.setUserId(removedAccountId);
                    mNewMessage.setNickName(toUser.getNickName());
                    NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);// ?????????????????????NewFriend??????????????????????????????status
                    FriendHelper.friendAccountRemoved(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                    NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_26);
                    NewFriendDao.getInstance().updateNewFriendContent(mNewMessage.getUserId(), chatMessage.getContent());
                    ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                    // ??????????????????
                    ChatActivity.callFinish(getContext(), chatMessage.getContent(), removedAccountId);
                }
                break;
            }
            case XmppMessage.TYPE_BACK_DELETE: {
                // ??????????????????????????????????????????
                JSONObject json = JSON.parseObject(chatMessage.getObjectId());
                String fromUserId = json.getString("fromUserId");
                String fromUserName = json.getString("fromUserName");
                String toUserId = json.getString("toUserId");
                String toUserName = json.getString("toUserName");
                if (TextUtils.equals(fromUserId, mLoginUserId)) {
                    // ??????????????????
                    mNewMessage.setUserId(toUserId);
                    mNewMessage.setNickName(toUserName);
                    NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);// ?????????????????????NewFriend??????????????????????????????status
                    FriendHelper.beDeleteAllNewFriend(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                    NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_16);
                    ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                } else {
                    // ??????????????????
                    mNewMessage.setUserId(fromUserId);
                    mNewMessage.setNickName(fromUserName);
                    NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);// ?????????????????????NewFriend??????????????????????????????status
                    FriendHelper.beDeleteAllNewFriend(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                    NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_17);
                    ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                }
                // ??????????????????
                ChatActivity.callFinish(getContext(), getString("JXAlert_DeleteFirend"), mNewMessage.getUserId());
                break;
            }
            case XmppMessage.TYPE_BACK_BLACK: {
                // ??????????????????????????????????????????????????????
                JSONObject json = JSON.parseObject(chatMessage.getObjectId());
                String fromUserId = json.getString("fromUserId");
                String fromUserName = json.getString("fromUserName");
                String toUserId = json.getString("toUserId");
                if (TextUtils.equals(fromUserId, mLoginUserId)) {
                    // ??????????????????
                    mNewMessage.setUserId(toUserId);
                    Friend toUser = FriendDao.getInstance().getFriend(mLoginUserId, toUserId);
                    if (toUser == null) {
                        Reporter.post("???????????????????????????????????????" + toUserId);
                        return;
                    }
                    mNewMessage.setNickName(toUser.getNickName());
                    FriendDao.getInstance().updateFriendStatus(mLoginUserId, toUserId, Friend.STATUS_BLACKLIST);
                    FriendHelper.addBlacklistExtraOperation(mLoginUserId, toUserId);

                    ChatMessage addBlackChatMessage = new ChatMessage();
//                    addBlackChatMessage.setContent(InternationalizationHelper.getString("JXFriendObject_AddedBlackList") + " " + toUser.getShowName());
                    addBlackChatMessage.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
                    FriendDao.getInstance().updateLastChatMessage(mLoginUserId, Friend.ID_NEW_FRIEND_MESSAGE, addBlackChatMessage);

                    NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);
                    NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_18);
                    ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                    // ??????????????????
                    ChatActivity.callFinish(getContext(), chatMessage.getContent(), toUserId);
                } else {
                    // ???????????????
                    mNewMessage.setUserId(fromUserId);
                    mNewMessage.setNickName(fromUserName);
                    NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);// ?????????????????????NewFriend??????????????????????????????status
                    NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_19);
                    FriendHelper.beDeleteAllNewFriend(mNewMessage.getOwnerId(), mNewMessage.getUserId());
               /* content = mNewMessage.getNickName() + " " + getString("JXFriendObject_PulledBlack");
                FriendDao.getInstance().updateLastChatMessage(mLoginUserId, Friend.ID_NEW_FRIEND_MESSAGE, content);*/
                    ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                    // ??????????????????
                    ChatActivity.callFinish(getContext(), getContext().getString(R.string.be_pulled_black), mNewMessage.getUserId());
                }
                break;
            }
            case XmppMessage.TYPE_BACK_REFUSED: {
                // ??????????????????????????????????????????????????????????????????
                JSONObject json = JSON.parseObject(chatMessage.getObjectId());
                String fromUserId = json.getString("fromUserId");
                String fromUserName = json.getString("fromUserName");
                String toUserId = json.getString("toUserId");
                if (TextUtils.equals(fromUserId, mLoginUserId)) {
                    // ?????????????????????????????????
                    mNewMessage.setUserId(toUserId);
                    Friend toUser = FriendDao.getInstance().getFriend(mLoginUserId, toUserId);
                    if (toUser == null) {
                        Reporter.post("?????????????????????????????????????????????" + toUserId);
                    } else {
                        mNewMessage.setNickName(toUser.getNickName());
                    }
                    NewFriendDao.getInstance().ascensionNewFriend(mNewMessage, Friend.STATUS_FRIEND);
                    FriendHelper.beAddFriendExtraOperation(mLoginUserId, toUserId);

                    User self = CoreManager.requireSelf(mService);
                    ChatMessage removeChatMessage = new ChatMessage();
//                    removeChatMessage.setContent(self.getNickName() + InternationalizationHelper.getString("REMOVE"));
                    removeChatMessage.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
                    FriendDao.getInstance().updateLastChatMessage(mLoginUserId, Friend.ID_NEW_FRIEND_MESSAGE, removeChatMessage);
                    NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);
                    NewFriendDao.getInstance().changeNewFriendState(toUserId, Friend.STATUS_24);
                    ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                } else {
                    // ?????????????????????
                    mNewMessage.setUserId(fromUserId);
                    mNewMessage.setNickName(fromUserName);
                    NewFriendDao.getInstance().ascensionNewFriend(mNewMessage, Friend.STATUS_FRIEND);
                    FriendHelper.beAddFriendExtraOperation(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                    NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_24);//?????????xxx
                    content = getString("JXMessageObject_BeFriendAndChat");
                    FriendDao.getInstance().updateLastChatMessage(mLoginUserId, mNewMessage.getUserId(), content);
                    ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                }
                break;
            }
            case XmppMessage.TYPE_NEWSEE:// ????????????????????????
            case XmppMessage.TYPE_DELSEE:// ????????????????????????????????????
                // ???????????? ???????????????
                break;
            case XmppMessage.TYPE_RECOMMEND:
                // ??????????????? ??????????????????
                break;
            default:
                break;
        }
        // ?????????????????????
        CardcastUiUpdateUtil.broadcastUpdateUi(mService);
    }

    /**
     * ?????????????????????????????????
     */
    private void chatAudioVideo(ChatMessage chatMessage) {
        int type = chatMessage.getType();
        Log.e("AVI", type + "");
        String fromUserId = chatMessage.getFromUserId();
        if (fromUserId.equals(mLoginUserId)) {
            switch (chatMessage.getType()) {
                case XmppMessage.TYPE_IS_CONNECT_VOICE:
                    // ???????????????????????????????????????????????????????????????
                    break;
                case XmppMessage.TYPE_CONNECT_VOICE:
                    // ???????????????????????????????????????????????????????????????????????????
                    EventBus.getDefault().post(new MessageHangUpPhone(chatMessage));
                    break;
                case XmppMessage.TYPE_NO_CONNECT_VOICE:
                    // ??????????????? || ????????? ?????????????????????????????????????????????????????????
                    EventBus.getDefault().post(new MessageHangUpPhone(chatMessage));
                    break;
                case XmppMessage.TYPE_END_CONNECT_VOICE:
                    // ??????????????????????????????????????????
                    break;
                case XmppMessage.TYPE_IS_CONNECT_VIDEO:
                    // ?????????????????????????????????????????????????????? ?????????
                    break;
                case XmppMessage.TYPE_CONNECT_VIDEO:
                    // ???????????????????????????????????????????????????????????????????????????
                    EventBus.getDefault().post(new MessageHangUpPhone(chatMessage));
                    break;
                case XmppMessage.TYPE_NO_CONNECT_VIDEO:
                    // ??????????????? || ????????? ?????????????????????????????????????????????????????????
                    EventBus.getDefault().post(new MessageHangUpPhone(chatMessage));
                    break;
                case XmppMessage.TYPE_END_CONNECT_VIDEO:
                    // ??????????????????????????????????????????
                    break;
                case XmppMessage.TYPE_IS_MU_CONNECT_VOICE:
                    // ?????????????????????????????????????????????
                    break;
                case XmppMessage.TYPE_IS_MU_CONNECT_VIDEO:
                    // ?????????????????????????????????????????????
                    break;
            }
        } else {
            /*
            ?????? ????????????
             */
            if (chatMessage.getType() == XmppMessage.TYPE_IS_CONNECT_VOICE) {
                // ????????????
                Log.e("AVI", TimeUtils.sk_time_current_time() - chatMessage.getTimeSend() + "");
                if (TimeUtils.sk_time_current_time() - chatMessage.getTimeSend() <= 30) {// ???????????????????????????????????????????????????30s??????
                    EventBus.getDefault().post(new MessageEventSipEVent(XmppMessage.TYPE_IS_CONNECT_VOICE, fromUserId, chatMessage));
                } else {
                    Log.e("AVI", "????????????");
                }
            } else if (chatMessage.getType() == XmppMessage.TYPE_CONNECT_VOICE) {
                // ?????????????????????????????????102
                EventBus.getDefault().post(new MessageEventSipEVent(XmppMessage.TYPE_CONNECT_VOICE, null, chatMessage));
            } else if (chatMessage.getType() == XmppMessage.TYPE_NO_CONNECT_VOICE) {
                // ???????????? || ?????????
                EventBus.getDefault().post(new MessageEventSipEVent(XmppMessage.TYPE_NO_CONNECT_VOICE, null, chatMessage));
                String content = "";
                chatMessage.setMySend(false);
                if (chatMessage.getTimeLen() == 0) {
                    content = getString("JXSip_Canceled") + " " + getString("JX_VoiceChat");
                } else {
                    content = getString("JXSip_noanswer");
                }
                ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage);
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, false);
                FriendDao.getInstance().updateFriendContent(mLoginUserId, fromUserId, content, XmppMessage.TYPE_NO_CONNECT_VOICE, chatMessage.getTimeSend());
                MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
                EventBus.getDefault().post(new MessageHangUpPhone(chatMessage));
            } else if (chatMessage.getType() == XmppMessage.TYPE_END_CONNECT_VOICE) {
                // ????????????????????????
                chatMessage.setMySend(false);
                ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage);
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, false);
                FriendDao.getInstance().updateFriendContent(mLoginUserId, fromUserId, getString("JXSip_finished") + " " + getString("JX_VoiceChat") + "," + getString("JXSip_timeLenth") + ":" + chatMessage.getTimeLen() + getString("JX_second"), XmppMessage.TYPE_END_CONNECT_VOICE, chatMessage.getTimeSend());
                MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
                // ????????????????????????
                EventBus.getDefault().post(new MessageHangUpPhone(chatMessage));
            }

             /*
            ??????  ????????????
             */
            if (type == XmppMessage.TYPE_IS_CONNECT_VIDEO) {
                Log.e("AVI", TimeUtils.sk_time_current_time() - chatMessage.getTimeSend() + "");
                if (TimeUtils.sk_time_current_time() - chatMessage.getTimeSend() <= 30) {// ???????????????????????????????????????????????????30s??????
                    EventBus.getDefault().post(new MessageEventSipEVent(110, fromUserId, chatMessage));
                } else {
                    Log.e("AVI", "????????????");
                }
            } else if (type == XmppMessage.TYPE_CONNECT_VIDEO) {
                EventBus.getDefault().post(new MessageEventSipEVent(112, null, chatMessage));
            } else if (type == XmppMessage.TYPE_NO_CONNECT_VIDEO) {
                EventBus.getDefault().post(new MessageEventSipEVent(113, null, chatMessage));
                chatMessage.setMySend(false);
                String content = "";
                if (chatMessage.getTimeLen() == 0) {
                    content = getString("JXSip_Canceled") + " " + getString("JX_VideoChat");
                } else {
                    content = getString("JXSip_noanswer");
                }
                ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage);
                FriendDao.getInstance().updateFriendContent(mLoginUserId, fromUserId, content, XmppMessage.TYPE_NO_CONNECT_VIDEO, chatMessage.getTimeSend());
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, false);
                EventBus.getDefault().post(new MessageHangUpPhone(chatMessage));
            } else if (type == XmppMessage.TYPE_END_CONNECT_VIDEO) {
                chatMessage.setMySend(false);
                ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage);
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, false);
                FriendDao.getInstance().updateFriendContent(mLoginUserId, fromUserId, getString("JX_VideoChat") + "," +
                        getString("JXSip_timeLenth") + ":" + chatMessage.getTimeLen() + getString("JX_second"), XmppMessage.TYPE_END_CONNECT_VIDEO, chatMessage.getTimeSend());
                EventBus.getDefault().post(new MessageHangUpPhone(chatMessage));
            }

            /**
             ?????? ?????????????????????
             */
            if (type == XmppMessage.TYPE_IS_MU_CONNECT_VOICE) {
                Log.e("AVI", TimeUtils.sk_time_current_time() - chatMessage.getTimeSend() + "");
                if (TimeUtils.sk_time_current_time() - chatMessage.getTimeSend() <= 30) {// ???????????????????????????????????????????????????30s??????
                    EventBus.getDefault().post(new MessageEventMeetingInvited(CallConstants.Audio_Meet, chatMessage));
                } else {
                    Log.e("AVI", "????????????");
                }
                ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage);
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, false);
                FriendDao.getInstance().updateFriendContent(mLoginUserId, fromUserId, chatMessage.getContent(), XmppMessage.TYPE_IS_MU_CONNECT_VOICE, chatMessage.getTimeSend());
            } else if (type == XmppMessage.TYPE_IS_MU_CONNECT_VIDEO) {
                Log.e("AVI", TimeUtils.sk_time_current_time() - chatMessage.getTimeSend() + "");
                if (TimeUtils.sk_time_current_time() - chatMessage.getTimeSend() <= 30) {// ???????????????????????????????????????????????????30s??????
                    EventBus.getDefault().post(new MessageEventMeetingInvited(CallConstants.Video_Meet, chatMessage));
                } else {
                    Log.e("AVI", "????????????");
                }
                ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage);
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, false);
                FriendDao.getInstance().updateFriendContent(mLoginUserId, fromUserId, chatMessage.getContent(), XmppMessage.TYPE_IS_MU_CONNECT_VIDEO, chatMessage.getTimeSend());
            }

            // ?????????????????????
            if (MyApplication.IS_SUPPORT_MULTI_LOGIN) {
                mService.sendChatMessage(mLoginUserId, chatMessage);
            }
        }
    }

    /**
     * ?????????????????????????????????
     */
    private void chatDiscover(String body, ChatMessage chatMessage) {
        if (MyZanDao.getInstance().hasSameZan(chatMessage.getPacketId())) {
            Log.e("msg", "???????????????????????????????????????");
            return;
        }
        MyZan zan = new MyZan();
        zan.setFromUserId(chatMessage.getFromUserId());
        zan.setFromUsername(chatMessage.getFromUserName());
        zan.setSendtime(String.valueOf(chatMessage.getTimeSend()));
        zan.setLoginUserId(mLoginUserId);
        zan.setZanbooleanyidu(0);
        zan.setSystemid(chatMessage.getPacketId());
        /**
         * object??????: id,type,content
         *
         * id
         * type:1 ?????? 2 ?????? 3 ?????? 4 ??????
         * content:????????????
         */
        String[] data = chatMessage.getObjectId().split(",");
        zan.setCricleuserid(data[0]);
        zan.setType(Integer.parseInt(data[1]));
        if (Integer.parseInt(data[1]) == 1) {// ????????????
            zan.setContent(data[2]);
        } else {// ????????????
            zan.setContenturl(data[2]);
        }

        if (chatMessage.getType() == XmppMessage.DIANZAN) {// ???
            zan.setHuifu("101");
            if (MyZanDao.getInstance().addZan(zan)) {
                int size = MyZanDao.getInstance().getZanSize(mLoginUserId);
                EventBus.getDefault().post(new MessageEventHongdian(size));
                EventBus.getDefault().post(new MessageEventNotifyDynamic(size));
            } else {
                // ??????????????????fromUserId?????????????????????????????????????????????????????????Return????????????????????????????????????
                return;
            }
        } else if (chatMessage.getType() == XmppMessage.PINGLUN) {// ??????
            if (chatMessage.getContent() != null) {
                zan.setHuifu(chatMessage.getContent());
            }
            JSONObject jsonObject = JSONObject.parseObject(body);
            String toUserName = jsonObject.getString("toUserName");
            if (!TextUtils.isEmpty(toUserName)) {
                zan.setTousername(toUserName);
            }
            MyZanDao.getInstance().addZan(zan);
            int size = MyZanDao.getInstance().getZanSize(mLoginUserId);
            EventBus.getDefault().post(new MessageEventHongdian(size));
            EventBus.getDefault().post(new MessageEventNotifyDynamic(size));
        } else if (chatMessage.getType() == XmppMessage.ATMESEE) {// ????????????
            zan.setHuifu("102");
            MyZanDao.getInstance().addZan(zan);
            int size = MyZanDao.getInstance().getZanSize(mLoginUserId);
            EventBus.getDefault().post(new MessageEventHongdian(size));
            EventBus.getDefault().post(new MessageEventNotifyDynamic(size));
        }

        // ??????????????????????????????
        NoticeVoicePlayer.getInstance().start();
    }

    private String getName(Friend friend, String userId) {
        if (friend == null) {
            return null;
        }
        RoomMember mRoomMember = RoomMemberDao.getInstance().getSingleRoomMember(friend.getRoomId(), mLoginUserId);
        if (mRoomMember != null && mRoomMember.getRole() == 1) {// ???????????? Name?????????????????????
            RoomMember member = RoomMemberDao.getInstance().getSingleRoomMember(friend.getRoomId(), userId);
            if (member != null && !TextUtils.equals(member.getUserName(), member.getCardName())) {
                // ???userName???cardName??????????????????????????????????????????????????????
                return member.getCardName();
            } else {
                Friend mFriend = FriendDao.getInstance().getFriend(mLoginUserId, userId);
                if (mFriend != null && !TextUtils.isEmpty(mFriend.getRemarkName())) {
                    return mFriend.getRemarkName();
                }
            }
        } else {// ????????? ????????????
            Friend mFriend = FriendDao.getInstance().getFriend(mLoginUserId, userId);
            if (mFriend != null && !TextUtils.isEmpty(mFriend.getRemarkName())) {
                return mFriend.getRemarkName();
            }
        }
        return null;
    }

    // ????????????
    private void moreLogin(Message message, String resource, ChatMessage chatMessage) {
        boolean onLine;
        if (chatMessage.getContent().equals("0")) {
            // ????????????
            onLine = false;
        } else {
            // ????????????
            onLine = true;
        }
        EventBus.getDefault().post(new EventLoginStatus(resource, onLine));
        MachineDao.getInstance().updateMachineOnLineStatus(resource, onLine);

        // ?????? type== 200 ???????????????????????????????????????
        Message ack = DeliveryReceiptManager.receiptMessageFor(message);
        if (ack == null) {
            Log.e("msg", "ack == null ");
            return;
        }
        try {
            mService.getmConnectionManager().getConnection().sendStanza(ack);
            Log.e("msg", "sendStanza success");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("msg", "sendStanza Exception");
        }
    }
}

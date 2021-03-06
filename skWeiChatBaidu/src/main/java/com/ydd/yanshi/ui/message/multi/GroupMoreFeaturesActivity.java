package com.ydd.yanshi.ui.message.multi;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;
import com.ydd.yanshi.MyApplication;
import com.ydd.yanshi.R;
import com.ydd.yanshi.bean.Friend;
import com.ydd.yanshi.bean.RoomMember;
import com.ydd.yanshi.bean.message.MucRoomMember;
import com.ydd.yanshi.db.InternationalizationHelper;
import com.ydd.yanshi.db.dao.FriendDao;
import com.ydd.yanshi.db.dao.RoomMemberDao;
import com.ydd.yanshi.helper.AvatarHelper;
import com.ydd.yanshi.helper.DialogHelper;
import com.ydd.yanshi.sortlist.BaseComparator;
import com.ydd.yanshi.sortlist.BaseSortModel;
import com.ydd.yanshi.sortlist.SideBar;
import com.ydd.yanshi.sortlist.SortHelper;
import com.ydd.yanshi.ui.base.BaseActivity;
import com.ydd.yanshi.ui.other.BasicInfoActivity;
import com.ydd.yanshi.util.AsyncUtils;
import com.ydd.yanshi.util.Constants;
import com.ydd.yanshi.util.PreferenceUtils;
import com.ydd.yanshi.util.TimeUtils;
import com.ydd.yanshi.util.ToastUtil;
import com.ydd.yanshi.util.ViewHolder;
import com.ydd.yanshi.view.BannedDialog;
import com.ydd.yanshi.view.ClearEditText;
import com.ydd.yanshi.view.SelectionFrame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * Features: ???????????????
 * Features  ?????? && ???????????????
 * Features: ???????????????????????????
 * <p>
 * ???????????????????????????????????????????????????????????????????????????????????????userName ????????????cardName ?????????????????????
 * // Todo ????????????????????????????????????????????????????????????????????????????????????????????????
 */
public class GroupMoreFeaturesActivity extends BaseActivity {
    private ClearEditText mEditText;
    private boolean isSearch;

    private PullToRefreshListView mListView;
    private GroupMoreFeaturesAdapter mAdapter;
     private List<BaseSortModel<RoomMember>> mSortRoomMember;
     private List<BaseSortModel<RoomMember>> mSearchSortRoomMember;
//    private List<RoomMember> mSortRoomMember;
//    private List<RoomMember> mSearchSortRoomMember;
    private BaseComparator<RoomMember> mBaseComparator;

    private TextView mTextDialog;
    private SideBar mSideBar;

    private String mRoomId;
    private boolean isLoadByService;
    private boolean isBanned;
    private boolean isDelete;
    private boolean isSetRemark;

    private RoomMember mRoomMember;
    private Map<String, String> mRemarksMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_all_member);
        mRoomId = getIntent().getStringExtra("roomId");
        isLoadByService = getIntent().getBooleanExtra("isLoadByService", false);
        isBanned = getIntent().getBooleanExtra("isBanned", false);
        isDelete = getIntent().getBooleanExtra("isDelete", false);
        isSetRemark = getIntent().getBooleanExtra("isSetRemark", false);

        initActionBar();
        initData();
        initView();
        if (isLoadByService) {
            loadDataByService(false);
        }
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
        tvTitle.setText(R.string.group_member);

    }

    private void initData() {
        AsyncUtils.doAsync(this, c -> {
            List<Friend> mFriendList = FriendDao.getInstance().getAllFriends(coreManager.getSelf().getUserId());
            for (int i = 0; i < mFriendList.size(); i++) {
                if (!TextUtils.isEmpty(mFriendList.get(i).getRemarkName())) {// ??????????????????????????????
                    mRemarksMap.put(mFriendList.get(i).getUserId(), mFriendList.get(i).getRemarkName());
                }
            }
            mSortRoomMember = new ArrayList<>();
            mSearchSortRoomMember = new ArrayList<>();
            mBaseComparator = new BaseComparator<>();

            List<RoomMember> data = RoomMemberDao.getInstance().getRoomMember(mRoomId);
            Map<String, Integer> existMap = new HashMap<>();
            List<BaseSortModel<RoomMember>> sortedList = SortHelper.toSortedModelList(data, existMap,f -> {
                return f.getShowName();
            });
            mRoomMember = RoomMemberDao.getInstance().getSingleRoomMember(mRoomId, coreManager.getSelf().getUserId());

            mSortRoomMember.addAll(sortedList);
            c.uiThread(r -> {
                mSideBar.setExistMap(existMap);
                mSortRoomMember = sortedList;
                mAdapter.setData(sortedList);
                mAdapter.notifyDataSetChanged();// ????????????
            });
        });


    }

    private void initView() {
        mListView = findViewById(R.id.pull_refresh_list);
        if (!isLoadByService) {// ???????????????
            mListView.setMode(PullToRefreshBase.Mode.DISABLED);
        }
        mAdapter = new GroupMoreFeaturesAdapter();
        mListView.getRefreshableView().setAdapter(mAdapter);

        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mSideBar.setVisibility(View.VISIBLE);
        mTextDialog = (TextView) findViewById(R.id.text_dialog);
        mSideBar.setTextView(mTextDialog);
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // ??????????????????????????????
                int position = mAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    ListView refreshableView = mListView.getRefreshableView();
                    refreshableView.setSelection(position);
                }
            }
        });

        mEditText = findViewById(R.id.search_et);
        mEditText.setHint(InternationalizationHelper.getString("JX_Seach"));
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                isSearch = true;
                mListView.setMode(PullToRefreshBase.Mode.DISABLED);
                mSearchSortRoomMember.clear();
                String str = mEditText.getText().toString();
                if (TextUtils.isEmpty(str)) {
                    isSearch = false;
                    mListView.setMode(PullToRefreshBase.Mode.BOTH);
                    mAdapter.setData(mSortRoomMember);
                    return;
                }
                for (int i = 0; i < mSortRoomMember.size(); i++) {

                    if (getName(mSortRoomMember.get(i).getBean()).contains(str)) { // ???????????????????????????
                        mSearchSortRoomMember.add((mSortRoomMember.get(i)));
                    }

//                    if (getName(mSortRoomMember.get(i)).contains(str)) { // ???????????????????????????
//                        mSearchSortRoomMember.add((mSortRoomMember.get(i)));
//                    }
                }
                mAdapter.setData(mSearchSortRoomMember);
            }
        });

        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                loadDataByService(true);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                loadDataByService(false);
            }
        });

        mListView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BaseSortModel<RoomMember> baseSortModel;
//                final RoomMember roomMember;
                if (isSearch) {
                     baseSortModel = mSearchSortRoomMember.get((int) id);
//                    roomMember = mSearchSortRoomMember.get((int) id);
                } else {
                     baseSortModel = mSortRoomMember.get((int) id);
//                    roomMember = mSortRoomMember.get((int) id);
                }
                RoomMember roomMember = baseSortModel.getBean();
                if (isDelete) {// ??????
                    if (roomMember.getUserId().equals(coreManager.getSelf().getUserId())) {
                        ToastUtil.showToast(mContext, R.string.can_not_remove_self);
                        return;
                    }
                    if (roomMember.getRole() == 1) {
                        ToastUtil.showToast(mContext, getString(R.string.tip_cannot_remove_owner));
                        return;
                    }

                    if (roomMember.getRole() == 2 && mRoomMember != null && mRoomMember.getRole() != 1) {
                        ToastUtil.showToast(mContext, getString(R.string.tip_cannot_remove_manager));
                        return;
                    }

                    SelectionFrame mSF = new SelectionFrame(GroupMoreFeaturesActivity.this);
                    mSF.setSomething(null, getString(R.string.sure_remove_member_for_group, getName(roomMember)),
                            new SelectionFrame.OnSelectionFrameClickListener() {
                                @Override
                                public void cancelClick() {

                                }

                                @Override
                                public void confirmClick() {
                                    deleteMember(roomMember, roomMember.getUserId());
                                }
                            });
                    mSF.show();
                } else if (isBanned) {// ??????
                    if (roomMember.getUserId().equals(coreManager.getSelf().getUserId())) {
                        ToastUtil.showToast(mContext, R.string.can_not_banned_self);
                        return;
                    }

                    if (roomMember.getRole() == 1) {
                        ToastUtil.showToast(mContext, getString(R.string.tip_cannot_ban_owner));
                        return;
                    }

                    if (roomMember.getRole() == 2) {
                        ToastUtil.showToast(mContext, getString(R.string.tip_cannot_ban_manager));
                        return;
                    }

                    showBannedDialog(roomMember.getUserId());
                } else if (isSetRemark) {// ??????
                    if (roomMember.getUserId().equals(coreManager.getSelf().getUserId())) {
                        ToastUtil.showToast(mContext, R.string.can_not_remark_self);
                        return;
                    }
                    setRemarkName(roomMember.getUserId(), getName(roomMember));
                } else {
                    BasicInfoActivity.start(mContext, roomMember.getUserId(), BasicInfoActivity.FROM_ADD_TYPE_GROUP);
                }
            }
        });
    }

    private void loadDataByService(boolean reset) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoomId);
        if (reset) {
            params.put("joinTime", String.valueOf(0));
        } else {
            long lastRoamingTime = PreferenceUtils.getLong(MyApplication.getContext(), Constants.MUC_MEMBER_LAST_JOIN_TIME + coreManager.getSelf().getUserId() + mRoomId, 0);
            params.put("joinTime", String.valueOf(lastRoamingTime));
        }
        params.put("pageSize", Constants.MUC_MEMBER_PAGE_SIZE);

        HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBER_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<MucRoomMember>(MucRoomMember.class) {
                    @Override
                    public void onResponse(ArrayResult<MucRoomMember> result) {
                        if (reset) {
                            mListView.onPullDownRefreshComplete();
                        } else {
                            mListView.onPullUpRefreshComplete();
                        }

                        HashMap<String, String> toRepeatHashMap = new HashMap<>();
                        for (BaseSortModel<RoomMember> baseSortModel : mSortRoomMember) {
                            RoomMember member = baseSortModel.getBean();
                            toRepeatHashMap.put(member.getUserId(), member.getUserId());
                        }
//                        for (RoomMember member : mSortRoomMember) {
//                            toRepeatHashMap.put(member.getUserId(), member.getUserId());
//                        }

                        if (Result.checkSuccess(mContext, result)) {
                            List<MucRoomMember> mucRoomMemberList = result.getData();
                            if (mucRoomMemberList.size() == Integer.valueOf(Constants.MUC_MEMBER_PAGE_SIZE)) {
                                mListView.setMode(PullToRefreshBase.Mode.BOTH);
                            } else {
                                mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                            }
                            if (mucRoomMemberList.size() > 0) {
                                List<RoomMember> roomMemberList = new ArrayList<>();
                                for (int i = 0; i < mucRoomMemberList.size(); i++) {
                                    if (!reset &&
                                            toRepeatHashMap.containsKey(mucRoomMemberList.get(i).getUserId())) {
                                        continue;
                                    }
                                    RoomMember roomMember = new RoomMember();
                                    roomMember.setRoomId(mRoomId);
                                    roomMember.setUserId(mucRoomMemberList.get(i).getUserId());
                                    roomMember.setUserName(mucRoomMemberList.get(i).getNickName());
                                    if (TextUtils.isEmpty(mucRoomMemberList.get(i).getRemarkName())) {
                                        roomMember.setCardName(mucRoomMemberList.get(i).getNickName());
                                    } else {
                                        roomMember.setCardName(mucRoomMemberList.get(i).getRemarkName());
                                    }
                                    roomMember.setRole(mucRoomMemberList.get(i).getRole());
                                    roomMember.setCreateTime(mucRoomMemberList.get(i).getCreateTime());
                                    roomMemberList.add(roomMember);
                                }

                                if (reset) {
                                    RoomMemberDao.getInstance().deleteRoomMemberTable(mRoomId);
                                }
                                AsyncUtils.doAsync(this, mucChatActivityAsyncContext -> {
                                    for (int i = 0; i < roomMemberList.size(); i++) {// ????????????????????????
                                        RoomMemberDao.getInstance().saveSingleRoomMember(mRoomId, roomMemberList.get(i));
                                    }
                                });

                                RoomInfoActivity.saveMucLastRoamingTime(coreManager.getSelf().getUserId(), mRoomId, mucRoomMemberList.get(mucRoomMemberList.size() - 1).getCreateTime(), reset);
                                Map<String, Integer> existMap = new HashMap<>();
                                List<BaseSortModel<RoomMember>> sortedList = SortHelper.toSortedModelList(roomMemberList, existMap,null);
                                // ??????????????????
                                if (reset) {
                                    mSortRoomMember.clear();
                                    mSortRoomMember.addAll(sortedList);
                                    mAdapter.notifyDataSetInvalidated();
                                } else {
                                    mSortRoomMember.addAll(sortedList);
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        if (reset) {
                            mListView.onPullDownRefreshComplete();
                        } else {
                            mListView.onPullUpRefreshComplete();
                        }
                        ToastUtil.showErrorNet(getApplicationContext());
                    }
                });
    }

    private String getName(RoomMember member) {
        if (mRoomMember != null && mRoomMember.getRole() == 1) {
            if (!TextUtils.equals(member.getUserName(), member.getCardName())) {// ???userName???cardName??????????????????????????????????????????????????????
                return member.getCardName();
            } else {
                if (mRemarksMap.containsKey(member.getUserId())) {
                    return mRemarksMap.get(member.getUserId());
                } else {
                    return member.getUserName();
                }
            }
        } else {
            if (mRemarksMap.containsKey(member.getUserId())) {
                return mRemarksMap.get(member.getUserId());
            } else {
                return member.getUserName();
            }
        }
    }

    private void showBannedDialog(final String userId) {
        final int daySeconds = 24 * 60 * 60;
        BannedDialog bannedDialog = new BannedDialog(mContext, new BannedDialog.OnBannedDialogClickListener() {

            @Override
            public void tv1Click() {
                bannedVoice(userId, 0);
            }

            @Override
            public void tv2Click() {
                bannedVoice(userId, TimeUtils.sk_time_current_time() + daySeconds / 48);
            }

            @Override
            public void tv3Click() {
                bannedVoice(userId, TimeUtils.sk_time_current_time() + daySeconds / 24);
            }

            @Override
            public void tv4Click() {
                bannedVoice(userId, TimeUtils.sk_time_current_time() + daySeconds);
            }

            @Override
            public void tv5Click() {
                bannedVoice(userId, TimeUtils.sk_time_current_time() + daySeconds * 3);
            }

            @Override
            public void tv6Click() {
                bannedVoice(userId, TimeUtils.sk_time_current_time() + daySeconds * 7);
            }

            @Override
            public void tv7Click() {
                bannedVoice(userId, TimeUtils.sk_time_current_time() + daySeconds * 15);
            }
        });
        bannedDialog.show();
    }

    /**
     * ???????????????
     */
    private void deleteMember(final RoomMember baseSortModel, final String userId) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoomId);
        params.put("userId", userId);
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBER_DELETE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            Toast.makeText(mContext, R.string.remove_success, Toast.LENGTH_SHORT).show();
                            mSortRoomMember.remove(baseSortModel);
                            mEditText.setText("");

                            RoomMemberDao.getInstance().deleteRoomMember(mRoomId, userId);
                            EventBus.getDefault().post(new EventGroupStatus(10001, Integer.valueOf(userId)));
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
     * ??????
     */
    private void bannedVoice(String userId, final long time) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoomId);
        params.put("userId", userId);
        params.put("memberTalkTime", String.valueOf(time));
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBER_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            if (time == 0) {
                                ToastUtil.showToast(mContext, R.string.canle_banned_succ);
                            } else {
                                ToastUtil.showToast(mContext, R.string.banned_succ);
                            }
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
     * ?????????????????????
     */
    private void setRemarkName(final String userId, final String name) {
        DialogHelper.showLimitSingleInputDialog(this, getString(R.string.change_remark), name, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String newName = ((EditText) v).getText().toString().trim();
                if (TextUtils.isEmpty(newName) || newName.equals(name)) {
                    return;
                }

                Map<String, String> params = new HashMap<>();
                params.put("access_token", coreManager.getSelfStatus().accessToken);
                params.put("roomId", mRoomId);
                params.put("userId", userId);
                params.put("remarkName", newName);
                DialogHelper.showDefaulteMessageProgressDialog(GroupMoreFeaturesActivity.this);

                HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBER_UPDATE)
                        .params(params)
                        .build()
                        .execute(new BaseCallback<Void>(Void.class) {

                            @Override
                            public void onResponse(ObjectResult<Void> result) {
                                DialogHelper.dismissProgressDialog();
                                if (Result.checkSuccess(mContext, result)) {
                                    ToastUtil.showToast(mContext, R.string.modify_succ);
                                    RoomMemberDao.getInstance().updateRoomMemberCardName(mRoomId, userId, newName);

                                    for (int i = 0; i < mSortRoomMember.size(); i++) {
/*
                                                if (mSortRoomMember.get(i).getBean().getUserId().equals(userId)) {
                                                    mSortRoomMember.get(i).getBean().setCardName(newName);
                                                }
*/
                                        if (mSortRoomMember.get(i).getBean().getUserId().equals(userId)) {
                                            mSortRoomMember.get(i).getBean().setCardName(newName);
                                        }
                                    }
                                    if (!TextUtils.isEmpty(mEditText.getText().toString())) {// ??????mEditText
                                        mEditText.setText("");
                                    } else {
                                        mAdapter.setData(mSortRoomMember);
                                    }
                                    // ????????????????????????
                                    EventBus.getDefault().post(new EventGroupStatus(10003, 0));
                                }
                            }

                            @Override
                            public void onError(Call call, Exception e) {
                                DialogHelper.dismissProgressDialog();
                                ToastUtil.showErrorNet(mContext);
                            }
                        });
            }
        });
    }

    class GroupMoreFeaturesAdapter extends BaseAdapter implements SectionIndexer {
         List<BaseSortModel<RoomMember>> mSortRoomMember;
//        List<RoomMember> mSortRoomMember;


        GroupMoreFeaturesAdapter() {
            Log.e("TAG_??????","sortRoomMember=");
            this.mSortRoomMember = new ArrayList<>();
        }

        public void setData(List<BaseSortModel<RoomMember>> sortRoomMember) {
            Log.e("TAG_??????","sortRoomMember="+sortRoomMember.size());
            this.mSortRoomMember = sortRoomMember;
            notifyDataSetChanged();
        }


//        GroupMoreFeaturesAdapter(List<RoomMember> sortRoomMember) {
//            this.mSortRoomMember = new ArrayList<>();
//            this.mSortRoomMember = sortRoomMember;
//        }
//
//        public void setData(List<RoomMember> sortRoomMember) {
//            this.mSortRoomMember = sortRoomMember;
//            notifyDataSetChanged();
//        }

        @Override
        public int getCount() {
            return mSortRoomMember.size();
        }

        @Override
        public Object getItem(int position) {
            return mSortRoomMember.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.row_room_all_member, parent, false);
            }
            TextView catagoryTitleTv = ViewHolder.get(convertView, R.id.catagory_title);
            ImageView avatarImg = ViewHolder.get(convertView, R.id.avatar_img);
            TextView roleS = ViewHolder.get(convertView, R.id.roles);
            TextView userNameTv = ViewHolder.get(convertView, R.id.user_name_tv);

///*
            // ??????position???????????????????????????Char ascii???
            int section = getSectionForPosition(position);
            // ?????????????????????????????????????????????Char????????? ??????????????????????????????
            if (position == getPositionForSection(section)) {
                catagoryTitleTv.setVisibility(View.VISIBLE);
                catagoryTitleTv.setText(mSortRoomMember.get(position).getFirstLetter());
            } else {
                catagoryTitleTv.setVisibility(View.GONE);
            }
//*/
//            catagoryTitleTv.setVisibility(View.GONE);

            // RoomMember member = mSortRoomMember.get(position).getBean();
            RoomMember member = mSortRoomMember.get(position).getBean();
            if (member != null) {
                AvatarHelper.getInstance().displayAvatar(getName(member), member.getUserId(), avatarImg, true);
                if (member.getRole() == 1) {
                    roleS.setBackgroundResource(R.drawable.bg_role1);
                    roleS.setText(InternationalizationHelper.getString("JXGroup_Owner"));
                } else if (member.getRole() == 2) {
                    roleS.setBackgroundResource(R.drawable.bg_role2);
                    roleS.setText(InternationalizationHelper.getString("JXGroup_Admin"));
                } else {
                    roleS.setBackgroundResource(R.drawable.bg_role3);
                    roleS.setText(InternationalizationHelper.getString("JXGroup_RoleNormal"));
                }
                userNameTv.setText(getName(member));
            }
            return convertView;
        }

        @Override
        public Object[] getSections() {
            return null;
        }

        @Override
        public int getPositionForSection(int section) {
            for (int i = 0; i < getCount(); i++) {
                String sortStr = mSortRoomMember.get(i).getFirstLetter();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int getSectionForPosition(int position) {
            return mSortRoomMember.get(position).getFirstLetter().charAt(0);
        }

    }
}

package com.ydd.yanshi.view.chatHolder;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ydd.yanshi.R;
import com.ydd.yanshi.bean.message.ChatMessage;
import com.ydd.yanshi.bean.message.XmppMessage;
import com.ydd.yanshi.util.StringUtils;

// 系统消息的holder
class SystemViewHolder extends AChatHolderInterface {

    TextView mTvContent;
    private String time;

    @Override
    public int itemLayoutId(boolean isMysend) {
        return R.layout.chat_item_system;
    }

    @Override
    public void initView(View view) {
        mTvContent = view.findViewById(R.id.chat_content_tv);
        mRootView = mTvContent;
    }

    @Override
    public void fillData(ChatMessage message) {
        SpannableString content;
        if (message.getFileSize() == XmppMessage.TYPE_83) {
            // 红包被领取的提示
            content = StringUtils.matcherSearchTitle(Color.parseColor("#EB9F4F"), message.getContent(), getString(R.string.chat_red));
        } else {
            //  验证该提示是否为邀请好友入群的验证提示，是的话高亮显示KeyWord 并针对Click事件进行处理
            // Todo  应该效仿红包被领取的提示，将原消息type与关键信息存在其他字段内，这样结构会更加清晰且不会出错
            String sure = message.isDownload() ? getString(R.string.has_confirm) : getString(R.string.to_confirm);
            content = StringUtils.matcherSearchTitle(Color.parseColor("#6699FF"), message.getContent(), sure);
        }
        int type = message.getType();
        Log.e("TAG_系统消息","type="+type+"\ncontent="+content.toString());
        switch (type){
            case XmppMessage.NEW_MEMBER:
                setText(message.getContent());
                break;
            default:
                setText(content.toString().contains("邀请成员:") ? content:"");
                break;
        }
        mTvContent.setOnClickListener(this);
    }

    @Override
    protected void onRootClick(View v) {

    }

    @Override
    public void showTime(String time) {
        this.time = time;
//        setText(mTvContent.getText());
    }

    private void setText(CharSequence content) {

        if ( !TextUtils.isEmpty(content)){
            mTvContent.setBackgroundResource(R.drawable.tip_drawable);
            mTvContent.setVisibility(View.VISIBLE);
        }else {
            mTvContent.setBackground(null);
            mTvContent.setVisibility(View.GONE);
        }
        mTvContent.setText(content);
    }

    @Override
    public boolean isLongClick() {
        return false;
    }

    @Override
    public boolean isOnClick() {
        return true;
    }

    @Override
    public boolean enableNormal() {
        return false;
    }
}

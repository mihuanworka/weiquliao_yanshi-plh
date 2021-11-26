package com.ydd.yanshi.adapter

import android.content.Context
import android.os.Build

import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ydd.yanshi.AppConstant.*
import com.ydd.yanshi.R
import com.ydd.yanshi.bean.redpacket.CardBean

/**
 * Created by 蒲弘宇的本地账户 on 2018/5/8.
 */
class CardsAdapter(layoutResId: Int, val context: Context, movies: List<CardBean>) : BaseQuickAdapter<CardBean, BaseViewHolder>(layoutResId, movies) {


    @RequiresApi(Build.VERSION_CODES.M)
    override fun convert(helper: BaseViewHolder?, item: CardBean?) {
        var bankId = item?.bankBrandId
        var bankName = item?.bankBrandName
//        var bankNum = StringUtils.hideCardNo(item?.cardNo)
        var bankNum = item?.cardNo

        var rel_card = helper?.getView<RelativeLayout>(R.id.rel_card)
        var tv_number = helper?.getView<TextView>(R.id.tv_number)
        var positions = helper?.adapterPosition

        when(bankId){
            ZHI_FU_BAO -> {
                tv_number?.setText(bankNum)
                rel_card?.setBackgroundResource(R.drawable.play_treasure)
            }
            ZHONG_GUO_YINHANG -> {
                tv_number?.setText(bankNum)
                rel_card?.setBackgroundResource(R.drawable.ic_cardbg_boc)
            }
            ZHONG_GUO_JIANSHE_YINHANG -> {
                tv_number?.setText(bankNum)
                rel_card?.setBackgroundResource(R.drawable.ic_cardbg_ccb)
            }
            ZHONG_GUO_GONGSHANG_YINHANG -> {
                tv_number?.setText(bankNum)
                rel_card?.setBackgroundResource(R.drawable.ic_cardbg_icbc)
            }
            ZHONG_GUO_NONGYE_YINHANG -> {
                tv_number?.setText(bankNum)
                rel_card?.setBackgroundResource(R.drawable.ic_cardbg_abc)
            }
            ZHONG_GUO_JIAOTONG_YINHANG -> {
                tv_number?.setText(bankNum)
                rel_card?.setBackgroundResource(R.drawable.ic_cardbg_comm)
            }
            ZHONG_GUO_YOUZHENG_YINHANG -> {
                tv_number?.setText(bankNum)
                rel_card?.setBackgroundResource(R.drawable.ic_cardbg_psbc)
            }
            ZHONG_GUO_YOUZHENG_beijing -> {
                tv_number?.setText(bankNum)
                rel_card?.setBackgroundResource(R.drawable.long_beijing)
            }
            ZHONG_GUO_YOUZHENG_bohai -> {
                tv_number?.setText(bankNum)
                rel_card?.setBackgroundResource(R.drawable.long_bohai)
            }
            ZHONG_GUO_YOUZHENG_zhongguoguangda -> {
                tv_number?.setText(bankNum)
                rel_card?.setBackgroundResource(R.drawable.long_guangda)
            }
            ZHONG_GUO_YOUZHENG_guangfa -> {
                tv_number?.setText(bankNum)
                rel_card?.setBackgroundResource(R.drawable.long_guangfa)
            }
            ZHONG_GUO_YOUZHENG_hengfeng -> {
                tv_number?.setText(bankNum)
                rel_card?.setBackgroundResource(R.drawable.long_hengfen)
            }
            ZHONG_GUO_YOUZHENG_huaxia -> {
                tv_number?.setText(bankNum)
                rel_card?.setBackgroundResource(R.drawable.long_huaxia)
            }
            ZHONG_GUO_YOUZHENG_zhongguomingsheng -> {
                tv_number?.setText(bankNum)
                rel_card?.setBackgroundResource(R.drawable.long_mingsheng)
            }
            ZHONG_GUO_YOUZHENG_shanghai -> {
                tv_number?.setText(bankNum)
                rel_card?.setBackgroundResource(R.drawable.long_shanghai)
            }
            ZHONG_GUO_YOUZHENG_wenzhou -> {
                tv_number?.setText(bankNum)
                rel_card?.setBackgroundResource(R.drawable.long_wenzhou)
            }
            ZHONG_GUO_YOUZHENG_xingye -> {
                tv_number?.setText(bankNum)
                rel_card?.setBackgroundResource(R.drawable.long_xingye)
            }
            ZHONG_GUO_YOUZHENG_zhaoshang -> {
                tv_number?.setText(bankNum)
                rel_card?.setBackgroundResource(R.drawable.long_zhaoshang)
            }
            ZHONG_GUO_YOUZHENG_zheshang -> {
                tv_number?.setText(bankNum)
                rel_card?.setBackgroundResource(R.drawable.long_zheshang)
            }
            ZHONG_GUO_YOUZHENG_zhongxin -> {
                tv_number?.setText(bankNum)
                rel_card?.setBackgroundResource(R.drawable.long_zhongxin)
            }

        }
//        //可链式调用赋值
//        helper?.setText(R.id.tv_payType, billDesc)
//                ?.setText(R.id.tv_tradeTime, tradeTime)
    }

}
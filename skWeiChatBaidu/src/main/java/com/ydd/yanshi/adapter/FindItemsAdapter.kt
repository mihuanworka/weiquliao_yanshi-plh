package com.ydd.yanshi.adapter

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ydd.yanshi.R
import com.ydd.yanshi.bean.circle.FindItem
import com.ydd.yanshi.sp.UserSp
import com.ydd.yanshi.ui.MWebViewActiviy

/**
 * Created by phy on 2020/2/29
 */
/**
 * Created by 蒲弘宇的本地账户 on 2018/5/8.
 */
class FindItemsAdapter(layoutResId: Int, val context: Context, movies: List<FindItem>) : BaseQuickAdapter<FindItem, BaseViewHolder>(layoutResId, movies) {


    @RequiresApi(Build.VERSION_CODES.M)
    override fun convert(helper: BaseViewHolder?, item: FindItem?) {
        var icon = item?.icon
        var title = item?.title
        var url = item?.url
        var rel_item = helper?.getView<RelativeLayout>(R.id.rel_find)
        var img = helper?.getView<ImageView>(R.id.icon)
        var tv_title = helper?.getView<TextView>(R.id.tv_title)
        var positions = helper?.adapterPosition

        tv_title?.text = title

        if (img != null) {
            Glide.with(mContext)
                    .load(icon)
                    .dontAnimate()
                    .error(R.mipmap.default_error)
                    .centerCrop()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(img)
        }
        rel_item?.setOnClickListener {
//            WebViewActivity.start(mContext, url)
            var phone = UserSp.getInstance(mContext).getTelephone("")
            Log.e("TAG_","PHONE="+phone)
            MWebViewActiviy.startActivity(mContext, title,url)
        }

    }

}
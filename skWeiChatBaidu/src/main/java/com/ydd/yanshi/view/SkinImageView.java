package com.ydd.yanshi.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.widget.ImageViewCompat;

import com.ydd.yanshi.R;
import com.ydd.yanshi.util.SkinUtils;

/**
 * 抽象出标题栏上的控件以实现根据皮肤切换深色浅色，
 */
public class SkinImageView extends AppCompatImageView {
    public SkinImageView(Context context) {
        super(context);
        init();
    }

    public SkinImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SkinImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        SkinUtils.Skin skin = SkinUtils.getSkin(getContext());
        if (skin.isLight()) {
            ImageViewCompat.setImageTintList(this, getContext().getResources().getColorStateList(R.color.black));
        }
    }

    public void setLeftBackImageColor(int ResId){
        ImageViewCompat.setImageTintList(this, getContext().getResources().getColorStateList(ResId));
    }
}

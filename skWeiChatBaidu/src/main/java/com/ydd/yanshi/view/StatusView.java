package com.ydd.yanshi.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.ydd.yanshi.util.lxtool.LxStatusBarTool;


public class StatusView extends View {
    private Context context;

    public StatusView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int statusBarHeight = LxStatusBarTool.getStatusbarHeight(context);

        @SuppressLint("Range")
        int widthSpec = MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.MATCH_PARENT, MeasureSpec.UNSPECIFIED);
        int heightSpec = MeasureSpec.makeMeasureSpec(statusBarHeight, MeasureSpec.UNSPECIFIED);
        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        setMeasuredDimension(widthSpec, heightSpec);
    }
}

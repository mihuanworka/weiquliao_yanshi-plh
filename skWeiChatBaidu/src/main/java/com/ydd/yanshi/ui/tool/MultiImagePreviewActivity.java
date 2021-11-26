package com.ydd.yanshi.ui.tool;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.example.qrcode.utils.DecodeUtils;
import com.google.zxing.Result;
import com.ydd.yanshi.AppConstant;
import com.ydd.yanshi.R;
import com.ydd.yanshi.db.InternationalizationHelper;
import com.ydd.yanshi.helper.AvatarHelper;
import com.ydd.yanshi.ui.base.BaseActivity;
import com.ydd.yanshi.ui.message.HandleQRCodeScanUtil;
import com.ydd.yanshi.util.FileUtil;
import com.ydd.yanshi.view.SaveWindow;
import com.ydd.yanshi.view.ZoomImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.kareluo.imaging.IMGEditActivity;

/**
 * 图片集的预览
 */
public class MultiImagePreviewActivity extends BaseActivity {
    public static final int REQUEST_IMAGE_EDIT = 1;

    SparseArray<View> mViews = new SparseArray<View>();
    private ArrayList<String> mImages;
    private int mPosition;
    private boolean mChangeSelected;
    private ViewPager mViewPager;
    private ImagesAdapter mAdapter;
    private CheckBox mCheckBox;
    private TextView mIndexCountTv;
    private List<Integer> mRemovePosition = new ArrayList<Integer>();
    private String imageUrl;
    private String mRealImageUrl;// 因为viewPager的预加载机制，需要记录当前页面真正的url
    private String mEditedPath;
    private SaveWindow mSaveWindow;
    private My_BroadcastReceivers my_broadcastReceiver = new My_BroadcastReceivers();

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images_preview);

        if (getIntent() != null) {
            mImages = (ArrayList<String>) getIntent().getSerializableExtra(AppConstant.EXTRA_IMAGES);
            for (String mImage : mImages) {
                Log.e("MultiImage", "dateSource:" + mImage);
            }
            mPosition = getIntent().getIntExtra(AppConstant.EXTRA_POSITION, 0);
            mChangeSelected = getIntent().getBooleanExtra(AppConstant.EXTRA_CHANGE_SELECTED, false);
        }
        if (mImages == null) {
            mImages = new ArrayList<String>();
        }

        initView();
        register();
    }

    private void doFinish() {
        if (mChangeSelected) {
            Intent intent = new Intent();
            ArrayList<String> resultImages = null;
            if (mRemovePosition.size() == 0) {
                resultImages = mImages;
            } else {
                resultImages = new ArrayList<String>();
                for (int i = 0; i < mImages.size(); i++) {
                    if (!isInRemoveList(i)) {
                        resultImages.add(mImages.get(i));
                    }
                }
            }
            intent.putExtra(AppConstant.EXTRA_IMAGES, resultImages);
            setResult(RESULT_OK, intent);
        }
        finish();
        overridePendingTransition(0, 0);// 关闭过场动画
    }

    @Override
    public void onBackPressed() {
        doFinish();
    }

    @Override
    protected boolean onHomeAsUp() {
        doFinish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (my_broadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(my_broadcastReceiver);
        }
    }

    private void initView() {
        getSupportActionBar().hide();
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mIndexCountTv = (TextView) findViewById(R.id.index_count_tv);
        mCheckBox = (CheckBox) findViewById(R.id.check_box);
        mViewPager.setPageMargin(10);

        mAdapter = new ImagesAdapter();
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        updateSelectIndex(mPosition);

        if (mPosition < mImages.size()) {
            mViewPager.setCurrentItem(mPosition);
        }

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                updateSelectIndex(arg0);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    public void updateSelectIndex(final int index) {
        if (mPosition >= mImages.size()) {
            mIndexCountTv.setText(null);
        } else {
            mRealImageUrl = mImages.get(index);
            mIndexCountTv.setText((index + 1) + "/" + mImages.size());
        }

        if (!mChangeSelected) {
            mCheckBox.setVisibility(View.GONE);
            return;
        }

        mCheckBox.setOnCheckedChangeListener(null);
        boolean removed = isInRemoveList(index);
        if (removed) {
            mCheckBox.setChecked(false);
        } else {
            mCheckBox.setChecked(true);
        }
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    removeFromRemoveList(index);
                } else {
                    addInRemoveList(index);
                }
            }
        });
    }

    void addInRemoveList(int position) {
        if (!isInRemoveList(position)) {
            mRemovePosition.add(Integer.valueOf(position));
        }
    }

    void removeFromRemoveList(int position) {
        if (isInRemoveList(position)) {
            mRemovePosition.remove(Integer.valueOf(position));
        }
    }

    boolean isInRemoveList(int position) {
        return mRemovePosition.indexOf(Integer.valueOf(position)) != -1;
    }

    private void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(com.ydd.yanshi.broadcast.OtherBroadcast.singledown);
        filter.addAction(com.ydd.yanshi.broadcast.OtherBroadcast.longpress);
        LocalBroadcastManager.getInstance(this).registerReceiver(my_broadcastReceiver, filter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_EDIT:
                    mRealImageUrl = mEditedPath;
                    mImages.set(mViewPager.getCurrentItem(), mEditedPath);
                    // 刷新当前页面，
                    mAdapter.refreshCurrent();
                    break;
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    class My_BroadcastReceivers extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(com.ydd.yanshi.broadcast.OtherBroadcast.singledown)) {
                doFinish();
            } else if (intent.getAction().equals(com.ydd.yanshi.broadcast.OtherBroadcast.longpress)) {
                // 长按屏幕，弹出菜单
                if (TextUtils.isEmpty(imageUrl)) {
                    Toast.makeText(MultiImagePreviewActivity.this, InternationalizationHelper.getString("JX_ImageIsNull"), Toast.LENGTH_SHORT).show();
                    return;
                }
                mSaveWindow = new SaveWindow(MultiImagePreviewActivity.this, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSaveWindow.dismiss();
                        switch (v.getId()) {
                            case R.id.save_image:
                                FileUtil.downImageToGallery(MultiImagePreviewActivity.this, mRealImageUrl);
                                break;
                            case R.id.edit_image:
                                Glide.with(MultiImagePreviewActivity.this)
                                        .load(mRealImageUrl)
                                        .dontAnimate().skipMemoryCache(true) // 不使用内存缓存
                                        .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                                        .downloadOnly(new SimpleTarget<File>() {
                                            @Override
                                            public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                                                mEditedPath = FileUtil.createImageFileForEdit().getAbsolutePath();
                                                IMGEditActivity.startForResult(MultiImagePreviewActivity.this, Uri.fromFile(resource), mEditedPath, REQUEST_IMAGE_EDIT);
                                            }
                                        });
                                break;
                            case R.id.identification_qr_code:
                                // 识别图中二维码
                                Glide.with(mContext)
                                       .load(mRealImageUrl)
                                        .centerCrop()
                                        .dontAnimate()
                                        .error(R.drawable.image_download_fail_icon)
                                        .skipMemoryCache(true)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .listener(new RequestListener<Drawable>() {

                                            @Override
                                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                return false;
                                            }

                                            @Override
                                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                new Thread(() -> {
                                                    BitmapDrawable bd = (BitmapDrawable) resource;
                                                    final Result result = DecodeUtils.decodeFromPicture(bd.getBitmap());
                                                    mViewPager.post(() -> {
                                                        if (result != null && !TextUtils.isEmpty(result.getText())) {
                                                            HandleQRCodeScanUtil.handleScanResult(mContext, result.getText());
                                                        } else {
                                                            Toast.makeText(MultiImagePreviewActivity.this, R.string.decode_failed, Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }).start();
                                                return false;
                                            }

                                        });
                                break;
                        }
                    }
                });
                mSaveWindow.show();
            }
        }
    }

    class ImagesAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mImages.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public void refreshCurrent() {
            AvatarHelper.getInstance().displayUrl(mRealImageUrl, (ZoomImageView) mViews.get(mViewPager.getCurrentItem()));
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view = mViews.get(position);
            if (view == null) {
                view = new ZoomImageView(MultiImagePreviewActivity.this);
                mViews.put(position, view);
            }
            // init status
            imageUrl = mImages.get(position);

            AvatarHelper.getInstance().displayUrl(imageUrl, (ZoomImageView) view);
           /* Scheme scheme = Scheme.ofUri(imageUrl);
            switch (scheme) {
                case HTTP:
                case HTTPS:// 需要网络加载的
                    AvatarHelper.getInstance().displayImage(imageUrl, (ZoomImageView) view);
                    break;
                case UNKNOWN:// 如果不知道什么类型，且不为空，就当做是一个本地文件的路径来加载
                    if (!TextUtils.isEmpty(imageUrl)) {
                        AvatarHelper.getInstance().displayImage(Uri.fromFile(new File(imageUrl)).toString(), (ZoomImageView) view);
                    }
                    break;
                default:
                    // 其他 drawable asset类型不处理
                    break;
            }*/
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = mViews.get(position);
            if (view == null) {
                super.destroyItem(container, position, object);
            } else {
                container.removeView(view);
            }
        }
    }
}

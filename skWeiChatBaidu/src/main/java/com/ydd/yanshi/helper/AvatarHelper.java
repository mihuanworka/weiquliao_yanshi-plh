package com.ydd.yanshi.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ydd.yanshi.BuildConfig;
import com.ydd.yanshi.MyApplication;
import com.ydd.yanshi.R;
import com.ydd.yanshi.Reporter;
import com.ydd.yanshi.bean.Friend;
import com.ydd.yanshi.db.dao.FriendDao;
import com.ydd.yanshi.db.dao.RoomMemberDao;
import com.ydd.yanshi.db.dao.UserAvatarDao;
import com.ydd.yanshi.ui.base.CoreManager;
import com.ydd.yanshi.util.AsyncUtils;
import com.ydd.yanshi.util.AvatarUtil;
import com.ydd.yanshi.util.DisplayUtil;
import com.ydd.yanshi.util.SkinUtils;
import com.ydd.yanshi.view.HeadView;
import com.ydd.yanshi.view.circularImageView.JoinBitmaps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import uk.co.senab.bitmapcache.BitmapLruCache;

/**
 * 用户头像的上传和获取
 */
public class AvatarHelper {
    private static final String TAG = "AvatarHelper";
    public static AvatarHelper INSTANCE;
    public Context mContext;
    private BitmapLruCache bitmapCache;
    private Map<String, Bitmap> mVideoThumbMap = new HashMap<>();

    private AvatarHelper(Context ctx) {
        this.mContext = ctx;
        bitmapCache = new BitmapLruCache.Builder(ctx)
                .setMemoryCacheEnabled(true)
                .setMemoryCacheMaxSizeUsingHeapSize()
                .setDiskCacheEnabled(true)
                .setDiskCacheLocation(ctx.getCacheDir())
                .build();
    }

    public static AvatarHelper getInstance() {
        if (INSTANCE == null) {
            synchronized (AvatarHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AvatarHelper(MyApplication.getContext());
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 更新头像
     */
    public static void updateAvatar(String userId) {
        UserAvatarDao.getInstance().saveUpdateTime(userId);
    }

    /**
     * 获取系统号的静态头像资源ID，
     *
     * @return 不是系统号就返回null,
     */
    @IdRes
    public static Integer getStaticAvatar(String userId) {
        Integer ret = null;
        switch (userId) {
            case Friend.ID_SYSTEM_MESSAGE:
                ret = R.mipmap.logo_1;
                break;
            case Friend.ID_NEW_FRIEND_MESSAGE:
                ret = R.drawable.im_new_friends;
                break;
            case Friend.ID_SK_PAY:
                ret = R.drawable.my_set_yuer;
                break;
            case "android":
            case "ios":
                ret = R.drawable.fdy;
                break;
            case "pc":
            case "mac":
            case "web":
                ret = R.drawable.feb;
                break;
        }
        return ret;
    }

    public static String getAvatarUrl(String userId, boolean isThumb) {
        if (TextUtils.isEmpty(userId) || userId.length() > 8) {
            return null;
        }
        int userIdInt = -1;
        try {
            userIdInt = Integer.parseInt(userId);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (userIdInt == -1 || userIdInt == 0) {
            return null;
        }

        int dirName = userIdInt % 10000;
        String url = null;
        if (isThumb) {
            url = CoreManager.requireConfig(MyApplication.getInstance()).AVATAR_THUMB_PREFIX + "/" + dirName + "/" + userId + ".jpg";
        } else {
            url = CoreManager.requireConfig(MyApplication.getInstance()).AVATAR_ORIGINAL_PREFIX + "/" + dirName + "/" + userId + ".jpg";
        }
        return url;
    }

    public static String getGroupAvatarUrl(String userId, boolean isThumb) {
        int jidHashCode = userId.hashCode();
        int oneLevelName = Math.abs(jidHashCode % 10000);
        int twoLevelName = Math.abs(jidHashCode % 20000);

        // Log.e("zx", "jidHashCode:  " + jidHashCode + "  oneLevelName: " + oneLevelName);

        int dirName = oneLevelName;
        String url;
        Random random = new Random();
        int num = random.nextInt(99) % (99 - 10 + 1) + 10;
        if (isThumb) {
            url = CoreManager.requireConfig(MyApplication.getInstance()).AVATAR_THUMB_PREFIX + "/" + dirName + "/" + twoLevelName + "/" + userId + ".jpg?" + num;
        } else {
            url = CoreManager.requireConfig(MyApplication.getInstance()).AVATAR_ORIGINAL_PREFIX + "/" + dirName + "/" + twoLevelName + "/" + userId + ".jpg?" + num;
        }
        return url;
    }

    public void displayAvatar(String userId, ImageView imageView) {
        displayAvatar(userId, imageView, true);
    }

    public void displayAvatar(String userId, HeadView headView) {
        displayAvatar(userId, headView.getHeadImage(), true);
    }

    private boolean handlerSpecialAvatar(String userId, ImageView iv) {
        if (userId.equals(Friend.ID_SYSTEM_MESSAGE)) {
            iv.setImageResource(R.mipmap.logo_1);
            return true;
        } else if (userId.equals(Friend.ID_NEW_FRIEND_MESSAGE)) {
            iv.setImageResource(R.drawable.im_new_friends);
            return true;
        } else if (userId.equals(Friend.ID_SK_PAY)) {
            iv.setImageResource(R.drawable.my_set_yuer);
            return true;
        } else if (userId.equals("android") || userId.equals("ios")) {// 我的手机
//            iv.setImageResource(R.drawable.fdy);
            iv.setImageResource(R.drawable.feb);
            return true;
        } else if (userId.equals("pc") || userId.equals("mac") || userId.equals("web")) {// 我的电脑
            iv.setImageResource(R.drawable.feb);
            return true;
        }
        return false;
    }

    /**
     * 显示头像
     *
     * @param userId
     * @param imageView
     * @param isThumb
     */
    public void displayAvatar(String userId, final ImageView imageView, final boolean isThumb) {
        if (handlerSpecialAvatar(userId, imageView)) {
            return;
        }

        String url = getAvatarUrl(userId, isThumb);
        if (TextUtils.isEmpty(url)) {
            return;
        }

        Friend friend = FriendDao.getInstance().getFriend(CoreManager.requireSelf(mContext).getUserId(), userId);
        if (friend != null) {
            String name = TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName();
            displayAvatar(name, userId, imageView, isThumb);
        } else if (CoreManager.requireSelf(mContext).getUserId().equals(userId)) {
            displayAvatar(CoreManager.requireSelf(mContext).getNickName(), CoreManager.requireSelf(mContext).getUserId(), imageView, isThumb);
        } else {
            Log.e("TAG_zq", "friend==null,直接调用下面传nickName的display方法");
            displayUrl(getAvatarUrl(userId, isThumb), imageView);
        }

    }

    /**
     * 显示头像
     *
     * @param userId
     * @param imageView
     * @param isThumb
     */
    public void displayAvatar(String nickName, String userId, final ImageView imageView, final boolean isThumb) {
        if (handlerSpecialAvatar(userId, imageView)) {
            return;
        }

        String url = getAvatarUrl(userId, isThumb);
        Log.e("TAG_加载头像","url="+url);
        if (TextUtils.isEmpty(url)) {
            return;
        }
        String time = UserAvatarDao.getInstance().getUpdateTime(userId);
        imageView.setTag(R.id.key_avatar, url);
        Glide.with(MyApplication.getContext())
                .asBitmap()
                .load(url)
                .placeholder(R.drawable.avatar_normal)
                .dontAnimate()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                if (imageView.getTag(R.id.key_avatar) != url) {
                    return;
                }
                imageView.setImageBitmap(resource);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                if (imageView.getTag(R.id.key_avatar) != url) {
                    return;
                }
                List<Object> bitmapList = new ArrayList();
                bitmapList.add(nickName);
                Bitmap avatar = AvatarUtil.getBuilder(mContext)
                        .setShape(AvatarUtil.Shape.CIRCLE)
                        .setList(bitmapList)
                        .setTextSize(DisplayUtil.dip2px(mContext, 40))
                        .setTextColor(R.color.white)
                        .setTextBgColor(SkinUtils.getSkin(mContext).getAccentColor())
                        .setBitmapSize(DisplayUtil.dip2px(mContext, 120), DisplayUtil.dip2px(mContext, 120))
                        .create();
                BitmapDrawable bitmapDrawable = new BitmapDrawable(avatar);
                bitmapDrawable.setAntiAlias(true);
                imageView.setImageDrawable(bitmapDrawable);
            }
        });

    }

    public void displayRoundAvatar(String nickName, String userId, final ImageView imageView, final boolean isThumb) {
        if (handlerSpecialAvatar(userId, imageView)) {
            return;
        }

        String url = getAvatarUrl(userId, isThumb);
        if (TextUtils.isEmpty(url)) {
            return;
        }
        String time = UserAvatarDao.getInstance().getUpdateTime(userId);

        Glide.with(MyApplication.getContext())
                .asBitmap()
                .load(url)
                .placeholder(R.drawable.avatar_normal)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .dontAnimate()
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        imageView.setImageBitmap(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        List<Object> bitmapList = new ArrayList();
                        bitmapList.add(nickName);
                        Bitmap avatar = AvatarUtil.getBuilder(mContext)
                                .setShape(AvatarUtil.Shape.ROUND)
                                .setList(bitmapList)
                                .setTextSize(DisplayUtil.dip2px(mContext, 40))
                                .setTextColor(R.color.white)
                                .setTextBgColor(SkinUtils.getSkin(mContext).getAccentColor())
                                .setBitmapSize(DisplayUtil.dip2px(mContext, 240), DisplayUtil.dip2px(mContext, 240))
                                .create();
                        BitmapDrawable bitmapDrawable = new BitmapDrawable(avatar);
                        bitmapDrawable.setAntiAlias(true);
                        imageView.setImageDrawable(bitmapDrawable);

                    }
                });
    }

    /**
     * 手机联系人加载头像 无userId
     */
    public void displayAddressAvatar(String nickName, final ImageView imageView) {
        List<Object> bitmapList = new ArrayList();
        bitmapList.add(nickName);

        Bitmap avatar = AvatarUtil.getBuilder(mContext)
                .setShape(AvatarUtil.Shape.CIRCLE)
                .setList(bitmapList)
                .setTextSize(DisplayUtil.dip2px(mContext, 40))
                .setTextColor(R.color.white)
                .setTextBgColor(SkinUtils.getSkin(mContext).getAccentColor())
                .setBitmapSize(DisplayUtil.dip2px(mContext, 120), DisplayUtil.dip2px(mContext, 120))
                .create();
        BitmapDrawable bitmapDrawable = new BitmapDrawable(avatar);
        bitmapDrawable.setAntiAlias(true);
        imageView.setImageDrawable(bitmapDrawable);
    }

    /**
     * 封装个人头像和群头像的处理，
     * 缓存过期通过UserAvatarDao判断，
     * 群更新成员列表时{@link RoomMemberDao#deleteRoomMemberTable(java.lang.String)}里调用UserAvatarDao标记过期，
     */
    public void displayAvatar(String selfId, Friend friend, HeadView headView) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "displayAvatar: <" + friend.getNickName() + ">");
        }
        headView.setRound(false);
        ImageView view = headView.getHeadImage();
        if (friend.getRoomFlag() == 0) {// 个人
            displayAvatar(friend.getUserId(), view, false);
        } else if (friend.getRoomId() != null) {  // 群组
            String url = getGroupAvatarUrl(friend.getUserId(), false);
            view.setTag(R.id.key_avatar, url);

            Glide.with(MyApplication.getContext())
                    .asBitmap()
                    .load(url)
                    .placeholder(R.drawable.groupdefault)
                    .dontAnimate()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            view.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {


                        }
                    });
        } else {
            view.setImageResource(R.drawable.groupdefault);
        }
    }


    private void displayJoinedBitmap(String roomId, List<Bitmap> bitmaps, HeadView headView) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "displayJoinedBitmap: size = " + bitmaps.size());
        }
        ImageView view = headView.getHeadImage();
        if (bitmaps.size() == 1) {
            view.setImageBitmap(bitmaps.get(0));
            return;
        }
        // 群组组合头像不能设置为圆形，否则边角有缺，
        headView.setRound(false);
        int width = view.getWidth();
        if (width > 0) {
            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            JoinBitmaps.join(canvas, width, bitmaps, 0.15F);
            displayBitmap(roomId, bitmap, headView);
        } else {
            // 加载太快可能布局还没加载出ner确保布局加载完成后再次设置头像，来，
            // 通过addOnLayoutChangeListe
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "displayJoinedBitmap: " + Integer.toHexString(view.hashCode()) + ".width = 0");
            }
            view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onLayoutChange: " + Integer.toHexString(view.hashCode()) + ".width = " + v.getWidth());
                    }
                    if (v.getWidth() > 0) {
                        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        JoinBitmaps.join(canvas, v.getWidth(), bitmaps, 0.15F);
                        displayBitmap(roomId, bitmap, headView);
                        v.removeOnLayoutChangeListener(this);
                    } else {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "displayJoinedBitmap: " + Integer.toHexString(view.hashCode()) + ".width = 0");
                        }
                    }
                }
            });
        }
    }

    private void displayBitmap(String roomId, Bitmap bitmap, HeadView headView) {
        AsyncUtils.doAsync(this, c -> {
            String time = UserAvatarDao.getInstance().getUpdateTime(roomId);
            bitmapCache.put(roomId + time, bitmap);
        });
        headView.getHeadImage().setImageBitmap(bitmap);
    }

    /**
     * 本地视频缩略图 缓存
     */
    public Bitmap displayVideoThumb(String videoFilePath, ImageView image) {
        if (TextUtils.isEmpty(videoFilePath)) {
            return null;
        }

        Bitmap bitmap = MyApplication.getInstance().getBitmapFromMemCache(videoFilePath);
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = ThumbnailUtils.createVideoThumbnail(videoFilePath, MediaStore.Video.Thumbnails.MINI_KIND);
            // 视频格式不支持可能导致得到缩略图bitmap为空，LruCache不能接受空，
            // 主要是系统相册里的存着的视频不一定都是真实有效的，
            if (bitmap != null) {
                MyApplication.getInstance().addBitmapToMemoryCache(videoFilePath, bitmap);
            }
        }

        if (bitmap != null && !bitmap.isRecycled()) {
            image.setImageBitmap(bitmap);
        } else {
            image.setImageBitmap(null);
        }

        return bitmap;
    }

    /**
     * 在线视频缩略图获取显示 缓存
     */
    public void asyncDisplayOnlineVideoThumb(String url, ImageView image) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (mVideoThumbMap.containsKey(url)) {
            image.setImageBitmap(mVideoThumbMap.get(url));
            return;
        }
//        image.setTag(url);
        AsyncUtils.doAsync(this, t -> {
            Reporter.post("获取在线视频缩略图失败, " + url, t);
        }, c -> {
            MediaMetadataRetriever retr = new MediaMetadataRetriever();
            Uri uri = Uri.parse(url);
            if (TextUtils.equals(uri.getScheme(), "file")) {
                // 本地文件不能使用file://开头的url加载，
                retr.setDataSource(uri.getPath());
            } else {
                retr.setDataSource(url, new HashMap<>());
            }
            Bitmap bitmap = retr.getFrameAtTime();
            mVideoThumbMap.put(url, bitmap);
            c.uiThread(r -> {
                image.setImageBitmap(bitmap);
/*
                if (image.getTag() == url) {
                    image.setImageBitmap(bitmap);
                }
*/
            });
        });
    }

    /**
     * 加载网络图片
     */
    public void displayUrl(String url, ImageView imageView, int errid) {
        Glide.with(MyApplication.getContext())
                .load(url)
                .error(errid)
                .skipMemoryCache(true)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageView);
    }

    public void displayUrl(String url, ImageView imageView) {
        displayUrl(url, imageView, R.drawable.image_download_fail_icon);
    }

    // 根据文件类型填充图片
    public void fillFileView(String type, ImageView v) {
        if (type.equals("mp3")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_y);
        } else if (type.equals("mp4") || type.equals("avi")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_v);
        } else if (type.equals("xls")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_x);
        } else if (type.equals("doc")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_w);
        } else if (type.equals("ppt")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_p);
        } else if (type.equals("pdf")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_f);
        } else if (type.equals("apk")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_a);
        } else if (type.equals("txt")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_t);
        } else if (type.equals("rar") || type.equals("zip")) {
            v.setImageResource(R.drawable.ic_muc_flie_type_z);
        } else {
            v.setImageResource(R.drawable.ic_muc_flie_type_what);
        }
    }
}

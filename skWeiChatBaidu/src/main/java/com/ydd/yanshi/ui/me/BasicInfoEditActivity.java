package com.ydd.yanshi.ui.me;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.huantansheng.easyphotos.EasyPhotos;
import com.huantansheng.easyphotos.models.album.entity.Photo;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.ydd.yanshi.AppConstant;
import com.ydd.yanshi.MyApplication;
import com.ydd.yanshi.R;
import com.ydd.yanshi.bean.Area;
import com.ydd.yanshi.bean.EventAvatarUploadSuccess;
import com.ydd.yanshi.bean.UploadImageResult;
import com.ydd.yanshi.bean.User;
import com.ydd.yanshi.db.InternationalizationHelper;
import com.ydd.yanshi.db.dao.UserDao;
import com.ydd.yanshi.helper.AvatarHelper;
import com.ydd.yanshi.helper.DialogHelper;
import com.ydd.yanshi.helper.LoginHelper;
import com.ydd.yanshi.ui.GlideEngine;
import com.ydd.yanshi.ui.base.BaseActivity;
import com.ydd.yanshi.ui.other.QRcodeActivity;
import com.ydd.yanshi.ui.tool.SelectAreaActivity;
import com.ydd.yanshi.util.CameraUtil;
import com.ydd.yanshi.util.Constants;
import com.ydd.yanshi.util.DeviceInfoUtil;
import com.ydd.yanshi.util.PreferenceUtils;
import com.ydd.yanshi.util.StringUtils;
import com.ydd.yanshi.util.TimeUtils;
import com.ydd.yanshi.util.ToastUtil;
import com.ydd.yanshi.volley.Result;

import org.apache.http.Header;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

import static com.ydd.yanshi.util.CameraUtil.REQUEST_CODE_CROP_PHOTO;

/**
 * 编辑个人资料
 */
public class BasicInfoEditActivity extends BaseActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_SET_ACCOUNT = 5;

    // widget
    private ImageView mAvatarImg;
    private EditText mNameEdit;
    private TextView mSexTv;
    private TextView mBirthdayTv;
    private TextView mCityTv;
    private TextView mTvDiyName;
    private Button mNextStepBtn;
    private TextView nickNameTv, sexTv, birthdayTv, cityTv, shiledTv, mAccountDesc, mAccount;
    private User mUser;
    // Temp
    private User mTempData;
    //    private File mCurrentFile;
//    private Uri mNewPhotoUri;
    Photo photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = coreManager.getSelf();
        if (!LoginHelper.isUserValidation(mUser)) {
            return;
        }
        setContentView(R.layout.activity_basic_info_edit);
        EasyPhotos.preLoad(this);
        initView();
    }

    private void initView() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(InternationalizationHelper.getString("JX_BaseInfo"));

        TextView tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        tvPhoneNumber.setText(R.string.myCellPhoneNumber);
        //  UsernameHelper.initTextView(tvPhoneNumber, coreManager.getConfig().registerUsername);

        mAvatarImg = (ImageView) findViewById(R.id.avatar_img);
        mNameEdit = (EditText) findViewById(R.id.name_edit);
        mSexTv = (TextView) findViewById(R.id.sex_tv);
        mBirthdayTv = (TextView) findViewById(R.id.birthday_tv);
        mCityTv = (TextView) findViewById(R.id.city_tv);
        mTvDiyName = (TextView) findViewById(R.id.tv_diy_name);
        mNextStepBtn = (Button) findViewById(R.id.next_step_btn);
//        mNextStepBtn.setBackgroundColor(SkinUtils.getSkin(this).getAccentColor());

        nickNameTv = (TextView) findViewById(R.id.name_text);
        sexTv = (TextView) findViewById(R.id.sex_text);
        birthdayTv = (TextView) findViewById(R.id.birthday_text);
        cityTv = (TextView) findViewById(R.id.city_text);
        shiledTv = (TextView) findViewById(R.id.iv_diy_name);
        mAccountDesc = (TextView) findViewById(R.id.sk_account_desc_tv);
        mAccountDesc.setText(getString(R.string.sk_account, getString(R.string.sk_account_code)));
        mAccount = (TextView) findViewById(R.id.sk_account_tv);
        TextView mQRCode = (TextView) findViewById(R.id.city_text_02);

        mQRCode.setText(InternationalizationHelper.getString("JX_MyQRImage"));
        nickNameTv.setText(InternationalizationHelper.getString("JX_NickName"));
        sexTv.setText(InternationalizationHelper.getString("JX_Sex"));
        birthdayTv.setText(InternationalizationHelper.getString("JX_BirthDay"));

        cityTv.setText(R.string.myCity);
        shiledTv.setText(InternationalizationHelper.getString("PERSONALIZED_SIGNATURE"));
        mNameEdit.setHint("请输入昵称");
        mTvDiyName.setHint(InternationalizationHelper.getString("ENTER_PERSONALIZED_SIGNATURE"));
        mNextStepBtn.setText(R.string.modificationSubmission);

        mAvatarImg.setOnClickListener(this);
        findViewById(R.id.sex_select_rl).setOnClickListener(this);
        findViewById(R.id.birthday_select_rl).setOnClickListener(this);
        if (coreManager.getConfig().disableLocationServer) {
            findViewById(R.id.city_select_rl).setVisibility(View.GONE);
        } else {
            findViewById(R.id.city_select_rl).setOnClickListener(this);
        }
        findViewById(R.id.diy_name_rl).setOnClickListener(this);
        findViewById(R.id.qccodeforshiku).setOnClickListener(this);
        mNextStepBtn.setOnClickListener(this);

        if (coreManager.getConfig().registerInviteCode == 2
                && !TextUtils.isEmpty(coreManager.getSelf().getMyInviteCode())) {
            TextView tvInviteCode = findViewById(R.id.invite_code_tv);
            tvInviteCode.setText(coreManager.getSelf().getMyInviteCode());
        } else {
            findViewById(R.id.rlInviteCode).setVisibility(View.GONE);
        }

        updateUI();
    }

    private void updateUI() {
        // clone一份临时数据，用来存数变化的值，返回的时候对比有无变化
        try {
            mTempData = (User) mUser.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        AvatarHelper.getInstance().updateAvatar(mTempData.getUserId());
        // AvatarHelper.getInstance().displayAvatar(mTempData.getUserId(), mAvatarImg, false);
        displayAvatar(mTempData.getUserId());

        mNameEdit.setText(mTempData.getNickName());
        if (mTempData.getSex() == 1) {
            mSexTv.setText(InternationalizationHelper.getString("JX_Man"));
        } else {
            mSexTv.setText(InternationalizationHelper.getString("JX_Wuman"));
        }
        mBirthdayTv.setText(TimeUtils.sk_time_s_long_2_str(mTempData.getBirthday()));
        mCityTv.setText(Area.getProvinceCityString(mTempData.getCityId(), mTempData.getAreaId()));
        mTvDiyName.setText(mTempData.getDescription());

        TextView mPhoneTv = (TextView) findViewById(R.id.phone_tv);
        String phoneNumber = coreManager.getSelf().getTelephone();
        int mobilePrefix = PreferenceUtils.getInt(this, Constants.AREA_CODE_KEY, -1);
        String sPrefix = String.valueOf(mobilePrefix);
        // 删除开头的区号，
        if (phoneNumber.startsWith(sPrefix)) {
            phoneNumber = phoneNumber.substring(sPrefix.length());
        }
        mPhoneTv.setText(phoneNumber);

        initAccount();
    }

    private void initAccount() {
        if (mTempData != null) {
            if (mTempData.getSetAccountCount() == 0) {
                // 之前未设置过sk号 前往设置
                findViewById(R.id.sk_account_rl).setOnClickListener(v -> {
                    Intent intent = new Intent(mContext, SetAccountActivity.class);
                    intent.putExtra(AppConstant.EXTRA_USER_ID, mTempData.getUserId());
                    intent.putExtra(AppConstant.EXTRA_NICK_NAME, mTempData.getNickName());
                    startActivityForResult(intent, REQUEST_CODE_SET_ACCOUNT);
                });
                findViewById(R.id.city_arrow_img_05).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.sk_account_rl).setOnClickListener(null);
                findViewById(R.id.city_arrow_img_05).setVisibility(View.INVISIBLE);
            }

            if (!TextUtils.isEmpty(mTempData.getAccount())) {
                mAccount.setText(mTempData.getAccount());
            }
        }
    }

    public void displayAvatar(final String userId) {
//        DialogHelper.showDefaulteMessageProgressDialog(this);
        final String mOriginalUrl = AvatarHelper.getAvatarUrl(userId, false);
        Log.e("TAG_加载头像", "displayAvatar: mOriginalUrl:  " + mOriginalUrl + " uID: " + userId);
        if (!TextUtils.isEmpty(mOriginalUrl)) {

            Glide.with(this)
                    .load(mOriginalUrl)
                    .placeholder(R.drawable.avatar_normal)
                    .dontAnimate()
                    .error(R.drawable.avatar_normal)
                    .skipMemoryCache(true) // 不使用内存缓存
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                    .into(mAvatarImg);
        } else {
//            DialogHelper.dismissProgressDialog();
            Log.e("zq", "未获取到原图地址");// 基本上不会走这里
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.avatar_img:
                showSelectAvatarDialog();
                break;
            case R.id.sex_select_rl:
                showSelectSexDialog();
                break;
            case R.id.birthday_select_rl:
                showSelectBirthdayDialog();
                break;
            case R.id.city_select_rl:
                Intent intent = new Intent(BasicInfoEditActivity.this, SelectAreaActivity.class);
                intent.putExtra(SelectAreaActivity.EXTRA_AREA_TYPE, Area.AREA_TYPE_PROVINCE);
                intent.putExtra(SelectAreaActivity.EXTRA_AREA_PARENT_ID, Area.AREA_DATA_CHINA_ID);
                intent.putExtra(SelectAreaActivity.EXTRA_AREA_DEEP, Area.AREA_TYPE_CITY);
                startActivityForResult(intent, 4);
                break;
            case R.id.diy_name_rl:
                inputDiyName();
                break;
            case R.id.qccodeforshiku:
                Intent intent2 = new Intent(BasicInfoEditActivity.this, QRcodeActivity.class);
                intent2.putExtra("isgroup", false);
                if (!TextUtils.isEmpty(mUser.getAccount())) {
                    intent2.putExtra("userid", mUser.getAccount());
                } else {
                    intent2.putExtra("userid", mUser.getUserId());
                }
                intent2.putExtra("userAvatar", mUser.getUserId());
                intent2.putExtra("userName", mUser.getNickName());
                startActivity(intent2);
                break;
            case R.id.next_step_btn:
                next();
                break;
        }
    }

    private void showSelectAvatarDialog() {
        String[] items = new String[]{InternationalizationHelper.getString("PHOTOGRAPH"), InternationalizationHelper.getString("ALBUM")};
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(InternationalizationHelper.getString("SELECT_AVATARS"))
                .setSingleChoiceItems(items, 0,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.e("TAG_上传头像", "which=" + which);
                                if (which == 0) {
                                    openCamera();
                                } else {
                                    selectPhoto();
                                }
                                dialog.dismiss();
                            }
                        });
        builder.show();
    }

    private void openCamera() {
        String packageName = DeviceInfoUtil.getPackageName(this);
        EasyPhotos.createCamera(this, false)//参数说明：上下文,是否使用宽高数据（false时宽高数据为0，扫描速度更快）
                .setFileProviderAuthority(packageName + ".fileprovider")//参数说明：见下方`FileProvider的配置`
                .start(101);

    }

    private void selectPhoto() {
        EasyPhotos.createAlbum(this, false, true, GlideEngine.getInstance())//参数说明：上下文，是否显示相机按钮,是否使用宽高数据（false时宽高数据为0，扫描速度更快），[配置Glide为图片加载引擎](https://github.com/HuanTanSheng/EasyPhotos/wiki/12-%E9%85%8D%E7%BD%AEImageEngine%EF%BC%8C%E6%94%AF%E6%8C%81%E6%89%80%E6%9C%89%E5%9B%BE%E7%89%87%E5%8A%A0%E8%BD%BD%E5%BA%93)
                .start(101);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CROP_PHOTO) {// 选择一张图片,然后立即调用裁减
                File mCurrentFile = new File(photo.path);
                try {
                    mCurrentFile.createNewFile();
                    if (!mCurrentFile.exists()) {
                        // 文件不存在
                        Log.e("TAG_上传头像", "调用相机文件不存在");
                        return;
                    }
                    if (mCurrentFile != null) {
                        Log.e("TAG_上传头像", "uri=" + mCurrentFile.getPath());
                        AvatarHelper.getInstance().displayUrl(mCurrentFile.getPath(), mAvatarImg);
                        // 上传头像
                        uploadAvatar(mCurrentFile);
                    } else {
                        ToastUtil.showToast(this, R.string.c_crop_failed);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (requestCode == 4) {// 选择城市
                if (resultCode == RESULT_OK && data != null) {
                    int countryId = data.getIntExtra(SelectAreaActivity.EXTRA_COUNTRY_ID, 0);
                    int provinceId = data.getIntExtra(SelectAreaActivity.EXTRA_PROVINCE_ID, 0);
                    int cityId = data.getIntExtra(SelectAreaActivity.EXTRA_CITY_ID, 0);
                    int countyId = data.getIntExtra(SelectAreaActivity.EXTRA_COUNTY_ID, 0);

                    String province_name = data.getStringExtra(SelectAreaActivity.EXTRA_PROVINCE_NAME);
                    String city_name = data.getStringExtra(SelectAreaActivity.EXTRA_CITY_NAME);
                    mCityTv.setText(province_name + "-" + city_name);

                    mTempData.setCountryId(countryId);
                    mTempData.setProvinceId(provinceId);
                    mTempData.setCityId(cityId);
                    mTempData.setAreaId(countyId);
                }
            } else if (requestCode == REQUEST_CODE_SET_ACCOUNT) {
                if (resultCode == RESULT_OK && data != null) {
                    String account = data.getStringExtra(AppConstant.EXTRA_USER_ACCOUNT);
                    mTempData.setAccount(account);
                    mTempData.setSetAccountCount(1);
                    initAccount();
                }
            } else if (requestCode == 101) {
                //返回对象集合：如果你需要了解图片的宽、高、大小、用户是否选中原图选项等信息，可以用这个
                ArrayList<Photo> resultPhotos =
                        data.getParcelableArrayListExtra(EasyPhotos.RESULT_PHOTOS);
                photo = resultPhotos.get(0);
                CameraUtil.cropImage(this, photo.uri, REQUEST_CODE_CROP_PHOTO, true);

            }
        }
    }

    private void uploadAvatar(File file) {
        Log.e("TAG_上传头像", "file=" + file.getPath());
        if (!file.exists()) {
            // 文件不存在
            Log.e("TAG_上传头像", "文件不存在");
            return;
        }
        // 显示正在上传的ProgressDialog
        DialogHelper.showDefaulteMessageProgressDialog(this);
        RequestParams params = new RequestParams();
        final String loginUserId = coreManager.getSelf().getUserId();
        params.put("userId", loginUserId);
        try {
            params.put("file1", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        AsyncHttpClient client = new AsyncHttpClient();
        String avatar_upload_url = coreManager.getConfig().AVATAR_UPLOAD_URL;
        Log.e("TAG_上传头像", "avatar_upload_url=" + avatar_upload_url + "\nparams=" + params);
        client.post(avatar_upload_url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
                DialogHelper.dismissProgressDialog();
                boolean success = false;
                UploadImageResult result = null;
                if (arg0 == 200) {
                    try {
                        result = JSON.parseObject(new String(arg2), UploadImageResult.class);
                        Log.e("TAG_上传头像", "result=" + new String(arg2));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (result != null && result.getResultCode() == Result.CODE_SUCCESS) {
                        success = true;
                    }
                }

                if (success) {
                    ToastUtil.showToast(BasicInfoEditActivity.this, R.string.upload_avatar_success);
                    AvatarHelper.getInstance().updateAvatar(loginUserId);// 更新时间
                    EventBus.getDefault().post(new EventAvatarUploadSuccess(true));
                    String tUrl = result.getData().getTUrl();
                    Log.e("TAG_上传头像成功", "result=" + tUrl);
                    Glide.with(BasicInfoEditActivity.this)
                            .load(tUrl)
                            .placeholder(R.drawable.avatar_normal)
                            .dontAnimate()
                            .error(R.drawable.avatar_normal)
                            .skipMemoryCache(true) // 不使用内存缓存
                            .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                            .into(mAvatarImg);
                } else {
                    ToastUtil.showToast(BasicInfoEditActivity.this, R.string.upload_avatar_failed);
                }
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showToast(BasicInfoEditActivity.this, R.string.upload_avatar_failed);
            }
        });
    }

    private void showSelectSexDialog() {
        String[] sexs = new String[]{InternationalizationHelper.getString("JX_Man"), InternationalizationHelper.getString("JX_Wuman")};
        new AlertDialog.Builder(this).setTitle(InternationalizationHelper.getString("GENDER_SELECTION"))
                .setSingleChoiceItems(sexs, mTempData.getSex() == 1 ? 0 : 1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            mTempData.setSex(1);
                            mSexTv.setText(InternationalizationHelper.getString("JX_Man"));
                        } else {
                            mTempData.setSex(0);
                            mSexTv.setText(InternationalizationHelper.getString("JX_Wuman"));
                        }
                        dialog.dismiss();
                    }
                }).setCancelable(true).create().show();
    }

    @SuppressWarnings("deprecation")
    private void showSelectBirthdayDialog() {
        Date date = new Date(mTempData.getBirthday() * 1000);
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                GregorianCalendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                mTempData.setBirthday(TimeUtils.getSpecialBeginTime(mBirthdayTv, calendar.getTime().getTime() / 1000));
                long currentTime = System.currentTimeMillis() / 1000;
                long birthdayTime = calendar.getTime().getTime() / 1000;
                if (birthdayTime > currentTime) {
                    ToastUtil.showToast(mContext, R.string.data_of_birth);
                }
            }
        }, date.getYear() + 1900, date.getMonth(), date.getDate());
        dialog.show();
    }

    private void inputDiyName() {
        final EditText inputServer = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(InternationalizationHelper.getString("PERSONALIZED_SIGNATURE")).setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
                .setNegativeButton(InternationalizationHelper.getString("JX_Cencal"), null);
        builder.setPositiveButton(InternationalizationHelper.getString("JX_Confirm"), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String diyName = inputServer.getText().toString();
                mTvDiyName.setText(diyName);
                mUser.setDescription(diyName);
            }
        });
        builder.show();
    }

    private void loadPageData() {
        mTempData.setNickName(mNameEdit.getText().toString().trim());
    }

    private void next() {
        if (!MyApplication.getInstance().isNetworkActive()) {
            ToastUtil.showToast(this, R.string.net_exception);
            return;
        }

        loadPageData();

        if (TextUtils.isEmpty(mTempData.getNickName())) {
            mNameEdit.requestFocus();
            mNameEdit.setError(StringUtils.editTextHtmlErrorTip(this, R.string.name_empty_error));
            return;
        }

        if (!coreManager.getConfig().disableLocationServer) {
            if (mTempData.getCityId() <= 0) {
                ToastUtil.showToast(this, getString(R.string.live_address_empty_error));
                return;
            }
        }

        if (mUser != null && !mUser.equals(mTempData)) {// 数据改变了，提交数据
            updateData();
        } else {
            finish();
        }
    }

    private void updateData() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        if (!mUser.getNickName().equals(mTempData.getNickName())) {
            params.put("nickname", mTempData.getNickName());
        }
        if (mUser.getSex() != mTempData.getSex()) {
            params.put("sex", String.valueOf(mTempData.getSex()));
        }
        if (mUser.getBirthday() != mTempData.getBirthday()) {
            params.put("birthday", String.valueOf(mTempData.getBirthday()));
        }
        if (mUser.getCountryId() != mTempData.getCountryId()) {
            params.put("countryId", String.valueOf(mTempData.getCountryId()));
        }
        if (mUser.getProvinceId() != mTempData.getProvinceId()) {
            params.put("provinceId", String.valueOf(mTempData.getProvinceId()));
        }
        if (mUser.getCityId() != mTempData.getCityId()) {
            params.put("cityId", String.valueOf(mTempData.getCityId()));
        }
        if (mUser.getAreaId() != mTempData.getAreaId()) {
            params.put("areaId", String.valueOf(mTempData.getAreaId()));
        }
//        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().USER_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
//                        DialogHelper.dismissProgressDialog();
                        saveData();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
//                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(BasicInfoEditActivity.this);
                    }
                });
    }

    private void saveData() {
        if (!mUser.getNickName().equals(mTempData.getNickName())) {
            coreManager.getSelf().setNickName(mTempData.getNickName());
            UserDao.getInstance().updateNickName(mTempData.getUserId(), mTempData.getNickName());     // 更新数据库
        }
        if (mUser.getSex() != mTempData.getSex()) {
            coreManager.getSelf().setSex(mTempData.getSex());
            UserDao.getInstance().updateSex(mTempData.getUserId(), mTempData.getSex() + "");          // 更新数据库
        }
        if (mUser.getBirthday() != mTempData.getBirthday()) {
            coreManager.getSelf().setBirthday(mTempData.getBirthday());
            UserDao.getInstance().updateBirthday(mTempData.getUserId(), mTempData.getBirthday() + "");// 更新数据库
        }

        if (mUser.getCountryId() != mTempData.getCountryId()) {
            coreManager.getSelf().setCountryId(mTempData.getCountryId());
            UserDao.getInstance().updateCountryId(mTempData.getUserId(), mTempData.getCountryId());
        }
        if (mUser.getProvinceId() != mTempData.getProvinceId()) {
            coreManager.getSelf().setProvinceId(mTempData.getProvinceId());
            UserDao.getInstance().updateProvinceId(mTempData.getUserId(), mTempData.getProvinceId());
        }
        if (mUser.getCityId() != mTempData.getCityId()) {
            coreManager.getSelf().setCityId(mTempData.getCityId());
            UserDao.getInstance().updateCityId(mTempData.getUserId(), mTempData.getCityId());
        }
        if (mUser.getAreaId() != mTempData.getAreaId()) {
            coreManager.getSelf().setAreaId(mTempData.getAreaId());
            UserDao.getInstance().updateAreaId(mTempData.getUserId(), mTempData.getAreaId());
        }

        setResult(RESULT_OK);
        finish();
    }
}

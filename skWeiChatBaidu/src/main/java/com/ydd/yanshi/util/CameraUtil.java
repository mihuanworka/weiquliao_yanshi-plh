package com.ydd.yanshi.util;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.ydd.yanshi.MyApplication;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.Environment.DIRECTORY_PICTURES;

public final class CameraUtil {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private static MediaScannerConnection sMediaScannerConnection;

    public static final int REQUEST_CODE_OPENPHOTO = 10002;
    public static final int REQUEST_CODE_OPENCAMERA = 10001;
    public static final int REQUEST_CODE_CROP_PHOTO = 10000;
    public static final int REQUEST_CODE_CROP_CAMERA = 10003;
    /**
     * Create a file Uri for saving an image or video
     *
     * @param context
     * @param type    the type of the file you want saved {@link #MEDIA_TYPE_IMAGE}
     *                {@link #MEDIA_TYPE_VIDEO}
     * @return return the uri of the file ,if create failed,return null
     */
    static Uri photoUri = null;
    public static Uri getOutputMediaFileUri(Context context, int type) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file = getOutputMediaFile(context, type);
        if (file == null) {
            return null;
        }

        //确保有相机来处理Intent
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
        
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    String packageName = DeviceInfoUtil.getPackageName(context);
                    //适配Android 7.0文件权限，通过FileProvider创建一个content类型的Uri
                    photoUri = FileProvider.getUriForFile(MyApplication.getInstance(), packageName+".fileprovider", file);
                } else {
                    photoUri = Uri.fromFile(file);
                }

        }
        return photoUri;
    }
    public static Uri getPhotoUri() {
        return photoUri;
    }
    /**
     * Create a file for saving an image or video,is default in the
     * ../Pictures/[you app PackageName] directory
     *
     * @param context
     * @param type    the type of the file you want saved {@link #MEDIA_TYPE_IMAGE}
     *                {@link #MEDIA_TYPE_VIDEO}
     * @return return the file you create,if create failed,return null
     */
    static String currentPath=null;
    private static File getOutputMediaFile(Context context, int type) {
        String filePath = null;
        if (type == MEDIA_TYPE_IMAGE) {
            filePath = FileUtil.getRandomImageFilePath();
        } else if (type == MEDIA_TYPE_VIDEO) {
            filePath = FileUtil.getRandomVideoFilePath();
        } else {
            return null;
        }
        if (TextUtils.isEmpty(filePath)) {
            return null;
        } else {

            File ji=null;
            try {
                ji=new File(filePath);
                ji.createNewFile();
                currentPath=ji.getAbsolutePath();
                Log.e("TAG_上传头像", "ji=" + ji.getPath());
                Log.e("TAG_上传头像", "currentPath=" + currentPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ji;

        }
    }
    public static String getCurrentPath() {
       return currentPath;
    }
    /**
     * invoke the system Camera app and capture a image。 you can received the
     * capture result in {@link (int,int,Intent)}。 If
     * successed,you can use the outputUri to get the image
     *
     * @param activity
     * @param outputUri   拍照后图片的存储路径
     * @param requestCode
     */
    public static void captureImage(Activity activity, Uri outputUri, int requestCode) {
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 调用系统裁减功能，裁减某张指定的图片，并输出到指定的位置
     *
     * @param activity
     * @param
     * @param outputFileUri   裁减后图片的输出位置，两个地址最好不一样。如果一样的话，有的手机上面无法保存裁减的结果
     * @return
     */
    public static void cropImage(Activity activity, Uri mNewPhotoUri, int requestCode,boolean fromCamera) {
        Log.e("TAG_上传头像", "调用裁剪");
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (fromCamera){
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mNewPhotoUri);
        }else {
            String path = CameraUtil.getFilePathFromURI(activity,mNewPhotoUri);
//                    mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, CameraUtil.MEDIA_TYPE_IMAGE);
            File mCurrentFile = new File(path);
            try {
                mCurrentFile.createNewFile();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            Uri uri = Uri.fromFile(mCurrentFile);
//            Uri cropImageUri = CameraUtil.getOutputMediaFileUri(activity, CameraUtil.MEDIA_TYPE_IMAGE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }
        intent.setDataAndType(mNewPhotoUri , "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 500);
        intent.putExtra("outputY", 500);
        intent.putExtra("scale", true);

        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
//        //将存储图片的uri读写权限授权给剪裁工具应用
//        List<ResolveInfo> resInfoList = activity.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//        for (ResolveInfo resolveInfo : resInfoList) {
//            String packageName = resolveInfo.activityInfo.packageName;
//            activity.grantUriPermission(packageName, mNewPhotoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        }

        activity.startActivityForResult(intent, requestCode);

    }

    /**
     * 调用系统图库选择照片 使用 {@link}方法从
     * onActivityResult的data.getData()中解析获得的Uri
     *
     * @param activity
     * @param requestCode
     * @return
     */
    public static void pickImageSimple(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivityForResult(intent, requestCode);
    }



    /**
     * 获得The data stream for the file
     */
    public static String getImagePathFromUri(Context context, Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                return getImagePathFromUriKitkat(context, uri);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }
        return getImagePathFromUriSimple(context, uri);
    }

    /**
     * 4.4以下
     *
     * @param context
     * @param uri
     */
    private static String getImagePathFromUriSimple(Context context, Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String returnStr = cursor.getString(column_index);
        cursor.close();
        return returnStr;
    }

    /**
     * 4.4以上的Document Uri
     *
     * @param context
     * @param uri
     * @return
     */
    private static String getImagePathFromUriKitkat(Context context, Uri uri) {
        String wholeID = DocumentsContract.getDocumentId(uri);
        if (TextUtils.isEmpty(wholeID) || !wholeID.contains(":")) {
            return null;
        }
        // 获得资源唯一ID
        String id = wholeID.split(":")[1];
        // 定义索引字段
        String[] column = {MediaStore.Images.Media.DATA};
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel, new String[]{id}, null);
        int columnIndex = cursor.getColumnIndex(column[0]);

        String filePath = null;
        if (cursor.moveToFirst()) {
            // DATA字段就是本地资源的全路径
            filePath = cursor.getString(columnIndex);
        }
        // 切记要关闭游标
        cursor.close();
        return filePath;
    }

    /**
     * 调用系统图库选择照片,裁减后返回
     * ,4.4上无法确定用户是否是在图库里选择的照片，所以不使用该方法，使用pickImageSimple，返回后在调用裁减
     *
     * @param activity
     * @param outputUri   拍照后图片的存储路径
     * @param requestCode
     * @return
     */
    @Deprecated
    public static void pickImageCrop(Activity activity, Uri outputUri, int requestCode, int aspectX, int aspectY, int outputX, int outputY) {
        // Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        Intent intent = new Intent();
        // 根据版本号不同使用不同的Action
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        intent.putExtra("crop", "true");
        // 裁剪框比例
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        // 图片输出大小
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true); // 部分机型没有设置该参数截图会有黑边
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);

        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        // 不启用人脸识别
        intent.putExtra("noFaceDetection", false);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 扫描某张指定的图片放入系统媒体库
     */
    public static void scannerImage(Activity activity, final Uri fileUri, final ScannerResult scannerResult) {
        if (fileUri == null) {
            if (scannerResult != null) {
                scannerResult.onResult(false);
            }
            return;
        }
        sMediaScannerConnection = new MediaScannerConnection(activity, new MediaScannerConnectionClient() {
            public void onMediaScannerConnected() {
                sMediaScannerConnection.scanFile(fileUri.getPath(), "image/*");
            }

            public void onScanCompleted(String path, Uri uri) {
                sMediaScannerConnection.disconnect();
                if (scannerResult != null) {
                    scannerResult.onResult(uri != null);
                }
            }
        });
        sMediaScannerConnection.connect();
    }

    /**
     * 查询某张图片有没有被扫描到媒体库
     *
     * @param context
     * @param filePath
     * @return 返回这个图片在媒体库的Uri，如果没有扫描到媒体库，则返回null
     */
    public static Uri isImageFileInMedia(Context context, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Images.Media.DISPLAY_NAME + "='" + file.getName() + "'", null, null);
        Uri uri = null;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToLast();
            long id = cursor.getLong(0);
            uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        }
        return uri;
    }

    /**
     * @param path
     * @return void
     * @Title: setPictureDegreeZero
     */
    public static void setPictureDegreeZero(String path) {
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            // 修正图片的旋转角度，设置其不旋转。这里也可以设置其旋转的角度，可以传值过去，
            // 例如旋转90度，传值ExifInterface.ORIENTATION_ROTATE_90，需要将这个值转换为String类型的
            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, "no");
            exifInterface.saveAttributes();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 旋转bitmap
     */
    public static Bitmap restoreRotatedImage(int degrees, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return bitmap;
    }


    /**
     * 翻转bitmap (-1,1)左右翻转  (1,-1)上下翻转
     */
    public static Bitmap turnCurrentLayer(Bitmap srcBitmap, float sx, float sy) {
        Bitmap cacheBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.ARGB_8888);// 创建缓存像素的位图
        int w = cacheBitmap.getWidth();
        int h = cacheBitmap.getHeight();

        Canvas cv = new Canvas(cacheBitmap);//使用canvas在bitmap上面画像素

        Matrix mMatrix = new Matrix();//使用矩阵 完成图像变换

        mMatrix.postScale(sx, sy);

        Bitmap resultBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, w, h, mMatrix, true);
        cv.drawBitmap(resultBitmap,
                new Rect(0, 0, srcBitmap.getWidth(), srcBitmap.getHeight()),
                new Rect(0, 0, w, h), null);
        return resultBitmap;
    }

    public static interface ScannerResult {
        void onResult(boolean success);
    }

    public static String getFilePathFromURI(Context context, Uri contentUri) {
        File rootDataDir = context.getExternalFilesDir(null);
        String fileName = getFileName(contentUri);
        if (!TextUtils.isEmpty(fileName)) {
            File copyFile = new File(rootDataDir + File.separator + fileName);
            copyFile(context, contentUri, copyFile);
            return copyFile.getAbsolutePath();
        }
        return null;
    }
    public static String getFileName(Uri uri) {
        if (uri == null) return null;
        String fileName = null;
        String path = uri.getPath();
        int cut = path.lastIndexOf('/');
        if (cut != -1) {
            fileName = path.substring(cut + 1);
        }
        return fileName;
    }

    public static void copyFile(Context context, Uri srcUri, File dstFile) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return;
            OutputStream outputStream = new FileOutputStream(dstFile);
            copyStream(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int copyStream(InputStream input, OutputStream output) throws Exception, IOException {
        final int BUFFER_SIZE = 1024 * 2;
        byte[] buffer = new byte[BUFFER_SIZE];
        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        BufferedOutputStream out = new BufferedOutputStream(output, BUFFER_SIZE);
        int count = 0, n = 0;
        try {
            while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
                out.write(buffer, 0, n);
                count += n;
            }
            out.flush();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
            }
            try {
                in.close();
            } catch (IOException e) {
            }
        }
        return count;
    }
    public static void gotoCrop(Activity activity,Uri sourceUri,int requestCode) {
        File imageCropFile = createImageFile(activity, true);
        if (imageCropFile != null) {
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            intent.putExtra("crop", "true");

            intent.putExtra("aspectX", 1);    //X方向上的比例
            intent.putExtra("aspectY", 1);    //Y方向上的比例
            intent.putExtra("outputX", 500);  //裁剪区的宽
            intent.putExtra("outputY", 500); //裁剪区的高
            intent.putExtra("scale ", true);  //是否保留比例
            intent.putExtra("return-data", false);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            intent.setDataAndType(sourceUri, "image/*"); //设置数据源
            if (Build.VERSION.SDK_INT >= 28) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            } else {
                Uri imgCropUri = Uri.fromFile(imageCropFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imgCropUri);
            }
            activity.startActivityForResult(intent, requestCode);
        }
    }

    public static File getAppRootDirPath() {
        return MyApplication.getContext().getExternalFilesDir(null).getAbsoluteFile();
    }

    public static File getCurrentFile() {
        return mCurrentFile;
    }

    public static Uri uri;
    public static File mCurrentFile;
    public static File createImageFile(Context context, boolean isCrop) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "";
            if (isCrop) {
                fileName = "IMG_"+timeStamp+"_CROP.jpg";
            } else {
                fileName = "IMG_"+timeStamp+".jpg";
            }
            File rootFile = new File(getAppRootDirPath() + File.separator + "capture");
            if (!rootFile.exists()) {
                rootFile.mkdirs();
            }

            if (Build.VERSION.SDK_INT >= 30) {
                mCurrentFile = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + File.separator + fileName);
                // 通过 MediaStore API 插入file 为了拿到系统裁剪要保存到的uri（因为App没有权限不能访问公共存储空间，需要通过 MediaStore API来操作）
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, mCurrentFile.getAbsolutePath());
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }else {
                mCurrentFile = new File(rootFile.getAbsolutePath() + File.separator + fileName);
            }
            return mCurrentFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}

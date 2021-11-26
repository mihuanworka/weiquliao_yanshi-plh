package com.ydd.yanshi.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.UUID;

/**
 * 获取系统设备信息的工具类
 *
 * @author dty
 */
public class DeviceInfoUtil {

    /* 获取手机唯一序列号 */
    public static String getDeviceId(Context context) {
        StringBuilder sbDeviceId = new StringBuilder();

        //获得设备默认IMEI（>=6.0 需要ReadPhoneState权限）
//        String imei = getIMEI(context);
        //获得AndroidId（无需权限）
        String androidid = getAndroidId(context);
        //获得设备序列号（无需权限）
        String serial = getSERIAL();
        //获得硬件uuid（根据硬件相关属性，生成uuid）（无需权限）
        String uuid = getDeviceUUID().replace("-", "");

        //追加imei
//        if (imei != null && imei.length() > 0) {
//            sbDeviceId.append(imei);
//            sbDeviceId.append("|");
//        }
        //追加androidid
        if (androidid != null && androidid.length() > 0) {
            sbDeviceId.append(androidid);
            sbDeviceId.append("|");
        }
        //追加serial
        if (serial != null && serial.length() > 0) {
            sbDeviceId.append(serial);
            sbDeviceId.append("|");
        }
        //追加硬件uuid
        if (uuid != null && uuid.length() > 0) {
            sbDeviceId.append(uuid);
        }

        //生成SHA1，统一DeviceId长度
        if (sbDeviceId.length() > 0) {
            try {
                byte[] hash = getHashByString(sbDeviceId.toString());
                String sha1 = bytesToHex(hash);
                if (sha1 != null && sha1.length() > 0) {
                    //返回最终的DeviceId
                    return sha1;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        //如果以上硬件标识数据均无法获得，
        //则DeviceId默认使用系统随机数，这样保证DeviceId不为空
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获得设备的AndroidId
     *
     * @param context 上下文
     * @return 设备的AndroidId
     */
    private static String getAndroidId(Context context) {

        try {
            String androidId = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            return androidId;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    /**
     * 获得设备序列号（如：WTK7N16923005607）, 个别设备无法获取
     *
     * @return 设备序列号
     */
    private static String getSERIAL() {
        try {
            return Build.SERIAL;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    /**
     * 获得设备硬件uuid
     * 使用硬件信息，计算出一个随机数
     *
     * @return 设备硬件uuid
     */
    private static String getDeviceUUID() {
        try {
            String dev = "3883756" +
                    Build.BOARD.length() % 10 +//主板信息
                    Build.BRAND.length() % 10 +//系统定制商
                    Build.DEVICE.length() % 10 +//设备参数
                    Build.HARDWARE.length() % 10 +//硬件名称
                    Build.ID.length() % 10 +//修订版本列表
                    Build.MODEL.length() % 10 +//版本即最终用户可见的名称
                    Build.PRODUCT.length() % 10 +//产品的名称
                    Build.SERIAL.length() % 10;//硬件序列号
            return new UUID(dev.hashCode(),
                    Build.SERIAL.hashCode()).toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    /**
     * 取SHA1
     * @param data 数据
     * @return 对应的hash值
     */
    private static byte[] getHashByString(String data)
    {
        try{
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            messageDigest.reset();
            messageDigest.update(data.getBytes("UTF-8"));
            return messageDigest.digest();
        } catch (Exception e){
            return "".getBytes();
        }
    }

    /**
     * 转16进制字符串
     * @param data 数据
     * @return 16进制字符串
     */
    private static String bytesToHex(byte[] data){
        StringBuilder sb = new StringBuilder();
        String stmp;
        for (int n = 0; n < data.length; n++){
            stmp = (Integer.toHexString(data[n] & 0xFF));
            if (stmp.length() == 1)
                sb.append("0");
            sb.append(stmp);
        }
        return sb.toString().toUpperCase(Locale.CHINA);
    }

    /* 获取操作系统版本号 */
    public static String getOsVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /* 获取手机型号 */
    public static String getModel() {
        return android.os.Build.MODEL;
    }

    /* 获取手机厂商 */
    public static String getManufacturers() {
        return android.os.Build.MANUFACTURER;
    }

    /* 获取app的版本信息 */
    public static int getVersionCode(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;// 系统版本号
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getVersionName(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;// 系统版本名
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getPackageName(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.packageName;// 系统版本名
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /*
    判断手机Rom
     */
    public static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
        return line;
    }

    /**
     * 判断 miui emui meizu oppo vivo Rom
     *
     * @return
     */
    public static boolean isMiuiRom() {
        String property = getSystemProperty("ro.miui.ui.version.name");
        return !TextUtils.isEmpty(property);
    }

    public static boolean isEmuiRom() {
        String property = getSystemProperty("ro.build.version.emui");
        return !TextUtils.isEmpty(property);
    }

    public static boolean isMeizuRom() {
        String property = getSystemProperty("ro.build.display.id");
        return property != null && property.toLowerCase().contains("flyme");
    }

    public static boolean isOppoRom() {
        String property = getSystemProperty("ro.build.version.opporom");
        return !TextUtils.isEmpty(property);
    }

    public static boolean isVivoRom() {
        String property = getSystemProperty("ro.vivo.os.version");
        return !TextUtils.isEmpty(property);
    }

    public static boolean is360Rom() {
        return Build.MANUFACTURER != null
                && (Build.MANUFACTURER.toLowerCase().contains("qiku")
                || Build.MANUFACTURER.contains("360"));
    }
}

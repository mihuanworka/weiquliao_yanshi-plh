package com.xuan.xuanhttplibrary.okhttp.http;

import com.google.gson.JsonSyntaxException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by lizheng on 2018/5/11.
 */

public abstract class ApiCallBack<T > implements Callback<T> {
    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        switch (response.code()) {
            case 200:
//                int code = response.body().getCode();
//                if (code == 200) {
                    onSuccess(response.body());
//                } else {
//                    String msg = response.body().getMsg();
//                    onFail(code, msg);
//                }
                break;
            default:
                onFailure(call, new RuntimeException("response error ->" + response.raw().toString()));
//                try {
//                    Response response= okHttpClient.newCall(response).execute();
//                    String result = response.body().string();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                } catch (SocketTimeoutException e) {
////                Log.i(TAG, "ontask rerun: "+e.getCause());
//                    okHttpClient.dispatcher().cancelAll();
//                    okHttpClient.connectionPool().evictAll();
//                    okHttpClient.dispatcher().cancelAll();
//                    okHttpClient.connectionPool().evictAll();
//
//                    //TODO: 重新请求
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                break;
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        String errorMessage = "获取数据失败";
//        String error = "网络异常，请重新链接！";
        if (t instanceof SocketTimeoutException) {
            errorMessage = "服务器响应超时";
        } else if (t instanceof ConnectException) {
            errorMessage = "网络连接异常";
        } else if (t instanceof JsonSyntaxException) {
            errorMessage = "解析数据失败";
        }

//        ToastUtil.showToast(errorMessage);
//        To.showLongToast(errorMessage);
        onFail(-1, errorMessage);
    }

    public abstract void onSuccess(T result);

    public void onFail(int code, String msg) {
    }

    /**
     * 判断LoginActivity是否处于栈顶
     *
     * @return true在栈顶false不在栈顶
     */
   /* private boolean isLoginActivityTop() {
        ActivityManager manager = (ActivityManager) MyApplication.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
        String name = manager.getRunningTasks(1).get(0).topActivity.getClassName();
        return name.equals(LoginActivity.class.getName());
    }*/

}

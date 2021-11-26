package com.xuan.xuanhttplibrary.okhttp.http;


import com.ydd.yanshi.AppConfig;
import com.ydd.yanshi.bean.PayBanBean;
import com.ydd.yanshi.bean.redpacket.NiuniuBean;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface Api {
    public static final String BASE_API = AppConfig.apiUrl;

//    public static final String BASE_API = "http://192.168.0.103:9002/";


//    /**
//     * 退出登录
//     *
//     * @param userId
//     * @return
//     */
//    @FormUrlEncoded
//    @POST("user/layout")
//    Call<BaseResponse> loginOut(@Field("uid") int userId);

    @FormUrlEncoded
    @POST("niuniu/sendXiazhu")
    Call<NiuniuBean> sendXiazhu(@Field("fromUserId") String fromUserId,@Field("content") String content,@Field("jid") String jid, @Field("atid") String atIds,@Field("atname") String atNames);

//    http://47.105.137.238:9002/unionpay/consume
    @FormUrlEncoded
    @POST("unionpay/consume")
    Call<PayBanBean> sendzhifu(@Field("amount") String amount, @Field("userId") String userId);

//    //用户信息
//    @GET("user/info/{id}")
//    Call<BaseResponse<UserResult>> userInfo(@Path("id") int id);



}

package com.orangepeople.movies.orangepeopletv.Interface;

import com.orangepeople.movies.orangepeopletv.Model.LiveBean;

import java.util.ArrayList;
import java.util.List;

/**
 * ========================================
 * <p/>
 * 版 权：dou361.com 版权所有 （C） 2015
 * <p/>
 * 作 者：陈冠明
 * <p/>
 * 个人网站：http://www.dou361.com
 * <p/>
 * 版 本：1.0
 * <p/>
 * 创建日期：2016/3/7
 * <p/>
 * 描 述：具体的网络接口请求，分阻塞线程和异步线程
 * <p/>
 * <p/>
 * 修订历史：
 * <p/>
 * ========================================
 */
public class ApiServiceUtils {

    public static List<LiveBean> getLiveList() {
        List<LiveBean> list = new ArrayList<>();
//        OkHttpClient client = new OkHttpClient
//                .Builder()
//                .connectTimeout(30, TimeUnit.SECONDS)
//                .readTimeout(30, TimeUnit.SECONDS)
//                .build();
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://apikg.kktv1.com:8080")
//                .client(client)
//                .addConverterFactory(ScalarsConverterFactory.create())
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//        ApiService mApiServicePresenter = retrofit.create(ApiService.class);
//        Call<String> call = mApiServicePresenter.live("{\"platform\":2,\"count\":2,\"start\":0,\"c\":90013,\"FuncTag\":80010001,\"a\":10}");
//        Response<String> response = null;
//        try {
//            response = call.execute();
//            String body = response.body();
//            JSONObject js = new JSONObject(body);
//            if (body != null) {
//                List<LiveBean> temp = JSON.parseArray(js.getJSONArray("roomList").toString(), LiveBean.class);
//                list.addAll(temp);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return list;
    }

}

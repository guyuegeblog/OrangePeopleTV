package com.orangepeople.movies.orangepeopletv.UIAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.orangepeople.movies.orangepeopletv.Activity.SuperVideoDetailsActivity;
import com.orangepeople.movies.orangepeopletv.Constant.Constant;
import com.orangepeople.movies.orangepeopletv.Interface.AlertInterface;
import com.orangepeople.movies.orangepeopletv.Model.BigVideoInfo;
import com.orangepeople.movies.orangepeopletv.Model.DoInfo;
import com.orangepeople.movies.orangepeopletv.Model.VideoInfo;
import com.orangepeople.movies.orangepeopletv.R;
import com.orangepeople.movies.orangepeopletv.Save.KeyFile;
import com.orangepeople.movies.orangepeopletv.Tool.VipTool;
import com.orangepeople.movies.orangepeopletv.Utils.AesUtils;
import com.orangepeople.movies.orangepeopletv.Utils.T;
import com.orangepeople.movies.orangepeopletv.Utils.Util;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/6/7.
 */
public class Jar_Content_Adapter extends BaseAdapter {
    private List<BigVideoInfo> list = new ArrayList<>();
    private AlertInterface alertInterface;
    private LayoutInflater inflater;
    private Activity context;
    private Util util;


    public void setAlertInterface(AlertInterface alertInterface) {
        this.alertInterface = alertInterface;
    }

    public Jar_Content_Adapter(Activity context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        util = Util.getUtils(context);
    }

    public List<BigVideoInfo> getList() {
        return list;
    }

    public void setList(List<BigVideoInfo> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ThreeViewHolder indexViewHolder = null;
        if (convertView == null) {
            indexViewHolder = new ThreeViewHolder();
            convertView = inflater.inflate(R.layout.three_item, null);
            indexViewHolder.big_images = (ImageView) convertView.findViewById(R.id.three_images);
            indexViewHolder.look = (ImageView) convertView.findViewById(R.id.look);
            indexViewHolder.big_video_name = (TextView) convertView.findViewById(R.id.three_video_name);
            convertView.setTag(indexViewHolder);
        } else {
            indexViewHolder = (ThreeViewHolder) convertView.getTag();
        }
        setDataToUI(convertView, indexViewHolder, parent, position);
        return convertView;
    }

    private void setDataToUI(View convertView, ThreeViewHolder indexViewHolder, ViewGroup parent, final int position) {

        if (list.size() > 18 && position < list.size() - 18) {
            return;
        }
        final BigVideoInfo info = list.get(position);
        indexViewHolder.big_video_name.setText(info.getName());
        TextPaint tp = indexViewHolder.big_video_name.getPaint();
        tp.setFakeBoldText(true);
        if (info.getIsLook().equals("1")) {
            //试看
            indexViewHolder.look.setImageResource(R.mipmap.look_at);
        } else if (info.getIsLook().equals("2")) {
            //vip
            indexViewHolder.look.setImageResource(R.mipmap.look_at);
        }
        Glide.with(context).
                load(info.getPic()).
                placeholder(R.mipmap.prait_loading)
                .override(180, 180)
                .error(R.mipmap.prait_error_images).
                into(indexViewHolder.big_images);
        indexViewHolder.big_images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserDoData("9", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
                AesUtils aesUtils = new AesUtils();
                String nickNameStr = util.sharedPreferencesReadData(context, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "nickName");
                String usernameStr = util.sharedPreferencesReadData(context, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "userName");
                String passWordStr = util.sharedPreferencesReadData(context, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "passWord");
                String showStr = util.sharedPreferencesReadData(context, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "show");
                String vipStatus = aesUtils.decrypt(util.sharedPreferencesReadData(context, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "vip_status"));
                String vipLastTime = aesUtils.decrypt(util.sharedPreferencesReadData(context, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "vip_lastTme"));
                if (TextUtils.isEmpty(showStr) || TextUtils.isEmpty(nickNameStr)
                        || TextUtils.isEmpty(usernameStr) ||
                        TextUtils.isEmpty(passWordStr) || TextUtils.isEmpty(vipStatus)
                        || TextUtils.isEmpty(vipLastTime)) {
                    new T().centershow(context, "~请到会员菜单进行账户登录哦~", 100);
                    return;
                }
                if (!VipTool.judgeIsThanSendTvTime()) {
                    doPlay(position);
                } else {
                    String result = util.compareTime(new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()), vipLastTime);

                    //是会员
                    if (vipStatus.equals("1")) {
                        //查看vip会员时间是否过期
                        //result  1过期 2未过期
                        if (result.equals("2")) {
                            ////继续执行
                            doPlay(position);
                        } else if (result.equals("1")) {
                            if (!context.isFinishing()) {
                                alertInterface.payClick(view);
                            }
                        }
                        //不是会员
                    } else if (vipStatus.equals("2")) {
                        //查看普通用户试用时间是否过期(本地控制)
                        //result  1过期 2未过期
                        File file = new File(Constant.TV_SHIYONG_MP4_ALL);
                        if (!file.exists()) {
                            //mp4(試用)
                            Util.createFile(Constant.TV_SHIYONG_MP4_ALL);
                            //继续执行
                            doPlay(position);
                        } else {
                            //有文件
                            try {
                                String oldTime = aesUtils.decrypt(Util.readFileToSDFile(Constant.TV_SHIYONG_MP4_ALL));
                                if (TextUtils.isEmpty(oldTime)) {
                                    doPlay(position);
                                } else {
                                    if (Integer.parseInt(oldTime) > Constant.doDate) {
                                        //试用过期
                                        alertInterface.payClick(view);
                                    } else {
                                        //没有过期
                                        doPlay(position);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            }
        });
    }


    public class ThreeViewHolder {
        ImageView look;
        ImageView big_images;
        TextView big_video_name;
    }

    public void doPlay(int position) {
        Intent intent = new Intent(context, SuperVideoDetailsActivity.class);
        Bundle bundle = new Bundle();
        BigVideoInfo threeVideoInfo = list.get(position);
        VideoInfo videoInfo = new VideoInfo();
        videoInfo.setAddress_hd(threeVideoInfo.getAddress_hd());
        videoInfo.setAddress_sd(threeVideoInfo.getAddress_sd());
        videoInfo.setName(threeVideoInfo.getName());
        bundle.putSerializable("videoInfo", videoInfo);
        intent.putExtras(bundle);
        intent.putExtra("isLive", false);
        context.startActivity(intent);
    }

    private void sendUserDoData(String type, String oprationtime) {
        //发起请求
        DoInfo data = new DoInfo();
        data.setUserName(util.getAndroidId(context));
        data.setType(type);
        data.setOperationTime(oprationtime);
        String json = com.alibaba.fastjson.JSONObject.toJSONString(data);
        String aesJson = new AesUtils().encrypt(json);
        RequestParams params = new RequestParams(Constant.USER_DO_INTERFACE);
        params.setCacheMaxAge(0);//最大数据缓存时间
        params.setConnectTimeout(5000);//连接超时时间
        params.setCharset("UTF-8");
        params.addQueryStringParameter("data", aesJson);

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }
}

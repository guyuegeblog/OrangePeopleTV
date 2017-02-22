package com.orangepeople.movies.orangepeopletv.UIAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
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
import com.orangepeople.movies.orangepeopletv.Model.SearchModel;
import com.orangepeople.movies.orangepeopletv.Model.VideoInfo;
import com.orangepeople.movies.orangepeopletv.R;
import com.orangepeople.movies.orangepeopletv.Save.KeyFile;
import com.orangepeople.movies.orangepeopletv.Tool.VipTool;
import com.orangepeople.movies.orangepeopletv.Utils.AesUtils;
import com.orangepeople.movies.orangepeopletv.Utils.T;
import com.orangepeople.movies.orangepeopletv.Utils.Util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/6/7.
 */
public class Search_Result_Adapter extends BaseAdapter {
    private List<SearchModel> list = new ArrayList<>();
    private LayoutInflater inflater;
    private AlertInterface alertInterface;
    private Activity context;
    private Util util;


    public void setAlertInterface(AlertInterface alertInterface) {
        this.alertInterface = alertInterface;
    }

    public Search_Result_Adapter(Activity context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        util = new Util(context);
    }

    public List<SearchModel> getList() {
        return list;
    }

    public void setList(List<SearchModel> list) {
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
            indexViewHolder.three_images = (ImageView) convertView.findViewById(R.id.three_images);
            indexViewHolder.look = (ImageView) convertView.findViewById(R.id.look);
            indexViewHolder.three_video_name = (TextView) convertView.findViewById(R.id.three_video_name);
            convertView.setTag(indexViewHolder);
        } else {
            indexViewHolder = (ThreeViewHolder) convertView.getTag();
        }
        setDataToUI(convertView, indexViewHolder, parent, position);
        return convertView;
    }

    private void setDataToUI(View convertView, ThreeViewHolder indexViewHolder, ViewGroup parent, final int position) {
        final SearchModel info = list.get(position);

        indexViewHolder.three_video_name.setText(info.getName());
        TextPaint tp = indexViewHolder.three_video_name.getPaint();
        tp.setFakeBoldText(true);
        if (info.getIsLook().equals("1")) {
            //试看
            indexViewHolder.look.setImageResource(R.mipmap.look_at);
        } else if (info.getIsLook().equals("2")) {
            //vip
            indexViewHolder.look.setImageResource(R.mipmap.look_at);
        }
        Glide.with(context).
                load(info.getPic())
                .override(180, 180).
                placeholder(R.mipmap.prait_loading).
                error(R.mipmap.prait_error_images).into(indexViewHolder.three_images);
        indexViewHolder.three_images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    public void doPlay(int position) {
        Intent intent = new Intent(context, SuperVideoDetailsActivity.class);
        Bundle bundle = new Bundle();
        SearchModel threeVideoInfo = list.get(position);
        VideoInfo videoInfo = new VideoInfo();
        videoInfo.setAddress_hd(threeVideoInfo.getAddress_hd());
        videoInfo.setAddress_sd(threeVideoInfo.getAddress_sd());
        videoInfo.setName(threeVideoInfo.getName());
        bundle.putSerializable("videoInfo", videoInfo);
        intent.putExtras(bundle);
        intent.putExtra("isLive", false);
        context.startActivity(intent);
    }

    public class ThreeViewHolder {
        ImageView look;
        ImageView three_images;
        TextView three_video_name;
    }
}

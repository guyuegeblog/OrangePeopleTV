package com.orangepeople.movies.orangepeopletv.UIAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.orangepeople.movies.orangepeopletv.Activity.SuperVideoDetailsActivity;
import com.orangepeople.movies.orangepeopletv.Constant.Constant;
import com.orangepeople.movies.orangepeopletv.Interface.AlertInterface;
import com.orangepeople.movies.orangepeopletv.Model.DoInfo;
import com.orangepeople.movies.orangepeopletv.Model.LiveInfo;
import com.orangepeople.movies.orangepeopletv.Model.Live_Id;
import com.orangepeople.movies.orangepeopletv.Model.ProgramInfo;
import com.orangepeople.movies.orangepeopletv.Model.ProgramList;
import com.orangepeople.movies.orangepeopletv.R;
import com.orangepeople.movies.orangepeopletv.Save.KeyFile;
import com.orangepeople.movies.orangepeopletv.Tool.VipTool;
import com.orangepeople.movies.orangepeopletv.Utils.AesUtils;
import com.orangepeople.movies.orangepeopletv.Utils.MobClick;
import com.orangepeople.movies.orangepeopletv.Utils.T;
import com.orangepeople.movies.orangepeopletv.Utils.Util;
import com.orangepeople.movies.orangepeopletv.View.SelfGridView;
import com.umeng.analytics.MobclickAgent;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NumberedAdapter extends RecyclerView.Adapter<NumberedAdapter.LiveViewHolder> {
    private List<LiveInfo> list = new ArrayList<>();
    private Activity context;
    private Util util;
    private AesUtils aesUtils;
    private boolean isShow = false;
    private AlertInterface alertInterface;

    public void setAlertInterface(AlertInterface alertInterface) {
        this.alertInterface = alertInterface;
    }

    /**
     * Handler
     */


    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            }
        }
    };


    public NumberedAdapter(Activity activity) {
        this.context = activity;
        util = new Util(context);
        aesUtils = new AesUtils();
    }

    public List<LiveInfo> getList() {
        return list;
    }

    public void setList(List<LiveInfo> list) {
        this.list = list;
    }


    /**
     * 、使用ViewHolder
     */
    class LiveViewHolder extends RecyclerView.ViewHolder {

        public ImageView look;
        public ImageView index_images, right_iv, play, hide_return, sd;
        public TextView sort, tv_description;
        public SelfGridView tv_list_grid;
        public ProgressBar more_progress;


        public LiveViewHolder(View itemView) {
            super(itemView);
            index_images = (ImageView) itemView.findViewById(R.id.index_images);
            sd = (ImageView) itemView.findViewById(R.id.sd);
            play = (ImageView) itemView.findViewById(R.id.play);
            look = (ImageView) itemView.findViewById(R.id.look);
            hide_return = (ImageView) itemView.findViewById(R.id.hide_return);
            sort = (TextView) itemView.findViewById(R.id.sort);
            right_iv = (ImageView) itemView.findViewById(R.id.right_iv);
            tv_list_grid = (SelfGridView) itemView.findViewById(R.id.tv_list_grid);
            more_progress = (ProgressBar) itemView.findViewById(R.id.more_progress);
            tv_description = (TextView) itemView.findViewById(R.id.tv_description);
        }
    }

    @Override
    public LiveViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.index_item, parent, false);
        LiveViewHolder holder = new LiveViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final LiveViewHolder holder, final int position) {
        final LiveInfo info = list.get(position);
        holder.sort.setText(position + 1 + "");
        if (info.getIsLook().equals("1")) {
            //试看
            holder.right_iv.setImageResource(R.mipmap.righit_look);
        } else if (info.getIsLook().equals("2")) {
            //vip
            holder.right_iv.setImageResource(R.mipmap.right_vip);
        }

        holder.tv_description.setText(info.getRemarks());

        Glide.with(context).
                load(info.getClientPic_Url())
                .diskCacheStrategy(DiskCacheStrategy.NONE).
                skipMemoryCache(true)
                .override(300, 300).
                placeholder(R.mipmap.screen_loading).
                error(R.mipmap.screen_error_images).into(holder.index_images);
        if (position == 0) {
            holder.sd.setVisibility(View.VISIBLE);
        } else {
            holder.sd.setVisibility(View.GONE);
        }
        holder.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (position == 0) {
                    MobclickAgent.onEvent(context, MobClick.Live1);//埋点统计
                } else if (position == 1) {
                    MobclickAgent.onEvent(context, MobClick.Live2);//埋点统计
                } else if (position == 2) {
                    MobclickAgent.onEvent(context, MobClick.Live3);//埋点统计
                } else if (position == 3) {
                    MobclickAgent.onEvent(context, MobClick.Live4);//埋点统计
                }

                String usernameStr = util.sharedPreferencesReadData(context, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "userName");
                String passWordStr = util.sharedPreferencesReadData(context, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "passWord");
                String showStr = util.sharedPreferencesReadData(context, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "show");
                String USER_VIP_STATUS = new AesUtils().decrypt(util.sharedPreferencesReadData(context, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "vip_status"));
                if (TextUtils.isEmpty(showStr) || TextUtils.isEmpty(usernameStr) || TextUtils.isEmpty(passWordStr) || TextUtils.isEmpty(USER_VIP_STATUS)) {
                    new T().centershow(context, "请您到会员菜单进行登录。", 10);
                    return;
                } else {
                    if (USER_VIP_STATUS.equals("1")) {
                        //会员
                        if (info.getIsLook().equals("1")) {
                            //试看
                            Intent liveIntent = new Intent(context, SuperVideoDetailsActivity.class);
                            liveIntent.putExtra("isLive", true);
                            liveIntent.putExtra("url", info.getLive_Url());
                            liveIntent.putExtra("title", info.getTv_name());
                            context.startActivity(liveIntent);
                        } else if (info.getIsLook().equals("2")) {
                            //vip
                            new T().centershow(context, "频道维护升级中...", 10);
                        }
                    } else if (!VipTool.judgeIsThanSendTvTime()) {
                        if (info.getIsLook().equals("1")) {
                            //试看
                            Intent liveIntent = new Intent(context, SuperVideoDetailsActivity.class);
                            liveIntent.putExtra("isLive", true);
                            liveIntent.putExtra("url", info.getLive_Url());
                            liveIntent.putExtra("title", info.getTv_name());
                            context.startActivity(liveIntent);
                        } else if (info.getIsLook().equals("2")) {
                            //vip
                            new T().centershow(context, "频道维护升级中...", 10);
                        }

                    } else if (VipTool.judgeIsThanSendTvTime()) {
                        //不是会员
                        if (info.getIsLook().equals("1")) {
                            //试看
                            File file = new File(Constant.TV_SHIYONG_M3U8_ALL);
                            if (!file.exists()) {
                                //继续执行
                                doPlay(info);
                            } else {
                                try {
                                    String oldTime = aesUtils.decrypt(Util.readFileToSDFile(Constant.TV_SHIYONG_M3U8_ALL));
                                    if (TextUtils.isEmpty(oldTime)) {
                                    } else {
                                        if (Integer.parseInt(oldTime) > Constant.doDate) {
                                            //试用过期
                                            alertInterface.payClick(view);
                                        } else {
                                            //没有过期
                                            doPlay(info);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (info.getIsLook().equals("2")) {
                            //vip
                            alertInterface.payClick(view);
                        }
                    }
                }
            }
        });

        holder.hide_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doEnvent(holder, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void doEnvent(final LiveViewHolder holder, final int position) {
        if (isShow == false) {
            isShow = true;
            holder.hide_return.setImageResource(R.drawable.hide_show);
            holder.tv_list_grid.setVisibility(View.GONE);
        } else {
            isShow = false;
            holder.hide_return.setImageResource(R.drawable.hide_return);
            holder.tv_list_grid.setVisibility(View.VISIBLE);
            holder.more_progress.setVisibility(View.VISIBLE);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    doData(holder, position);
                }
            }, 1000);
        }
    }

    private void doData(final LiveViewHolder holder, final int position) {
        Live_Id infoLive = new Live_Id();
        infoLive.setLive_id(Integer.parseInt(list.get(position).getId()));
        String json = com.alibaba.fastjson.JSONObject.toJSONString(infoLive);
        String aesJson = aesUtils.encrypt(json);
        RequestParams params = new RequestParams(Constant.TV_JIEMU_LIST);
        params.setCacheMaxAge(0);//最大数据缓存时间
        params.setConnectTimeout(5000);//连接超时时间
        params.setCharset("UTF-8");
        params.addQueryStringParameter("data", aesJson);
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    holder.more_progress.setVisibility(View.GONE);
                    String json = aesUtils.decrypt(result);
                    ProgramList programList = JSONObject.parseObject(json, ProgramList.class);
                    List<ProgramInfo> programInfoList = programList.getProgramJson();
                    ProgramAdapter programAdapter = new ProgramAdapter(context);
                    programAdapter.setList(programInfoList);
                    holder.tv_list_grid.setAdapter(programAdapter);
                    holder.tv_list_grid.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                holder.more_progress.setVisibility(View.GONE);
                Log.i("liveerror", "liveerror" + ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    private void doPlay(LiveInfo info) {
        Intent liveIntentV = new Intent(context, SuperVideoDetailsActivity.class);
        liveIntentV.putExtra("isLive", true);
        liveIntentV.putExtra("url", info.getLive_Url());
        liveIntentV.putExtra("title", info.getTv_name());
        context.startActivity(liveIntentV);
    }

}
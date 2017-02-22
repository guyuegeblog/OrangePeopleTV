package com.orangepeople.movies.orangepeopletv.Activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.github.ybq.android.spinkit.SpinKitView;
import com.orangepeople.movies.orangepeopletv.Constant.Constant;
import com.orangepeople.movies.orangepeopletv.DBManager.DBManager;
import com.orangepeople.movies.orangepeopletv.Interface.AlertInterface;
import com.orangepeople.movies.orangepeopletv.Model.AppData;
import com.orangepeople.movies.orangepeopletv.Model.BigVideo;
import com.orangepeople.movies.orangepeopletv.Model.BigVideoInfo;
import com.orangepeople.movies.orangepeopletv.Model.Comment;
import com.orangepeople.movies.orangepeopletv.Model.CommentInfo;
import com.orangepeople.movies.orangepeopletv.Model.Live;
import com.orangepeople.movies.orangepeopletv.Model.LiveInfo;
import com.orangepeople.movies.orangepeopletv.Model.ThreeVideo;
import com.orangepeople.movies.orangepeopletv.Model.ThreeVideoInfo;
import com.orangepeople.movies.orangepeopletv.R;
import com.orangepeople.movies.orangepeopletv.Thread.DataThread;
import com.orangepeople.movies.orangepeopletv.UIAdapter.Hot_Content_Adapter;
import com.orangepeople.movies.orangepeopletv.UIAdapter.ZuiXin_Content_Adapter;
import com.orangepeople.movies.orangepeopletv.Utils.AesUtils;
import com.orangepeople.movies.orangepeopletv.Utils.T;
import com.orangepeople.movies.orangepeopletv.Utils.Util;
import com.orangepeople.movies.orangepeopletv.View.SelfGridView;
import com.orangepeople.movies.orangepeopletv.View.SystemBarTintManager;
import com.orangepeople.movies.orangepeopletv.wxapi.WXPayEntryActivity;
import com.umeng.analytics.MobclickAgent;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ThreeActivity extends AppCompatActivity implements AlertInterface {

    private SwipeRefreshLayout three_swiperefreshlayout;
    private RecyclerView three_recyclerview;
    private SpinKitView progress;
    private TextView orange_tv, jarpan_video, three_video, user_vip;
    private LinearLayoutManager mLinearLayoutManager;
    private int lastVisibleItemPosition;
    private boolean isLoading = true;
    private AesUtils aesUtils = new AesUtils();
    private DBManager dbManager = DBManager.getDBManager(this);
    private Map<Integer, Object> allDataMap = new HashMap<>();
    private RecyclerViewAdapter adapter = new RecyclerViewAdapter();
    private ZuiXin_Content_Adapter zuixin_adatper;
    private Hot_Content_Adapter hot_adapter;
    private Activity context;
    private T t;
    private boolean isEmpty = false;

    /**
     * Handler
     */
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 123456:
                    startNotifyRequestData();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatus();
        setContentView(R.layout.activity_three);
        context = this;
        initView();
        initData();
    }

    private void initStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.orange_page);//通知栏所需颜色
        }
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    private void initView() {
        three_swiperefreshlayout = (SwipeRefreshLayout) findViewById(R.id.three_swiperefreshlayout);
        three_recyclerview = (RecyclerView) findViewById(R.id.three_recyclerview);
        progress = (SpinKitView) findViewById(R.id.progress);

        orange_tv = (TextView) findViewById(R.id.orange_tv);
        user_vip = (TextView) findViewById(R.id.user_vip);
        three_video = (TextView) findViewById(R.id.three_video);
        jarpan_video = (TextView) findViewById(R.id.jarpan_video);

        orange_tv.setBackgroundResource(R.mipmap.orange_tv);
        jarpan_video.setBackgroundResource(R.mipmap.jarpan_video);
        three_video.setBackgroundResource(R.mipmap.three_video_select);
        user_vip.setBackgroundResource(R.mipmap.user_vip);

        user_vip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ThreeActivity.this, WXPayEntryActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade, R.anim.hold);
            }
        });
        orange_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ThreeActivity.this, LiveActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade, R.anim.hold);
            }
        });
        jarpan_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ThreeActivity.this, JarpanActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade, R.anim.hold);
            }
        });

        progress.setVisibility(View.VISIBLE);
        three_swiperefreshlayout.setColorSchemeResources(android.R.color.holo_red_light, android.R.color.holo_blue_dark, android.R.color.holo_orange_dark, android.R.color.holo_green_light);
        three_swiperefreshlayout.post(new Runnable() {
            @Override
            public void run() {
                three_swiperefreshlayout.setRefreshing(false);
            }
        });
        three_swiperefreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //刷新请求
                progress.setVisibility(View.VISIBLE);
                page = 1;
                pageList.clear();
                getNetData();
            }
        });
        mLinearLayoutManager = new LinearLayoutManager(this);
        three_recyclerview.setHasFixedSize(true);
        three_recyclerview.setLayoutManager(mLinearLayoutManager);
        three_recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.d("test", "StateChanged = " + newState);
                if (lastVisibleItemPosition + 1 == adapter.getItemCount()) {
                    Log.d("test", "loading executed");
                    boolean isRefreshing = three_swiperefreshlayout.isRefreshing();
                    if (isRefreshing) {
                        adapter.notifyItemRemoved(adapter.getItemCount());
                        return;
                    }
                    if (isLoading) {
                        page++;
                        progress.setVisibility(View.GONE);
                        isLoading = false;
                        final List<ThreeVideoInfo> zuixin = dbManager.queryPagerThreeVideo(6, new Util(context).createTranslateRandom());
                        //加载更多....
                        List<ThreeVideoInfo> hot = dbManager.queryPagerThreeVideo(18, page);
                        if (hot == null || hot.size() == 0) {
//                            setToastHideAndShow(context, "没有数据了", 10);
//                            three_swiperefreshlayout.setRefreshing(false);
//                            adapter.notifyItemRemoved(adapter.getItemCount());
//                            progress.setVisibility(View.GONE);
//                            return;
                            isEmpty = true;
                        } else {
                            isEmpty = false;
                        }
                        pageList.addAll(hot);
                        allDataMap.clear();
                        allDataMap.put(0, zuixin);
                        allDataMap.put(1, pageList);
                        adapterData();

                    } else {
                        progress.setVisibility(View.GONE);
                        three_swiperefreshlayout.setRefreshing(false);
                        adapter.notifyItemRemoved(adapter.getItemCount());
                        setToastHideAndShow(context, "没有数据了", 10);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition();
            }
        });
    }

    private void initData() {
        three_recyclerview.setAdapter(adapter);
        if (t == null) {
            t = new T();
        }
        if (TextUtils.isEmpty(Constant.saveData)) {
            getNetData();
        } else {
            doData();
        }
    }

    private void setToastHideAndShow(Activity context, String message, int duration) {
        if (t != null) {
            t.centershow(context, message, duration);
        }
        if (t != null) {
            t.isShow = false;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (t != null) {
                    t.isShow = true;
                }
            }
        }, 5000);
    }


    private int page = 1;

    private List<ThreeVideoInfo> pageList = new ArrayList<>();

    private void getNetData() {
        //发起请求
        AppData data = new AppData();
        //渠道号
        String area = Util.getUtils(this).getAppMetaData(this, "UMENG_CHANNEL");//渠道号
        if (TextUtils.isEmpty(area)) {
            area = "chengrentv1007";//渠道为空，则上传此渠道号
        }
        data.setArea(area);
        String json = com.alibaba.fastjson.JSONObject.toJSONString(data);
        String aesJson = aesUtils.encrypt(json);
        RequestParams params = new RequestParams(Constant.APP_DATA);
        params.setCacheMaxAge(0);//最大数据缓存时间
        params.setConnectTimeout(5000);//连接超时时间
        params.setCharset("UTF-8");
        params.addQueryStringParameter("data", aesJson);
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    Log.i("aesJsonStr", "====================aes start===================");
                    String aesJson = aesUtils.decrypt(result);
                    Log.i("aesJsonStr", "====================aes  end===================");

                    Log.i("aesJsonStr", "====================fast start===================");
                    ThreeVideo threeVideo = JSONObject.parseObject(aesJson, ThreeVideo.class);
                    Comment comment = JSONObject.parseObject(aesJson, Comment.class);
                    BigVideo bigVideo = JSONObject.parseObject(aesJson, BigVideo.class);
                    Live live = JSONObject.parseObject(aesJson, Live.class);

                    List<ThreeVideoInfo> threeVideoInfoList = threeVideo.getThreeJson();
                    List<BigVideoInfo> bigVideoInfoList = bigVideo.getBigJson();
                    List<CommentInfo> commentInfoList = comment.getCommentJson();
                    List<LiveInfo> liveInfoList = live.getLiveJson();

                    if (threeVideoInfoList == null || bigVideoInfoList == null || commentInfoList == null || liveInfoList == null) {
                        new T().centershow(context, "没有获取到数据,下拉刷新试试吧", 50);
                        return;
                    }

                    if (threeVideoInfoList.size() == 0 || bigVideoInfoList.size() == 0 || commentInfoList.size() == 0 || liveInfoList.size() == 0) {
                        new T().centershow(context, "没有获取到数据,下拉刷新试试吧", 50);
                        return;
                    }
                    DataThread dataThread = new DataThread(threeVideoInfoList, bigVideoInfoList, commentInfoList
                            , liveInfoList, mHandler, false, dbManager);
                    dataThread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                isLoading = true;
                three_swiperefreshlayout.setRefreshing(false);
                progress.setVisibility(View.GONE);
                t.centershow(context, "刷新试试", 50);
                Log.i("aesJsonStr", "====================end===================" + ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    private void doData() {
        List<ThreeVideoInfo> zuixin = dbManager.queryPagerThreeVideo(6, new Util(context).createTranslateRandom());
        List<ThreeVideoInfo> hot = dbManager.queryPagerThreeVideo(18, page);
        if (zuixin == null || zuixin.size() == 0 || hot == null || hot.size() == 0) {
            t.centershow(context, "~没有数据了~", 50);
            three_swiperefreshlayout.setRefreshing(false);
            adapter.notifyItemRemoved(adapter.getItemCount());
            progress.setVisibility(View.GONE);
            return;
        }
        pageList.clear();
        pageList.addAll(hot);
        allDataMap.clear();
        allDataMap.put(0, zuixin);
        allDataMap.put(1, pageList);
        adapterData();
    }

    private void startNotifyRequestData() {
        List<ThreeVideoInfo> zuixin = dbManager.queryPagerThreeVideo(6, new Util(context).createTranslateRandom());
        List<ThreeVideoInfo> hot = dbManager.queryPagerThreeVideo(18, page);
        if (zuixin == null || zuixin.size() == 0 || hot == null || hot.size() == 0) {
            t.centershow(context, "~没有数据了~", 50);
            three_swiperefreshlayout.setRefreshing(false);
            adapter.notifyItemRemoved(adapter.getItemCount());
            progress.setVisibility(View.GONE);
            return;
        }
        pageList.addAll(hot);
        allDataMap.clear();
        allDataMap.put(0, zuixin);
        allDataMap.put(1, pageList);
        adapterData();
    }


    private void adapterData() {
        adapter.notifyDataSetChanged();
        three_swiperefreshlayout.setRefreshing(false);
        adapter.notifyItemRemoved(adapter.getItemCount());
        progress.setVisibility(View.GONE);
        isLoading = true;
    }

    /**
     * recyclerView适配器
     */

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
        private static final int TYPE_ZUIXIN = 0;//最新影片
        private static final int TYPE_HOT_TUIJIAN = 1;//热门推荐
        private static final int TYPE_LoadMore = 2;//

        /***
         * 先创建ViewHolder，并分类型
         *
         * @param parent
         * @param viewType
         * @return
         */
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            switch (viewType) {
                case TYPE_ZUIXIN:
                    return onCreateHeaderViewHolder(parent, viewType);
                case TYPE_HOT_TUIJIAN:
                    return onCreateHotViewHolder(parent, viewType);
                case TYPE_LoadMore:
                    return onCreateLoadMoreViewHolder(parent, viewType);
            }
            return null;
        }

        /**
         * 0、最新影片
         *
         * @param parent
         * @param viewType
         * @return
         */
        private RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.zuixin_content, parent, false);
            HeaderViewHolder holder = new HeaderViewHolder(view);
            return holder;
        }

        /**
         * 、使用ViewHolder
         */
        class HeaderViewHolder extends RecyclerView.ViewHolder {

            public SelfGridView zuixin_grid;

            public HeaderViewHolder(View itemView) {
                super(itemView);
                zuixin_grid = (SelfGridView) itemView.findViewById(R.id.zuixin_grid);
            }
        }


        /**
         * 1、热门推荐
         *
         * @param parent
         * @param viewType
         * @return
         */
        private RecyclerView.ViewHolder onCreateHotViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.hot_tuijian_content, parent, false);
            HotViewHolder holder = new HotViewHolder(view);
            return holder;
        }

        /**
         * 、使用ViewHolder
         */
        class HotViewHolder extends RecyclerView.ViewHolder {

            public SelfGridView hot_tuijian_grid;
            public ImageView hot_tuijian;
            public LinearLayout load_more;

            public HotViewHolder(View itemView) {
                super(itemView);
                hot_tuijian_grid = (SelfGridView) itemView.findViewById(R.id.hot_tuijian_grid);
                hot_tuijian = (ImageView) itemView.findViewById(R.id.hot_tuijian);
                load_more = (LinearLayout) itemView.findViewById(R.id.load_more);
            }
        }

        /**
         * 1加载更多
         *
         * @param parent
         * @param viewType
         * @return
         */
        private RecyclerView.ViewHolder onCreateLoadMoreViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.loadmore, parent, false);
            LoadMoreViewHolder holder = new LoadMoreViewHolder(view);
            return holder;
        }

        public class LoadMoreViewHolder extends RecyclerView.ViewHolder {
            public LoadMoreViewHolder(View itemView) {
                super(itemView);
            }
        }

        //绑定视图
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == TYPE_ZUIXIN) {
                onBindHeaderViewHolder((HeaderViewHolder) holder, position);
            } else if (getItemViewType(position) == TYPE_HOT_TUIJIAN) {
                onBindHotViewHolder((HotViewHolder) holder, position);
            }
        }

        @Override
        public void onClick(View view) {
            /****/
        }

        //数据长度
        @Override
        public int getItemCount() {
            return allDataMap.size() == 0 ? 0 : allDataMap.size() + 1;
        }

        int return_Type = 2;

        /**
         * 1设置视图类型
         * 这句话是关键
         * adapter会将此方法的返回值传入onCreateViewHolder
         *
         * @param position
         * @return
         */
        @Override
        public int getItemViewType(int position) {
//            if (allDataMap.size() == 2) {
//                if (position == 0) {
//                    return TYPE_ZUIXIN;
//                } else if (position == 1) {
//                    return TYPE_HOT_TUIJIAN;
//                } else if (position == getItemCount()) {
//                    return TYPE_LoadMore;
//                }
//            }
//            return TYPE_LoadMore;
            if (allDataMap.size() == 2) {
                if (position == 0) {
                    return_Type = TYPE_ZUIXIN;
                } else if (position == 1) {
                    return_Type = TYPE_HOT_TUIJIAN;
                } else if (position == getItemCount()) {
                    return_Type = TYPE_LoadMore;
                }
            }
            return return_Type;
        }

        /**
         * 绑定最新影片内容
         */

        private void onBindHeaderViewHolder(final HeaderViewHolder holder, int position) {
            List<ThreeVideoInfo> threeInfo = (List<ThreeVideoInfo>) allDataMap.get(0);
            if (threeInfo == null || threeInfo.size() == 0) {
                new T().centershow(context, "刷新试试", 500);
                return;
            }
            if (zuixin_adatper == null) {
                zuixin_adatper = new ZuiXin_Content_Adapter(context);
                zuixin_adatper.setAlertInterface(ThreeActivity.this);
                zuixin_adatper.setList(threeInfo);
                holder.zuixin_grid.setAdapter(zuixin_adatper);
            } else {
                zuixin_adatper.setList(threeInfo);
                zuixin_adatper.notifyDataSetChanged();
            }

        }

        /**
         * 绑定热门推荐内容
         */
        private Activity activity = null;

        private void onBindHotViewHolder(final HotViewHolder holder, int position) {
            List<ThreeVideoInfo> hotInfo = (List<ThreeVideoInfo>) allDataMap.get(1);
            if (isEmpty == true) {
                holder.hot_tuijian.setVisibility(View.GONE);
                if (activity == null) {
                    activity = ThreeActivity.this;
                    holder.load_more.setVisibility(View.VISIBLE);
                }

                return;
            }
            if (hotInfo == null || hotInfo.size() == 0) {
                holder.hot_tuijian.setVisibility(View.GONE);
            } else {
                if (hot_adapter == null) {
                    hot_adapter = new Hot_Content_Adapter(context);
                    hot_adapter.setList(hotInfo);
                    hot_adapter.setAlertInterface(ThreeActivity.this);
                    holder.hot_tuijian_grid.setAdapter(hot_adapter);
                    holder.hot_tuijian.setVisibility(View.VISIBLE);
                } else {
                    hot_adapter.setList(hotInfo);
                    hot_adapter.notifyDataSetChanged();
                    holder.hot_tuijian.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void payClick(View view) {
        alertPay(this);
    }


    Dialog dialog_pay;

    public void alertPay(final Activity context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_pay, null);
        //对话框
        //一定是dialog,而非dialog.builder,不然不全屏的情况会发生
        if (dialog_pay == null) {
            dialog_pay = new Dialog(context, R.style.Dialog);
            dialog_pay.show();
            dialog_pay.setCancelable(false);
            Window window = dialog_pay.getWindow();
            window.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = window.getAttributes();
//            layout.getBackground().setAlpha(130);
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
            window.setContentView(layout);

            final ImageView ok = (ImageView) layout.findViewById(R.id.btn_close);
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog_pay.dismiss();
                }
            });

            final ImageView pay = (ImageView) layout.findViewById(R.id.pay);
            pay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog_pay.dismiss();
                    Intent intent = new Intent(context, WXPayEntryActivity.class);
                    context.startActivity(intent);
                    context.overridePendingTransition(R.anim.fade, R.anim.hold);
                }
            });
        } else {
            dialog_pay.show();
        }
    }


    //字体需要的设置
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    /***
     * 友盟统计
     */
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("三级页面"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("三级页面"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(this);
    }

}

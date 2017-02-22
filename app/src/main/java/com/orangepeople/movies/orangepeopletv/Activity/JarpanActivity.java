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
import com.orangepeople.movies.orangepeopletv.Model.DoInfo;
import com.orangepeople.movies.orangepeopletv.Model.Live;
import com.orangepeople.movies.orangepeopletv.Model.LiveInfo;
import com.orangepeople.movies.orangepeopletv.Model.ThreeVideo;
import com.orangepeople.movies.orangepeopletv.Model.ThreeVideoInfo;
import com.orangepeople.movies.orangepeopletv.Model.VideoInfo;
import com.orangepeople.movies.orangepeopletv.R;
import com.orangepeople.movies.orangepeopletv.Save.KeyFile;
import com.orangepeople.movies.orangepeopletv.Thread.DataThread;
import com.orangepeople.movies.orangepeopletv.Tool.VipTool;
import com.orangepeople.movies.orangepeopletv.UIAdapter.Jar_Content_Adapter;
import com.orangepeople.movies.orangepeopletv.Utils.AesUtils;
import com.orangepeople.movies.orangepeopletv.Utils.T;
import com.orangepeople.movies.orangepeopletv.Utils.Util;
import com.orangepeople.movies.orangepeopletv.View.GlideImageLoader;
import com.orangepeople.movies.orangepeopletv.View.SelfGridView;
import com.orangepeople.movies.orangepeopletv.View.SystemBarTintManager;
import com.orangepeople.movies.orangepeopletv.wxapi.WXPayEntryActivity;
import com.umeng.analytics.MobclickAgent;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.listener.OnBannerClickListener;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class JarpanActivity extends AppCompatActivity implements AlertInterface {

    private SwipeRefreshLayout jar_swiperefreshlayout;
    private RecyclerView jar_recyclerview;
    private SpinKitView progress;
    private TextView orange_tv, jarpan_video, three_video, user_vip;
    private LinearLayoutManager mLinearLayoutManager;
    private int lastVisibleItemPosition;
    private boolean isLoading = true;
    private AesUtils aesUtils = new AesUtils();
    private DBManager dbManager;
    private Map<Integer, Object> allDataMap = new HashMap<>();
    private RecyclerViewAdapter adapter = new RecyclerViewAdapter();
    private Jar_Content_Adapter jar_Adapter;
    private Activity context;
    private Util util;
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
                    starNotifyRequestData();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatus();
        setContentView(R.layout.activity_jarpan);
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
        jar_swiperefreshlayout = (SwipeRefreshLayout) findViewById(R.id.jar_swiperefreshlayout);
        jar_recyclerview = (RecyclerView) findViewById(R.id.jar_recyclerview);
        progress = (SpinKitView) findViewById(R.id.progress);

        orange_tv = (TextView) findViewById(R.id.orange_tv);
        user_vip = (TextView) findViewById(R.id.user_vip);
        three_video = (TextView) findViewById(R.id.three_video);
        jarpan_video = (TextView) findViewById(R.id.jarpan_video);

        orange_tv.setBackgroundResource(R.mipmap.orange_tv);
        jarpan_video.setBackgroundResource(R.mipmap.jarpan_video_select);
        three_video.setBackgroundResource(R.mipmap.three_video);
        user_vip.setBackgroundResource(R.mipmap.user_vip);

        user_vip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, WXPayEntryActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade, R.anim.hold);
            }
        });
        orange_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, LiveActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade, R.anim.hold);
            }
        });
        three_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ThreeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade, R.anim.hold);
            }
        });
        progress.setVisibility(View.VISIBLE);
        jar_swiperefreshlayout.setColorSchemeResources(android.R.color.holo_red_light, android.R.color.holo_blue_dark, android.R.color.holo_orange_dark, android.R.color.holo_green_light);
        jar_swiperefreshlayout.post(new Runnable() {
            @Override
            public void run() {
                jar_swiperefreshlayout.setRefreshing(false);
            }
        });
        jar_swiperefreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
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
        jar_recyclerview.setHasFixedSize(true);
        jar_recyclerview.setLayoutManager(mLinearLayoutManager);
        jar_recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.d("test", "StateChanged = " + newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItemPosition + 1 == adapter.getItemCount()) {
                    Log.d("test", "loading executed");
                    boolean isRefreshing = jar_swiperefreshlayout.isRefreshing();
                    if (isRefreshing) {
                        adapter.notifyItemRemoved(adapter.getItemCount());
                        return;
                    }
                    if (isLoading) {
                        Log.i("isLoadingStraaaaaa", "loading ");
                        page++;
                        progress.setVisibility(View.GONE);
                        isLoading = false;
                        List<BigVideoInfo> gallery = dbManager.queryPagerBigVideo(6, new Util(context).createTranslateRandom());
                        //加载更多....
                        List<BigVideoInfo> hot = dbManager.queryPagerBigVideo(18, page);
                        if (hot == null || hot.size() == 0) {
//                            util.showTextToast(context, "没有数据了");
//                            jar_swiperefreshlayout.setRefreshing(false);
//                            adapter.notifyItemRemoved(adapter.getItemCount());
//                            progress.setVisibility(View.GONE);
//                            return;
                            isEmpty = true;
                        } else {
                            isEmpty = false;
                        }
                        pageList.addAll(hot);
                        allDataMap.clear();
                        allDataMap.put(0, gallery);
                        allDataMap.put(1, pageList);
                        adapterData();
                    } else {
                        progress.setVisibility(View.GONE);
                        jar_swiperefreshlayout.setRefreshing(false);
                        adapter.notifyItemRemoved(adapter.getItemCount());
                        util.showTextToast(context, "没有数据了");
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
        dbManager = DBManager.getDBManager(this);
        jar_recyclerview.setAdapter(adapter);
        util = Util.getUtils(this);
        if (TextUtils.isEmpty(Constant.saveData)) {
            getNetData();
        } else {
            doData();
        }
    }

    private int page = 1;

    private List<BigVideoInfo> pageList = new ArrayList<>();

    private void getNetData() {
        //发起请求
        AppData data = new AppData();
        //渠道号
        String area = util.getAppMetaData(this, "UMENG_CHANNEL");//渠道号
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
                jar_swiperefreshlayout.setRefreshing(false);
                progress.setVisibility(View.GONE);
                util.showTextToast(context, "下拉刷新试试");
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
        List<BigVideoInfo> gallery = dbManager.queryPagerBigVideo(6, new Util(context).createTranslateRandom());//轮播画廊
        List<BigVideoInfo> zuixin = dbManager.queryPagerBigVideo(18, page);//最新数据
        if (zuixin == null || zuixin.size() == 0 || gallery == null || gallery.size() == 0) {
            util.showTextToast(context, "没有数据了");
            jar_swiperefreshlayout.setRefreshing(false);
            adapter.notifyItemRemoved(adapter.getItemCount());
            progress.setVisibility(View.GONE);
            return;
        }
        pageList.clear();
        pageList.addAll(zuixin);
        allDataMap.clear();
        allDataMap.put(0, gallery);
        allDataMap.put(1, pageList);
        adapterData();
    }

    private void starNotifyRequestData() {
        List<BigVideoInfo> gallery = dbManager.queryPagerBigVideo(6, new Util(context).createTranslateRandom());
        List<BigVideoInfo> hot = dbManager.queryPagerBigVideo(18, page);
        if (gallery == null || gallery.size() == 0 || hot == null || hot.size() == 0) {
            util.showTextToast(context, "没有数据了");
            jar_swiperefreshlayout.setRefreshing(false);
            adapter.notifyItemRemoved(adapter.getItemCount());
            progress.setVisibility(View.GONE);
            return;
        }
        pageList.addAll(hot);
        allDataMap.clear();
        allDataMap.put(0, gallery);
        allDataMap.put(1, pageList);
        adapterData();
    }


    private void adapterData() {
        adapter.notifyDataSetChanged();
        jar_swiperefreshlayout.setRefreshing(false);
        adapter.notifyItemRemoved(adapter.getItemCount());
        progress.setVisibility(View.GONE);
        isLoading = true;
    }


    /**
     * recyclerView适配器
     */

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
        private static final int TYPE_ZUIXIN = 0;//gallery
        private static final int TYPE_HOT_TUIJIAN = 1;//最新影片
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
         * 0、gallery
         *
         * @param parent
         * @param viewType
         * @return
         */
        private RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.gallery, parent, false);
            HeaderViewHolder holder = new HeaderViewHolder(view);
            return holder;
        }

        /**
         * 、使用ViewHolder
         */
        class HeaderViewHolder extends RecyclerView.ViewHolder {
            public Banner banner;

            public HeaderViewHolder(View itemView) {
                super(itemView);
                banner = (Banner) itemView.findViewById(R.id.banner);
            }
        }


        /**
         * 1、最新影片
         *
         * @param parent
         * @param viewType
         * @return
         */
        private RecyclerView.ViewHolder onCreateHotViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.zuixin_content_jar, parent, false);
            HotViewHolder holder = new HotViewHolder(view);
            return holder;
        }

        /**
         * 、使用ViewHolder
         */
        class HotViewHolder extends RecyclerView.ViewHolder {

            public SelfGridView zuixin_grid;
            public ImageView zuijin_update;
            public LinearLayout load_more;

            public HotViewHolder(View itemView) {
                super(itemView);
                zuixin_grid = (SelfGridView) itemView.findViewById(R.id.zuixin_grid);
                zuijin_update = (ImageView) itemView.findViewById(R.id.zuijin_update);
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
        }

        /**
         * 绑定头部轮播
         */
        private List<BigVideoInfo> bannerList = new ArrayList<>();
        private List<String> titles = null;
        private List<String> imageUrls = null;

        private void onBindHeaderViewHolder(final HeaderViewHolder holder, int position) {
            bannerList = dbManager.queryPagerBigVideo(8, new Util(context).createTranslateRandom());
            if (titles == null) {
                titles = new ArrayList<>();
            }
            if (imageUrls == null) {
                imageUrls = new ArrayList<>();
            }
            titles.clear();
            imageUrls.clear();
            for (int i = 0; i < bannerList.size(); i++) {
                titles.add(i, bannerList.get(i).getName());
            }
            for (int i = 0; i < bannerList.size(); i++) {
                imageUrls.add(i, bannerList.get(i).getPic_heng());
            }
            //轮播逻辑
            //显示圆形指示器和标题
            holder.banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
            //设置标题列表
            holder.banner.setBannerTitles(titles);
            //设置轮播间隔时间 在布局文件中设置了5秒
            holder.banner.setDelayTime(3000);
            //设置动画
            //holder.banner.setBannerAnimation(Transformer.CubeOut);//立体
            holder.banner.setBannerAnimation(com.youth.banner.Transformer.Accordion);//延伸
            //holder.banner.setBannerAnimation(Transformer.BackgroundToForeground);//淡入放大
            //holder.banner.setBannerAnimation(Transformer.CubeIn);
            //holder.banner.setBannerAnimation(Transformer.DepthPage);
            //holder.banner.setBannerAnimation(Transformer.ZoomOutSlide);
            /**
             * 可以选择设置图片网址或者资源文件，默认用Glide加载
             * 如果你想设置默认图片就在xml里设置default_image
             * banner.setImages(images);
             */
            //如果你想用自己项目的图片加载,那么----->自定义图片加载框架
            //设置图片加载器
            holder.banner.setImageLoader(new GlideImageLoader());
            //设置图片集合
            holder.banner.setImages(imageUrls);
            //设置点击事件
            holder.banner.setOnBannerClickListener(new OnBannerClickListener() {
                @Override
                public void OnBannerClick(int position) {
//                    pagerInterface.pagerClick(null, bannerList.get(position - 1).getLink(), bannerList.get(position - 1).getUid());
                    BigVideoInfo threeVideoInfo = bannerList.get(position - 1);
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
                        util.showTextToast(context, "请到会员菜单进行账户登录哦");
                        return;
                    }
                    if (!VipTool.judgeIsThanSendTvTime()) {
                        doPlay(threeVideoInfo);
                    } else {
                        String result = util.compareTime(new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()), vipLastTime);
                        //是会员
                        if (vipStatus.equals("1")) {
                            //查看vip会员时间是否过期
                            //result  1过期 2未过期
                            if (result.equals("2")) {
                                ////继续执行
                                doPlay(threeVideoInfo);
                            } else if (result.equals("1")) {
                                if (!context.isFinishing()) {
                                    alertPay(context);
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
                                doPlay(threeVideoInfo);
                            } else {
                                //有文件
                                try {
                                    String oldTime = aesUtils.decrypt(Util.readFileToSDFile(Constant.TV_SHIYONG_MP4_ALL));
                                    if (TextUtils.isEmpty(oldTime)) {
                                        doPlay(threeVideoInfo);
                                    } else {
                                        if (Integer.parseInt(oldTime) > Constant.doDate) {
                                            //试用过期
                                            alertPay(context);
                                        } else {
                                            //没有过期
                                            doPlay(threeVideoInfo);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    sendUserDoData("9", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
                }
            });
            //banner设置方法全部调用完毕时最后调用
            holder.banner.start();
        }

        private void doPlay(BigVideoInfo threeVideoInfo) {
            Intent intent = new Intent(context, SuperVideoDetailsActivity.class);
            Bundle bundle = new Bundle();
            VideoInfo videoInfo = new VideoInfo();
            videoInfo.setAddress_hd(threeVideoInfo.getAddress_hd());
            videoInfo.setAddress_sd(threeVideoInfo.getAddress_sd());
            videoInfo.setName(threeVideoInfo.getName());
            bundle.putSerializable("videoInfo", videoInfo);
            intent.putExtras(bundle);
            context.startActivity(intent);
        }


        /**
         * 绑定最新影片内容
         */
        private Activity activity = null;

        private void onBindHotViewHolder(HotViewHolder holder, int position) {

            List<BigVideoInfo> bigInfo = (List<BigVideoInfo>) allDataMap.get(1);
            if (isEmpty == true) {
                if (activity == null) {
                    activity = JarpanActivity.this;
                    holder.load_more.setVisibility(View.VISIBLE);
                }
                holder.zuijin_update.setVisibility(View.GONE);
                return;
            }
            if (bigInfo == null || bigInfo.size() == 0) {
                holder.zuijin_update.setVisibility(View.GONE);
            } else {
                if (jar_Adapter == null) {
                    jar_Adapter = new Jar_Content_Adapter(context);
                    jar_Adapter.setAlertInterface(JarpanActivity.this);
                    jar_Adapter.setList(bigInfo);
                    holder.zuixin_grid.setAdapter(jar_Adapter);
                    holder.zuijin_update.setVisibility(View.VISIBLE);
                } else {
                    jar_Adapter.setList(bigInfo);
                    jar_Adapter.notifyDataSetChanged();
                    holder.zuijin_update.setVisibility(View.VISIBLE);
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

    /***
     * 友盟统计
     */
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("大片页面"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("大片页面"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(this);
    }

    //字体需要的设置
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}

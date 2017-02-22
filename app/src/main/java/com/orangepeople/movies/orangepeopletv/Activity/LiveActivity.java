package com.orangepeople.movies.orangepeopletv.Activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.ybq.android.spinkit.SpinKitView;
import com.orangepeople.movies.orangepeopletv.Constant.Constant;
import com.orangepeople.movies.orangepeopletv.DBManager.DBManager;
import com.orangepeople.movies.orangepeopletv.Interface.AlertInterface;
import com.orangepeople.movies.orangepeopletv.Model.Apk;
import com.orangepeople.movies.orangepeopletv.Model.ApkJson;
import com.orangepeople.movies.orangepeopletv.Model.AppData;
import com.orangepeople.movies.orangepeopletv.Model.BigVideo;
import com.orangepeople.movies.orangepeopletv.Model.BigVideoInfo;
import com.orangepeople.movies.orangepeopletv.Model.Comment;
import com.orangepeople.movies.orangepeopletv.Model.CommentInfo;
import com.orangepeople.movies.orangepeopletv.Model.DownUpdate;
import com.orangepeople.movies.orangepeopletv.Model.Live;
import com.orangepeople.movies.orangepeopletv.Model.LiveInfo;
import com.orangepeople.movies.orangepeopletv.Model.PriceInfo;
import com.orangepeople.movies.orangepeopletv.Model.PriceList;
import com.orangepeople.movies.orangepeopletv.Model.ThreeVideo;
import com.orangepeople.movies.orangepeopletv.Model.ThreeVideoInfo;
import com.orangepeople.movies.orangepeopletv.Model.UpdateList;
import com.orangepeople.movies.orangepeopletv.Net.MobClick;
import com.orangepeople.movies.orangepeopletv.Net.OkHttp;
import com.orangepeople.movies.orangepeopletv.R;
import com.orangepeople.movies.orangepeopletv.Save.KeyFile;
import com.orangepeople.movies.orangepeopletv.Service.UpdateService;
import com.orangepeople.movies.orangepeopletv.Thread.BigThread;
import com.orangepeople.movies.orangepeopletv.Thread.CommentThread;
import com.orangepeople.movies.orangepeopletv.Thread.DataThread;
import com.orangepeople.movies.orangepeopletv.Thread.LiveThread;
import com.orangepeople.movies.orangepeopletv.Thread.ThreeThread;
import com.orangepeople.movies.orangepeopletv.Tool.AppTool;
import com.orangepeople.movies.orangepeopletv.UIAdapter.NumberedAdapter;
import com.orangepeople.movies.orangepeopletv.Utils.AesUtils;
import com.orangepeople.movies.orangepeopletv.Utils.T;
import com.orangepeople.movies.orangepeopletv.Utils.Util;
import com.orangepeople.movies.orangepeopletv.View.MarginDecoration;
import com.orangepeople.movies.orangepeopletv.View.SystemBarTintManager;
import com.orangepeople.movies.orangepeopletv.wxapi.WXPayEntryActivity;
import com.umeng.analytics.MobclickAgent;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LiveActivity extends AppCompatActivity implements AlertInterface {
    private SwipeRefreshLayout tv_swiperefreshlayout;
    private RecyclerView tv_recyclerview;
    private SpinKitView progress;
    private TextView orange_tv, jarpan_video, three_video, user_vip;
    private LinearLayoutManager mLinearLayoutManager;
    private int lastVisibleItemPosition;
    private boolean isLoading = true;
    private AesUtils aesUtils = new AesUtils();
    private Map<Integer, Object> allDataMap = new HashMap<>();
    private NumberedAdapter adapter = new NumberedAdapter(this);
    private DBManager dbManager;
    private Activity context;
    private Util util;
    private boolean isBind = false;
    /**
     * Handler
     */
    private final int GO_DOWN_VPN_FAILED = 1056;
    private final int GO_DOWN_VPN_SUCCES = 1089;
    private final int GO_DOWN_VPN_PROGRESS = 1058;
    private final int GO_DOWN_TV_FAILED = 1066;
    private final int GO_DOWN_TV_SUCCES = 1099;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 000:
                    startNotify(handlerList);
                    break;
                case 123456:
                    startNotifyRequestData();
                    break;
                case 666:
                    threadCount = threadCount + 1;
                    if (threeVideo == null)
                        threeVideo = (ThreeVideo) msg.getData().getSerializable("three");
                    if (comment == null)
                        comment = (Comment) msg.getData().getSerializable("comment");
                    if (bigVideo == null)
                        bigVideo = (BigVideo) msg.getData().getSerializable("big");
                    if (live == null) live = (Live) msg.getData().getSerializable("live");
                    if (threadCount == 4) adapData();
                    break;
                case GO_DOWN_VPN_FAILED:
                    MobclickAgent.onEvent(LiveActivity.this, MobClick.DOWNLOAD_VPN_FAILED);//埋点统计
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    apkDownLoad();
                    break;
                case GO_DOWN_VPN_SUCCES:
                    MobclickAgent.onEvent(LiveActivity.this, MobClick.DOWNLOAD_VPN);//埋点统计
                    Log.d("h_bl", "文件下载安装");
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    try {
                        AppTool.installApk(LiveActivity.this, Util.getSDCardPath() + "/" + apkPath);
                    } catch (Exception e) {
                        return;
                    }
                    break;
                case GO_DOWN_TV_FAILED:
                    MobclickAgent.onEvent(LiveActivity.this, MobClick.DOWNLOAD_UPDATE_FAILED);//埋点统计
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    apkDataDownLoad();
                    break;
                case GO_DOWN_TV_SUCCES:
                    MobclickAgent.onEvent(LiveActivity.this, MobClick.DOWNLOAD_UPDATE);//埋点统计
                    Log.d("h_bl", "文件下载安装");
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    try {
                        AppTool.installApk(LiveActivity.this, Util.getSDCardPath() + "/" + apkDataPath);
                    } catch (Exception e) {
                        return;
                    }
                    break;
                case GO_DOWN_VPN_PROGRESS:
                    showProgressDialog(msg.arg1, msg.arg2);
                    break;
            }
        }
    };
    private ServiceConnection updateService = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatus();
        setContentView(R.layout.activity_main);
        context = this;
        initView();
        initData();
        if (!isBind) {
            isBind = bindService(new Intent(this, UpdateService.class), updateService, Context.BIND_AUTO_CREATE);
        }
        registerBoradcastReceiver();
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

        tv_swiperefreshlayout = (SwipeRefreshLayout) findViewById(R.id.tv_swiperefreshlayout);
        tv_recyclerview = (RecyclerView) findViewById(R.id.tv_recyclerview);
        progress = (SpinKitView) findViewById(R.id.progress);

        orange_tv = (TextView) findViewById(R.id.orange_tv);
        user_vip = (TextView) findViewById(R.id.user_vip);
        three_video = (TextView) findViewById(R.id.three_video);
        jarpan_video = (TextView) findViewById(R.id.jarpan_video);

        orange_tv.setBackgroundResource(R.mipmap.orange_tv_select);
        jarpan_video.setBackgroundResource(R.mipmap.jarpan_video);
        three_video.setBackgroundResource(R.mipmap.three_video);
        user_vip.setBackgroundResource(R.mipmap.user_vip);

        user_vip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LiveActivity.this, WXPayEntryActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade, R.anim.hold);
            }
        });
        three_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LiveActivity.this, ThreeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade, R.anim.hold);
            }
        });
        jarpan_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LiveActivity.this, JarpanActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade, R.anim.hold);
            }
        });

        progress.setVisibility(View.VISIBLE);
        tv_swiperefreshlayout.setColorSchemeResources(android.R.color.holo_red_light, android.R.color.holo_blue_dark, android.R.color.holo_orange_dark, android.R.color.holo_green_light);
        tv_swiperefreshlayout.post(new Runnable() {
            @Override
            public void run() {
                tv_swiperefreshlayout.setRefreshing(false);
            }
        });
        tv_swiperefreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //刷新请求
                threadCount = 0;
                pageList.clear();
                tv_swiperefreshlayout.setRefreshing(true);
                Live live = JSONObject.parseObject(util.getFromRaw(context), Live.class);
                List<LiveInfo> liveInfoList = live.getLiveJson();
                startNotify(liveInfoList);
                tv_swiperefreshlayout.setRefreshing(true);
                progress.setVisibility(View.VISIBLE);
                getNetData();
            }
        });
        tv_recyclerview.addItemDecoration(new MarginDecoration(this));
        tv_recyclerview.setHasFixedSize(true);
        tv_recyclerview.setLayoutManager(new GridLayoutManager(this, 1));
        tv_recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.d("test", "StateChanged = " + newState);
                if (lastVisibleItemPosition + 1 == adapter.getItemCount()) {
//                    Log.d("test", "loading executed");
//                    boolean isRefreshing = tv_swiperefreshlayout.isRefreshing();
//                    if (isRefreshing) {
//                        adapter.notifyItemRemoved(adapter.getItemCount());
//                        return;
//                    }
//                    if (isLoading) {
//                        page++;
//                        progress.setVisibility(View.VISIBLE);
//                        isLoading = false;
//                        //加载更多....
//                        List<LiveInfo> pageVideo = dbHelper.getLiveDBHelper().pageQuery(page, 18);
//                        if (pageVideo == null || pageVideo.size() == 0) {
//                            setToastHideAndShow(context, "没有数据了", 10);
//                            tv_swiperefreshlayout.setRefreshing(false);
//                            adapter.notifyItemRemoved(adapter.getItemCount());
//                            progress.setVisibility(View.GONE);
//                            return;
//                        }
//                        pageList.addAll(pageVideo);
//                        allDataMap.clear();
//                        allDataMap.put(0, pageList);
//                        adapterData();
//                    } else {
//                        progress.setVisibility(View.GONE);
//                        tv_swiperefreshlayout.setRefreshing(false);
//                        adapter.notifyItemRemoved(adapter.getItemCount());
//                        setToastHideAndShow(context, "没有数据了", 10);
//                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
//                lastVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition();
            }
        });
    }

    private String intentJson = "";

    private void initData() {
        Util.createFileDir(Constant.TV_SHIYONG_M3U8);
        Util.createFileDir(Constant.TV_SHIYONG_MP4);
        adapter.setAlertInterface(this);
        tv_recyclerview.setAdapter(adapter);
        intentJson = Constant.saveData;
        if (util == null) {
            util = new Util(context);
        }
        dbManager = DBManager.getDBManager(this);
        if (Constant.saveDataSucces) {
            //启动插入成功
            doData(intentJson, false);
        } else {
            if (TextUtils.isEmpty(intentJson)) {
                tv_swiperefreshlayout.setRefreshing(true);
                Live live = JSONObject.parseObject(util.getFromRaw(this), Live.class);
                List<LiveInfo> liveInfoList = live.getLiveJson();
                startNotify(liveInfoList);
                tv_swiperefreshlayout.setRefreshing(true);
                progress.setVisibility(View.VISIBLE);
                getNetData();
            } else {
                doData(intentJson, true);
            }
        }
    }

    private List<LiveInfo> pageList = new ArrayList<>();
    private List<LiveInfo> finLiveInfoList = new ArrayList<>();
    public static Apk apkInfo;
    private int threadCount = 0;
    private ThreeVideo threeVideo;
    private Comment comment;
    private BigVideo bigVideo;
    private Live live;
    private String decodeAesJson;
    private UpdateList downUpdateApk;

    private void getNetData() {
        if (!util.isNetworkConnected(this)) {
            Live live = JSONObject.parseObject(util.getFromRaw(this), Live.class);
            List<LiveInfo> liveInfoList = live.getLiveJson();
            startNotify(liveInfoList);
            return;
        }
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
                    threadCount = 0;
                    decodeAesJson = aesUtils.decrypt(result);
                    ThreeThread threeThread = new ThreeThread(decodeAesJson, mHandler);
                    threeThread.start();

                    CommentThread comThread = new CommentThread(decodeAesJson, mHandler);
                    comThread.start();

                    BigThread bigThread = new BigThread(decodeAesJson, mHandler);
                    bigThread.start();

                    LiveThread liveThread = new LiveThread(decodeAesJson, mHandler);
                    liveThread.start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                progress.setVisibility(View.GONE);
                isLoading = true;
                tv_swiperefreshlayout.setRefreshing(false);
                util.showCenterToast(context, "没有拉取到数据,下拉刷新试试!!" + ex.getMessage());
                Live live = JSONObject.parseObject(util.getFromRaw(context), Live.class);
                List<LiveInfo> liveInfoList = live.getLiveJson();
                startNotify(liveInfoList);
                return;
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    private void adapData() {
        PriceList price = JSON.parseObject(decodeAesJson, PriceList.class);
        ApkJson apkJson = JSON.parseObject(decodeAesJson, ApkJson.class);
        downUpdateApk = JSON.parseObject(decodeAesJson, UpdateList.class);
        if (price == null || threeVideo == null || bigVideo == null || comment == null || live == null || apkJson == null) {
            new T().centershow(LiveActivity.this, "没有获取到数据,下拉刷新试试吧", 50);
            return;
        }
        if (apkJson.getApkJson().size() != 0) apkInfo = apkJson.getApkJson().get(0);
        List<PriceInfo> priceList = price.getPriceJson();
        List<ThreeVideoInfo> threeVideoInfoList = threeVideo.getThreeJson();
        List<BigVideoInfo> bigVideoInfoList = bigVideo.getBigJson();
        List<CommentInfo> commentInfoList = comment.getCommentJson();
        List<LiveInfo> liveInfoList = live.getLiveJson();

        if (threeVideoInfoList == null || bigVideoInfoList == null || commentInfoList == null || liveInfoList == null) {
            new T().centershow(LiveActivity.this, "没有获取到数据,下拉刷新试试吧", 50);
            return;
        }

        if (threeVideoInfoList.size() == 0 || bigVideoInfoList.size() == 0 || commentInfoList.size() == 0 || liveInfoList.size() == 0) {
            new T().centershow(LiveActivity.this, "没有获取到数据,下拉刷新试试吧", 50);
            return;
        }

        Constant.priceInfoList = priceList;
        Constant.saveData = decodeAesJson;
        finLiveInfoList = liveInfoList;
        DataThread dataThread = new DataThread(threeVideoInfoList, bigVideoInfoList, commentInfoList
                , liveInfoList, mHandler, false, dbManager);
        dataThread.start();
    }


    List<LiveInfo> handlerList;

    private void doData(String aesJson, boolean isWrite) {
        ThreeVideo threeVideo = JSONObject.parseObject(aesJson, ThreeVideo.class);
        Comment comment = JSONObject.parseObject(aesJson, Comment.class);
        BigVideo bigVideo = JSONObject.parseObject(aesJson, BigVideo.class);
        Live live = JSONObject.parseObject(aesJson, Live.class);
        ApkJson apkJson = JSON.parseObject(aesJson, ApkJson.class);

        if (apkJson.getApkJson().size() != 0) {
            apkInfo = apkJson.getApkJson().get(0);
        }
        List<ThreeVideoInfo> threeVideoInfoList = threeVideo.getThreeJson();
        List<BigVideoInfo> bigVideoInfoList = bigVideo.getBigJson();
        List<CommentInfo> commentInfoList = comment.getCommentJson();
        List<LiveInfo> liveInfoList = live.getLiveJson();

        if (threeVideoInfoList == null || bigVideoInfoList == null || commentInfoList == null || liveInfoList == null) {
            new T().centershow(LiveActivity.this, "没有获取到数据,下拉刷新试试吧", 50);
            return;
        }

        if (threeVideoInfoList.size() == 0 || bigVideoInfoList.size() == 0 || commentInfoList.size() == 0 || liveInfoList.size() == 0) {
            new T().centershow(LiveActivity.this, "没有获取到数据,下拉刷新试试吧", 50);
            return;
        }
        handlerList = liveInfoList;
        DataThread dataThread = new DataThread(threeVideoInfoList, bigVideoInfoList, commentInfoList
                , liveInfoList, mHandler, true, dbManager);
        dataThread.start();
    }

    private void startNotify(List<LiveInfo> itemList) {
        allDataMap.clear();
        List<LiveInfo> list = new ArrayList<>();
        list.clear();
        for (int i = 0; i < itemList.size(); i++) {
            String clientUrl = itemList.get(i).getClientPic_Url();
            String[] urlStr = clientUrl.split(",");
            if (urlStr.length > 1) {
                String url = urlStr[util.createLiveRandom(1, urlStr.length - 1)];
                itemList.get(i).setClientPic_Url(url.trim());
            } else {
                itemList.get(i).setClientPic_Url(clientUrl.replace(",", "").trim());
            }
            list.add(itemList.get(i));
        }
        pageList.clear();
        pageList.addAll(list);
        allDataMap.put(0, pageList);
        adapterData();
    }


    private void startNotifyRequestData() {
        List<LiveInfo> itemList = finLiveInfoList;
        allDataMap.clear();
        List<LiveInfo> list = new ArrayList<LiveInfo>();
        list.clear();
        pageList.clear();
        for (int i = 0; i < itemList.size(); i++) {
            String clientUrl = itemList.get(i).getClientPic_Url();
            String[] urlStr = clientUrl.split(",");
            if (urlStr.length > 1) {
                String url = urlStr[util.createLiveRandom(1, urlStr.length - 1)];
                itemList.get(i).setClientPic_Url(url.trim());
            } else {
                itemList.get(i).setClientPic_Url(clientUrl.replace(",", "").trim());
            }
            list.add(itemList.get(i));
        }
        pageList.addAll(list);
        allDataMap.put(0, pageList);
        adapterData();
    }

    private void adapterData() {
        adapter.setList((List<LiveInfo>) allDataMap.get(0));
        adapter.notifyDataSetChanged();
        tv_swiperefreshlayout.setRefreshing(false);
        progress.setVisibility(View.GONE);
        isLoading = true;
        getApkUpdate();
        getUpdateData();
    }

    /***
     * 下载apk
     */
    private String apkUpdateUrl = "";

    public void getUpdateData() {
        try {
            if (downUpdateApk != null) {
                DownUpdate downUpdate = downUpdateApk.getPropJson().get(0);
                double newVersion = Double.parseDouble(downUpdate.getVersion());
                double currentVersion = Double.parseDouble(util.getVersion(this));
                if (newVersion > currentVersion) {
                    apkUpdateUrl = downUpdate.getUrl();
                    alert_Dilaog_DownLoad();
                }
            }
        } catch (Exception e) {
        }
    }


    private AlertDialog.Builder dialog_DownLoad;

    public void alert_Dilaog_DownLoad() {
        MobclickAgent.onEvent(LiveActivity.this, MobClick.DOWNLOAD_UPDATE_TAN);//埋点统计
        if (dialog_DownLoad == null) {
            dialog_DownLoad = new AlertDialog.Builder(this);
            dialog_DownLoad.setCancelable(false);
            dialog_DownLoad.setTitle("升级提示");
            dialog_DownLoad.setIcon(android.R.drawable.ic_dialog_info);
            dialog_DownLoad.setMessage("橙人TV有最新的版本,尽快下载吧。");
            dialog_DownLoad.setPositiveButton("马上下载", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    apkDataDownLoad();
                }
            }).show();
        } else {
            dialog_DownLoad.show();
        }
    }

    private String apkDataPath = "tvupdates.apk";

    public void apkDataDownLoad() {
        /***
         * apk文件下载
         */
        if (util.isNetworkConnected(this)) {
            //网络已连接
        } else {
            new T().centershow(this, "世界上最远的距离就是没网", 500);
            return;
        }
        if (TextUtils.isEmpty(apkUpdateUrl)) {
            return;
        }
        File file = new File(Util.getSDCardPath() + "/" + apkDataPath);
        if (file.exists()) {
            Util.deleteFile(file);
        }
        Request request = new Request.Builder().url(apkUpdateUrl).build();
        OkHttp.getInstance().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("h_bl", "文件下载失败");
                mHandler.sendEmptyMessage(GO_DOWN_TV_FAILED);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                String SDPath = Util.getSDCardPath();
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = new File(SDPath, apkDataPath);
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        Log.d("h_bl", "progress=" + progress);
                        Message msg = mHandler.obtainMessage();
                        msg.what = GO_DOWN_VPN_PROGRESS;
                        msg.arg1 = progress;
                        msg.arg2 = (int) total;
                        mHandler.sendMessage(msg);
                    }
                    fos.flush();
                    Log.d("h_bl", "文件下载成功");
                    mHandler.sendEmptyMessage(GO_DOWN_TV_SUCCES);
                } catch (Exception e) {
                    Log.d("h_bl", "文件下载失败");
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                        Log.d("h_bl", "文件下载失败" + e.getMessage());
                    }
                }
            }
        });

    }

    /***
     * 更新apk
     */
    public static String apkUrl = "";

    public void getApkUpdate() {
        if (util.isNetworkConnected(this)) {
            //网络已连接
        } else {
            util.showTextToast(context, "请检查您的网络连接");
            return;
        }
        try {
            ApkJson apkJson = JSON.parseObject(decodeAesJson, ApkJson.class);
            if (apkJson.getApkJson().size() != 0) apkInfo = apkJson.getApkJson().get(0);
        }catch (Exception e){
        }
        try {
            if (apkInfo == null) {
            } else {
                apkUrl = apkInfo.getUrl();
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
                    startActivity(new Intent(context, LoginActivity.class));
                    return;
                }
                //是会员
                if (vipStatus.equals("1")) {
                    //成为会员就下载,非会员第二天下载
                    alert_Dilaog_Max();
                    //不是会员
                } else if (vipStatus.equals("2")) {
                    //查看普通用户试用时间是否过期(本地控制)
                    //result  1过期 2未过期
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    File file = new File(Constant.TV_SHIYONG_ALL);
                    if (!file.exists()) {
                        //没有控制试用vpn的文件(試用)
                        Date newDate = new Date();
                        String date = sdf.format(newDate);
                        Util.createFile(Constant.TV_SHIYONG_ALL);
                        Util.writeFileToSDFile(Constant.TV_SHIYONG_ALL, aesUtils.encrypt(date));
                        //继续执行
                    } else {
                        //有文件
                        try {
                            String oldDate = aesUtils.decrypt(Util.readFileToSDFile(Constant.TV_SHIYONG_ALL));
                            Date newDate = new Date();
                            //指定2小时的试用
                            long[] time = util.getTime(newDate, sdf.parse(oldDate));
                            if (time == null) {
                                alert_Dilaog_Max();
                            }
                            //天数
                            if (time[0] >= 1) {
                                alert_Dilaog_Max();
                            }
                        } catch (Exception e) {
                            alert_Dilaog_Max();
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    private AlertDialog.Builder dialog_Max;

    public void alert_Dilaog_Max() {
        if (!util.appIsExist(context, Constant.vpnVipPackageName)) {
            MobclickAgent.onEvent(LiveActivity.this, MobClick.DOWNLOAD_VPN_TAN);//埋点统计
            if (dialog_Max == null) {
                dialog_Max = new AlertDialog.Builder(this);
                dialog_Max.setCancelable(false);
                dialog_Max.setTitle("消息提示");
                dialog_Max.setIcon(android.R.drawable.ic_dialog_info);
                dialog_Max.setMessage("恭喜您获得VPN免费使用大礼包");
                dialog_Max.setPositiveButton("开始下载", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        apkDownLoad();
                    }
                }).show();

//            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                }
//            })
            } else {
                dialog_Max.show();
            }
        }

    }

    private String apkPath = "tvtovpn.apk";

    public void apkDownLoad() {
        /***
         * apk文件下载
         */
        if (util.isNetworkConnected(this)) {
            //网络已连接
        } else {
            util.showTextToast(context, "世界上最远的距离就是没网");
            return;
        }
        if (TextUtils.isEmpty(apkUrl)) {
            getApkUpdate();
            return;
        }
        File file = new File(Util.getSDCardPath() + "/" + apkPath);
        if (file.exists()) {
            Util.deleteFile(file);
        }
        Request request = new Request.Builder().url(apkUrl).build();
        OkHttp.getInstance().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("h_bl", "文件下载失败");
                mHandler.sendEmptyMessage(GO_DOWN_VPN_FAILED);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                String SDPath = Util.getSDCardPath();
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = new File(SDPath, apkPath);
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        Log.d("h_bl", "progress=" + progress);
                        Message msg = mHandler.obtainMessage();
                        msg.what = GO_DOWN_VPN_PROGRESS;
                        msg.arg1 = progress;
                        msg.arg2 = (int) total;
                        mHandler.sendMessage(msg);
                    }
                    fos.flush();
                    Log.d("h_bl", "文件下载成功");
                    mHandler.sendEmptyMessage(GO_DOWN_VPN_SUCCES);
                } catch (Exception e) {
                    Log.d("h_bl", "文件下载失败");
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                        Log.d("h_bl", "文件下载失败" + e.getMessage());
                    }
                }
            }
        });

    }

    ProgressDialog progressDialog;

    public void showProgressDialog(int current, int total) {
        //改变样式，水平样式的进度条可以显示出当前百分比进度
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setTitle("应用正在下载中，请稍候...");
        }
        //设置进度条最大值
        progressDialog.setProgress(current);
//      progressDialog.setMax(total / 1024 / 1024);
        progressDialog.setProgressNumberFormat(total / 1024 / 1024 + "MB");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    /***
     * 友盟统计
     */
    public void onResume() {
        super.onResume();
        getApkUpdate();
        getUpdateData();
        MobclickAgent.onPageStart("直播首页面"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("直播首页面"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) dbManager.closeDB();
        //注销广播
        this.unregisterReceiver(mBroadcastReceiver);

        //去除绑定服务
        unbindService(updateService);
    }

    //字体需要的设置
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction("updateinterface");
        //注册广播
        registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        int isLook = 0;

        @Override
        public void onReceive(final Context context, final Intent intent) {
            //接受广播做逻辑处理
            Log.i("receiveStr", "接收到广播了，通知更新");
            String action = intent.getAction();
            if (action.equals("updateinterface")) {
                isLook = 0;
                //发起请求
                AppData data = new AppData();
                //渠道号
                String area = util.getAppMetaData(LiveActivity.this, "UMENG_CHANNEL");//渠道号
                if (TextUtils.isEmpty(area)) {
                    area = "chengrentv1007";//渠道为空，则上传此渠道号
                }
                data.setArea(area);
                String json = com.alibaba.fastjson.JSONObject.toJSONString(data);
                String aesJson = aesUtils.encrypt(json);
                RequestParams params = new RequestParams(Constant.APP_DATA);
                params.setCacheMaxAge(0);//最大数据缓存时间
                params.setConnectTimeout(3000);//连接超时时间
                params.setCharset("UTF-8");
                params.addQueryStringParameter("data", aesJson);

                x.http().post(params, new Callback.CommonCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            Log.i("sqliteStrDoGetNet", "sqlite=====开始解析数据");
                            String aesJson = aesUtils.decrypt(result);

                            Live live = JSONObject.parseObject(aesJson, Live.class);


                            List<LiveInfo> list = live.getLiveJson();

                            if (list == null) {
                                new T().centershow(LiveActivity.this, "没有获取到数据,下拉刷新试试吧", 50);
                                return;
                            }

                            if (list.size() == 0) {
                                new T().centershow(LiveActivity.this, "没有获取到数据,下拉刷新试试吧", 50);
                                return;
                            }

                            for (int j = 0; j < list.size(); j++) {

                                if (list.get(j).getIsLook().equals("1")) {
                                    isLook = isLook + 1;
                                }
                            }

                            for (int i = 0; i < list.size(); i++) {
                                // 1~4实时更新数据海报
                                if (i < isLook) {
                                    //更新后处理
                                    LiveInfo liveInfo = list.get(i);
                                    list.get(i).setClientPic_Url(liveInfo.getClientPic_Url().replace(",", "").trim());
                                    adapter.setList(list);
                                    adapter.notifyItemChanged(i);
                                } else {
                                    List<LiveInfo> client_pic = dbManager.queryLiveById(list.get(i).getId());
                                    String clientUrl = client_pic.get(0).getClientPic_Url();
                                    String[] urlStr = clientUrl.split(",");
                                    if (urlStr.length > 1) {
                                        String url = urlStr[util.createLiveRandom(1, urlStr.length - 1)];
                                        list.get(i).setClientPic_Url(url.trim());
                                    } else {
                                        list.get(i).setClientPic_Url(clientUrl.replace(",", "").trim());
                                    }
                                    //更新后处理
                                    adapter.setList(list);
                                    adapter.notifyItemChanged(i);
                                }
                            }

                        } catch (Exception e) {
                            return;
                        }
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
    };

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

    @Override
    public void onBackPressed() {
        alert_Dilaog_Max();
        ExitApp();
    }

    private long exitTime = 0;

    public void ExitApp() {
        if ((System.currentTimeMillis() - exitTime) > 3000) {
            util.showTextToast(this, "再按一次退出程序");
            exitTime = System.currentTimeMillis();
        } else {
            this.finish();
            System.exit(0);//正常退出App
        }
    }
}

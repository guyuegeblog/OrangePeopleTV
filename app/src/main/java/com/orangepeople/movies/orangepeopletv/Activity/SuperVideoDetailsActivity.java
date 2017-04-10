package com.orangepeople.movies.orangepeopletv.Activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.orangepeople.movies.orangepeopletv.Constant.Constant;
import com.orangepeople.movies.orangepeopletv.DBManager.DBManager;
import com.orangepeople.movies.orangepeopletv.Model.AppData;
import com.orangepeople.movies.orangepeopletv.Model.CommentInfo;
import com.orangepeople.movies.orangepeopletv.Model.DoInfo;
import com.orangepeople.movies.orangepeopletv.Model.VideoInfo;
import com.orangepeople.movies.orangepeopletv.R;
import com.orangepeople.movies.orangepeopletv.Save.KeyFile;
import com.orangepeople.movies.orangepeopletv.Thread.BigThread;
import com.orangepeople.movies.orangepeopletv.Thread.CommentThread;
import com.orangepeople.movies.orangepeopletv.Thread.LiveThread;
import com.orangepeople.movies.orangepeopletv.Thread.ThreeThread;
import com.orangepeople.movies.orangepeopletv.Utils.AesUtils;
import com.orangepeople.movies.orangepeopletv.Utils.T;
import com.orangepeople.movies.orangepeopletv.Utils.Util;
import com.orangepeople.movies.orangepeopletv.View.BarrageItem;
import com.orangepeople.movies.orangepeopletv.View.BarrageView;
import com.orangepeople.movies.orangepeopletv.wxapi.WXPayEntryActivity;
import com.superplayer.library.SuperPlayer;

import org.w3c.dom.Text;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 类描述：视频详情页
 *
 * @author Super南仔
 * @time 2016-9-19
 */
public class SuperVideoDetailsActivity extends AppCompatActivity implements View.OnClickListener, SuperPlayer.OnNetChangeListener {

    private SuperPlayer player;
    private boolean isLive;
    private VideoInfo videoInfo;
    private String video_title;
    private Activity mContext;
    private Util util = Util.getUtils(this);
    private AesUtils aesUtils = new AesUtils();
    private List<String> barTexts = new ArrayList<>();

    private Handler videoHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    /**
     * 测试地址
     */
    private String url;
    private BarrageView barrageView;
    private ImageView submit;
    private EditText et_barrage;
    private DBManager dbManager;
    private RelativeLayout barrage_submit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 禁止屏幕休眠
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.video_player);
        mContext = this;
        initData();
        initView();
        initPlayer();
        registerBoradcastReceiver();
    }


    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction("play_video_succes");
        //注册广播
        registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            //接受广播做逻辑处理
            String action = intent.getAction();
            if (action.equals("play_video_succes")) {
                String playContent = intent.getStringExtra("playContent");
                if (playContent.equals("hd")) {
                    if (isLive == true) {
                        //不执行任何代码
                    } else {
                        if (url.equals(videoInfo.getAddress_hd())) {
                            return;
                        }
                        url = videoInfo.getAddress_hd();
                        video_title = videoInfo.getName();
                        player.setTitle(video_title)//设置视频的titleName
                                .play(url, player.getCurrentPosition());//开始播放视频
                        player.setScaleType(SuperPlayer.SCALETYPE_16_9);
                        player.setPlayerWH(0, player.getMeasuredHeight());//设置竖屏的时候屏幕的高度，如果不设置会切换后按照16:9的高度重置
                        choiceVip(mContext, false);
                    }

                } else if (playContent.equals("sd")) {
                    if (isLive == true) {
                        //不执行任何代码
                    } else {
                        if (url.equals(videoInfo.getAddress_sd())) {
                            return;
                        }
                        url = videoInfo.getAddress_sd();
                        video_title = videoInfo.getName();
                        player.setTitle(video_title)//设置视频的titleName
                                .play(url, player.getCurrentPosition());//开始播放视频
                        player.setScaleType(SuperPlayer.SCALETYPE_16_9);
                        player.setPlayerWH(0, player.getMeasuredHeight());//设置竖屏的时候屏幕的高度，如果不设置会切换后按照16:9的高度重置
                        choiceVip(mContext, false);
                    }

                } else if (playContent.equals("dialog_show")) {
                    if (player != null) {
                        player.stop();
                    }
                    alerPayFailTiShi();
                } else if (playContent.equals("activity_finish")) {
                    doPlayData();
                    finish();
                } else if (playContent.equals("playing")) {
                    if (startPlayDate == null) {
                        sendUserDoData("3", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
                    }
                    startPlayDate = new Date();
                } else if (playContent.equals("stop")) {
                    doPlayData();
                } else if (playContent.equals("loading")) {
                    doPlayData();
                } else if (playContent.equals("play_finish")) {
                    doPlayData();
                } else if (playContent.equals("resume_timer")) {
                    resumeTimer();
                }
                //直播处理
                else if (playContent.equals("playing_live")) {
                    if (startPlayDate_live == null) {
                        sendUserDoData("3", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
                    }
                    startPlayDate_live = new Date();
                } else if (playContent.equals("stop_live")) {
                    doPlayData_Live();
                } else if (playContent.equals("loading_live")) {
                    doPlayData_Live();
                } else if (playContent.equals("play_finish_live")) {
                    doPlayData_Live();
                } else if (playContent.equals("resume_timer_live")) {
                    resumeTimer_Live();
                }
                //弹幕
                else if (playContent.equals("barrage_close")) {
                    barrageView.setVisibility(View.GONE);
                    Constant.barra_show = false;
                    barrageView.stopBarrage();
                } else if (playContent.equals("barrage_show")) {
                    barrageView.setVisibility(View.VISIBLE);
                    Constant.barra_show = true;
                    initBarrage();
                    barrageView.startBarrage();

                } else if (playContent.equals("send_barra")) {
                    if (barrage_submit.getVisibility() == View.VISIBLE) {
                        barrage_submit.setVisibility(View.GONE);
                    } else {
                        barrage_submit.setVisibility(View.VISIBLE);
                    }
                } else if (playContent.equals("hide_input")) {
                    barrage_submit.setVisibility(View.GONE);
                }
            }
        }
    };

    private Date startPlayDate_live;
    private Date loadAndStopDate_live;

    private void resumeTimer_Live() {
        String vipStatus = aesUtils.decrypt(util.sharedPreferencesReadData(this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "vip_status"));
        if (!TextUtils.isEmpty(vipStatus)) {
            if (vipStatus.equals("1")) {
                return;
            }
        }
        try {
            String oldTime = aesUtils.decrypt(Util.readFileToSDFile(Constant.TV_SHIYONG_M3U8_ALL));
            if (TextUtils.isEmpty(oldTime)) {
            } else {
                if (Integer.parseInt(oldTime) > Constant.doDate) {
                    //试用过期
                    alerPayFailTiShi();
                    if (player != null) {
                        player.stop();
                    }
                    player.isStartTime = true;
                } else {
                    //没有过期
                    if (player != null) {
                        //player.timeSecond = Constant.doDate - Integer.parseInt(oldTime);
                    }
                    player.isStartTime = true;
                    player.startTime();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resumeTimer() {
        String vipStatus = aesUtils.decrypt(util.sharedPreferencesReadData(this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "vip_status"));
        if (!TextUtils.isEmpty(vipStatus)) {
            if (vipStatus.equals("1")) {
                return;
            }
        }
        try {
            String oldTime = aesUtils.decrypt(Util.readFileToSDFile(Constant.TV_SHIYONG_MP4_ALL));
            if (TextUtils.isEmpty(oldTime)) {
            } else {
                if (Integer.parseInt(oldTime) > Constant.doDate) {
                    //试用过期
                    alerPayFailTiShi();
                    if (player != null) {
                        player.stop();
                    }
                    player.isStartTime = true;
                } else {
                    //没有过期
                    if (player != null) {
                        //player.timeSecond = Constant.doDate - Integer.parseInt(oldTime);
                    }
                    player.isStartTime = true;
                    player.startTime();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doPlayData_Live() {
        String vipStatus = aesUtils.decrypt(util.sharedPreferencesReadData(this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "vip_status"));
        if (!TextUtils.isEmpty(vipStatus)) {
            if (vipStatus.equals("1")) {
                return;
            }
        }
        if (isLive == true) {
            loadAndStopDate_live = new Date();
            File file = new File(Constant.TV_SHIYONG_M3U8_ALL);
            if (!file.exists()) {
                Util.createFile(Constant.TV_SHIYONG_M3U8_ALL);
            }
            String old = Util.readFileToSDFile(Constant.TV_SHIYONG_M3U8_ALL);
            String oldTime = aesUtils.decrypt(old);
            if (TextUtils.isEmpty(oldTime)) {
                //第一次写入(给1个小时)
                if (player != null) {
                    player.isStartTime = true;
                    player.timeSecond = Constant.doDate;
                }
                Util.writeFileToSDFile(Constant.TV_SHIYONG_M3U8_ALL, aesUtils.encrypt("1"));
            } else {
                if (startPlayDate_live != null) {
                    long[] time = util.getTime(loadAndStopDate_live, startPlayDate_live);
                    int minutes = Integer.parseInt(oldTime) + Integer.parseInt(time[3] + "");
                    Util.writeFileToSDFile(Constant.TV_SHIYONG_M3U8_ALL, aesUtils.encrypt(minutes + ""));
                }
            }
        }
    }

    private void doPlayData() {
        String vipStatus = aesUtils.decrypt(util.sharedPreferencesReadData(this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "vip_status"));
        if (!TextUtils.isEmpty(vipStatus)) {
            if (vipStatus.equals("1")) {
                return;
            }
        }
        if (isLive == false) {
            loadAndStopDate = new Date();
            File file = new File(Constant.TV_SHIYONG_MP4_ALL);
            if (!file.exists()) {
                Util.createFile(Constant.TV_SHIYONG_MP4_ALL);
            }
            String old = Util.readFileToSDFile(Constant.TV_SHIYONG_MP4_ALL);
            String oldTime = aesUtils.decrypt(old);
            if (TextUtils.isEmpty(oldTime)) {
                //第一次写入(给1个小时)
                if (player != null) {
                    player.isStartTime = true;
                    player.timeSecond = Constant.doDate;
                }
                Util.writeFileToSDFile(Constant.TV_SHIYONG_MP4_ALL, aesUtils.encrypt("1"));
            } else {
                if (startPlayDate != null) {
                    long[] time = util.getTime(loadAndStopDate, startPlayDate);
                    int minutes = Integer.parseInt(oldTime) + Integer.parseInt(time[3] + "");
                    Util.writeFileToSDFile(Constant.TV_SHIYONG_MP4_ALL, aesUtils.encrypt(minutes + ""));
                }
            }
        }
    }

    private Date startPlayDate;
    private Date loadAndStopDate;

    /**
     * 初始化相关的信息
     */
    private void initData() {
        dbManager = DBManager.getDBManager(this);
        isLive = getIntent().getBooleanExtra("isLive", false);
        if (isLive == true) {
            url = getIntent().getStringExtra("url");
            video_title = getIntent().getStringExtra("title");
        } else {
            videoInfo = (VideoInfo) getIntent().getSerializableExtra("videoInfo");
            url = videoInfo.getAddress_hd();
            video_title = videoInfo.getName();
        }
    }

    /**
     * 初始化视图
     */
    private void initView() {
//        findViewById(R.id.tv_replay).setOnClickListener(this);
//        findViewById(R.id.tv_play_location).setOnClickListener(this);
//        findViewById(R.id.tv_play_switch).setOnClickListener(this);
        barrage_submit = (RelativeLayout) findViewById(R.id.barrage_submit);
        et_barrage = (EditText) findViewById(R.id.et_barrage);
        barrageView = (BarrageView) findViewById(R.id.containerView);
        barrageView.setVisibility(Constant.barra_show ? View.VISIBLE : View.GONE);
        submit = (ImageView) findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               try {
                   String text = et_barrage.getText().toString();
                   barrage_submit.setVisibility(View.GONE);
                   if (TextUtils.isEmpty(text)) {
                       util.showTextToast(mContext, "请输入弹幕内容");
                       return;
                   }
//                barTexts.add(barrageView.currentCount + 1, text);
//
//                barrageView.textCount = barTexts.size();
//                barrageView.setItemText(barTexts);
                   barrageView.initBarrageItem(new BarrageItem(), text);
                   util.showTextToast(mContext, "评论成功!!!");
               }catch (Exception e){
               }
            }
        });
        initBarrage();
    }

    private void initBarrage() {
        barrageView.getShowList().clear();
        List<CommentInfo> commentInfoList = dbManager.queryCommentAll();
        Collections.shuffle(commentInfoList);
        int listSize = 0;
        if (commentInfoList.size() != 0) {
            barrageView.getItemText().clear();
            if (commentInfoList.size() < 10) {
                listSize = commentInfoList.size();
            } else {
                listSize = 10;
            }
            barrageView.textCount = listSize;
            for (int i = 0; i < listSize; i++) {
                barTexts.add(commentInfoList.get(i).getInfo());
            }
        }
        barrageView.setItemText(barTexts);
    }


    /**
     * 初始化播放器
     */
    private void initPlayer() {
        player = (SuperPlayer) findViewById(R.id.view_super_player);
        if (isLive == true) {
            player.setLive(true);//设置该地址是直播的地址
        }
        player.setNetChangeListener(true)//设置监听手机网络的变化
                .setOnNetChangeListener(this)//实现网络变化的回调
                .onPrepared(new SuperPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared() {
                        /**
                         * 监听视频是否已经准备完成开始播放。（可以在这里处理视频封面的显示跟隐藏）
                         */
                    }
                }).onComplete(new Runnable() {
            @Override
            public void run() {
                /**
                 * 监听视频是否已经播放完成了。（可以在这里处理视频播放完成进行的操作）
                 */
                showTextToast(mContext, "播放结束");
                player.stop();
            }
        }).onInfo(new SuperPlayer.OnInfoListener() {
            @Override
            public void onInfo(int what, int extra) {
                /**
                 * 监听视频的相关信息。
                 */
            }
        }).onError(new SuperPlayer.OnErrorListener() {
            @Override
            public void onError(int what, int extra) {
                /**
                 * 监听视频播放失败的回调
                 */
                showTextToast(mContext, "视频出了点小问题");
            }
        }).setTitle(video_title)//设置视频的titleName
                .play(url);//开始播放视频
        player.setScaleType(SuperPlayer.SCALETYPE_16_9);
        player.setPlayerWH(0, player.getMeasuredHeight());//设置竖屏的时候屏幕的高度，如果不设置会切换后按照16:9的高度重置
        if (isLive == true) {
            //直播
            player.tv_zhibo.setVisibility(View.VISIBLE);
            choiceVip(mContext, true);
        } else {
            //不是直播
            player.tv_zhibo.setVisibility(View.GONE);
            videoHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    choiceVip(mContext, false);
                }
            }, 2000);
        }
    }

    @Override
    public void onClick(View view) {
//        if(view.getId() == R.id.tv_replay){
//            if(player != null){
//                player.play(url);
//            }
//        } else if(view.getId() == R.id.tv_play_location){
//            if(isLive){
//                Toast.makeText(this,"直播不支持指定播放",Toast.LENGTH_SHORT).show();
//                return;
//            }
//            if(player != null){
//                /**
//                 * 这个节点是根据视频的大小来获取的。不同的视频源节点也会不一致（一般用在退出视频播放后保存对应视频的节点从而来达到记录播放）
//                 */
//                player.play(url,89528);
//            }
//        } else if(view.getId() == R.id.tv_play_switch) {
//            /**
//             * 切换视频播放源（一般是标清，高清的切换ps：由于我没有找到高清，标清的视频源，所以也是换相同的地址）
//             */
//        if(isLive){
//            player.playSwitch(url);
//        } else {
//            player.playSwitch("http://baobab.wandoujia.com/api/v1/playUrl?vid=2614&editionType=high");
//        }
//        }
    }

    private Toast toast = null;
    private int toastDuration = 10;

    private void showTextToast(Activity activity, String msg) {
        if (toast == null) {
            toast = Toast.makeText(activity, msg, toastDuration);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }

    /**
     * 网络链接监听类
     */
    @Override
    public void onWifi() {
        showTextToast(mContext, "当前网络环境是WIFI");
    }

    @Override
    public void onMobile() {
        showTextToast(mContext, "当前网络环境是手机网络");
    }

    @Override
    public void onDisConnect() {
        showTextToast(mContext, "网络链接断开");
    }

    @Override
    public void onNoAvailable() {
        showTextToast(mContext, "无网络链接");
    }

    /**
     * 下面的这几个Activity的生命状态很重要
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.onPause();
        }
        if (isLive == false) {
            doPlayData();
        }
        if (isLive == true) {
            doPlayData_Live();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.onResume();
        }
        mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isLive) {
            sendUserDoData("4", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        }
        if (player != null) {
            player.onDestroy();
        }
        if (videoHandler != null) {
            videoHandler = null;
        }
        if (barrageView != null) {
            barrageView = null;
        }
        //注销广播
        this.unregisterReceiver(mBroadcastReceiver);
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        if (player != null) {
//            player.onConfigurationChanged(newConfig);
//        }
//    }

    private int PLAY_ISPLAYING = 2;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (player.getStatus() == PLAY_ISPLAYING && isLive == false) {
            doPlayData();
        }
        if (player.getStatus() == PLAY_ISPLAYING && isLive == true) {
            doPlayData_Live();
        }
        finish();
    }

    private void choiceVip(Activity context, boolean isLive) {
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
        if (isLive == true) {
            String result = util.compareTime(new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()), vipLastTime);
            //是会员
            if (vipStatus.equals("1")) {
                //查看vip会员时间是否过期
                //result  1过期 2未过期
                if (result.equals("2")) {
                    //继续执行
                } else if (result.equals("1")) {
                    if (!context.isFinishing()) {
                        if (player != null) {
                            player.stop();
                        }
                        alerPayFailTiShi();
                    }
                }
                //不是会员
            } else if (vipStatus.equals("2")) {
                //查看普通用户试用时间是否过期(本地控制)
                //result  1过期 2未过期
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                File file = new File(Constant.TV_SHIYONG_M3U8_ALL);
                if (!file.exists()) {
                    //没有控制试用vpn的文件(試用)
                    Util.createFile(Constant.TV_SHIYONG_M3U8_ALL);
                    player.isStartTime = true;
                    player.timeSecond = Constant.doDate;
                    //继续执行
                } else {
                    //有文件
                    try {
                        String oldTime = aesUtils.decrypt(Util.readFileToSDFile(Constant.TV_SHIYONG_M3U8_ALL));
                        if (TextUtils.isEmpty(oldTime)) {
                        } else {
                            if (Integer.parseInt(oldTime) > Constant.doDate) {
                                //试用过期
                                alerPayFailTiShi();
                                if (player != null) {
                                    player.stop();
                                }
                                player.isStartTime = true;
                            } else {
                                //没有过期
                                if (player != null) {
                                    player.timeSecond = Constant.doDate - Integer.parseInt(oldTime);
                                }
                                player.isStartTime = true;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            //不是直播
            String result = util.compareTime(new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()), vipLastTime);

            //是会员
            if (vipStatus.equals("1")) {
                //查看vip会员时间是否过期
                //result  1过期 2未过期
                if (result.equals("2")) {
                    //继续执行
                    player.isStartTime = false;
                } else if (result.equals("1")) {
                    if (!context.isFinishing()) {
                        alerPayFailTiShi();
                        if (player != null) {
                            player.stop();
                        }
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
                    player.isStartTime = true;
                    player.timeSecond = Constant.doDate;
                    //继续执行
                } else {
                    //有文件
                    try {
                        String oldTime = aesUtils.decrypt(Util.readFileToSDFile(Constant.TV_SHIYONG_MP4_ALL));
                        if (TextUtils.isEmpty(oldTime)) {
                        } else {
                            if (Integer.parseInt(oldTime) > Constant.doDate) {
                                //试用过期
                                alerPayFailTiShi();
                                if (player != null) {
                                    player.stop();
                                }
                                player.isStartTime = true;
                            } else {
                                //没有过期
                                if (player != null) {
                                    player.timeSecond = Constant.doDate - Integer.parseInt(oldTime);
                                }
                                player.isStartTime = true;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public void alerPayFailTiShi() {
//        new android.support.v7.app.AlertDialog.Builder(this).setTitle("消息提示")
//                .setIcon(android.R.drawable.ic_dialog_info)
//                .setCancelable(false)
//                .setMessage("您的试用期已过期,请立即充值")
//                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // 点击“确认”后的操作
//                        dialog.dismiss();
//                        Intent intent = new Intent(mContext, WXPayEntryActivity.class);
//                        startActivity(intent);
//                        overridePendingTransition(R.anim.fade, R.anim.hold);
//                        mContext.finish();
//                    }
//                }).show();
        alertVipPay(this);
    }

    Dialog dialog_pay_time;

    public void alertVipPay(final Activity context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_vip_time, null);
        //对话框
        //一定是dialog,而非dialog.builder,不然不全屏的情况会发生
        if (dialog_pay_time == null) {
            dialog_pay_time = new Dialog(context, R.style.Dialog);
            dialog_pay_time.show();
            dialog_pay_time.setCancelable(false);
            Window window = dialog_pay_time.getWindow();
            window.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = window.getAttributes();
            layout.getBackground().setAlpha(130);
            lp.width = util.getWidth() / 5 * 3;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
            window.setContentView(layout);

            final ImageView pay = (ImageView) layout.findViewById(R.id.pay);
            pay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog_pay_time.dismiss();
                    Intent intent = new Intent(context, WXPayEntryActivity.class);
                    context.startActivity(intent);
                    context.overridePendingTransition(R.anim.fade, R.anim.hold);
                    mContext.finish();
                }
            });
        } else {
            dialog_pay_time.show();
        }
    }

    private void sendUserDoData(String type, String oprationtime) {
        //发起请求
        DoInfo data = new DoInfo();
        data.setUserName(util.getAndroidId(this));
        data.setType(type);
        data.setOperationTime(oprationtime);
        String json = com.alibaba.fastjson.JSONObject.toJSONString(data);
        String aesJson = aesUtils.encrypt(json);
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

package com.orangepeople.movies.orangepeopletv.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.orangepeople.movies.orangepeopletv.Bean.SharedBean.sharedLogin;
import com.orangepeople.movies.orangepeopletv.Bean.login.LoginBean;
import com.orangepeople.movies.orangepeopletv.Constant.Constant;
import com.orangepeople.movies.orangepeopletv.Model.AppData;
import com.orangepeople.movies.orangepeopletv.Model.PriceInfo;
import com.orangepeople.movies.orangepeopletv.Model.PriceList;
import com.orangepeople.movies.orangepeopletv.R;
import com.orangepeople.movies.orangepeopletv.Save.KeyFile;
import com.orangepeople.movies.orangepeopletv.Utils.AesUtils;
import com.orangepeople.movies.orangepeopletv.Utils.CheckPermissionUtil;
import com.orangepeople.movies.orangepeopletv.Utils.Nick;
import com.orangepeople.movies.orangepeopletv.Utils.PhoneInfo;
import com.orangepeople.movies.orangepeopletv.Utils.T;
import com.orangepeople.movies.orangepeopletv.Utils.Util;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.net.URLEncoder;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    boolean isFirstIn = false;

    private final int GO_HOME = 1000;
    private final int GO_GUIDE = 1001;
    //延迟7秒
    private int SPLASH_SHOW_MILLIS = 4;

    private final String SHAREDPREFERENCES_NAME = "first_pref";

    private String install_key = "instalatv";
    /**
     * Handler:跳转到不同界面
     */
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GO_HOME:
                    goHome();
                    break;
                case GO_GUIDE:
                    goGuide();
                    break;
                case 100:
                    if (SPLASH_SHOW_MILLIS <= 0) {
                        //time out
                        goGuide();
                    } else {
                        SPLASH_SHOW_MILLIS--;
                        if (SPLASH_SHOW_MILLIS <= 0) {
                            timer_content.setText("跳过");
                            goGuide();
                        } else {
                            timer_content.setText(SPLASH_SHOW_MILLIS + "S 跳过");
                        }
                    }
                    break;
            }
        }
    };
    private Util util = new Util(this);
    private T t;
    private AesUtils aesUtils;
    private RelativeLayout timer_bg;
    private TextView timer_content;
    private Timer timer;
    private TimerTask timerTask;
    private TextView appVersion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        initView();
        doData();
    }

    private void doData() {
        initData();
        init();
        setListener();
        MobclickAgent.setDebugMode(true);//集成模式
        MobclickAgent.openActivityDurationTrack(false);//禁止默认的页面统计方式，
    }

    private void initView() {
        Util.createFileDir(Constant.send_Tv_Directory);
        Util.createFile(Constant.send_Tv);
        timer_bg = (RelativeLayout) findViewById(R.id.timer_bg);
        timer_content = (TextView) findViewById(R.id.timer_content);
        appVersion = (TextView) findViewById(R.id.appVersion);
        appVersion.setText("Version" + getAPPVersionCode());
        timer_bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.sendEmptyMessage(GO_HOME);
            }
        });
        timer_bg.getBackground().setAlpha(50);
        if (timerTask == null) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    Message msg = new Message();
                    msg.what = 0;
                    mHandler.sendEmptyMessage(100);
                }
            };
            //启动倒计时
            timer = new Timer();
            timer.schedule(timerTask, 0, 1000);
        }
        timer_bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                destroyTimer();
                goGuide();
            }
        });

    }

    private void initData() {
        t = new T();
        aesUtils = new AesUtils();

    }

    private void init() {
        initAnim();
        //splash_imageview.startAnimation(mFadeIn);

        // 读取SharedPreferences中需要的数据
        // 使用SharedPreferences来记录程序的使用次数
        SharedPreferences preferences = getSharedPreferences(
                SHAREDPREFERENCES_NAME, MODE_PRIVATE);

        // 取得相应的值，如果没有该值，说明还未写入，用true作为默认值
        isFirstIn = preferences.getBoolean("VpnIsFirstIn", true);
        refreshLocalData();
        // 判断程序与第几次运行，如果是第一次运行则跳转到引导界面，否则跳转到主界面
//        if (!isFirstIn) {
//            // 使用Handler的postDelayed方法，3秒后执行跳转到MainActivity
//            //刷新本地数据
//            refreshLocalData();
//            mHandler.sendEmptyMessageDelayed(GO_HOME, SPLASH_DELAY_MILLIS);
//        } else {
//            refreshLocalData();
//            mHandler.sendEmptyMessageDelayed(GO_GUIDE, SPLASH_DELAY_MILLIS);
//        }
    }

    private void initAnim() {
    }

    private void setListener() {
    }

    private void goHome() {
        destroyTimer();
        Intent intent = new Intent(SplashActivity.this, LiveActivity.class);
        SplashActivity.this.startActivity(intent);
        SplashActivity.this.finish();
    }

    private void goGuide() {
        destroyTimer();
        Intent intent = new Intent(SplashActivity.this, LiveActivity.class);
        SplashActivity.this.startActivity(intent);
        SplashActivity.this.finish();
    }

    private String nickname;
    private String username;
    private String password;
    private String imsi;
    private String iMeilLastId;
    private String email;
    private String mobieBrand;
    private String mobileModel;
    private String tel_phone;
    private String tele_supo;
    private String area;
    private String show;
    private int failIjk = 0;//失败次数

    private void refreshLocalData() {
        String firstReg = util.sharedPreferencesReadData(this, KeyFile.USER_FIRST_RGISTER, KeyFile.USER_FIRST_RGISTER);
        if (TextUtils.isEmpty(firstReg)) {
            Constant.isFirstRegister = true;
            util.sharedPreferencesWriteData(this, KeyFile.USER_FIRST_RGISTER, KeyFile.USER_FIRST_RGISTER, KeyFile.USER_FIRST_RGISTER);
        }

        if (!util.isNetworkConnected(this)) {
            t.show(this, "网络没有连接,请检查您的网络", 1000);
            mHandler.sendEmptyMessage(GO_HOME);
            return;
        }
        //自动登录
        //本地获取数据如果不空,则直接登陆,如果空,则直接获取手机数据
        final String aesShow = util.sharedPreferencesReadData(SplashActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "show");
        show = aesUtils.getInstance().decrypt(aesShow);
        nickname = aesUtils.getInstance().decrypt(util.sharedPreferencesReadData(SplashActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "nickName"));
        username = aesUtils.getInstance().decrypt(util.sharedPreferencesReadData(SplashActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "userName"));
        password = aesUtils.getInstance().decrypt(util.sharedPreferencesReadData(SplashActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "passWord"));
        imsi = aesUtils.getInstance().decrypt(util.sharedPreferencesReadData(SplashActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "imsi"));
        email = aesUtils.getInstance().decrypt(util.sharedPreferencesReadData(SplashActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "email"));
        mobieBrand = aesUtils.getInstance().decrypt(util.sharedPreferencesReadData(SplashActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "mobieBrand"));
        mobileModel = aesUtils.getInstance().decrypt(util.sharedPreferencesReadData(SplashActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "mobileModel"));
        tel_phone = aesUtils.getInstance().decrypt(util.sharedPreferencesReadData(SplashActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "tel_phone"));
        tele_supo = aesUtils.getInstance().decrypt(util.sharedPreferencesReadData(SplashActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "tele_supo"));
        area = aesUtils.getInstance().decrypt(util.sharedPreferencesReadData(SplashActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "area"));

        //取加密或手机直接数据
        LoginBean info = new LoginBean();
        if (TextUtils.isEmpty(username)
                || TextUtils.isEmpty(password) || TextUtils.isEmpty(imsi)
                || TextUtils.isEmpty(show)) {
            //手机铭文数据
            nickname = new String(Nick.getName());//昵称
            TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            username = util.getAndroidId(this);//用户名
            iMeilLastId = username.substring(username.length() - 1);//lastid
            PhoneInfo phoneInfo = new PhoneInfo(SplashActivity.this);
            phoneInfo.getProvidersName();
            imsi = phoneInfo.getIMSI();//
            if (TextUtils.isEmpty(imsi)) {
                imsi = "1234567890";//手机卡号
            }
            String passWordStr = util.sharedPreferencesReadData(SplashActivity.this, KeyFile.PASS_DATA, "pass");
            if (TextUtils.isEmpty(passWordStr)) {
                password = "123456";
                show = "123456";//默认密码
            } else {
                password = aesUtils.decrypt(passWordStr);
                show = password;
            }

            email = "18376542390@163.com";//邮箱
            mobieBrand = android.os.Build.BRAND;//手机品牌
            if (TextUtils.isEmpty(mobieBrand)) {
                mobieBrand = "mobieBrand is null";
            }
            mobileModel = android.os.Build.MODEL; // 手机型号
            if (TextUtils.isEmpty(mobileModel)) {
                mobileModel = "mobileModel is null";
            }
            tel_phone = tm.getLine1Number();//手机号码
            if (TextUtils.isEmpty(tel_phone)) {
                tel_phone = "tel_phone is null";
            }
            tele_supo = util.getTele_Supo(imsi, this);//运营商
            if (TextUtils.isEmpty(tele_supo)) {
                tele_supo = "telesupo is null";
            }
            area = getAreaName();//area

            info.setUserName(username);
            info.setPassWord(util.getMD5Str(password));
            info.setNickName(URLEncoder.encode(nickname));
            info.setEmail(email);
            info.setImei(username);
            info.setImeiLastId(iMeilLastId);
            info.setImsi(imsi);
            info.setMobieBrand(mobieBrand);
            info.setMobileModel(mobileModel);
            info.setTel_phone(tel_phone);
            info.setTele_supo(tele_supo);
            info.setArea(area);
        } else {
            //本地加密数据
            iMeilLastId = username.trim().substring(username.length() - 1);
            info.setUserName(username);
            info.setPassWord(util.getMD5Str(password));
            info.setNickName(URLEncoder.encode(nickname));
            info.setEmail(email);
            info.setImei(username);
            info.setImeiLastId(iMeilLastId);
            info.setImsi(imsi);
            info.setMobieBrand(mobieBrand);
            info.setMobileModel(mobileModel);
            info.setTel_phone(tel_phone);
            info.setTele_supo(tele_supo);
            info.setArea(area);
        }
        final String json = JSON.toJSONString(info);
        String aesJson = aesUtils.encrypt(json);//对json加密
        //发起请求
        RequestParams params = new RequestParams(Constant.LOGIN_INTERFACE);
        params.setCacheMaxAge(0);//最大数据缓存时间
        params.setConnectTimeout(6000);//连接超时时间
        params.setCharset("UTF-8");
        params.addQueryStringParameter("data", aesJson);
        //异步线程获取数据
        getNetData();
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                String jsonStr = result.toString();
                String aesJson = aesUtils.decrypt(jsonStr);
                if (TextUtils.isEmpty(aesJson)) {
                    failIjk++;
                    if (failIjk <= 0) {
                        refreshLocalData();
                    } else {
                        //mHandler.sendEmptyMessageDelayed(GO_HOME, SPLASH_DELAY_MILLIS);
                    }
                    return;
                }
                try {
                    JSONObject jo = new JSONObject(aesJson);//拿到整体json
                    String loginStatus = jo.getString("respMsg");//登陆是否成功信息判断
                    sharedLogin info = new sharedLogin();//shared保存
                    if (loginStatus.equals("fail")) {
                        refreshLocalData();
                        return;
                    } else if (loginStatus.equals("success")) {
                        //登陆后处理
                        JSONObject data = jo.getJSONObject("json");//成功后的信息
                        info.setId(data.getString("id"));
                        info.setVip_status(data.getString("vip_status"));
                        info.setUserName(data.getString("userName"));
                        info.setVip_lastTme(data.getString("vip_lastTime"));
                        info.setPay_count(data.getString("pay_count"));
                        info.setEmail(email);
                        info.setTel_phone(tel_phone);
                        //保存到shared
                        //保存数据之前清空旧的数据
                        util.sharedPreferencesDelByFileAllData(SplashActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE);

                        //保存用户信息
                        util.sharedPreferencesWriteData(SplashActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "id", aesUtils.getInstance().encrypt(info.getId()));
                        util.sharedPreferencesWriteData(SplashActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "nickName", aesUtils.getInstance().encrypt(nickname));
                        util.sharedPreferencesWriteData(SplashActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "vip_status", aesUtils.getInstance().encrypt(info.getVip_status()));
                        util.sharedPreferencesWriteData(SplashActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "userName", aesUtils.getInstance().encrypt(info.getUserName()));
                        util.sharedPreferencesWriteData(SplashActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "vip_lastTme", aesUtils.getInstance().encrypt(info.getVip_lastTme()));
                        util.sharedPreferencesWriteData(SplashActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "passWord", aesUtils.getInstance().encrypt(show));
                        util.sharedPreferencesWriteData(SplashActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "email", aesUtils.getInstance().encrypt(info.getEmail()));
                        util.sharedPreferencesWriteData(SplashActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "tel_phone", aesUtils.getInstance().encrypt(info.getTel_phone()));

                        //存储一个特殊字符
                        util.sharedPreferencesWriteData(SplashActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "show", aesUtils.getInstance().encrypt(show));

                        util.sharedPreferencesWriteData(SplashActivity.this, KeyFile.PASS_DATA, "pass", aesUtils.getInstance().encrypt(show));

                        failIjk = 0;
                        if (Integer.parseInt(info.getPay_count()) >= 1) {
                            //隐藏38元支付
                            Constant.isPay = true;
                        }
                        //mHandler.sendEmptyMessageDelayed(GO_HOME, SPLASH_DELAY_MILLIS);
                        //获取服务器给用户分配的数据接口
                    } else {
                        refreshLocalData();
                    }
                } catch (JSONException e) {
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

    public void getNetData() {
        if (!util.isNetworkConnected(this)) {
            //网络连接
            mHandler.sendEmptyMessage(GO_HOME);
            return;
        }
        //发起请求
        RequestParams params = new RequestParams(Constant.APP_DATA);
        AppData data = new AppData();
        //渠道号
        String area = util.getAppMetaData(this, "UMENG_CHANNEL");//渠道号
        if (TextUtils.isEmpty(area)) {
            area = "chengrentv1007";//渠道为空，则上传此渠道号
        }
        data.setArea(area);
        String json = com.alibaba.fastjson.JSONObject.toJSONString(data);
        String aesJson = aesUtils.encrypt(json);
        params.setCacheMaxAge(0);//最大数据缓存时间
        params.setConnectTimeout(2000);//连接超时时间
        params.setCharset("UTF-8");
        params.addQueryStringParameter("data", aesJson);
        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                try {
//                    String json = result.toString();
//                    String aesJson = aesUtils.decrypt(json);

//                    Constant.saveData = aesJson;
//                    ThreeVideo threeVideo = com.alibaba.fastjson.JSONObject.parseObject(aesJson, ThreeVideo.class);
//                    Comment comment = com.alibaba.fastjson.JSONObject.parseObject(aesJson, Comment.class);
//                    BigVideo bigVideo = com.alibaba.fastjson.JSONObject.parseObject(aesJson, BigVideo.class);
//                    Live live = com.alibaba.fastjson.JSONObject.parseObject(aesJson, Live.class);
//                    PriceList price = JSON.parseObject(aesJson, PriceList.class);
//
//
//                    List<PriceInfo> priceList = price.getPriceJson();
//                    List<ThreeVideoInfo> threeVideoInfoList = threeVideo.getThreeJson();
//                    List<BigVideoInfo> bigVideoInfoList = bigVideo.getBigJson();
//                    List<CommentInfo> commentInfoList = comment.getCommentJson();
//                    List<LiveInfo> liveInfoList = live.getLiveJson();
//
//                    if (threeVideoInfoList == null || bigVideoInfoList == null || commentInfoList == null || liveInfoList == null) {
//                        Constant.saveDataSucces = false;
//                        mHandler.sendEmptyMessageDelayed(GO_HOME, SPLASH_DELAY_MILLIS);
//                        return;
//                    }
//
//                    if (threeVideoInfoList.size() == 0 || bigVideoInfoList.size() == 0 || commentInfoList.size() == 0 || liveInfoList.size() == 0) {
//                        Constant.saveDataSucces = false;
//                        mHandler.sendEmptyMessageDelayed(GO_HOME, SPLASH_DELAY_MILLIS);
//                        return;
//                    }
//                    Constant.priceInfoList = priceList;
//
//                    Log.i("aesJsonStr", "====================fast end===================");
//
//                    Log.i("aesJsonStr", "====================sqlite insert start===================");
//                    threeDBHelper.createThreeTable();
//                    commentDBHelper.createCommentTable();
//                    liveDBHelper.createLiveTable();
//                    bigDBHelper.createBigTable();
//
//                    threeDBHelper.getDb().beginTransaction();
//                    for (ThreeVideoInfo three : threeVideoInfoList) {
//                        threeDBHelper.insertThreeVideo(three);
//                    }
//                    threeDBHelper.getDb().setTransactionSuccessful();//设置事务处理成功，不设置会自动回滚不提交
//                    threeDBHelper.getDb().endTransaction();
//
//                    for (CommentInfo commentInfo : commentInfoList) {
//                        commentDBHelper.insertCommentVideo(commentInfo);
//                    }
//                    for (LiveInfo liveInfo : liveInfoList) {
//                        liveDBHelper.insertLiveVideo(liveInfo);
//                    }
//
//                    bigDBHelper.getDb().beginTransaction();
//                    for (BigVideoInfo bigVideoInfo : bigVideoInfoList) {
//                        bigDBHelper.insertBigVideo(bigVideoInfo);
//                    }
//                    bigDBHelper.getDb().setTransactionSuccessful();//设置事务处理成功，不设置会自动回滚不提交
//                    bigDBHelper.getDb().endTransaction();
                    Constant.saveDataSucces = false;
                    mHandler.sendEmptyMessage(GO_HOME);
                } catch (Exception e) {
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Constant.saveDataSucces = false;
                mHandler.sendEmptyMessage(GO_HOME);
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });

    }


    //获取友盟渠道
    public String getAreaName() {
        //渠道号
        String area = util.getAppMetaData(this, "UMENG_CHANNEL");//暂时测试渠道号
        if (TextUtils.isEmpty(area)) {
            return "area is null";
        }
        return area;
    }

    /***
     * 友盟统计
     */
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("tv启动页面"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("tv启动页面"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyTimer();
    }

    public void destroyTimer() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
    public String getAPPVersionCode() {
        int currentVersionCode = 0;
        String appVersionName = null;
        PackageManager manager = this.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            appVersionName = info.versionName; // 版本名
            currentVersionCode = info.versionCode; // 版本号
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appVersionName;
    }
}

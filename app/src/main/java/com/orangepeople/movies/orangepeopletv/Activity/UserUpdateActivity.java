package com.orangepeople.movies.orangepeopletv.Activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.orangepeople.movies.orangepeopletv.Bean.login.LoginBean;
import com.orangepeople.movies.orangepeopletv.Constant.Constant;
import com.orangepeople.movies.orangepeopletv.R;
import com.orangepeople.movies.orangepeopletv.Save.KeyFile;
import com.orangepeople.movies.orangepeopletv.Tool.SendTvTimeTool;
import com.orangepeople.movies.orangepeopletv.Tool.VipTool;
import com.orangepeople.movies.orangepeopletv.Utils.AesUtils;
import com.orangepeople.movies.orangepeopletv.Utils.Util;
import com.orangepeople.movies.orangepeopletv.View.SystemBarTintManager;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class UserUpdateActivity extends AppCompatActivity {

    private ImageView back;
    private EditText et_up_username, et_up_phone, et_up_pass;
    private EditText et_info_username, et_info_lasttime, et_info_viptype;
    private ImageView update_button;
    private Util util;
    private AesUtils aesUtils;
    private String username;
    private String password;
    private String iMeilLastId;
    private String tel_phone;
    private ImageView iv_return;
    private Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatus();
        setContentView(R.layout.activity_user_update);
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
        iv_return = (ImageView) findViewById(R.id.iv_return);
        iv_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.finish();
            }
        });
        et_up_pass = (EditText) findViewById(R.id.et_password);
        et_up_username = (EditText) findViewById(R.id.et_username);
        //et_pass = (EditText) findViewById(R.id.et_pass);
        et_up_phone = (EditText) findViewById(R.id.et_phone);
        update_button = (ImageView) findViewById(R.id.update_ok);

        et_info_username = (EditText) findViewById(R.id.et_info_username);
        et_info_lasttime = (EditText) findViewById(R.id.et_info_lasttime);
        et_info_viptype = (EditText) findViewById(R.id.et_info_viptype);

    }

    private void initData() {
        util = new Util(this);
        aesUtils = new AesUtils();
        String show = util.sharedPreferencesReadData(context, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "show");
        String imsi = util.sharedPreferencesReadData(context, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "imsi");
        String nickName = util.sharedPreferencesReadData(context, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "nickName");
        String userName = util.sharedPreferencesReadData(context, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "userName");
        password = util.sharedPreferencesReadData(context, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "passWord");
        String USER_VIP_STATUS = util.sharedPreferencesReadData(context, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "vip_status");
        String VIP_LAST_TIME = util.sharedPreferencesReadData(context, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "vip_lastTme");
        final TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String phone = tm.getLine1Number();
        String androidId = util.getAndroidId(this);
        et_info_username.setText(util.getAndroidId(this));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (TextUtils.isEmpty(nickName) || TextUtils.isEmpty(USER_VIP_STATUS) || TextUtils.isEmpty(userName) || TextUtils.isEmpty(password) || TextUtils.isEmpty(VIP_LAST_TIME)) {
            //没有登陆
            et_info_username.setText(androidId);
            et_info_viptype.setText("普通用户");
            String oldDate = aesUtils.decrypt(Util.readFileToSDFile(Constant.TV_SHIYONG_ALL));//第一次用户注册的时间文件
            if (VipTool.judgeIsThanSendTvTime()) {
                //超过送tv的时间
                et_info_lasttime.setText("您还没有开通VIP哦");
            } else {
                try {
                    Date date = sdf.parse(oldDate);
                    if (SendTvTimeTool.SEND_TV_TIME_TYPE == SendTvTimeTool.SEND_TV_TIME_DAY_TYPE) {
                        date.setDate(date.getDate() + SendTvTimeTool.Time_Day);
                    } else if (SendTvTimeTool.SEND_TV_TIME_TYPE == SendTvTimeTool.SEND_TV_TIME_HOURS_TYPE) {
                        date.setHours(date.getHours() + SendTvTimeTool.Time_Hours);
                    }
                    et_info_lasttime.setText(sdf.format(date));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } else {
            //已经登陆,但要区分会员和非会员状态
            String statusStr = aesUtils.getInstance().decrypt(USER_VIP_STATUS);
            if (TextUtils.isEmpty(statusStr)) {
            } else if (statusStr.equals("1")) {
                //会员
                et_info_username.setText(androidId);
                et_info_viptype.setText("VIP高级会员");
                String time = aesUtils.getInstance().decrypt(VIP_LAST_TIME);
                time = time.substring(0, 10);
                et_info_lasttime.setText(time);
            } else if (statusStr.equals("2")) {
                //不是会员
                et_info_username.setText(androidId);
                String oldDate_shiyong = aesUtils.decrypt(Util.readFileToSDFile(Constant.TV_SHIYONG_ALL));//第一次用户注册的时间文件
                if (TextUtils.isEmpty(oldDate_shiyong)) {
                    et_info_viptype.setText("试用用户");
                    et_info_lasttime.setText("试用剩余20分钟");
                } else {
                    et_info_viptype.setText("试用用户");
                    if (VipTool.judgeIsThanSendTvTime()) {
                        String oldDate = aesUtils.decrypt(Util.readFileToSDFile(Constant.TV_SHIYONG_M3U8_ALL));
                        try {
                            if (Integer.parseInt(oldDate) > Constant.doDate) {
                                et_info_lasttime.setText("您的试用期已过期");
                            } else {
                                if (Integer.parseInt(oldDate) == 0) {
                                    et_info_lasttime.setText("试用剩余20分钟");
                                } else {
                                    String ltime = (((int) ((Integer.parseInt(oldDate)) / 60))) + "";
                                    int time = (Constant.doDate / 60) - Integer.parseInt(ltime);
                                    et_info_lasttime.setText("试用剩余" + time + "分钟");
                                }
                            }
                        } catch (Exception e) {
                        }
                    } else {
                        //在送tv的时间内
                        try {
                            Date date = sdf.parse(oldDate_shiyong);
                            if (SendTvTimeTool.SEND_TV_TIME_TYPE == SendTvTimeTool.SEND_TV_TIME_DAY_TYPE) {
                                date.setDate(date.getDate() + SendTvTimeTool.Time_Day);
                            } else if (SendTvTimeTool.SEND_TV_TIME_TYPE == SendTvTimeTool.SEND_TV_TIME_HOURS_TYPE) {
                                date.setHours(date.getHours() + SendTvTimeTool.Time_Hours);
                            }
                            et_info_lasttime.setText(sdf.format(date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        if (TextUtils.isEmpty(phone)) {
            phone = "10086";
        }
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(userName)) {
            //本地没有登陆的数据
            String username = util.getAndroidId(this);//用户名
            //et_pass.setText("123456");
            et_up_username.setText(androidId);
            et_up_phone.setText(phone);
        } else {
            //本地有登陆的数据
            //et_pass.setText(aesUtils.decrypt(passWordStr));
            et_up_username.setText(aesUtils.decrypt(username));
            et_up_phone.setText(phone);
        }
    }

    public void updateDB(View view) {
        final Dialog toast = createLoadingDialog(UserUpdateActivity.this, "账户修改中...请稍候");
        if (isEmty()) {
            //修改逻辑处理
            toast.show();
            if (!util.isNetworkConnected(this)) {
                toast.dismiss();
                util.showTextToast(this, "网络没有连接");
                return;
            }
            final LoginBean info = new LoginBean();
            info.setUserName(username);
            info.setPassWord(util.getMD5Str(password));
            iMeilLastId = username.substring(username.length() - 1);
            info.setImeiLastId(iMeilLastId);
            final TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            String phone = tm.getLine1Number();
            if (TextUtils.isEmpty(phone)) {
                phone = "phone is null";
            }
            tel_phone = phone;
            info.setTel_phone(phone);
            final String json = JSON.toJSONString(info);
            String aesJson = aesUtils.encrypt(json);
            //发起请求
            RequestParams params = new RequestParams(Constant.USER_UPDATE);
            params.setCacheMaxAge(0);//最大数据缓存时间
            params.setConnectTimeout(15000);//连接超时时间
            params.setCharset("UTF-8");
            params.addQueryStringParameter("data", aesJson);

            x.http().post(params, new Callback.CommonCallback<String>() {

                @Override
                public void onSuccess(String result) {
                    String jsonStr = result.toString();
                    String aesJson = aesUtils.decrypt(jsonStr);
                    try {
                        JSONObject object = new JSONObject(aesJson);
                        String updateStatus = object.getString("respMsg");
                        if (updateStatus.equals("fail")) {
                            util.showTextToast(UserUpdateActivity.this, "用户信息修改失败");
                            toast.dismiss();
                            return;
                        } else if (updateStatus.equals("success")) {
                            //用户信息修改成功 逻辑处理
                            //重新写入昵称和密码(用户名imeiId无法改动)
                            //删除旧的数据
                            util.sharedPreferencesDelOrderData(UserUpdateActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "passWord");
                            util.sharedPreferencesDelOrderData(UserUpdateActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "show");
                            util.sharedPreferencesDelOrderData(UserUpdateActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "tel_phone");

                            //写入修改成功后的数据
                            util.sharedPreferencesWriteData(UserUpdateActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "passWord", aesUtils.getInstance().encrypt(password));
                            util.sharedPreferencesWriteData(UserUpdateActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "show", aesUtils.getInstance().encrypt(password));
                            util.sharedPreferencesWriteData(UserUpdateActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE, "tel_phone", aesUtils.getInstance().encrypt(tel_phone));

                            util.sharedPreferencesWriteData(UserUpdateActivity.this, KeyFile.PASS_DATA, "pass", aesUtils.getInstance().encrypt(password));
                            util.showTextToast(UserUpdateActivity.this, "用户信息修改成功");
                            toast.dismiss();
                            //自动登录
                            //autoLogin();
                            util.sharedPreferencesDelByFileAllData(UserUpdateActivity.this, KeyFile.SAVE_LOGIN_SUCCES_DATA_FILE);
                            Intent intent = new Intent(UserUpdateActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            util.showTextToast(UserUpdateActivity.this, "系统繁忙,请稍候再试");
                            toast.dismiss();
                        }
                    } catch (JSONException e) {
                        util.showTextToast(UserUpdateActivity.this, "系统繁忙,请稍候再试");
                        toast.dismiss();
                    }
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    util.showTextToast(UserUpdateActivity.this, "系统繁忙,请稍候再试");
                    ;
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


    /**
     * 对修改非空判断
     */
//    et_pass.setText("123456");
//    et_up_username.setText(username);
//    et_up_phone.setText(tel_phone);
    public boolean isEmty() {
        boolean flag = false;
        username = et_up_username.getText().toString();
        password = et_up_pass.getText().toString();
        tel_phone = et_up_phone.getText().toString();
        if (TextUtils.isEmpty(username)) {
            flag = false;
            util.showTextToast(UserUpdateActivity.this, "请输入用户名");
        }
        if (TextUtils.isEmpty(password)) {
            flag = false;
            util.showTextToast(UserUpdateActivity.this, "请输入密码");
        }

        if (TextUtils.isEmpty(tel_phone)) {
            flag = false;
            util.showTextToast(UserUpdateActivity.this, "请输入手机号码");
        }

        if (!TextUtils.isEmpty(tel_phone) && !TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            flag = true;
        }
        return flag;
    }

    public Dialog createLoadingDialog(Context context, String msg) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.progressdialog_no_deal, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
        // main.xml中的ImageView
        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
        // 加载动画
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
                context, R.anim.anim);
        // 使用ImageView显示动画
        spaceshipImage.startAnimation(hyperspaceJumpAnimation);
        tipTextView.setText(msg);// 设置加载信息

        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog

        loadingDialog.setCancelable(false);// 可以用“返回键”取消
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局
        return loadingDialog;

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
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
        MobclickAgent.onPageStart("修改页面"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("修改页面"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(this);
    }
}

package com.orangepeople.movies.orangepeopletv.Activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSONObject;
import com.github.ybq.android.spinkit.SpinKitView;
import com.orangepeople.movies.orangepeopletv.Constant.Constant;
import com.orangepeople.movies.orangepeopletv.DBManager.DBManager;
import com.orangepeople.movies.orangepeopletv.Interface.AlertInterface;
import com.orangepeople.movies.orangepeopletv.Model.SearchList;
import com.orangepeople.movies.orangepeopletv.Model.SearchModel;
import com.orangepeople.movies.orangepeopletv.Model.SearchPram;
import com.orangepeople.movies.orangepeopletv.Model.ThreeVideoInfo;
import com.orangepeople.movies.orangepeopletv.R;
import com.orangepeople.movies.orangepeopletv.UIAdapter.Hot_Content_Adapter;
import com.orangepeople.movies.orangepeopletv.UIAdapter.Search_Result_Adapter;
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

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SearchActivity extends AppCompatActivity implements AlertInterface {

    private EditText et_search;
    private ImageView iv_vip_head, top_search;
    private SwipeRefreshLayout search_swiperefreshlayout;
    private RecyclerView search_recyclerview;
    private SpinKitView progress;
    private LinearLayoutManager mLinearLayoutManager;
    private int lastVisibleItemPosition;
    private boolean isLoading = true;
    private AesUtils aesUtils = new AesUtils();
    private Map<Integer, Object> allDataMap = new HashMap<>();
    private RecyclerViewAdapter adapter = new RecyclerViewAdapter();
    private Search_Result_Adapter search_result_adapter;
    private Hot_Content_Adapter hot_adapter;
    private DBManager dbManager = DBManager.getDBManager(this);
    private Activity context;
    private T t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatus();
        setContentView(R.layout.activity_search);
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
        et_search = (EditText) findViewById(R.id.et_search);
        iv_vip_head = (ImageView) findViewById(R.id.iv_vip_head);
        top_search = (ImageView) findViewById(R.id.top_search);

        search_swiperefreshlayout = (SwipeRefreshLayout) findViewById(R.id.search_swiperefreshlayout);
        search_recyclerview = (RecyclerView) findViewById(R.id.search_recyclerview);
        progress = (SpinKitView) findViewById(R.id.progress);

        iv_vip_head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, WXPayEntryActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade, R.anim.hold);
            }
        });
        search_swiperefreshlayout.setColorSchemeResources(android.R.color.holo_red_light, android.R.color.holo_blue_dark, android.R.color.holo_orange_dark, android.R.color.holo_green_light);
        search_swiperefreshlayout.post(new Runnable() {
            @Override
            public void run() {
                search_swiperefreshlayout.setRefreshing(false);
            }
        });
        iv_vip_head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, WXPayEntryActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade, R.anim.hold);
            }
        });
//        search_swiperefreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                //刷新请求
//                progress.setVisibility(View.VISIBLE);
//                page = 1;
//                pageList.clear();
//                getNetData();
//            }
//        });
        mLinearLayoutManager = new LinearLayoutManager(this);
        search_recyclerview.setHasFixedSize(true);
        search_recyclerview.setLayoutManager(mLinearLayoutManager);
        search_recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
//                Log.d("test", "StateChanged = " + newState);
//                if (lastVisibleItemPosition + 1 == adapter.getItemCount()) {
//                    Log.d("test", "loading executed");
//                    boolean isRefreshing = search_swiperefreshlayout.isRefreshing();
//                    if (isRefreshing) {
//                        adapter.notifyItemRemoved(adapter.getItemCount());
//                        return;
//                    }
//                    if (isLoading) {
//                        page++;
//                        progress.setVisibility(View.GONE);
//                        isLoading = false;
//                        final List<ThreeVideoInfo> zuixin = threeDBHelper.pageQuery(5, 6);
//                        //加载更多....
//                        List<ThreeVideoInfo> hot = threeDBHelper.pageQuery(page, 9);
//                        if (hot == null || hot.size() == 0) {
//                            t.centershow(context, "~没有数据了~", 50);
//                            search_swiperefreshlayout.setRefreshing(false);
//                            adapter.notifyItemRemoved(adapter.getItemCount());
//                            progress.setVisibility(View.GONE);
//                            return;
//                        }
//                        pageList.addAll(hot);
//                        allDataMap.clear();
//                        allDataMap.put(0, zuixin);
//                        allDataMap.put(1, pageList);
//                        adapterData();
//
//                    } else {
//                        progress.setVisibility(View.GONE);
//                        search_swiperefreshlayout.setRefreshing(false);
//                        adapter.notifyItemRemoved(adapter.getItemCount());
//                        t.centershow(context, "~没有数据了~", 50);
//                    }
//                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition();
            }
        });
        top_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchText = et_search.getText().toString();
                if (TextUtils.isEmpty(searchText)) {
                    t.centershow(context, "请输入关键字", 1000);
                    return;
                }
                progress.setVisibility(View.VISIBLE);
                et_search.setText("");
                et_search.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
                getNetData(searchText);
            }
        });
    }

    private String intentJson = "";

    private void initData() {
        search_recyclerview.setAdapter(adapter);
        intentJson = Constant.saveData;
        if (t == null) {
            t = new T();
        }
    }

    private void getNetData(String text) {
        //请求搜索数据
        //发起请求
        RequestParams params = new RequestParams(Constant.DATA_SEARCH);
        SearchPram pram = new SearchPram();
        pram.setName(URLEncoder.encode(text));
        String search = JSONObject.toJSONString(pram);
        String searchJson = aesUtils.encrypt(search);
        params.setCacheMaxAge(0);//最大数据缓存时间
        params.setConnectTimeout(10000);//连接超时时间
        params.setCharset("UTF-8");
        params.addQueryStringParameter("data", searchJson);
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    Log.i("aesJsonStr", "====================aes start===================");
                    String searchResultJson = aesUtils.decrypt(result);
                    Log.i("aesJsonStr", "====================aes  end===================");

                    Log.i("aesJsonStr", "====================fast start===================");
                    SearchList searchList = JSONObject.parseObject(searchResultJson, SearchList.class);
                    List<SearchModel> searchModelList = searchList.getSearchJson();
                    Log.i("aesJsonStr", "====================fast end===================");
                    if (searchModelList == null || searchModelList.size() == 0) {
                        progress.setVisibility(View.GONE);
                        t.centershow(context, "没有搜到任何结果", 100);
                        return;
                    }
                    List<ThreeVideoInfo> search_tuijian = dbManager.queryPagerThreeVideo(6, new Util(context).createTranslateRandom());

                    allDataMap.clear();
                    allDataMap.put(0, searchModelList);
                    allDataMap.put(1, search_tuijian);
                    adapterData();
                    progress.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                isLoading = true;
                search_swiperefreshlayout.setRefreshing(false);
                progress.setVisibility(View.GONE);
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


    private void adapterData() {
        adapter.notifyDataSetChanged();
        search_swiperefreshlayout.setRefreshing(false);
        adapter.notifyItemRemoved(adapter.getItemCount());
        progress.setVisibility(View.GONE);
        isLoading = true;
    }

    /**
     * recyclerView适配器
     */

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
        private static final int TYPE_SEARCH_RESULT = 0;//搜索内容
        private static final int TYPE_SEARCH_TUIJIAN = 1;//搜索推荐

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
                case TYPE_SEARCH_RESULT:
                    return onCreateSearchResultViewHolder(parent, viewType);
                case TYPE_SEARCH_TUIJIAN:
                    return onCreateSearchTuiJianHolder(parent, viewType);
            }
            return null;
        }

        /**
         * 0、搜索结果
         *
         * @param parent
         * @param viewType
         * @return
         */
        private RecyclerView.ViewHolder onCreateSearchResultViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.search_result, parent, false);
            HeaderViewHolder holder = new HeaderViewHolder(view);
            return holder;
        }

        /**
         * 、使用ViewHolder
         */
        class HeaderViewHolder extends RecyclerView.ViewHolder {

            public SelfGridView search_result_grid;

            public HeaderViewHolder(View itemView) {
                super(itemView);
                search_result_grid = (SelfGridView) itemView.findViewById(R.id.search_result_grid);
            }
        }


        /**
         * 1、搜索推荐
         *
         * @param parent
         * @param viewType
         * @return
         */
        private RecyclerView.ViewHolder onCreateSearchTuiJianHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.search_tuijian, parent, false);
            HotViewHolder holder = new HotViewHolder(view);
            return holder;
        }

        /**
         * 、使用ViewHolder
         */
        class HotViewHolder extends RecyclerView.ViewHolder {

            public SelfGridView search_tuijian_grid;

            public HotViewHolder(View itemView) {
                super(itemView);
                search_tuijian_grid = (SelfGridView) itemView.findViewById(R.id.search_tuijian_grid);
            }
        }

        //绑定视图
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == TYPE_SEARCH_RESULT) {
                onBindHeaderViewHolder((HeaderViewHolder) holder, position);
            } else if (getItemViewType(position) == TYPE_SEARCH_TUIJIAN) {
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
            return allDataMap.size() == 0 ? 0 : allDataMap.size();
        }


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
                    return TYPE_SEARCH_RESULT;
                } else if (position == 1) {
                    return TYPE_SEARCH_TUIJIAN;
                }
            }
            return TYPE_SEARCH_TUIJIAN;
        }

        /**
         * 绑定搜索结果
         */

        private void onBindHeaderViewHolder(final HeaderViewHolder holder, int position) {
            List<SearchModel> result = (List<SearchModel>) allDataMap.get(0);
            if (result == null || result.size() == 0) {
                new T().centershow(context, "没有搜到任何东西", 500);
                return;
            }
            if (search_result_adapter == null) {
                search_result_adapter = new Search_Result_Adapter(context);
                search_result_adapter.setList(result);
                search_result_adapter.setAlertInterface(SearchActivity.this);
                holder.search_result_grid.setAdapter(search_result_adapter);
            } else {
                search_result_adapter.setList(result);
                search_result_adapter.notifyDataSetChanged();
            }

        }

        /**
         * 绑定搜索推荐
         */

        private void onBindHotViewHolder(final HotViewHolder holder, int position) {
            List<ThreeVideoInfo> tuiJianInfo = (List<ThreeVideoInfo>) allDataMap.get(1);
            if (hot_adapter == null) {
                hot_adapter = new Hot_Content_Adapter(context);
                hot_adapter.setAlertInterface(SearchActivity.this);
                hot_adapter.setList(tuiJianInfo);
                holder.search_tuijian_grid.setAdapter(hot_adapter);
            } else {
                hot_adapter.setList(tuiJianInfo);
                hot_adapter.notifyDataSetChanged();
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

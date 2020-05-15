package com.noplugins.keepfit.userplatform.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshViewFooter;
import com.google.gson.Gson;
import com.noplugins.keepfit.userplatform.R;
import com.noplugins.keepfit.userplatform.activity.MessageDetailActivity;
import com.noplugins.keepfit.userplatform.adapter.PlatFormAdapter;
import com.noplugins.keepfit.userplatform.entity.MessageDateEntity;
import com.noplugins.keepfit.userplatform.global.AppConstants;
import com.noplugins.keepfit.userplatform.util.BaseUtils;
import com.noplugins.keepfit.userplatform.util.MessageEvent;
import com.noplugins.keepfit.userplatform.util.SpUtils;
import com.noplugins.keepfit.userplatform.util.net.Network;
import com.noplugins.keepfit.userplatform.util.net.entity.Bean;
import com.noplugins.keepfit.userplatform.util.net.progress.GsonSubscriberOnNextListener;
import com.noplugins.keepfit.userplatform.util.net.progress.ProgressSubscriber;
import com.noplugins.keepfit.userplatform.util.net.progress.ProgressSubscriberNew;
import com.noplugins.keepfit.userplatform.util.net.progress.SubscriberOnNextListener;
import okhttp3.RequestBody;
import org.greenrobot.eventbus.EventBus;
import rx.Subscription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zhy.http.okhttp.log.LoggerInterceptor.TAG;


public class PlatformFragment extends Fragment {

    @BindView(R.id.xrefreshview)
    XRefreshView xrefreshview;
    @BindView(R.id.recycler_view)
    RecyclerView recycler_view;
    private View view;
    private LinearLayoutManager layoutManager;
    private PlatFormAdapter platFormAdapter;
    private int page = 1;
    private List<MessageDateEntity.ListBean> messageBeans = new ArrayList<>();
    private int maxPage;
    private boolean is_not_more;
    private boolean is_go_to_detail_back;

    public static PlatformFragment newInstance(String title) {
        PlatformFragment fragment = new PlatformFragment();
        Bundle args = new Bundle();
        args.putString("home_fragment_title", title);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_platform, container, false);
            ButterKnife.bind(this, view);//绑定黄牛刀
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        set_list_resource();
        initView();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(getActivity());

    }

    private void initMessageDate() {
        Map<String, Object> params = new HashMap<>();
        if ("".equals(SpUtils.getString(getActivity(), AppConstants.USER_NAME))) {

        } else {
            String user_id = SpUtils.getString(getActivity(), AppConstants.USER_NAME);
            params.put("userNum", user_id);//用户编号
        }
        params.put("page", page);
        params.put("type", "3");
        Subscription subscription = Network.getInstance("日志消息列表", getActivity())
                .zhanghu_message_list(params,
                        new ProgressSubscriber<>("日志消息列表", new SubscriberOnNextListener<Bean<MessageDateEntity>>() {
                            @Override
                            public void onNext(Bean<MessageDateEntity> result) {
                                maxPage = result.getData().getMaxPage();
                                if (page == 1) {//表示刷新
                                    messageBeans.clear();
                                    messageBeans.addAll(result.getData().getList());
                                    platFormAdapter.notifyDataSetChanged();
                                } else {
                                    if (page <= maxPage) {//表示加载还有数据
                                        is_not_more = false;
                                        messageBeans.addAll(result.getData().getList());
                                        platFormAdapter.notifyDataSetChanged();
                                    } else {//表示没有更多数据了
                                        is_not_more = true;
                                        messageBeans.addAll(result.getData().getList());
                                        platFormAdapter.notifyDataSetChanged();
                                    }
                                }
                            }

                            @Override
                            public void onError(String error) {

                            }
                        }, getActivity(), false));
    }

    private void change_read_type(String message_number) {
        Map<String, Object> params = new HashMap<>();
        params.put("messageNum", message_number);//用户编号
        params.put("type", "3");
        Subscription subscription = Network.getInstance("变成已读模式", getActivity())
                .messageRead(params, new ProgressSubscriber<>("变成已读模式", new SubscriberOnNextListener<Bean<Object>>() {
                    @Override
                    public void onNext(Bean<Object> result) {
                        Log.e("变成已读模式成功", result.getMessage() + "变成已读模式成功");
//                        MessageEvent messageEvent = new MessageEvent("update_message_num");
//                        EventBus.getDefault().postSticky(messageEvent);
                        EventBus.getDefault().post("消息已读");
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("变成已读模式失败", "变成已读模式失败:" + error);
                    }
                }, getActivity(), false));
    }


    private void initView() {
        initMessageDate();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (is_go_to_detail_back) {
            page = 1;
            //填写刷新数据的网络请求，一般page=1，List集合清空操作
            xrefreshview.startRefresh();
            initMessageDate();
            xrefreshview.stopRefresh();//刷新停止
        }

    }

    private void set_list_resource() {
        //设置上拉刷新下拉加载
        recycler_view.setHasFixedSize(false);
        recycler_view.setItemAnimator(null);
        layoutManager = new LinearLayoutManager(getActivity());
        recycler_view.setLayoutManager(layoutManager);
        platFormAdapter = new PlatFormAdapter(messageBeans, getActivity());
        platFormAdapter.notifyDataSetChanged();
        platFormAdapter.setOnItemClickListener(new PlatFormAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (BaseUtils.isFastClick()){
                    change_read_type(messageBeans.get(position).getMessageNum());

                    Intent intent = new Intent(getActivity(), MessageDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("message_item", messageBeans.get(position));
                    intent.putExtras(bundle);
                    startActivity(intent);

                    is_go_to_detail_back = true;
                }

            }
        });
        recycler_view.setAdapter(platFormAdapter);
        // 静默加载模式不能设置footerview
        // 设置静默加载模式
        //xrefreshview.setSilenceLoadMore(true);
        //设置刷新完成以后，headerview固定的时间
        xrefreshview.setPinnedTime(1000);
        xrefreshview.setMoveForHorizontal(true);
        //xrefreshview.setPullRefreshEnable(true);
        xrefreshview.setPullLoadEnable(true);//关闭加载更多
        xrefreshview.setAutoLoadMore(false);
        xrefreshview.enableRecyclerViewPullUp(true);
        xrefreshview.enablePullUpWhenLoadCompleted(true);
        //给recycler_view设置底部加载布局
        if (messageBeans.size() > 9) {
            xrefreshview.enableReleaseToLoadMore(true);
            platFormAdapter.setCustomLoadMoreView(new XRefreshViewFooter(getActivity()));//加载更多
            xrefreshview.setLoadComplete(false);//显示底部
        } else {
            xrefreshview.enableReleaseToLoadMore(false);
            xrefreshview.setLoadComplete(true);//隐藏底部
        }
        //设置静默加载时提前加载的item个数
//        xefreshView1.setPreLoadCount(4);

        xrefreshview.setOnRecyclerViewScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        xrefreshview.setXRefreshViewListener(new XRefreshView.SimpleXRefreshListener() {
            @Override
            public void onRefresh(boolean isPullDown) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        page = 1;
                        //填写刷新数据的网络请求，一般page=1，List集合清空操作
//                        messageBeans.clear();
                        initMessageDate();
                        xrefreshview.stopRefresh();//刷新停止


                    }
                }, 1000);//2000是刷新的延时，使得有个动画效果
            }

            @Override
            public void onLoadMore(boolean isSilence) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        page = page + 1;
                        initMessageDate();
                        //填写加载更多的网络请求，一般page++
//                        //没有更多数据时候
                        if (is_not_more) {
                            xrefreshview.setLoadComplete(true);
                        } else {
                            //刷新完成必须调用此方法停止加载
                            xrefreshview.stopLoadMore(true);
                        }


                    }
                }, 1000);//1000是加载的延时，使得有个动画效果


            }
        });
    }


}

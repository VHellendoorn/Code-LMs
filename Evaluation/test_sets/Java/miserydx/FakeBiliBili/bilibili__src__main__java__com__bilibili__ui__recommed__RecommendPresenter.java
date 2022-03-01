package com.bilibili.ui.recommed;

import android.util.Log;

import com.bilibili.model.api.ApiHelper;
import com.bilibili.model.api.RecommendApis;
import com.bilibili.model.bean.recommend.AppIndex;
import com.bilibili.model.bean.DataListResponse;
import com.bilibili.ui.bangumi.BangumiFragment;
import com.bilibili.ui.recommed.viewbinder.RecommendBannerItemViewBinder;
import com.common.base.AbsBasePresenter;
import com.common.util.DateUtil;

import javax.inject.Inject;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import me.drakeet.multitype.Items;

/**
 * Created by miserydx on 17/7/6.
 */

public class RecommendPresenter extends AbsBasePresenter<RecommendContract.View> implements RecommendContract.Presenter {

    private static final String TAG = RecommendPresenter.class.getSimpleName();
    private static final int LOGIN_EVENT_NORMAL = 0;
    private static final int LOGIN_EVENT_INITIAL = 1;
    private static final String OPEN_EVENT_NULL = "";
    private static final String OPEN_EVENT_COLD = "cold";
    private static final int STYLE = 2;
    public static final int STATE_NORMAL = 0;
    public static final int STATE_INITIAL = 1;
    public static final int STATE_REFRESHING = 2;
    public static final int STATE_LOAD_MORE = 3;

    private RecommendApis recommendApis;
    private int loginEvent;
    private String openEvent;
    private boolean pull;
    private int state;
    private int idx;

    @Inject
    public RecommendPresenter(RecommendApis recommendApis) {
        this.recommendApis = recommendApis;
    }

    @Override
    public void loadData() {
        state = STATE_INITIAL;
        getIndex(state);
    }

    @Override
    public void pullToRefresh(int idx) {
        if (state == STATE_REFRESHING) {
            return;
        }
        state = STATE_REFRESHING;
        this.idx = idx;
        getIndex(state);
    }

    @Override
    public void loadMore(int idx) {
        if (state == STATE_LOAD_MORE) {
            return;
        }
        state = STATE_LOAD_MORE;
        this.idx = idx;
        getIndex(state);
    }


    /**
     * 列表数据接口
     *
     * @param operationState 请求状态：初次请求，下拉刷新，上滑加载更多
     */
    private void getIndex(final int operationState) {
        switch (operationState) {
            case STATE_INITIAL:
                loginEvent = LOGIN_EVENT_INITIAL;
                openEvent = OPEN_EVENT_COLD;
                pull = true;
                break;
            case STATE_REFRESHING:
                loginEvent = LOGIN_EVENT_NORMAL;
                openEvent = OPEN_EVENT_NULL;
                pull = true;
                break;
            case STATE_LOAD_MORE:
                loginEvent = LOGIN_EVENT_NORMAL;
                openEvent = OPEN_EVENT_NULL;
                pull = false;
                break;
        }
        recommendApis.getIndex(ApiHelper.APP_KEY,
                ApiHelper.BUILD,
                idx,
                loginEvent,
                ApiHelper.MOBI_APP,
                ApiHelper.NETWORK_WIFI,
                openEvent,
                ApiHelper.PLATFORM,
                pull,
                STYLE,
                DateUtil.getSystemTime())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .map(new Function<DataListResponse<AppIndex>, Items>() {

                    @Override
                    public Items apply(DataListResponse<AppIndex> appIndexDataListResponse) throws Exception {
                        Items items = new Items();
                        for (AppIndex appIndex : appIndexDataListResponse.getData()) {
                            if (appIndex.getBanner_item() != null) {
                                RecommendBannerItemViewBinder.Banner banner = new RecommendBannerItemViewBinder.Banner();
                                banner.setBannerItemList(appIndex.getBanner_item());
                                banner.set_goto(appIndex.getGoto());
                                banner.setIdx(appIndex.getIdx());
                                banner.setParam(appIndex.getParam());
                                items.add(banner);
                            } else {
                                items.add(appIndex);
                            }
                        }
                        return items;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Items>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        registerRx(d);
                        if (operationState == STATE_INITIAL || operationState == STATE_REFRESHING) {
                            mView.onRefreshingStateChanged(true);
                        }
                    }

                    @Override
                    public void onNext(Items items) {
                        mView.onDataUpdated(items, operationState);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError");
                        e.printStackTrace();
                        mView.onRefreshingStateChanged(false);
                        mView.showLoadFailed();
                    }

                    @Override
                    public void onComplete() {
                        state = STATE_NORMAL;
                    }
                });
    }

    @Override
    public void releaseData() {

    }
}
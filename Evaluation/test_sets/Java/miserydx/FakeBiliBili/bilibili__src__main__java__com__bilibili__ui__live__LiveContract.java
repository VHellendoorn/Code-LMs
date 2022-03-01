package com.bilibili.ui.live;

import com.common.base.BasePresenter;
import com.common.base.BaseView;

import me.drakeet.multitype.Items;

/**
 * Created by Android_ZzT on 17/6/18.
 */

public interface LiveContract {

    interface View extends BaseView {

        void onDataUpdated(Items items);

        void onRefreshingStateChanged(boolean isRefresh);

        void showLoadFailed();
    }

    interface Presenter extends BasePresenter {

    }
}

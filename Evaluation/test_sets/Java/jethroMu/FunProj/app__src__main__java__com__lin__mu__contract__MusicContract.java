package com.lin.mu.contract;

import com.lin.mu.base.BaseView;
import com.lin.mu.model.Music;
import com.lin.mu.model.PhotoModel;

import java.util.List;

/**
 * Created by lin on 2016/8/2.
 */
public interface MusicContract {

    interface View extends BaseView {
        void onLoadSuccess(List<Music.ResultBean.SongsBean> lists);
    }

    interface Presenter {
        void loadMusciData(String type, String key, String limit, String offset);
    }
}

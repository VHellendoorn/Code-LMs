package com.common.widget.adapter;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.common.widget.adapter.binder.DefaultLoadFailedBinder;
import com.common.widget.adapter.binder.DefaultLoadMoreBinder;
import com.common.widget.adapter.binder.DefaultLoadingBinder;

/**
 * 实现加载更多、加载失败、Loading功能的AdapterWrapper
 * Created by miserydx on 18/1/17.
 */
public class DefaultAdapterWrapper<T extends RecyclerView.Adapter> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = DefaultAdapterWrapper.class.getSimpleName();

    private static final int ITEM_TYPE_LOAD_MORE = Integer.MAX_VALUE - 1;

    private static final int ITEM_TYPE_LOADING = Integer.MAX_VALUE - 2;

    private static final int ITEM_TYPE_LOAD_FAILED = Integer.MAX_VALUE - 3;

    private static final int STATE_DEFAULT = 0x00000000;

    private static final int STATE_LOADING = 0x00000001;

    private static final int STATE_LOAD_FAILED = 0x00000002;

    /**
     * 加载更多ItemViewBinder
     */
    private BaseLoadMoreBinder loadMoreBinder = new DefaultLoadMoreBinder();

    /**
     * 加载失败ItemViewBinder
     */
    private BaseLoadFailedBinder loadFailedBinder = new DefaultLoadFailedBinder();

    /**
     * Loading ItemViewBinder
     */
    private BaseLoadingBinder loadingBinder = new DefaultLoadingBinder();

    /**
     * 是否启用加载更多
     */
    private boolean loadMoreEnabled = false;

    /**
     * 当前状态
     */
    private int state = STATE_DEFAULT;

    /**
     * onLoadMore是否在进行中
     */
    private boolean isLoading = false;

    /**
     * 加载更多监听器
     */
    private OnLoadMoreListener onLoadMoreListener;

    /**
     * 目标Adapter
     */
    protected T innerAdapter;

    /**
     * 滑动事件处理
     */
    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!ViewCompat.canScrollVertically(recyclerView, 1)
                    && !isLoading
                    && loadMoreBinder.getState() == BaseLoadMoreBinder.STATE_LOAD_MORE) {
                if (onLoadMoreListener != null) {
                    isLoading = true;
                    onLoadMoreListener.onLoadMore();
                }
            }
        }
    };

    public DefaultAdapterWrapper(T adapter) {
        innerAdapter = adapter;
    }

    @Override
    public final int getItemViewType(int position) {
        if (state == STATE_LOADING) {
            return ITEM_TYPE_LOADING;
        } else if (state == STATE_LOAD_FAILED) {
            return ITEM_TYPE_LOAD_FAILED;
        } else if (isShowLoadMore(position)) {
            return ITEM_TYPE_LOAD_MORE;
        }
        return innerAdapter.getItemViewType(position);
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
        innerAdapter.setHasStableIds(hasStableIds);
    }

    @Override
    public final long getItemId(int position) {
        if (state == STATE_LOADING || state == STATE_LOAD_FAILED) {
            return super.getItemId(position);
        } else if (isShowLoadMore(position)) {
            return ITEM_TYPE_LOAD_MORE;
        }
        return innerAdapter.getItemId(position);
    }

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_TYPE_LOAD_MORE:
                return loadMoreBinder.onCreateViewHolder(LayoutInflater.from(parent.getContext()), parent);
            case ITEM_TYPE_LOADING:
                return loadingBinder.onCreateViewHolder(LayoutInflater.from(parent.getContext()), parent);
            case ITEM_TYPE_LOAD_FAILED:
                return loadFailedBinder.onCreateViewHolder(LayoutInflater.from(parent.getContext()), parent);
            default:
                return innerAdapter.onCreateViewHolder(parent, viewType);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case ITEM_TYPE_LOAD_MORE:
                loadMoreBinder.onBindViewHolder((BaseViewHolder) holder);
                break;
            case ITEM_TYPE_LOADING:
                loadingBinder.onBindViewHolder((BaseViewHolder) holder);
                break;
            case ITEM_TYPE_LOAD_FAILED:
                loadFailedBinder.onBindViewHolder((BaseViewHolder) holder);
                break;
            default:
                innerAdapter.onBindViewHolder(holder, position);
                break;
        }

    }

    @Override
    public final int getItemCount() {
        if (innerAdapter.getItemCount() == 0
                && (state == STATE_LOADING || state == STATE_LOAD_FAILED)) {
            return 1;
        } else if (state == STATE_LOADING || state == STATE_LOAD_FAILED) {
            Log.d(TAG, "You can not call method showLoading() or showLoadFailed() with a empty data list.");
        }
        state = STATE_DEFAULT;
        return innerAdapter.getItemCount() == 0 ? 0 : innerAdapter.getItemCount() + (hasLoadMore() ? 1 : 0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        if (!isBaseViewHolder(holder)) {
            innerAdapter.onViewRecycled(holder);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
        if (!isBaseViewHolder(holder)) {
            return innerAdapter.onFailedToRecycleView(holder);
        }
        return super.onFailedToRecycleView(holder);
    }

    /**
     * 如果需要把{@link GridLayoutManager.SpanSizeLookup}传入自定义的{@link RecyclerView.ItemDecoration}
     * 中，因为这里包裹了一层，所以不能直接传入用做参数，用时应从RecyclerView对象中获取
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(mOnScrollListener);
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            final GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            final GridLayoutManager.SpanSizeLookup oldSpanSizeLookup = layoutManager.getSpanSizeLookup();
            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (getItemViewType(position) == ITEM_TYPE_LOAD_MORE
                            || getItemViewType(position) == ITEM_TYPE_LOADING
                            || getItemViewType(position) == ITEM_TYPE_LOAD_FAILED) {
                        return layoutManager.getSpanCount();
                    } else {
                        return oldSpanSizeLookup.getSpanSize(position);
                    }
                }
            });
        }
        innerAdapter.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        recyclerView.removeOnScrollListener(mOnScrollListener);
        innerAdapter.onDetachedFromRecyclerView(recyclerView);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        if (!isBaseViewHolder(holder)) {
            innerAdapter.onViewAttachedToWindow(holder);
        } else {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        if (!isBaseViewHolder(holder)) {
            innerAdapter.onViewDetachedFromWindow(holder);
        }
    }

    @Override
    public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(observer);
        innerAdapter.registerAdapterDataObserver(observer);
    }

    @Override
    public void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        super.unregisterAdapterDataObserver(observer);
        innerAdapter.unregisterAdapterDataObserver(observer);
    }

    public void setInnerAdapter(T innerAdapter) {
        this.innerAdapter = innerAdapter;
    }

    public T getInnerAdapter() {
        return innerAdapter;
    }

    public void setOnLoadMoreListener(@NonNull DefaultAdapterWrapper.OnLoadMoreListener listener) {
        loadMoreEnabled = true;
        onLoadMoreListener = listener;
    }

    public void setOnClickRetryListener(@NonNull DefaultAdapterWrapper.OnClickRetryListener listener) {
        if (loadMoreBinder != null) {
            loadMoreBinder.setOnClickRetryListener(listener);
        }
    }

    /**
     * 通知加载更多状态完成
     */
    public final void loadMoreComplete() {
        if (hasLoadMore()) {
            isLoading = false;
            loadMoreBinder.loadMoreComplete();
            notifyItemInserted(getItemCount());
        }
    }

    /**
     * 设置是否启用加载更多
     */
    public final void setLoadMoreEnabled(boolean flag) {
        loadMoreEnabled = flag;
        if (loadMoreEnabled) {
            notifyItemInserted(getItemCount());
        } else {
            notifyItemRemoved(getItemCount());
        }
    }


    /**
     * 设置LoadMoreItem，默认不显示
     */
    @SuppressWarnings("unchecked")
    public final void setLoadMoreBinder(@NonNull BaseLoadMoreBinder binder) {
        loadMoreBinder = binder;
    }

    /**
     * 设置LoadFailedItem，默认不显示
     */
    @SuppressWarnings("unchecked")
    public final void setLoadFailedBinder(@NonNull BaseLoadFailedBinder binder) {
        loadFailedBinder = binder;
    }

    /**
     * 设置LoadingItem，默认不显示
     */
    @SuppressWarnings("unchecked")
    public final void setLoadingBinder(@NonNull BaseLoadingBinder binder) {
        loadingBinder = binder;
    }

    /**
     * 显示加载失败
     * 当目标Adapter的getItemCount返回0时此方法才会有效
     */
    @SuppressWarnings("unchecked")
    public final void showLoadFailed() {
        if (loadFailedBinder != null) {
            state = STATE_LOAD_FAILED;
            notifyDataSetChanged();
        }
    }

    /**
     * 显示Loading
     * 当目标Adapter的getItemCount返回0时此方法才会有效
     */
    @SuppressWarnings("unchecked")
    public final void showLoading() {
        if (loadingBinder != null) {
            state = STATE_LOADING;
            notifyDataSetChanged();
        }
    }

    public final boolean isLoading() {
        return state == STATE_LOADING;
    }

    public final boolean isLoadFailed() {
        return state == STATE_LOAD_FAILED;
    }

    /**
     * 设置默认footer显示没有更多
     */
    public final void showNoMore() {
        changeLoadMoreState(BaseLoadMoreBinder.STATE_NO_MORE);
    }

    /**
     * 设置默认footer显示加载失败
     */
    public final void showFailToLoadMore() {
        changeLoadMoreState(BaseLoadMoreBinder.STATE_LOAD_FAIL);
    }

    private void changeLoadMoreState(int state) {
        if (hasLoadMore()) {
            loadMoreBinder.setState(state);
            notifyItemChanged(getItemCount() - 1);
        }
    }

    private boolean hasLoadMore() {
        return loadMoreEnabled && loadMoreBinder != null && onLoadMoreListener != null;
    }

    private boolean isShowLoadMore(int position) {
        return hasLoadMore() && (position == innerAdapter.getItemCount());
    }

    private boolean isBaseViewHolder(RecyclerView.ViewHolder holder) {
        return holder instanceof BaseViewHolder;
    }

    /**
     * 加载更多的回调接口
     */
    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    /**
     * 加载更多失败，点击重试的回调接口
     */
    public interface OnClickRetryListener {
        void onClickRetry();
    }

}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Android.App;
using Android.Content;
using Android.OS;
using Android.Runtime;
using Android.Views;
using Android.Widget;
using Android.Support.V7.Widget;
using EasyAdapterLibrary.Helpers;
using Android.Util;
using Java.Lang;

namespace EasyAdapterLibrary.RecyclerViews
{
    public abstract class EasyRVAdapter<T> : RecyclerView.Adapter, DataHelper<T>
    {

        protected Context mContext;
        protected List<T> mList;
        protected int[] layoutIds;
        protected LayoutInflater mLInflater;

        private SparseArray<View> mConvertViews = new SparseArray<View>();

        public EasyRVAdapter(Context context, List<T> list, params int[] layoutIds)
        {
            this.mContext = context;
            this.mList = list;
            this.layoutIds = layoutIds;
            this.mLInflater = LayoutInflater.From(mContext);
        }

        public override RecyclerView.ViewHolder OnCreateViewHolder(ViewGroup parent, int viewType)
        {
            if (viewType < 0 || viewType > layoutIds.Length)
            {
                throw new ArrayIndexOutOfBoundsException("layoutIndex");
            }
            if (layoutIds.Length == 0)
            {
                throw new IllegalArgumentException("not layoutId");
            }
            int layoutId = layoutIds[viewType];
            View view = mConvertViews.Get(layoutId);
            if (view == null)
            {
                view = mLInflater.Inflate(layoutId, parent, false);
            }
            EasyRVHolder viewHolder = (EasyRVHolder)view.Tag;
            if (viewHolder == null || viewHolder.getLayoutId() != layoutId)
            {
                viewHolder = new EasyRVHolder(mContext, layoutId, view);
                return viewHolder;
            }
            return viewHolder;
        }

        public override void OnBindViewHolder(RecyclerView.ViewHolder holder, int position)
        {
            T item = mList[position];
            OnBindData(holder, position, item);
        }

        public override int GetItemViewType(int position)
        {
            return getLayoutIndex(position, mList[position]);
        }

        public override int ItemCount => mList == null ? 0 : mList.Count();

        /**
         * 指定item布局样式在layoutIds的索引。默认为第一个
         *
         * @param position
         * @param item
         * @return
         */
        public int getLayoutIndex(int position, T item)
        {
            return 0;
        }

        protected abstract void OnBindData(RecyclerView.ViewHolder viewHolder, int position, T item);

        public void addAll(List<T> list)
        {
            mList.AddRange(list);
            NotifyDataSetChanged();
        }

        public void addAll(int position, List<T> list)
        {
            mList.InsertRange(position, list);
            NotifyDataSetChanged();
        }

        public void add(T data)
        {
            mList.Add(data);
            NotifyDataSetChanged();
        }

        public void add(int position, T data)
        {
            mList.Insert(position, data);
            NotifyDataSetChanged();
        }

        public void clear()
        {
            mList.Clear();
            NotifyDataSetChanged();
        }

        public bool contains(T data)
        {
            return mList.Contains(data);
        }

        public T getData(int index)
        {
            return mList[index];
        }

        public void modify(T oldData, T newData)
        {
            modify(mList.IndexOf(oldData), newData);
        }

        public void modify(int index, T newData)
        {
            mList[index] = newData;
            NotifyDataSetChanged();
        }

        public bool remove(T data)
        {
            bool result = mList.Remove(data);
            NotifyDataSetChanged();
            return result;
        }

        public void remove(int index)
        {
            mList.RemoveAt(index);
            NotifyDataSetChanged();
        }
    }
}
package com.ld.qmwj.view.popuplist;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ld.qmwj.R;

import java.util.List;

/**
 * RecyclerView控件，是更高级更灵活的ListView版本，是展示大数据集的视图容器
 * 通过保持有限view的方式实现高效滚动
 * 当你有一个数据集合，并且数据在运行时会因用户操作或网络事件而变化，用RecyclerView吧
 * <p/>
 * 布局管理器LayoutManager：把item view放进RecyclerView中并自动决定何时回收复用不可见的item view
 * 内置的布局管理器：LinearLayoutManager水平/竖直列表 GridLayoutManager网格布局
 * StaggeredGridLayoutManager交错布局/瀑布流式布局
 */
public class PopupListAdapter extends RecyclerView.Adapter<PopupListAdapter.ViewHolder> {

    private List<String> mDatas;
    private OnPopupListClickListener onPopupListClickListener;

    /**
     * 弹出菜单的上下文View，即要弹出菜单的那个列表项
     */
    private View contextView;

    /**
     * 点击的上下文列表项位置
     */
    private int contextPosition;

    /**
     * PopupWindow管理类PopupList实例
     */
    private PopupList popupList;

    /**
     * 为每个数据项提供视图引用
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;

        public ViewHolder(View v) {
            super(v);
        }
    }

    /**
     * 根据数据集的类型书写合适的构造器
     *
     * @param mDatas 数据集
     */
    public PopupListAdapter(PopupList popupList, List<String> mDatas) {
        this.popupList = popupList;
        this.mDatas = mDatas;
    }

    /**
     * 创建新的view - 由LayoutManager布局管理器调用
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_popup_list, parent, false);
        ViewHolder vh = new ViewHolder(v);
        vh.mTextView = (TextView) v.findViewById(R.id.tv_popup);
        //设置view的大小、内边距、外边距、布局参数
        //...
        return vh;
    }

    //替换view的内容，以避免每次都创建不必要的view或者执行findViewById()的昂贵开销 - 由LayoutManager布局管理器调用
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //获取数据集中position位置的数据并用其替换view的内容
        holder.mTextView.setText(mDatas.get(position));
        //如果设置了回调，则设置点击事件
        if (onPopupListClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onPopupListClickListener.onPopupListItemClick(contextView, contextPosition, holder.itemView, position);
                    popupList.hiddenPopupWindow();
                }
            });

        }
    }

    //数据集的大小 - 由LayoutManager布局管理器调用
    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public OnPopupListClickListener getOnPopupListClickListener() {
        return onPopupListClickListener;
    }

    public void setOnPopupListClickListener(OnPopupListClickListener onPopupListClickListener) {
        this.onPopupListClickListener = onPopupListClickListener;
    }

    public interface OnPopupListClickListener {
        void onPopupListItemClick(View contextView, int contextPosition, View view, int position);
    }

    public View getContextView() {
        return contextView;
    }

    public void setContextView(View contextView) {
        this.contextView = contextView;
    }

    public int getContextPosition() {
        return contextPosition;
    }

    public void setContextPosition(int contextPosition) {
        this.contextPosition = contextPosition;
    }
}

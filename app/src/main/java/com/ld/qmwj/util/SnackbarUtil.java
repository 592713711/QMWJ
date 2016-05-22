package com.ld.qmwj.util;

import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.ld.qmwj.R;

/**
 * SnackBar工具类
 * Created by zsg on 2016/5/11.
 */
public class SnackbarUtil {

    /**
     * 向snackbar布局中添加view
     *
     * @param snackbar
     * @param layoutId 新添加布局 id
     * @param index    添加的位置
     */
    public static void SnackbarAddView(Snackbar snackbar, int layoutId, int index) {
        View snackbarview = snackbar.getView();//获取snackbar的View(其实就是SnackbarLayout)

        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbarview;//将获取的View转换成SnackbarLayout

        View add_view = LayoutInflater.from(snackbarview.getContext()).inflate(layoutId, null);//加载布局文件新建View

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);//设置新建布局参数

        p.gravity = Gravity.CENTER_VERTICAL;//设置新建布局在Snackbar内垂直居中显示

        snackbarLayout.addView(add_view, index, p);//将新建布局添加进snackbarLayout相应位置
    }

    /**
     * 为snackbar中指定按钮控件添加点击监听器
     * @param snackbar
     * @param btn_id
     * @param onClickListener
     */
    public static void SetAction(Snackbar snackbar, int btn_id, String action_string, View.OnClickListener onClickListener) {
        View view = snackbar.getView();//获取Snackbar的view
        if (view != null) {
            //为添加的按钮设置监听器
            ((Button)view.findViewById(btn_id)).setText(action_string);
            (view.findViewById(btn_id)).setOnClickListener(onClickListener);
        }
    }
}

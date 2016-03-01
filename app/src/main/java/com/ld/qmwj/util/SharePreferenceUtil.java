package com.ld.qmwj.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.ld.qmwj.model.User;

/**
 * 用来操作SharePreference的工具类
 * Created by zsg on 2016/2/16.
 */
public class SharePreferenceUtil {

    public static final String USER_KEY = "user";     //用户信息标识

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private Gson gson;

    public SharePreferenceUtil(Context context, String file) {
        sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        editor = sp.edit();
        gson = new Gson();
    }

    /**
     * 返回sp中的User信息 若没有则返回空
     *
     * @return
     */
    public User getUser() {
        User user;
        String userTemp = sp.getString(USER_KEY, "null");
        if (userTemp.equals("null")) {
            user = null;
        } else {
            //将gson字符串转化为user对象
            user = gson.fromJson(userTemp, User.class);
        }

        return user;
    }

    /**
     * 将用户信息写入sp
     * @param user
     */
    public void writeUser(User user) {
        if (user != null) {
            String userTemp = gson.toJson(user);
            editor.putString(USER_KEY,userTemp);
            editor.commit();
        }
    }
}

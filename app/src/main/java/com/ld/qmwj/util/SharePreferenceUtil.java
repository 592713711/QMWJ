package com.ld.qmwj.util;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;


import com.google.gson.Gson;
import com.ld.qmwj.Config;
import com.ld.qmwj.model.User;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 用来操作SharePreference的工具类
 * Created by zsg on 2016/2/16.
 */
public class SharePreferenceUtil {

    public static final String USER_KEY = "user";     //用户信息标识
    public static final String BAND_KEY="miband";     //手环信息标识

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

    /**
     * 得到绑定手环的蓝牙设备
     */
    public BluetoothDevice getDevice(){
        String deviceJson=sp.getString(BAND_KEY, null);
        BluetoothDevice device=null;
        if(deviceJson==null)
            return device;
        device=gson.fromJson(deviceJson, BluetoothDevice.class);
        return device;
    }

    public SharedPreferences getSp(){
        return sp;
    }

    public void writeDevice(BluetoothDevice device) {
        //将蓝牙设备按 字节流写入选项储存中
        String deviceJson=gson.toJson(device);
        editor.putString(BAND_KEY,deviceJson);
        editor.commit();


    }
}

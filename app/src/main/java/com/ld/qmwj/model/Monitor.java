package com.ld.qmwj.model;

import java.io.Serializable;

/**
 * 用户所监护/被监护 对象类
 *
 * @author zsg
 */
public class Monitor implements Serializable{
    public int id;        //该对象id
    public String username;    //该对象用户名
    public String remark_name;        //该对象备注名
    public int identify;            //用户user对于monitor的身份  0被监护方  1 主监护方  2 副监护
    public int icon;            //记录头像编号
    public int state;           //用户当前状态
    public int bandstate;       //手环状态
    public int safe;            //位置安全
    public String location_msg;        //位置信息
    public int heart_rate;      //最后一次心跳信息

    @Override
    public String toString() {
        return "Monitor{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", remark_name='" + remark_name + '\'' +
                ", identify=" + identify +
                ", icon=" + icon +
                ", state=" + state +
                '}';
    }
}

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
    public int status;            //该对象身份
    public int icon;            //记录头像编号

    @Override
    public String toString() {
        return "Monitor{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", remark_name='" + remark_name + '\'' +
                ", status=" + status +
                ", icon=" + icon +
                '}';
    }

}

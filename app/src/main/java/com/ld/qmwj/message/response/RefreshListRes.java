package com.ld.qmwj.message.response;

import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.model.Monitor;

import java.util.ArrayList;



/**
 * 刷新列表响应
 * @author zsg
 *
 */
public class RefreshListRes extends Response{
    public ArrayList<Monitor> monitorList;		//	用户监护/被监护的列表
    public RefreshListRes(){
        tag= MessageTag.REFRESHLIST_RES;
    }
}

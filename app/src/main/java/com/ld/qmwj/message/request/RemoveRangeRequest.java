package com.ld.qmwj.message.request;

import com.ld.qmwj.message.MessageTag;

/**
 * 删除安全区域请求
 * Created by zsg on 2016/4/29.
 */
public class RemoveRangeRequest extends Request{
    public int pos;        // 该安全区域位置
    public RemoveRangeRequest(){
        this.tag= MessageTag.REMOVERANGE_REQ;
    }
}

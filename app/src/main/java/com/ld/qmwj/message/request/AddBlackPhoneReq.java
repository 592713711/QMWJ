package com.ld.qmwj.message.request;

import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.model.BlackPhone;

/**
 * 添加黑名单请求
 * Created by zsg on 2016/5/4.
 */
public class AddBlackPhoneReq extends Request {
    public BlackPhone blackPhone;

    public AddBlackPhoneReq() {
        this.tag = MessageTag.ADDBLACKPHONE_REQ;
    }
}

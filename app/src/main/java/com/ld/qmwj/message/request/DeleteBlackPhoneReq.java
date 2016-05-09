package com.ld.qmwj.message.request;

import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.model.BlackPhone;

/**
 * Created by zsg on 2016/5/4.
 */
public class DeleteBlackPhoneReq extends Request {
    public BlackPhone blackPhone;

    public DeleteBlackPhoneReq() {
        this.tag = MessageTag.DELETEBLACKPHONE_REQ;
    }
}

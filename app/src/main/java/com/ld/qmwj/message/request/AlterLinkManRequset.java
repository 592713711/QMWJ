package com.ld.qmwj.message.request;

import com.ld.qmwj.model.Contacts;

/**
 * Created by zsg on 2016/5/4.
 */
public class AlterLinkManRequset extends Request{
    public Contacts contacts;
    public Contacts oldcontacts;
    public AlterLinkManRequset(int tag){
        this.tag=tag;
    }
}

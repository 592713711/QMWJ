package com.ld.qmwj.message.request;

import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.model.RecordVoice;

/**
 * Created by zsg on 2016/5/10.
 */
public class RecordRequest extends Request{
    public RecordVoice recordVoice;
    public RecordRequest(){
        this.tag= MessageTag.RECORD_REQ;
    }

}

package com.ld.qmwj.message.response;

import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.model.User;

/**
 * 登陆响应
 * 若是自动登录或登陆失败  则响应的user，refreshListRes为空
 * @author zsg
 *
 */
public class LoginResponse extends Response{
    public User user;		//登陆用户的信息
    public boolean isSuccess;		//是否登陆成功
    public RefreshListRes refreshListRes;	//刷新列表响应

    public LoginResponse() {
        tag= MessageTag.LOGIN_RES;
    }
}

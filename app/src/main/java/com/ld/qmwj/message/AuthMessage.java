package com.ld.qmwj.message;


import com.ld.qmwj.model.Authority;

/**
 * 权限消息
 * @author zsg
 *
 */
public class AuthMessage extends Message{
	public Authority authority;
	public AuthMessage(){
		this.tag=MessageTag.AUTH_MSG;
	}

}

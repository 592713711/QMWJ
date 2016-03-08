package com.ld.qmwj.util;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.ld.qmwj.R;


/**
 * 登陆时 弹出的等待自定义对话框
 */

public class WaitDialog extends DialogFragment
{

	private String mMsg = "Loading";

	public void setMsg(String msg)
	{
		this.mMsg = msg;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_loading, null);
		TextView title = (TextView) view
				.findViewById(R.id.id_dialog_loading_msg);
		title.setText(mMsg);
		Dialog dialog = new Dialog(getActivity(), R.style.dialog);
		dialog.setContentView(view);
		return dialog;
	}
}

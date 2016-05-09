package com.ld.qmwj.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;
import com.ld.qmwj.R;
import com.ld.qmwj.dao.BlacklistDao;
import com.ld.qmwj.dao.LinkManDao;
import com.ld.qmwj.message.MessageTag;
import com.ld.qmwj.message.request.AddBlackPhoneReq;
import com.ld.qmwj.message.request.AlterLinkManRequset;
import com.ld.qmwj.model.BlackPhone;
import com.ld.qmwj.model.Contacts;
import com.ld.qmwj.model.Monitor;

/**
 * 输入信息对话框
 */
public class EditActivity extends AppCompatActivity {

    private EditText et_name = null;
    private EditText et_num = null;
    private Button btn_ok = null;
    private Button btn_cancel = null;
    public String oldName = "";
    public String oldNum = "";
    //指令
    String command = null;
    private Monitor monitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        monitor = (Monitor) getIntent().getSerializableExtra("monitor");
        initView();
        initData();
    }

    private void initView() {
        et_name = (EditText) findViewById(R.id.contacts_name);
        et_num = (EditText) findViewById(R.id.contacts_num);
        btn_ok = (Button) findViewById(R.id.btn_ok);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);

        btn_ok.setOnClickListener(new ButtonListener());
        btn_cancel.setOnClickListener(new ButtonListener());
    }

    private void initData() {
        Intent intent = getIntent();
        //获得指令
        command = intent.getStringExtra(Config.CONTACTS);
        //判断是修改还是新建

        if (command.equals(Config.DOUPDATE)) { //更新联系人
            //设置title
            this.setTitle("修改联系人");
            oldName = intent.getStringExtra(Config.CONTACTS_NAME);
            oldNum = intent.getStringExtra(Config.CONTACTS_NUM);
            et_name.setText(oldName);
            et_num.setText(oldNum);
        } else if (command.equals(Config.DONEW)) {//新建联系人
            this.setTitle("新建联系人");
        } else if (command.equals(Config.DOADD)) {//增加黑名单联系人
            //将输入名字的输入框彻底隐藏
            et_name.setVisibility(EditText.GONE);
            this.setTitle("添加黑名单");
        }
    }

    class ButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_ok) {
                if (command.equals(Config.DOUPDATE)) {  //修改联系人
                    doUpdateLinkMan();
                } else if (command.equals(Config.DONEW)) {
                    //新建联系人
                    doAddLinkMan();

                } else if (command.equals(Config.DOADD)) {
                    //添加黑名单
                    doAddBlackPhone();

                }
            }

            EditActivity.this.finish();
        }
    }

    /**
     * 更新联系人
     */
    private void doUpdateLinkMan() {
        String name = et_name.getText().toString();
        String phonenum = et_num.getText().toString();
        if (name.trim().isEmpty()) {
            Toast.makeText(EditActivity.this, "姓名不能为空", Toast.LENGTH_SHORT).show();
            return;
        } else if (phonenum.trim().isEmpty()) {
            Toast.makeText(EditActivity.this, "号码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        Contacts contacts = new Contacts(name, phonenum);

        //修改数据库中的数据  先删除后添加
        MyApplication.getInstance().getLinkManDao().deleteLinkMan(monitor.id,oldNum);
        MyApplication.getInstance().getLinkManDao().insertContact(monitor.id,contacts);

        //向服务器发送消息
        AlterLinkManRequset request = new AlterLinkManRequset(MessageTag.UPDATELINKMAN_REQ);
        request.from_id = MyApplication.getInstance().getSpUtil().getUser().id;
        request.into_id = monitor.id;
        request.contacts = contacts;
        request.oldcontacts=new Contacts(oldName,oldNum);
        MyApplication.getInstance().getSendMsgUtil().sendCacheMessageToServer(
                MyApplication.getInstance().getGson().toJson(request)
        );
        Toast.makeText(EditActivity.this, "修改联系人成功", Toast.LENGTH_SHORT).show();

    }

    /**
     * 添加黑名单
     */
    private void doAddBlackPhone() {
        BlackPhone blackphone = new BlackPhone();
        blackphone.phonenum = et_num.getText().toString();
        BlacklistDao dao = MyApplication.getInstance().getBlacklistDao();
        //先判断黑名单上有没有改号码
        if (dao.isAlreadyadd(monitor.id, blackphone.phonenum)) {
            //存在
            Toast.makeText(EditActivity.this, "该号码已经是黑名单", Toast.LENGTH_SHORT).show();
        } else {
            //不存在
            //添加到数据库中
            dao.insertBlackPhone(monitor.id, blackphone);

            //发送消息到服务器
            AddBlackPhoneReq request = new AddBlackPhoneReq();
            request.from_id = MyApplication.getInstance().getSpUtil().getUser().id;
            request.into_id = monitor.id;
            request.blackPhone = blackphone;
            MyApplication.getInstance().getSendMsgUtil().sendCacheMessageToServer(
                    MyApplication.getInstance().getGson().toJson(request)
            );
            Toast.makeText(EditActivity.this, "添加黑名单成功", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 添加联系人
     */
    private void doAddLinkMan() {
        String name = et_name.getText().toString();
        String phonenum = et_num.getText().toString();
        if (name.trim().isEmpty()) {
            Toast.makeText(EditActivity.this, "姓名不能为空", Toast.LENGTH_SHORT).show();
            return;
        } else if (phonenum.trim().isEmpty()) {
            Toast.makeText(EditActivity.this, "号码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        Contacts contacts = new Contacts(name, phonenum);
        //修改数据库中的数据
        //判断是否存在
        LinkManDao dao = MyApplication.getInstance().getLinkManDao();
        if (dao.isAlreadyAdd(monitor.id, contacts)) {
            Toast.makeText(EditActivity.this, "该号码已经存在，请重新添加", Toast.LENGTH_SHORT).show();
            return;
        }

        dao.insertContact(monitor.id, contacts);

        //向服务器发送消息
        AlterLinkManRequset request = new AlterLinkManRequset(MessageTag.ADDLINKMAN_REQ);
        request.from_id = MyApplication.getInstance().getSpUtil().getUser().id;
        request.into_id = monitor.id;
        request.contacts = contacts;
        MyApplication.getInstance().getSendMsgUtil().sendCacheMessageToServer(
                MyApplication.getInstance().getGson().toJson(request)
        );
        Toast.makeText(EditActivity.this, "添加联系人成功", Toast.LENGTH_SHORT).show();

    }
}
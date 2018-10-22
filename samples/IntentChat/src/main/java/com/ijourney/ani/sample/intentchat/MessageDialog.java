package com.ijourney.ani.sample.intentchat;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.ijourney.ani.sample.adapter.MessageAdapter;
import com.ijourney.ani.sample.bean.MessageBean;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class MessageDialog extends Dialog implements View.OnClickListener {
    ListView lv_message;
    List<MessageBean> list = new ArrayList<>();
    MessageAdapter adapter;
    Context context;
    Button btn_delete_message, btn_send_message;

    public MessageDialog(Context mContext) {
        super(mContext, R.style.Dialog);
        list = DataSupport.findAll(MessageBean.class);
        context = mContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_message);
        lv_message = (ListView) findViewById(R.id.lv_message);
        adapter = new MessageAdapter(context, list);
        btn_delete_message = (Button) findViewById(R.id.btn_delete_message);
        btn_send_message = (Button) findViewById(R.id.btn_send_message);
        lv_message.setAdapter(adapter);
        lv_message.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MessageBean bean = (MessageBean) adapter.getItem(position);
                Toast.makeText(context,bean.getMessage(),Toast.LENGTH_LONG).show();


            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_delete_message:
                break;
            case R.id.btn_send_message:
                break;
        }
    }
}

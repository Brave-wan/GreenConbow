package com.ijourney.ani.sample.intentchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ijourney.ani.sample.adapter.GridFixedListAdapter;
import com.ijourney.ani.sample.adapter.GridListAdapter;
import com.ijourney.ani.sample.adapter.IChatView;
import com.ijourney.ani.sample.adapter.MyGridView;
import com.ijourney.ani.sample.adapter.MyRecyclerView;
import com.ijourney.ani.sample.bean.ChatMsgBean;
import com.ijourney.ani.sample.bean.FeaturesBean;
import com.ijourney.ani.sample.bean.FixedBean;
import com.ijourney.ani.sample.bean.MessageBean;
import com.ijourney.ani.sample.bean.SharedPreferencesUtils;
import com.ijourney.ani.sample.discovery.Discovery;
import com.ijourney.ani.sample.discovery.DiscoveryException;
import com.ijourney.ani.sample.discovery.DiscoveryListener;
import com.ijourney.ani.sample.transmitter.Transmitter;
import com.ijourney.ani.sample.transmitter.TransmitterException;

import org.litepal.crud.DataSupport;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ChatActivity extends Activity implements DiscoveryListener, IChatView, OnEditorActionListener, OnClickListener, MessageDialog.onMessageListener {
    private EditText ed_content;
    private MyGridView rv_list, rv_fixed_list;


    private Discovery discovery;
    private Transmitter transmitter;
    ImageView img_tune_add;
    ScrollView scrollView;


    private boolean discoveryStarted;
    private ChatPresent present;
    BaseQuickAdapter<FeaturesBean, BaseViewHolder> adapter;
    List<FeaturesBean> featuresBeans = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);


        present = new ChatPresent(this, this);
        present.setListener();
        initView();
    }

    private void initView() {

        featuresBeans = DataSupport.findAll(FeaturesBean.class);
        discovery = new Discovery();
        discovery.setDisoveryListener(this);
        transmitter = new Transmitter();
        findViewById(R.id.img_tune_add).setOnClickListener(this);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        rv_fixed_list = (MyGridView) findViewById(R.id.rv_fixed_list);
        rv_list = (MyGridView) findViewById(R.id.rv_list);
        findViewById(R.id.tx_send).setOnClickListener(this);
        findViewById(R.id.tx_save).setOnClickListener(this);
        findViewById(R.id.btn_add).setOnClickListener(this);
        ed_content = (EditText) findViewById(R.id.ed_content);


        initAdapter();
        initFixedAdapter();
        clearListState();


    }

    List<FixedBean> fixedBeans = new ArrayList<>();
    FixedBean fixedBean;

    private void initFixedAdapter() {

        fixedBeans = present.getFixedTop();
        fixedListAdapter = new GridFixedListAdapter(this, fixedBeans);
        rv_fixed_list.setAdapter(fixedListAdapter);
        rv_fixed_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fixedBean = (FixedBean) fixedListAdapter.getItem(position);
                bean = null;
                clearListState();
                for (FixedBean features : fixedBeans) {
                    features.setCheck(false);
                }
                fixedBeans.get(position).setCheck(true);
                fixedListAdapter.setData(fixedBeans);
                if (!StringUtils.isEmpty(fixedBean.getContent())) {
                    sendChatMessage(fixedBean.getContent());
                }
                ed_content.setText(fixedBean.getContent());
            }
        });
    }

    FeaturesBean bean;
    GridListAdapter gridListAdapter;
    GridFixedListAdapter fixedListAdapter;

    private void initAdapter() {
        rv_list.setVisibility(featuresBeans.size() <= 0 ? View.GONE : View.VISIBLE);
        gridListAdapter = new GridListAdapter(this, featuresBeans);
        rv_list.setAdapter(gridListAdapter);

        rv_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bean = (FeaturesBean) gridListAdapter.getItem(position);
                fixedBean = null;
                clearFixedState();
                for (FeaturesBean features : featuresBeans) {
                    features.setCheck(false);
                    features.save();
                }
                bean.setCheck(true);
                bean.save();
                initAdapter();
                ed_content.setText(bean.getContent());
                if (!StringUtils.isEmpty(bean.getContent())) {
                    sendChatMessage(bean.getContent());
                }

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            discovery.enable();
            discoveryStarted = true;
        } catch (DiscoveryException exception) {
            LogUtils.i("* (!) Could not start discovery: " + exception.getMessage());
            discoveryStarted = false;
        }
        ed_content.setText("");
        featuresBeans = DataSupport.findAll(FeaturesBean.class);
        initAdapter();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (discoveryStarted) {
            discovery.disable();
        }
    }


    private void appendChatMessageFromSender(String sender, final String message) {
        String localIp = present.getLocalIpAddress();
        final String total = "<" + sender + "> " + message;
        LogUtils.i(total);
    }

    @Override
    public void onDiscoveryError(Exception exception) {
        LogUtils.i("* (!) Discovery error: " + exception.getMessage());
    }

    @Override
    public void onDiscoveryStarted() {
        LogUtils.i("* (>) Discovery started");
    }

    @Override
    public void onDiscoveryStopped() {
        LogUtils.i("* (<) Discovery stopped");
    }

    @Override
    public void onIntentDiscovered(InetAddress address, String message) {
        String sender = address.getHostName();
        appendChatMessageFromSender(sender, message);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.img_tune_add:
                startActivity(new Intent(this, FeaturesActivity.class));
                break;

            case R.id.tx_send:
                if (!StringUtils.isEmpty(ed_content.getText().toString())) {
                    sendChatMessage(ed_content.getText().toString());
                } else {
                    Toast.makeText(this, "请输入命令类容!", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.tx_save:
                String edContentMsg = ed_content.getText().toString().trim();
                if (!StringUtils.isEmpty(edContentMsg)) {
                    msgSave(edContentMsg);
                }
                break;

            case R.id.btn_add:
                MessageDialog messageDialog = new MessageDialog(this, new FeaturesBean());
                messageDialog.setOnMessageListener(this);
                messageDialog.show();
                break;

        }
    }


    public void msgSave(String msg) {
        if (bean == null) {//选择保存的是按钮
            SharedPreferencesUtils.init(this).put(fixedBean.getTag(), msg);
            fixedBeans = present.getFixedTop();
            fixedListAdapter.setData(fixedBeans);
            Toast.makeText(this, "保存成功!", Toast.LENGTH_LONG).show();
        } else {//item 内容保存
            if (!StringUtils.isEmpty(msg) && bean != null) {
                bean.setContent(msg);
                Toast.makeText(this, bean.save() ? "保存成功!" : "保存失败!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "保存类容为空!", Toast.LENGTH_LONG).show();
            }
        }

    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
//            String message = inputView.getText().toString();
//            sendChatMessage(message);
            return true;
        }

        return false;
    }

    public void sendChatMessage(String message) {
        if (message.length() == 0) {
            return; // No message to send
        }
        transmitIntentOnBackgroundThread(message);
    }


    private void transmitIntentOnBackgroundThread(final String msg) {
        new Thread() {
            public void run() {
                transmitIntent(msg);
            }
        }.start();
    }

    private void transmitIntent(final String msg) {
        try {
            transmitter.transmit(msg);
        } catch (TransmitterException exception) {
            LogUtils.i("Could not transmit intent: " + exception.getMessage());
        }
    }


    @Override
    public void sendMessage(FeaturesBean bean) {
        featuresBeans = DataSupport.findAll(FeaturesBean.class);
        initAdapter();
    }

    @Override
    public void showMsg() {

    }

    private String btnType = null;


    @Override
    public void sendChatMsg(String s, String s1, String type, String s2) {

    }


    public void clearListState() {
        if (featuresBeans != null) {
            for (FeaturesBean featuresBean : featuresBeans) {
                featuresBean.setCheck(false);
                featuresBean.save();
            }
        }
        gridListAdapter.setData(featuresBeans);

    }

    public void clearFixedState() {
        if (fixedBeans != null) {
            for (FixedBean featuresBean : fixedBeans) {
                featuresBean.setCheck(false);
            }
        }
        fixedListAdapter.setData(fixedBeans);
    }
}

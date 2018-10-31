package com.ijourney.ani.sample.intentchat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.blankj.utilcode.util.StringUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ijourney.ani.sample.adapter.IChatView;
import com.ijourney.ani.sample.bean.ChatMsgBean;
import com.ijourney.ani.sample.bean.FeaturesBean;
import com.ijourney.ani.sample.bean.MessageBean;
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
    private TextView chatView, tx_send, tx_edit, tx_save;
    private EditText inputView, ed_content, ed_socket_position, ed_socket_page;
    private ImageButton sendButton;
    private RecyclerView rv_list;


    private Discovery discovery;
    private Transmitter transmitter;
    ImageView img_tune_add;


    private boolean discoveryStarted;
    private ScrollView scrollView;
    private ChatPresent present;
    BaseQuickAdapter<FeaturesBean, BaseViewHolder> adapter;
    List<FeaturesBean> featuresBeans = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);
        initView();

        present = new ChatPresent(this, this);
        present.setListener();

    }

    private void initView() {
        featuresBeans = DataSupport.findAll(FeaturesBean.class);
        discovery = new Discovery();
        discovery.setDisoveryListener(this);
        transmitter = new Transmitter();
        findViewById(R.id.img_tune_add).setOnClickListener(this);
        rv_list = (RecyclerView) findViewById(R.id.rv_list);
        findViewById(R.id.tx_send).setOnClickListener(this);
        findViewById(R.id.tx_edit).setOnClickListener(this);
        findViewById(R.id.tx_save).setOnClickListener(this);
        findViewById(R.id.btn_home).setOnClickListener(this);
        findViewById(R.id.btn_video).setOnClickListener(this);
        findViewById(R.id.btn_Introduction).setOnClickListener(this);
        findViewById(R.id.btn_start).setOnClickListener(this);
        findViewById(R.id.btn_socket_save).setOnClickListener(this);
        ed_socket_position = (EditText) findViewById(R.id.ed_socket_position);
        ed_socket_page = (EditText) findViewById(R.id.ed_socket_page);
        chatView = (TextView) findViewById(R.id.chat);
        scrollView = (ScrollView) findViewById(R.id.scroll);
        inputView = (EditText) findViewById(R.id.input);
        inputView.setOnEditorActionListener(this);
        sendButton = (ImageButton) findViewById(R.id.send);
        sendButton.setOnClickListener(this);
        ed_content = (EditText) findViewById(R.id.ed_content);

        findViewById(R.id.btn_clear).setOnClickListener(this);
        initAdapter();
    }

    FeaturesBean bean;

    private void initAdapter() {
        rv_list.setVisibility(featuresBeans.size() <= 0 ? View.GONE : View.VISIBLE);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayout.HORIZONTAL);
        rv_list.setLayoutManager(linearLayoutManager);
        adapter = new BaseQuickAdapter<FeaturesBean, BaseViewHolder>(R.layout.item_features, featuresBeans) {
            @Override
            protected void convert(BaseViewHolder helper, final FeaturesBean item) {
                Button btn = (Button) helper.itemView.findViewById(R.id.btn_name);
                btn.setText(item.getName());
                btn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bean = item;
                        ed_content.setText(bean.getContent());
                        ed_socket_page.setText(bean.getSocket_page());
                        ed_socket_position.setText(bean.getSocket_position());
                    }
                });
            }
        };
        rv_list.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            discovery.enable();
            discoveryStarted = true;
        } catch (DiscoveryException exception) {
            appendChatMessage("* (!) Could not start discovery: " + exception.getMessage());
            discoveryStarted = false;
        }
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

    private void appendChatMessage(final String message) {
        runOnUiThread(new Runnable() {
            public void run() {
                chatView.append(message + "\n");
            }
        });
    }

    private void appendChatMessageFromSender(String sender, final String message) {
        String localIp = getLocalIpAddress();
        final String total = "<" + sender + "> " + message;
        appendChatMessage(total);
    }

    @Override
    public void onDiscoveryError(Exception exception) {
        appendChatMessage("* (!) Discovery error: " + exception.getMessage());
    }

    @Override
    public void onDiscoveryStarted() {
        appendChatMessage("* (>) Discovery started");
    }

    @Override
    public void onDiscoveryStopped() {
        appendChatMessage("* (<) Discovery stopped");
    }

    @Override
    public void onIntentDiscovered(InetAddress address, String message) {
        String sender = address.getHostName();
        appendChatMessageFromSender(sender, message);
    }

    Handler mHandler = new Handler();
    List<ChatMsgBean> listMsg = new ArrayList<>();

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send:
                String message = inputView.getText().toString();
                saveMessage(message);
                sendChatMessage(message);
                break;
            case R.id.btn_clear:
                chatView.setText("");
                break;
            case R.id.img_tune_add:
                startActivity(new Intent(this, FeaturesActivity.class));
                break;
            case R.id.tx_edit:
                ed_content.setFocusable(ed_content.isFocusable() ? false : true);
                break;

            case R.id.tx_send:
                if (!StringUtils.isEmpty(ed_content.getText().toString())) {
                    sendChatMessage(ed_content.getText().toString());
                    if (bean != null && !StringUtils.isEmpty(bean.getSocket_page()) && !StringUtils.isEmpty(bean.getSocket_position())) {
                        present.sendMsgData(bean.getSocket_position(), bean.getSocket_page());
                    }
                } else {
                    Toast.makeText(this, "请输入命令类容!", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.tx_save:
                if (ed_content.getText().toString().trim().length() > 0 && bean != null) {
                    bean.setContent(ed_content.getText().toString().trim());
                    Toast.makeText(this, bean.save() ? "保存成功!" : "保存失败!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "保存类容为空!", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_socket_save:
                if (bean != null) {
                    if (!StringUtils.isEmpty(ed_socket_page.getText().toString()) && !StringUtils.isEmpty(ed_socket_position.getText().toString())) {
                        bean.setSocket_page(ed_socket_page.getText().toString().trim());
                        bean.setSocket_position(ed_socket_position.getText().toString().trim());
                        Toast.makeText(this, bean.save() ? "保存成功!" : "保存失败!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "保存类容为空!", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case R.id.btn_home:
                present.sendMsgData("20", "20.png");
                break;
            case R.id.btn_video:
                present.sendMsgData("21", "21.png");
                break;
            case R.id.btn_Introduction:
                String msg = getString(R.string.first_paragraph) + getString(R.string.two_paragraph) + getString(R.string.three_paragraph) + getString(R.string.four_paragraph);
                sendChatMessage(msg);
                present.sendMsgData("1", "1.png");
                break;
            case R.id.btn_start:
                present.sendMsgData("2", "2.png");
                break;
        }
    }

    int i = 0;
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (i < 4) {
                        transmitIntent(listMsg.get(i).getMsg());
                        handler.postDelayed(this, listMsg.get(i).getTime());
                        i++;
                    }
                }
            });
        }
    };


    List<String> list = new ArrayList<>();
    // 每隔2秒发送一次心跳包，检测连接没有断开
    private static final long HEART_BEAT_RATE = 5 * 1000;
    private long sendTime = 0L;
    private int postion = 0;
    Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (System.currentTimeMillis() - sendTime >= HEART_BEAT_RATE) {
                Log.e("TAG", "postion:" + postion);
                if (postion <= 2) {
                    sendChatMessage(list.get(postion));
                } else {
                    mHandler.removeCallbacks(heartBeatRunnable);
                }
            }
            mHandler.postDelayed(this, HEART_BEAT_RATE); //每隔一定的时间，对长连接进行一次心跳检测
        }
    };

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            String message = inputView.getText().toString();
            sendChatMessage(message);
            return true;
        }

        return false;
    }

    public void sendChatMessage(String message) {

        if (message.length() == 0) {
            return; // No message to send
        }
        inputView.setText("");

        transmitIntentOnBackgroundThread(message);
    }

    public void saveMessage(String message) {
        MessageBean news = new MessageBean();
        news.setMessage(message);
        news.setTime();
        if (news.save()) {
            Toast.makeText(this, "Success", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Fail", Toast.LENGTH_LONG).show();
        }
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
            appendChatMessage("Could not transmit intent: " + exception.getMessage());
        }
    }

    public static String getLocalIpAddress() {
        String ipaddress = "";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress.isSiteLocalAddress()) {
                        ipaddress = inetAddress.getHostAddress().toString();
                        break;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("", ex.toString());
        }

        return ipaddress;
    }

    @Override
    public void sendMessage(MessageBean bean) {
        sendChatMessage(bean.getMessage());
    }

    @Override
    public void showMsg() {

    }
}

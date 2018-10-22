package com.ijourney.ani.sample.intentchat;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.ijourney.ani.sample.bean.MessageBean;
import com.ijourney.ani.sample.discovery.Discovery;
import com.ijourney.ani.sample.discovery.DiscoveryException;
import com.ijourney.ani.sample.discovery.DiscoveryListener;
import com.ijourney.ani.sample.transmitter.Transmitter;
import com.ijourney.ani.sample.transmitter.TransmitterException;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class ChatActivity extends Activity implements DiscoveryListener, OnEditorActionListener, OnClickListener {
    private TextView chatView;
    private EditText inputView;
    private ImageButton sendButton;
    private Button btn_history;

    private Discovery discovery;
    private Transmitter transmitter;

    private boolean discoveryStarted;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);

        discovery = new Discovery();
        discovery.setDisoveryListener(this);
        transmitter = new Transmitter();

        chatView = (TextView) findViewById(R.id.chat);

        scrollView = (ScrollView) findViewById(R.id.scroll);

        inputView = (EditText) findViewById(R.id.input);
        inputView.setOnEditorActionListener(this);
        btn_history = (Button) findViewById(R.id.btn_history);
        btn_history.setOnClickListener(this);
        sendButton = (ImageButton) findViewById(R.id.send);
        sendButton.setOnClickListener(this);
        findViewById(R.id.btn_clear).setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send:

                sendChatMessage();
                break;
            case R.id.btn_clear:
                chatView.setText("");
                break;

            case R.id.btn_history:
                MessageDialog dialog=new MessageDialog(this);
                dialog.show();
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            sendChatMessage();
            return true;
        }

        return false;
    }

    public void sendChatMessage() {
        String message = inputView.getText().toString();
        if (message.length() == 0) {
            return; // No message to send
        }
        inputView.setText("");
        saveMessage(message);
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
}

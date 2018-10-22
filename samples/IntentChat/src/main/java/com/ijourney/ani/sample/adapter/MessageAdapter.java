package com.ijourney.ani.sample.adapter;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.VideoView;

import com.ijourney.ani.sample.bean.MessageBean;
import com.ijourney.ani.sample.intentchat.R;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private Context mContext;
    private List<MessageBean> list = new ArrayList<>();

    public MessageAdapter(Context mContext, List<MessageBean> list) {
        this.mContext = mContext;
        this.list = list;
        inflater = LayoutInflater.from(mContext);

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_message, null);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tv_message_content = (TextView) convertView.findViewById(R.id.tv_message_content);
        holder.tv_message_time = (TextView) convertView.findViewById(R.id.tv_message_time);
        MessageBean bean = list.get(position);
        holder.tv_message_time.setText(bean.getTime());
        holder.tv_message_content.setText(bean.getMessage());
        return convertView;
    }

    ViewHolder holder;

    class ViewHolder {
        TextView tv_message_content;
        TextView tv_message_time;
    }
}

package com.ijourney.ani.sample.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.ijourney.ani.sample.bean.FeaturesBean;
import com.ijourney.ani.sample.bean.MessageBean;
import com.ijourney.ani.sample.intentchat.R;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class FeaturesAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private Context mContext;
    private List<FeaturesBean> list = new ArrayList<>();

    public FeaturesAdapter(Context mContext, List<FeaturesBean> list) {
        this.mContext = mContext;
        this.list = list;
        inflater = LayoutInflater.from(mContext);

    }

    public void setData() {
        list = DataSupport.findAll(FeaturesBean.class);
        notifyDataSetChanged();
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
        holder.tv_message_time = (Button) convertView.findViewById(R.id.tv_message_time);
        final FeaturesBean bean = list.get(position);
        holder.tv_message_content.setText(bean.getName());
        holder.tv_message_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bean.delete();
                setData();
            }
        });
        return convertView;
    }

    ViewHolder holder;

    class ViewHolder {
        TextView tv_message_content;
        Button tv_message_time;
    }
}

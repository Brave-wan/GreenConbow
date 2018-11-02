package com.ijourney.ani.sample.intentchat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.ijourney.ani.sample.adapter.FeaturesAdapter;
import com.ijourney.ani.sample.bean.FeaturesBean;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class FeaturesActivity extends Activity implements View.OnClickListener {
    private ListView lv_features_list;
    private ImageView img_features_back;
    FeaturesAdapter adapter;
    List<FeaturesBean> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_features);
        initView();
    }

    private void initView() {
        list = DataSupport.findAll(FeaturesBean.class);
        lv_features_list = (ListView) findViewById(R.id.lv_features_list);
        img_features_back = (ImageView) findViewById(R.id.img_features_back);
        adapter = new FeaturesAdapter(this, list);
        lv_features_list.setAdapter(adapter);
        img_features_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_features_back:
                finish();
                break;
        }

    }
}

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
    private EditText ed_features_input;
    private Button btn_features_save;
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
        ed_features_input = (EditText) findViewById(R.id.ed_features_input);
        btn_features_save = (Button) findViewById(R.id.btn_features_save);
        adapter = new FeaturesAdapter(this, list);
        lv_features_list.setAdapter(adapter);
        btn_features_save.setOnClickListener(this);
        img_features_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_features_back:
                finish();
                break;

            case R.id.btn_features_save:
                String value = ed_features_input.getText().toString().trim();
                if (value.length() <= 0) {
                    Toast.makeText(this, "输入不能为空哦!", Toast.LENGTH_LONG).show();
                    return;
                }
                FeaturesBean bean = new FeaturesBean();
                bean.setContent("");
                bean.setName(ed_features_input.getText().toString().trim());
                if (bean.save()) {
                    ed_features_input.setText("");
                    Toast.makeText(this, "创建成功!", Toast.LENGTH_LONG).show();
                    adapter.setData();
                } else {
                    Toast.makeText(this, "创建失败，请再试试!", Toast.LENGTH_LONG).show();
                }
                break;
        }

    }
}

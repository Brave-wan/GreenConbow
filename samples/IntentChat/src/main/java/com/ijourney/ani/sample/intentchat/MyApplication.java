package com.ijourney.ani.sample.intentchat;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

public class MyApplication extends LitePalApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
    }
}

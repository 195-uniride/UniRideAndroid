package com.sjsu.se195.uniride;

import android.app.Application;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by akshat on 11/15/17.
 */

public class CustomApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Slabo27px-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}

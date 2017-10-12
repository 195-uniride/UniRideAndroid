package com.sjsu.se195.uniride;

import android.os.Bundle;

/**
 * Created by akshat on 10/9/17.
 */

public class MainSubcategoryActivity extends MainActivity {
    private boolean postType;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        postType = getIntent().getExtras().getBoolean("driverMode");

        if(postType){
            setContentView(R.layout.activity_driver_main);
        }
        else{
            setContentView(R.layout.activity_rider_main);
        }

    }

}

package com.sjsu.se195.uniride;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        System.out.println("I m in the Alamr Listener calss");
        System.out.println("This is the context: " + context);
        Intent intent2 = new Intent(context, CarpoolActivity.class);
    }
}

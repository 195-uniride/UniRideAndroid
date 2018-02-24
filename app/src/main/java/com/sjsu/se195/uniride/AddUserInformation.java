package com.sjsu.se195.uniride;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by Marta on 2/24/18.
 */

public class AddUserInformation extends BaseActivity implements View.OnClickListener{

    private Button saveButton;
    private Button skipButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_information);

        saveButton = findViewById(R.id.save_information);
        skipButton = findViewById(R.id.skip_this);

        skipButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
    }

    public void onStart() {
        super.onStart();
        }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.save_information:
                break;
            case R.id.skip_this:
                startActivity(new Intent(AddUserInformation.this, MainActivity.class));
        }
    }
}

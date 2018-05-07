package com.sjsu.se195.uniride;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class GarageListActivity extends AppCompatActivity {

    //n_g stands for north garage, and so on
    private Button n_g;
    private Button w_g;
    private Button e_g;
    private Button s_g;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garage_list);
        n_g = findViewById(R.id.north_g);
        w_g = findViewById(R.id.west_g);
        e_g = findViewById(R.id.east_g);
        s_g = findViewById(R.id.south_g);

        //Each button goes into a new garage
        n_g.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //this method will start the intent and will take to the new activity
                // where the parking will be shown
                Intent intent = new Intent(GarageListActivity.this, OrganizationParkingActivity.class);
                intent.putExtra("garage_name","north-garage");
                startActivity(intent);
            }
        });
        w_g.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GarageListActivity.this, OrganizationParkingActivity.class);
                intent.putExtra("garage_name","west-garage");
                startActivity(intent);
            }
        });
        e_g.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GarageListActivity.this, OrganizationParkingActivity.class);
                intent.putExtra("garage_name","east-garage");
                startActivity(intent);
            }
        });
        s_g.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GarageListActivity.this, OrganizationParkingActivity.class);
                intent.putExtra("garage_name","south-garage");
                startActivity(intent);
            }
        });
    }
}

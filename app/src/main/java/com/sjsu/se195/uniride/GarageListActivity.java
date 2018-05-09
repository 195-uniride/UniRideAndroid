package com.sjsu.se195.uniride;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class GarageListActivity extends AppCompatActivity {

    //n_g stands for north garage, and so on
    private String org_name;
    private DatabaseReference mDatabase;
    private int num_of_garg = 0;
    private String[] garage_names = new String[4];
    private Button[] buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Bundle args = getIntent().getExtras();
        org_name = args.getString("organization_name");
        System.out.println("garage List Act : org Name : " + org_name);
        setContentView(R.layout.activity_garage_list);
        Button[] buttons_array = {findViewById(R.id.north_g), findViewById(R.id.west_g), findViewById(R.id.east_g), findViewById(R.id.south_g)};
        buttons= buttons_array;

        //A query that gets the garage names
        Query parkingSpotQuery = getQuery(mDatabase);

        //Get the names of all the garages (max number of garages are set to 6-random)
        parkingSpotQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("Count " ,""+dataSnapshot.getChildrenCount());
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    System.out.println("Child: " + child.getKey());
                    garage_names[num_of_garg] = child.getKey();
                    num_of_garg++;
                }
                initializeButtons();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void initializeButtons(){
        //Set listener for each button
        System.out.println("size of gara: "+ num_of_garg);
        System.out.println("size of but: "+ buttons.length);
        for(int i = 0; i < buttons.length; i++) {
            if (i < num_of_garg) {
                final int j = i;    //need because i can't be used inside the inner class
                buttons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //this method will start the intent and will take to the new activity
                        // where the parking will be shown
                        Intent intent = new Intent(GarageListActivity.this, OrganizationParkingActivity.class);
                        intent.putExtra("garage_name", garage_names[j]);
                        intent.putExtra("organization_name", org_name);
                        startActivity(intent);

                    }
                });
                //Format the name before setting text (making each word's first letter uppercase);
                String[] parts = garage_names[i].split("-");
                String g_name = "";
                for (int m = 0; m < parts.length; m++) {
                    g_name = g_name + Character.toUpperCase(parts[m].charAt(0)) + parts[m].substring(1, parts[m].length() - 1);
                }
                //set the buttons's text
                System.out.println("the button's text will be: " + g_name);
                buttons[i].setText(g_name);
                buttons[i].setBackgroundColor(Color.GREEN);
                buttons[i].setVisibility(View.VISIBLE);
                System.out.println("set visibility "+buttons[i].getText());
            }
            //clear all the empty buttons
            else{
                //buttons[i].setVisibility(View.GONE);
                System.out.println("set visibility off "+buttons[i].getText());
            }
        }
    }

    //Get the names of all the garages
    public Query getQuery(DatabaseReference databaseReference){                 //}, String level, String section) {
        // All my organizations
        System.out.println("*** Get the garages for : " + org_name);
        String parking_spots_here = "";//TODO: combine the other strings to so parking spots on specific levels can be found
        return databaseReference.child("parking-garage").child(org_name);
    }
}

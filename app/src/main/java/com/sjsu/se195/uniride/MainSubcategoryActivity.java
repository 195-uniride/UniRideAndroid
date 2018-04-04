package com.sjsu.se195.uniride;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sjsu.se195.uniride.fragment.PostListFragment;
import com.sjsu.se195.uniride.fragment.RecentPostsFragment;
import com.sjsu.se195.uniride.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by akshat on 10/9/17.
 */

public class MainSubcategoryActivity extends MainActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "MainSubcategoryActivity";

    private User currentUser;
    private HashMap<String, String> OrganizationNameIdMap;
    private Spinner orgSpinner;
    private ArrayAdapter<String> orgAdapter;
    private String selectedOrganizationId;

    private boolean isShowingDriveOffers;
    private ViewPager mViewPager;
    private FragmentPagerAdapter mPagerAdapter;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        isShowingDriveOffers = getIntent().getExtras().getBoolean("driverMode");


        if (isShowingDriveOffers) {
            setContentView(R.layout.activity_1_driver_main);
            findViewById(R.id.new_drive_offer_post).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainSubcategoryActivity.this, NewPostActivity.class);
                    intent.putExtra("driveOffer", true);
                    startActivity(intent);
                }
            });
        } else {
            setContentView(R.layout.activity_1_rider_main);

            findViewById(R.id.new_ride_request_post).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainSubcategoryActivity.this, NewPostActivity.class);
                    intent.putExtra("driveOffer", false);
                    startActivity(intent);
                }
            });
        }

        findViewById(R.id.driver_feed_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // After contentView has been set, setup the spinner:
        orgSpinner = (Spinner) findViewById(R.id.chosen_organization);
        addListenerOnSpinnerItemSelection();

    }

    public Drawable getArch(){
        Shape arcShape = new ArcShape(0, 180);
        ShapeDrawable arcDrawable = new ShapeDrawable(arcShape);
        arcDrawable.getPaint().setColor(getResources().getColor(R.color.colorPrimary));
        arcDrawable.getPaint().setStyle(Paint.Style.FILL);
        return arcDrawable;
    }

    private void loadPosts() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("postType", this.isShowingDriveOffers); // TODO: change name?
        Fragment posts = new RecentPostsFragment();
        posts.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.post_fragment_placeholder, posts, "PostsList").commit();
    }

    private void reloadPosts() {
        // Reload fragment: // TODO: this did not re-call
        PostListFragment postListFragment = (PostListFragment) getSupportFragmentManager().findFragmentById(R.id.post_fragment_placeholder);
        getSupportFragmentManager().beginTransaction().detach(postListFragment).attach(postListFragment).commit();
    }

    private void addListenerOnSpinnerItemSelection(){
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String userID = currentFirebaseUser.getUid();

        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("users").child(userID);

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Organization object and use the values to update the UI
                currentUser = dataSnapshot.getValue(User.class);

                fillOrganizations();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Organization failed, log a message
                Log.w(TAG, "loadUser:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(MainSubcategoryActivity.this, "Failed to load user.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        });
    }

    private void fillOrganizations() {
        OrganizationNameIdMap = new HashMap<>();

        DatabaseReference userOrgsRef = FirebaseDatabase.getInstance().getReference()
                .child("user-organizations").child(getUid());

        userOrgsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> allOrganizationNames = new ArrayList<String>();

                String defaultOrgName = "";
                String defaultOrgId = "";
                // Gets the name of all organizations the user has joined:
                for (DataSnapshot orgSnapshot: dataSnapshot.getChildren()) {
                    String orgName = orgSnapshot.child("name").getValue(String.class);

                    allOrganizationNames.add(orgName);

                    // Link the organization name to its key (for later lookup):
                    String orgId = orgSnapshot.getKey();
                    OrganizationNameIdMap.put(orgName, orgId); // NOTE: This assumes all org names are unique.

                    // Get default org name to set as the selected item in the list:
                    if (currentUser.defaultOrganizationId.equals(orgId)) {
                        defaultOrgName = orgName;
                        defaultOrgId = orgId;
                    }
                }

                //Fills spinner with organization names
                orgAdapter = new ArrayAdapter<String>(MainSubcategoryActivity.this,
                        android.R.layout.simple_spinner_item, allOrganizationNames);
                orgAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                System.out.println("orgSpinner = " + orgSpinner);
                orgSpinner.setAdapter(orgAdapter);

                // Set the default org name as the initial selected item:
                orgSpinner.setSelection(orgAdapter.getPosition(defaultOrgName));
                selectedOrganizationId = defaultOrgId;
                System.out.println("Setting selectedOrganizationId = " + selectedOrganizationId);

                // Set the action for when an item is selected:
                orgSpinner.setOnItemSelectedListener(MainSubcategoryActivity.this);

                // Now OK to load posts:
                loadPosts();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public String getSelectedOrganizationId() {
        return selectedOrganizationId;
    }

    // OnItemSelectedListener interface required methods for Spinner:

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using parent.getItemAtPosition(pos).

        String selectedOrgName = (String) parent.getItemAtPosition(pos);

        System.out.println("selectedOrgName = " + selectedOrgName);

        selectedOrganizationId = OrganizationNameIdMap.get(selectedOrgName);

        reloadPosts();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

}

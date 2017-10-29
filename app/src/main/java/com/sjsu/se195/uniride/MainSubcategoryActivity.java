package com.sjsu.se195.uniride;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.sjsu.se195.uniride.fragment.RecentPostsFragment;


/**
 * Created by akshat on 10/9/17.
 */

public class MainSubcategoryActivity extends MainActivity {
    private boolean postType;
    private ViewPager mViewPager;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        postType = getIntent().getExtras().getBoolean("driverMode");

        if (postType) {
            setContentView(R.layout.activity_driver_main);
            findViewById(R.id.new_drive_offer_post).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainSubcategoryActivity.this, NewPostActivity.class);
                    intent.putExtra("driveOffer", true);
                    startActivity(intent);
                }
            });
        } else {
            setContentView(R.layout.activity_rider_main);
            findViewById(R.id.new_ride_request_post).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainSubcategoryActivity.this, NewPostActivity.class);
                    intent.putExtra("driveOffer", false);
                    startActivity(intent);
                }
            });
        }

        // Create the adapter that will return a fragment for each section
        /*mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] mFragments = new Fragment[] {
                    new RecentPostsFragment(),
                    new MyPostsFragment(),
                    new MyTopPostsFragment(),
            };
            private final String[] mFragmentNames = new String[] {
                    getString(R.string.heading_recent),
                    getString(R.string.heading_my_posts),
                    getString(R.string.heading_my_top_posts)
            };
            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }
            @Override
            public int getCount() {
                return mFragments.length;
            }
            @Override
            public CharSequence getPageTitle(int position) {
                return mFragmentNames[position];
            }
        };*/
        // Set up the ViewPager with the sections adapter.
        /*mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);*/
        Bundle bundle = new Bundle();
        bundle.putBoolean("postType", this.postType);
        RecentPostsFragment RecentPostFragment = new RecentPostsFragment();
        RecentPostFragment.setArguments(bundle);
        // Button launches NewPostActivity

    }

}

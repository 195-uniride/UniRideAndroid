package com.sjsu.se195.uniride;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.sjsu.se195.uniride.fragment.ParkingSpotsListFragment;
import com.sjsu.se195.uniride.models.User;

import static android.support.v4.view.ViewPager.SCROLL_STATE_IDLE;
import static android.support.v4.view.ViewPager.SCROLL_STATE_SETTLING;

public class OrganizationParkingActivity extends AppCompatActivity {

    //private FragmentPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private User user;
    private DatabaseReference mDatabase;
    private String ABOUT_TAB_TITLE;
    final Bundle bundle = new Bundle();
    private Fragment mFragment;
    private String garage_name;
    private String org_name;

    private TextView userNameView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getIntent().getExtras();
        garage_name = args.getString("garage_name");
        org_name = args.getString("organization_name");
        setContentView(R.layout.activity_organization_parking);
        //get the buttons from the activity xml
        Button [] buttons = {
                (Button) findViewById(R.id.level0),
                (Button) findViewById(R.id.level1),
                (Button) findViewById(R.id.level2),
                (Button) findViewById(R.id.level3),
                (Button) findViewById(R.id.level4),
                (Button) findViewById(R.id.level5)
        };

        //set the listener for the buttons
        for(int i = 0; i < buttons.length; i++){
            final String level = Integer.toString(i);
            buttons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setFragment(garage_name, level);
                }
            });
        }

    }

    //Basically copy pasted from profile page
    private void setFragment(String garage_name, String garage_level){
        //First remove the previous stuff
        FrameLayout o_p_c = (FrameLayout) findViewById(R.id.organization_parking_container);
        o_p_c.removeAllViews();
        //initialize to the proper button press
        mFragment = new ParkingSpotsListFragment();
        OrganizationParkingActivity.this.bundle.putString("organization", org_name);
        OrganizationParkingActivity.this.bundle.putString("garage",garage_name);
        OrganizationParkingActivity.this.bundle.putString("level",garage_level);
        mFragment.setArguments(OrganizationParkingActivity.this.bundle);

//        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager()){
//            private final Fragment[] mFragments = new Fragment[] {
//                    new ParkingSpotsListFragment(),
//                    new ParkingSpotsListFragment(),
//                    new ParkingSpotsListFragment(),
//                    new ParkingSpotsListFragment()
//            };
//            @Override
//            public Fragment getItem(int position) {
//                if(position == 0) {
//                    System.out.println("position 1: fragment-org-parkin");
//                    OrganizationParkingActivity.this.bundle.putString("garage","north-garage");
//                }
//                else if(position == 1) {
//                    System.out.println("position 1: fragment-org-parkin");
//                    OrganizationParkingActivity.this.bundle.putString("garage","west-garage");
//                }
//                else if(position == 2) {
//                    System.out.println("position 1: fragment-org-parkin");
//                    OrganizationParkingActivity.this.bundle.putString("garage","east-garage");
//                }
//                else if(position == 3) {
//                    System.out.println("position 1: fragment-org-parkin");
//                    OrganizationParkingActivity.this.bundle.putString("garage","south-garage");
//                }
//                System.out.println("I m on current position: " + position);
//                System.out.println("this is the garage  name: " + OrganizationParkingActivity.this.bundle.getString("garage"));
//                mFragments.setArguments(OrganizationParkingActivity.this.bundle);
//                return mFragments[position];
//            }
//            @Override
//            public int getCount() {
//                return mFragments.length;
//            }
//        };
//
//        // Set up the ViewPager with the sections adapter.
//        mViewPager = (ViewPager) findViewById(R.id.container);
//        mViewPager.setAdapter(mSectionsPagerAdapter);
//
//        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
//
//        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
//        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        System.out.println("removed all the previous views and adding new ones.");
        Fragment parkingspots= new ParkingSpotsListFragment();
        parkingspots.setArguments(OrganizationParkingActivity.this.bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.organization_parking_container, parkingspots, "ParkingSpots").commit();

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            return OrganizationParkingActivity.PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 5;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static OrganizationParkingActivity.PlaceholderFragment newInstance(int sectionNumber) {
            OrganizationParkingActivity.PlaceholderFragment fragment = new OrganizationParkingActivity.PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_parking_spots_list, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }
}

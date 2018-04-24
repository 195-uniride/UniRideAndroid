package com.sjsu.se195.uniride;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sjsu.se195.uniride.fragment.MyDriverPostsFragment;
import com.sjsu.se195.uniride.fragment.MyOrganizationsFragment;
import com.sjsu.se195.uniride.fragment.MyPostsFragment;
import com.sjsu.se195.uniride.fragment.MyRiderPostsFragment;
import com.sjsu.se195.uniride.fragment.RecentPostsFragment;
import com.sjsu.se195.uniride.fragment.UserInformationFragment;
import com.sjsu.se195.uniride.models.User;

import org.w3c.dom.Text;

public class ProfilePageActivity extends BaseActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private User user;
    private DatabaseReference mDatabase;
    private String ABOUT_TAB_TITLE;
    final Bundle bundle = new Bundle();
    private TextView userNameView;
    private ImageButton mSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);
        this.userNameView = (TextView) this.findViewById(R.id.profile_page_user_name);
        String uID = getUid(); //TODO: should instead get value from intent to show different users.

        if(uID == getUid()){
            mSignOut = (ImageButton) this.findViewById(R.id.profile_page_sign_out);
            mSignOut.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(ProfilePageActivity.this, SignInActivity.class));
                    finish();
                }
            });
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mDatabase = FirebaseDatabase.getInstance().getReference();
        bundle.putString("uID", uID);
        this.getUser(uID);
        setNavBar(this);
    }

    private void setFragment(){
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager()){
            private final Fragment[] mFragments = new Fragment[] {
                    new UserInformationFragment(),
                    new MyDriverPostsFragment(),
                    new MyRiderPostsFragment(),
                    new MyOrganizationsFragment()
            };
            @Override
            public Fragment getItem(int position) {
                mFragments[position].setArguments(ProfilePageActivity.this.bundle);
                return mFragments[position];
            }
            @Override
            public int getCount() {
                return mFragments.length;
            }
        };

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

            return PlaceholderFragment.newInstance(position + 1);
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
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_profile_page, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    public void getUser(String uid) {

        // System.out.println("Starting to set user....");

        mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Need to get the user object before loading posts because the query to find posts requires user.

                // Get User object and use the values to update the UI
                user = dataSnapshot.getValue(User.class);
                String userName = user.firstName + " " + user.lastName;
                ProfilePageActivity.this.userNameView.setText(userName);
                bundle.putString("userName", user.firstName);
                ProfilePageActivity.this.setFragment();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //getUid()
    }
}

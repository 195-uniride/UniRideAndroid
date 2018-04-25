package com.sjsu.se195.uniride.fragment;

/**
 * Created by timhdavis on 11/11/17.
 */

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.sjsu.se195.uniride.ProfilePageActivity;
import com.sjsu.se195.uniride.R;

public class MyOrganizationsFragment extends OrganizationListFragment {

    private LinearLayout linearLayout;
    private TextView mTabTitle;
    private String MY_ORGANIZATIONS_TITLE;

    public MyOrganizationsFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        if(getActivity() instanceof ProfilePageActivity){
            String userName = getArguments().getString("userName");
            this.MY_ORGANIZATIONS_TITLE = userName + "'s Organizations";
            this.linearLayout = rootView.findViewById(R.id.fragment_all_organizations_linearlayout);
            this.createTitle(MY_ORGANIZATIONS_TITLE);
        }
        return rootView;
    }

    private void createTitle(String title){
        mTabTitle = new TextView(getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        mTabTitle.setLayoutParams(layoutParams);
        mTabTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        mTabTitle.setPadding(10, 10, 10, 10);
        mTabTitle.setText(title);
        this.linearLayout.addView(this.mTabTitle, 0);
    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // All my organizations
        return databaseReference.child("user-organizations")
                .child(super.uID);
    }
}

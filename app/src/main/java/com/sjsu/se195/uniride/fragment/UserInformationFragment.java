package com.sjsu.se195.uniride.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.sjsu.se195.uniride.AddUserInformation;
import com.sjsu.se195.uniride.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserInformationFragment extends Fragment {

    private String ABOUT_TAB_TITLE;
    private TextView mTabTitle;
    private ImageButton mEditProfile;

    public UserInformationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState);
        String firstName = getArguments().getString("userName");
        String uID = getArguments().getString("uID");
        ABOUT_TAB_TITLE = "About " + firstName;
        View rootView = inflater.inflate(R.layout.fragment_user_information, container, false);

        mTabTitle = (TextView) rootView.findViewById(R.id.user_info_title);
        mTabTitle.setText(ABOUT_TAB_TITLE);

        if(uID == getUid()){
            mEditProfile = rootView.findViewById(R.id.profile_user_information_edit);
            mEditProfile.setVisibility(View.VISIBLE);
            mEditProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getActivity(), AddUserInformation.class));
                    getActivity().finish();
                }
            });
        }

        //createTitle(ABOUT_TAB_TITLE);
        return rootView;
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

}

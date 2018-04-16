package com.sjsu.se195.uniride.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sjsu.se195.uniride.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserInformationFragment extends Fragment {

    private String ABOUT_TAB_TITLE;
    private TextView mTabTitle;
    private LinearLayout linearLayout;
    public UserInformationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState);
        String firstName = getArguments().getString("userName");
        ABOUT_TAB_TITLE = "About " + firstName;

        View rootView = inflater.inflate(R.layout.fragment_user_information, container, false);
        linearLayout = rootView.findViewById(R.id.fragment_all_posts_linearlayout);
        createTitle(ABOUT_TAB_TITLE);

        return rootView;
    }

    private void createTitle(String title){
        mTabTitle = new TextView(getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(5, 5, 5, 5);
        layoutParams.gravity = Gravity.CENTER;
        mTabTitle.setLayoutParams(layoutParams);
        mTabTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        mTabTitle.setPadding(10, 10, 10, 10);
        mTabTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.maroon));
        mTabTitle.setText(title);
        linearLayout.addView(mTabTitle, 0);
    }

}

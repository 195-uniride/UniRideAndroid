package com.sjsu.se195.uniride.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by maninder on 2/23/18.
 */

public class DatePickerFragment extends DialogFragment{

    private Activity mActivity;
    private DatePickerDialog.OnDateSetListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;

        // This error will remind you to implement an OnTimeSetListener
        //   in your Activity if you forget
        try {
            mListener = (DatePickerDialog.OnDateSetListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnDateSetListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int date = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(mActivity, mListener, year, month, date);
    }

}

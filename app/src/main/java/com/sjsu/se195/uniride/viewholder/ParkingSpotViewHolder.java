package com.sjsu.se195.uniride.viewholder;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.sjsu.se195.uniride.R;
import com.sjsu.se195.uniride.models.ParkingSpot;

public class ParkingSpotViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "ParkingSpotViewHolder";
    public TextView nameView; // TODO
    // TODO: add more

    public ParkingSpotViewHolder(View itemView) {
        super(itemView);

        nameView = (TextView) itemView.findViewById(R.id.parkingspot_name); // TODO
//        authorView = (TextView) itemView.findViewById(R.id.post_author);// TODO
//        starView = (ImageView) itemView.findViewById(R.id.star);// TODO
//        numStarsView = (TextView) itemView.findViewById(R.id.post_num_stars);// TODO
//        bodyView = (TextView) itemView.findViewById(R.id.post_body);// TODO
    }

    public void bindToParkingSpot(ParkingSpot parkingSpot) {
        nameView.setText(parkingSpot.state);// TODO
//        set the section+number for parking spot
        Log.d(TAG, "setting organization name to: " + parkingSpot.state); //TODO: investigate why My Organization organizations are null here...
//        authorView.setText(organization.author);// TODO
//        numStarsView.setText(String.valueOf(organization.starCount));// TODO: don't need.
//        bodyView.setText(organization.body);// TODO

//        starView.setOnClickListener(starClickListener);// TODO: don't need.
    }
}

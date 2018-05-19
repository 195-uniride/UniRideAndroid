package com.sjsu.se195.uniride.viewholder;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjsu.se195.uniride.PostInfo;
import com.sjsu.se195.uniride.R;
import com.sjsu.se195.uniride.models.Carpool;
import com.sjsu.se195.uniride.models.RouteWayPoint;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by timhdavis on 4/17/18.
 */

public class RouteWayPointListRecyclerAdapter extends RecyclerView.Adapter<RouteWayPointListRecyclerAdapter.RouteWayPointViewHolder> {
    private List<RouteWayPoint> mDataset;
    private OnItemClickListener clickListener;

    // Provide a suitable constructor (depends on the kind of dataset)
    public RouteWayPointListRecyclerAdapter(List<RouteWayPoint> myDataset) {
        mDataset = myDataset;
    }


    @Override
    public RouteWayPointViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_waypoint, parent, false);

        return new RouteWayPointViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RouteWayPointViewHolder holder, int position) {
        holder.bindToPost(mDataset.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    public void setClickListener(OnItemClickListener itemClickListener) {
        System.out.println("RouteWayPointListRecyclerAdapter: Setting itemClickListener = " + itemClickListener);
        this.clickListener = itemClickListener;
    }



    // View Holder inner class:


    public class RouteWayPointViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView background;
        private TextView wayPointTitleText;
        private TextView participantNameText;
        private TextView addressText;
        private TextView timeText;
        private ImageView participantPhoto;

        public RouteWayPointViewHolder(View itemView) {
            super(itemView);

            background = (ImageView) itemView.findViewById(R.id.card_background); // TODO change??

            wayPointTitleText = (TextView) itemView.findViewById(R.id.text_waypoint_title);
            participantNameText = (TextView) itemView.findViewById(R.id.waypoint_participant_name);
            addressText = (TextView) itemView.findViewById(R.id.text_waypoint_address);
            timeText = (TextView) itemView.findViewById(R.id.text_waypoint_time);
            participantPhoto = itemView.findViewById(R.id.waypoint_participant_photo);

            itemView.setOnClickListener(this);
        }

        public void bindToPost(RouteWayPoint routeWayPoint) {

            background.setImageResource(R.drawable.waypoint_card);

            wayPointTitleText.setText(routeWayPoint.text);
            participantNameText.setText(routeWayPoint.participantName);


            timeText.setText(routeWayPoint.time);

            String preAddressString = "";
            if (routeWayPoint.type.equals("driver")) {
                preAddressString = "from ";
                participantNameText.setText(routeWayPoint.participantName);
                participantPhoto.setBackgroundResource(R.drawable.ic_user_outline);
            }
            else if (routeWayPoint.type.equals("passenger")) {
                preAddressString = "pickup at ";
                participantNameText.setText(routeWayPoint.participantName);
                participantPhoto.setBackgroundResource(R.drawable.ic_user_outline);
            }
            else if (routeWayPoint.type.equals("destination")) {
                preAddressString = "";
                participantNameText.setVisibility(View.INVISIBLE);
                participantPhoto.setBackgroundResource(R.drawable.ic_account_balance_black_24dp);//R.drawable.icon_organization);
            }

            String addressString = preAddressString + routeWayPoint.address;
            addressText.setText(addressString);
        }

        @Override
        public void onClick(View view) {
            System.out.println("RouteWayPointViewHolder Clicked!");
            clickListener.onClick(view, getAdapterPosition()); // call the onClick in the OnItemClickListener
        }
    }
}

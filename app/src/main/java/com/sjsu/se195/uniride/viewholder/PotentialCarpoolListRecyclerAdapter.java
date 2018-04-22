package com.sjsu.se195.uniride.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sjsu.se195.uniride.R;
import com.sjsu.se195.uniride.models.Carpool;
import com.sjsu.se195.uniride.models.Post;

import java.util.List;

/**
 * Created by timhdavis on 4/17/18.
 */

public class PotentialCarpoolListRecyclerAdapter extends RecyclerView.Adapter<PotentialCarpoolListRecyclerAdapter.PotentialCarpoolViewHolder> {
    private List<Carpool> mDataset;
    private OnItemClickListener clickListener;

    // Provide a suitable constructor (depends on the kind of dataset)
    public PotentialCarpoolListRecyclerAdapter(List<Carpool> myDataset) {
        mDataset = myDataset;
    }


    @Override
    public PotentialCarpoolViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_matched_post, parent, false);

        return new PotentialCarpoolViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PotentialCarpoolViewHolder holder, int position) {
        holder.bindToPost(mDataset.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    public void setClickListener(OnItemClickListener itemClickListener) {
        System.out.println("PostListRecyclerAdapter: Setting itemClickListener = " + itemClickListener);
        this.clickListener = itemClickListener;
    }



    // View Holder inner class:


    public class PotentialCarpoolViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView sourceView;
        private TextView authorView;
        private TextView destinationView;
        private TextView passengerCountTextView;
        private TextView estimatedTravelTimeTextView;
        private TextView estimatedTravelDistanceTextView;

        public PotentialCarpoolViewHolder(View itemView) {
            super(itemView);

            sourceView = (TextView) itemView.findViewById(R.id.post_source);
            authorView = (TextView) itemView.findViewById(R.id.post_author);
            destinationView = (TextView) itemView.findViewById(R.id.post_destination);

            passengerCountTextView = itemView.findViewById(R.id.post_passenger_count);
            estimatedTravelTimeTextView = itemView.findViewById(R.id.carpool_estimated_trip_time);
            estimatedTravelDistanceTextView = itemView.findViewById(R.id.carpool_estimated_trip_distance);

            itemView.setOnClickListener(this);
        }

        public void bindToPost(Carpool carpool) {
            sourceView.setText(carpool.source);
            authorView.setText(carpool.author);
            destinationView.setText(carpool.destination);

            passengerCountTextView.setText(carpool.getNumberSeatsTaken() + " / " + carpool.getPassengerCount() + " Passengers");
            estimatedTravelTimeTextView.setText(carpool.getEstimatedTotalTripTimeInMinutes() + " minutes");
            estimatedTravelDistanceTextView.setText(carpool.getEstimatedTotalTripDistanceInKilometers() + " km");
        }

        @Override
        public void onClick(View view) {
            System.out.println("SearchPostViewHolder Clicked!");
            clickListener.onClick(view, getAdapterPosition()); // call the onClick in the OnItemClickListener
        }
    }
}

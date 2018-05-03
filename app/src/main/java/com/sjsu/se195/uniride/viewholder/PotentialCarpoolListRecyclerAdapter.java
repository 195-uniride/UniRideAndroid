package com.sjsu.se195.uniride.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjsu.se195.uniride.PostInfo;
import com.sjsu.se195.uniride.R;
import com.sjsu.se195.uniride.models.Carpool;
import com.sjsu.se195.uniride.models.Post;

import java.text.DecimalFormat;
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

        public ImageView background;
        private TextView sourceView;
        private TextView authorView;
        private TextView destinationView;
        private TextView passengerCountTextView;
        private TextView estimatedTravelTimeTextView;
        private TextView estimatedTravelDistanceTextView;
        public TextView to;

        // TODO: add driver & passengers

        public TextView postTripDateText;
        public TextView postDateTimeText;

        public PotentialCarpoolViewHolder(View itemView) {
            super(itemView);

            background = (ImageView) itemView.findViewById(R.id.card_background);

            sourceView = (TextView) itemView.findViewById(R.id.post_source);
            authorView = (TextView) itemView.findViewById(R.id.post_cardview_author_name);
            destinationView = (TextView) itemView.findViewById(R.id.post_destination);

            passengerCountTextView = itemView.findViewById(R.id.post_passenger_count);
            estimatedTravelTimeTextView = itemView.findViewById(R.id.carpool_estimated_trip_time);
            estimatedTravelDistanceTextView = itemView.findViewById(R.id.carpool_estimated_trip_distance);
            to = (TextView) itemView.findViewById(R.id.post_card_address_to);

            postTripDateText = itemView.findViewById(R.id.post_date);
            postDateTimeText = itemView.findViewById(R.id.post_time);

            itemView.setOnClickListener(this);
        }

        public void bindToPost(Carpool carpool) {

            background.setImageResource(R.drawable.carpool_card);

            sourceView.setText(carpool.source);
            authorView.setText(carpool.author);
            destinationView.setText(carpool.destination);

            to.setText("to");
            postTripDateText.setText(PostInfo.getTripDateText(carpool));
            postDateTimeText.setText("Arrive at " + PostInfo.getArrivalDateTimeText(carpool));

            // Show seats taken - 1 because 1 will always be taken by the potential new rider:
            passengerCountTextView.setText((carpool.getNumberSeatsTaken() - 1) + " / " + carpool.getPassengerCount() + " Passengers");
            estimatedTravelTimeTextView.setText(carpool.getEstimatedTotalTripTimeInMinutes() + " minutes");

            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            String distance = decimalFormat.format(carpool.getEstimatedTotalTripDistanceInMiles()) + " mi";
            estimatedTravelDistanceTextView.setText(distance);
        }

        @Override
        public void onClick(View view) {
            System.out.println("SearchPostViewHolder Clicked!");
            clickListener.onClick(view, getAdapterPosition()); // call the onClick in the OnItemClickListener
        }
    }
}

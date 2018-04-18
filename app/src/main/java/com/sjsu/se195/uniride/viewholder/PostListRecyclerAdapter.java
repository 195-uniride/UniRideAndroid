package com.sjsu.se195.uniride.viewholder;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjsu.se195.uniride.PostDetailActivity;
import com.sjsu.se195.uniride.R;
import com.sjsu.se195.uniride.models.Post;

import java.util.List;

/**
 * Created by timhdavis on 4/9/18.
 */

public class PostListRecyclerAdapter extends RecyclerView.Adapter<PostListRecyclerAdapter.SearchPostViewHolder> {

    private List<Post> mDataset;
    private OnItemClickListener clickListener;

    // Provide a suitable constructor (depends on the kind of dataset)
    public PostListRecyclerAdapter(List<Post> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public SearchPostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_matched_post, parent, false);

        return new SearchPostViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SearchPostViewHolder holder, int position) {
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


    public class SearchPostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView sourceView;
        private TextView authorView;
        private TextView destinationView;
        private TextView passengerCountTextView;
        private TextView estimatedTravelTimeTextView;
        private TextView estimatedTravelDistanceTextView;

        public SearchPostViewHolder(View itemView) {
            super(itemView);

            sourceView = (TextView) itemView.findViewById(R.id.post_source);
            authorView = (TextView) itemView.findViewById(R.id.post_author);
            destinationView = (TextView) itemView.findViewById(R.id.post_destination);

            passengerCountTextView = itemView.findViewById(R.id.post_passenger_count);
            estimatedTravelTimeTextView = itemView.findViewById(R.id.carpool_estimated_trip_time);
            estimatedTravelDistanceTextView = itemView.findViewById(R.id.carpool_estimated_trip_distance);

            itemView.setOnClickListener(this);
        }

        public void bindToPost(Post post) {
            sourceView.setText(post.source);
            authorView.setText(post.author);
            destinationView.setText(post.destination);

            passengerCountTextView.setText("X / Y Passengers");
            estimatedTravelTimeTextView.setText("X01 minutes");
            estimatedTravelDistanceTextView.setText("Z23 miles");
        }

        @Override
        public void onClick(View view) {
            System.out.println("SearchPostViewHolder Clicked!");
            clickListener.onClick(view, getAdapterPosition()); // call the onClick in the OnItemClickListener
        }
    }


}

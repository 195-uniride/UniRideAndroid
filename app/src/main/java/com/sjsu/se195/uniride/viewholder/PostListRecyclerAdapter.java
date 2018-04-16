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
                .inflate(R.layout.item_post, parent, false);

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

        public TextView sourceView;
        public TextView authorView;
        public ImageView starView;
        public TextView numStarsView;
        public TextView destinationView;

        public SearchPostViewHolder(View itemView) {
            super(itemView);

            sourceView = (TextView) itemView.findViewById(R.id.post_source);
            authorView = (TextView) itemView.findViewById(R.id.post_author);
            starView = (ImageView) itemView.findViewById(R.id.star);
            numStarsView = (TextView) itemView.findViewById(R.id.post_num_stars);
            destinationView = (TextView) itemView.findViewById(R.id.post_destination);

            itemView.setOnClickListener(this);
        }

        public void bindToPost(Post post, View.OnClickListener starClickListener) {
            sourceView.setText(post.source);
            authorView.setText(post.author);
            numStarsView.setText(String.valueOf(post.starCount));
            destinationView.setText(post.destination);

            starView.setOnClickListener(starClickListener);
        }

        public void bindToPost(Post post) {
            sourceView.setText(post.source);
            authorView.setText(post.author);
            numStarsView.setText(String.valueOf(post.starCount));
            destinationView.setText(post.destination);
        }

        public void bindStarClickListenerToPost(View.OnClickListener starClickListener) {
            starView.setOnClickListener(starClickListener);
        }

        @Override
        public void onClick(View view) {
            System.out.println("SearchPostViewHolder Clicked!");
            clickListener.onClick(view, getAdapterPosition()); // call the onClick in the OnItemClickListener
        }
    }


}

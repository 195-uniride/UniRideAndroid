package com.sjsu.se195.uniride.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sjsu.se195.uniride.R;
import com.sjsu.se195.uniride.models.Post;

import java.util.List;

/**
 * Created by timhdavis on 4/9/18.
 */

public class PostListRecyclerAdapter extends RecyclerView.Adapter<PostViewHolder> {

    private List<Post> mDataset;

    // Provide a suitable constructor (depends on the kind of dataset)
    public PostListRecyclerAdapter(List<Post> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);

        return new PostViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PostViewHolder holder, int position) {
        holder.bindToPost(mDataset.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

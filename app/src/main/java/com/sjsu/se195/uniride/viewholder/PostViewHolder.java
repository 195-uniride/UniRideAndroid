package com.sjsu.se195.uniride.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjsu.se195.uniride.R;
import com.sjsu.se195.uniride.models.Post;

public class PostViewHolder extends RecyclerView.ViewHolder {

    public TextView sourceView;
    public TextView authorView;
    public ImageView starView;
    public TextView numStarsView;
    public TextView destinationView;

    public PostViewHolder(View itemView) {
        super(itemView);

        sourceView = (TextView) itemView.findViewById(R.id.post_source);
        authorView = (TextView) itemView.findViewById(R.id.post_author);
        starView = (ImageView) itemView.findViewById(R.id.star);
        numStarsView = (TextView) itemView.findViewById(R.id.post_num_stars);
        destinationView = (TextView) itemView.findViewById(R.id.post_destination);
    }

    public void bindToPost(Post post, View.OnClickListener starClickListener) {
        sourceView.setText(post.source);
        authorView.setText(post.author);
        numStarsView.setText(String.valueOf(post.starCount));
        destinationView.setText(post.destination);

        starView.setOnClickListener(starClickListener);
    }
}

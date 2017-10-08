package com.sjsu.se195.uniride.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
//import com.sjsu.se195.uniride.PostDetailActivity; //TODO
import com.sjsu.se195.uniride.OrganizationDetailActivity; //changed
import com.sjsu.se195.uniride.R;
import com.sjsu.se195.uniride.models.Organization; //changed
//import com.sjsu.se195.uniride.viewholder.PostViewHolder; //TODO
import com.sjsu.se195.uniride.viewholder.OrganizationViewHolder; //changed

public abstract class OrganizationListFragment extends Fragment {

    private static final String TAG = "OrganizationListFragment";

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]

    private FirebaseRecyclerAdapter<Organization, OrganizationViewHolder> mAdapter; // TODO
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    public OrganizationListFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_organizations, container, false); // TODO

        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]

        mRecycler = (RecyclerView) rootView.findViewById(R.id.messages_list); // TODO: don't need??
        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query organizationsQuery = getQuery(mDatabase);
        mAdapter = new FirebaseRecyclerAdapter<Organization, OrganizationViewHolder>(Organization.class, R.layout.item_post,  // TODO
                OrganizationViewHolder.class, organizationsQuery) {
            @Override
            protected void populateViewHolder(final OrganizationViewHolder viewHolder, final Organization model, final int position) {  // TODO
                final DatabaseReference organizationRef = getRef(position); // TODO

                // Set click listener for the whole organization view
                final String organizationKey = organizationRef.getKey(); // TODO
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch OrganizationDetailActivity // TODO
                        Intent intent = new Intent(getActivity(), OrganizationDetailActivity.class); // TODO
                        intent.putExtra(OrganizationDetailActivity.EXTRA_ORGANIZATION_KEY, organizationKey); // TODO
                        startActivity(intent);
                    }
                });

//                // Determine if the current user has liked this post and set UI accordingly
//                if (model.stars.containsKey(getUid())) {
//                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_24);
//                } else {
//                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
//                }

                // Bind Post to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToPost(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View starView) {
                        // Need to write to both places the post is stored
                        DatabaseReference globalPostRef = mDatabase.child("organizations").child(organizationRef.getKey()); // TODO
                        //TODO:?
//                        DatabaseReference userPostRef = mDatabase.child("user-posts").child(model.uid).child(organizationKey.getKey()); // TODO

                        // Run two transactions
//                        onStarClicked(globalPostRef); // TODO
//                        onStarClicked(userPostRef); // TODO
                    }
                });
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    // [START post_stars_transaction]
    private void onStarClicked(DatabaseReference organizationRef) { // TODO
        organizationRef.runTransaction(new Transaction.Handler() { // TODO
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Organization org = mutableData.getValue(Organization.class); // TODO
                if (org == null) { // TODO
                    return Transaction.success(mutableData);
                }

//                if (p.stars.containsKey(getUid())) { // TODO
//                    // Unstar the post and remove self from stars
//                    p.starCount = p.starCount - 1; // TODO
//                    p.stars.remove(getUid()); // TODO
//                } else {
//                    // Star the post and add self to stars
//                    p.starCount = p.starCount + 1; // TODO
//                    p.stars.put(getUid(), true); // TODO
//                }

                // Set value and report transaction success
                mutableData.setValue(org); // TODO
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "orgTransaction:onComplete:" + databaseError); // TODO
            }
        });
    }
    // [END post_stars_transaction]

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);

}

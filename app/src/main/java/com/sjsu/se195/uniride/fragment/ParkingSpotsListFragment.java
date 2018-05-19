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
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.sjsu.se195.uniride.R;
import com.sjsu.se195.uniride.models.ParkingSpot; //changed
import com.sjsu.se195.uniride.viewholder.ParkingSpotViewHolder; //changed

//A lot of this file is taken from OrganizationListFragment
public class ParkingSpotsListFragment extends Fragment {

    private static final String TAG = "ParkingSpotsListFragment";
    private RecyclerView mRecycler;
    private DatabaseReference mDatabase;
    private FirebaseRecyclerAdapter<ParkingSpot, ParkingSpotViewHolder> mAdapter;
    private LinearLayoutManager mManager;
    protected String uID;
    private String garage_name;
    private String garage_level;
    private String org_name;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_parkingspots, container, false);
        this.uID = getUid(); // TODO: pass userID to keep query independent of current user

        Bundle args = new Bundle();
        garage_name = this.getArguments().getString("garage");
        garage_level = this.getArguments().getString("level");
        org_name = this.getArguments().getString("organization");

        System.out.println("inside onCreateOf ParkingSpotsListFragment &&&&&&&&&&&&&&&&&&&&&&&&&: " + garage_name + ", " + garage_level);
        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]

        mRecycler = (RecyclerView) rootView.findViewById(R.id.messages_list_parkingspots);
        mRecycler.setHasFixedSize(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        System.out.println("query sent");
        final Query parkingSpotQuery = getQuery(mDatabase);
        System.out.println("Waiting for query");

        mAdapter = new FirebaseRecyclerAdapter<ParkingSpot, ParkingSpotViewHolder>(ParkingSpot.class, R.layout.item_parkingspot,
                ParkingSpotViewHolder.class, parkingSpotQuery) {
            @Override
            protected void populateViewHolder(final ParkingSpotViewHolder viewHolder, final ParkingSpot model, final int position) {
                final DatabaseReference parkingSpotRef = getRef(position); //TODO: investigate: this is fine (viewing the item works).
                System.out.println("Populating views: " + parkingSpotRef);
                System.out.println("Query : " + parkingSpotQuery + ">>>>>>>>>>>>>>>>>>>>>>>>");

                //no need to set onclick listener (the spot list won't be clickable, for now)
                final String organizationKey = parkingSpotRef.getKey();
//                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        // Launch OrganizationDetailActivity
//                        Intent intent = new Intent(getActivity(), OrganizationDetailActivity.class);
//                        intent.putExtra(OrganizationDetailActivity.EXTRA_ORGANIZATION_KEY, organizationKey);
//                        startActivity(intent);
//                    }
//                });

                // Bind Organization to ViewHolder
                //Log.d(TAG, "<2> organization model: " + model.name);//TODO: investigate why My Organization organizations are null here...
                viewHolder.bindToParkingSpot(model);
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

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

    public Query getQuery(DatabaseReference databaseReference){                 //}, String level, String section) {
        // All my organizations
        System.out.println("*** get the parking spots for garage name: " + garage_name + ", level "+garage_level.getClass().getName());
        String parking_spots_here = "";//TODO: combine the other strings to so parking spots on specific levels can be found
        return databaseReference.child("parking-garage").child(org_name).child(garage_name).child(garage_level).orderByChild("State").equalTo("empty");
    }
}

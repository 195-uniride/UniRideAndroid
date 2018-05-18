package com.sjsu.se195.uniride.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sjsu.se195.uniride.PreviewCarpoolDetailActivity;
import com.sjsu.se195.uniride.R;
import com.sjsu.se195.uniride.models.Carpool;
import com.sjsu.se195.uniride.models.Post;
import com.sjsu.se195.uniride.models.RouteWayPoint;
import com.sjsu.se195.uniride.viewholder.OnItemClickListener;
import com.sjsu.se195.uniride.viewholder.PotentialCarpoolListRecyclerAdapter;
import com.sjsu.se195.uniride.viewholder.RouteWayPointListRecyclerAdapter;

import java.util.ArrayList;

/**
 * Created by timhdavis on 5/18/18.
 */

public class RouteWayPointListFragment extends Fragment implements OnItemClickListener {
    private static final String TAG = "RouteWayPointListFragment";
    public static final String EXTRA_ROUTE_WAYPOINT_LIST = "RouteWayPointListFragment.routeWayPointList";

    private RecyclerView mRecycler;
    private RouteWayPointListRecyclerAdapter mAdapter;
    private LinearLayoutManager mManager;

    private ArrayList<RouteWayPoint> mRouteWaypointList;

    public RouteWayPointListFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Get list of RouteWayPoint objects to display:
        mRouteWaypointList = getArguments().getParcelableArrayList(RouteWayPointListFragment.EXTRA_ROUTE_WAYPOINT_LIST);

        if (mRouteWaypointList == null) {
            throw new IllegalArgumentException(TAG + ": Must pass EXTRA_SEARCH_RESULTS.");
        }

        if (mRouteWaypointList.size() == 0) {
            throw new IllegalArgumentException(TAG + ": mRouteWaypointList size must be greater than 0.");
        }

        View rootView;

        rootView = inflater.inflate(R.layout.fragment_route_waypoints, container, false);

        mRecycler = rootView.findViewById(R.id.messages_list);

        mRecycler.setHasFixedSize(true);


        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        System.out.println("in Fragment onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        //postType = savedInstanceState.getBundle("postType");

        if (mRouteWaypointList.size() > 0) {
            // Set up Layout Manager, reverse layout
            mManager = new LinearLayoutManager(getActivity());
            mManager.setReverseLayout(true);
            mManager.setStackFromEnd(true);
            mRecycler.setLayoutManager(mManager);

            loadWayPoints();
        }



    }


    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (mAdapter != null) {
//            // mAdapter.cleanup(); // TODO?
//        }
    }


    private void loadWayPoints() {
        System.out.println("About to load waypoints.....");

        // With Potential Carpool objects:
        mAdapter = new RouteWayPointListRecyclerAdapter(mRouteWaypointList);

        mRecycler.setAdapter(mAdapter);

        mAdapter.setClickListener(this);
    }

    @Override
    public void onClick(View view, int position) {
        // Get potential carpool post clicked from position in list:

        RouteWayPoint routeWayPointClicked = mRouteWaypointList.get(position);

        System.out.println("routeWayPointClicked = " + routeWayPointClicked);

        // DO NOTHING...
    }
}

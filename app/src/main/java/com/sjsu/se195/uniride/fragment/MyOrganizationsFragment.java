package com.sjsu.se195.uniride.fragment;

/**
 * Created by timhdavis on 11/11/17.
 */

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class MyOrganizationsFragment extends OrganizationListFragment {

    public MyOrganizationsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // All my organizations
        return databaseReference.child("user-organizations")
                .child(getUid()); //TODO: investigate why this is returning null organizations in populateViewHolder in Query organizationsQuery in OrganizationListFragment...
    }
}

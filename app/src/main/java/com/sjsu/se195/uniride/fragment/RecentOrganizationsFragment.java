package com.sjsu.se195.uniride.fragment;

/**
 * Created by timhdavis on 10/8/17.
 */

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class RecentOrganizationsFragment extends OrganizationListFragment {

    public RecentOrganizationsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // [START recent_organizations_query]
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keysm
        Query recentOrganizationsQuery = databaseReference.child("organizations")
                .limitToFirst(100);
        // [END recent_organizations_query]

        return recentOrganizationsQuery;
    }
}

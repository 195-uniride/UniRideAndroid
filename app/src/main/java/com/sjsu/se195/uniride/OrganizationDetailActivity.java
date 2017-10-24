package com.sjsu.se195.uniride;

/**
 * Created by timhdavis on 10/8/17.
 */

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sjsu.se195.uniride.models.Organization;
import com.sjsu.se195.uniride.models.User;
//import com.sjsu.se195.uniride.models.Comment;
//import com.sjsu.se195.uniride.models.Post;

import java.util.ArrayList;
import java.util.List;

public class OrganizationDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "OrganizationDetailActivity";

    public static final String EXTRA_ORGANIZATION_KEY = "organization_key";

    private DatabaseReference mOrganizationReference;
//    private DatabaseReference mCommentsReference;
    private ValueEventListener mOrganizationListener;
    private String mOrganizationKey;
//    private CommentAdapter mAdapter;

    // TODO: change all...vvv
    private TextView mNameView;
    // TODO...add other fields...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_detail); // TODO

        // Get post key from intent // TODO
        mOrganizationKey = getIntent().getStringExtra(EXTRA_ORGANIZATION_KEY);
        if (mOrganizationKey == null) { // TODO
            throw new IllegalArgumentException("Must pass EXTRA_ORGANIZATION_KEY");
        }

        // Initialize Database
        mOrganizationReference = FirebaseDatabase.getInstance().getReference()
                .child("organizations").child(mOrganizationKey); // TODO
//        mCommentsReference = FirebaseDatabase.getInstance().getReference()
//                .child("post-comments").child(mPostKey); // TODO

        // Initialize Views // TODO: all...vvv
        mNameView = (TextView) findViewById(R.id.organization_name);
//        mTitleView = (TextView) findViewById(R.id.post_title);
//        mBodyView = (TextView) findViewById(R.id.post_body);
//        mCommentField = (EditText) findViewById(R.id.field_comment_text);
//        mCommentButton = (Button) findViewById(R.id.button_post_comment);
//        mCommentsRecycler = (RecyclerView) findViewById(R.id.recycler_comments);

//        mCommentButton.setOnClickListener(this);
//        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    public void onStart() {
        super.onStart();

        // Add value event listener to the post
        // [START organization_value_event_listener]
        ValueEventListener organizationListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Organization object and use the values to update the UI
                Organization organization = dataSnapshot.getValue(Organization.class);
                // [START_EXCLUDE]
                mNameView.setText(organization.name); // TODO
//                mTitleView.setText(organization.title); // TODO
//                mBodyView.setText(organization.body); // TODO
                // [END_EXCLUDE]
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Organization failed, log a message
                Log.w(TAG, "loadOrg:onCancelled", databaseError.toException()); // TODO
                // [START_EXCLUDE]
                Toast.makeText(OrganizationDetailActivity.this, "Failed to load organization.", // TODO
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        mOrganizationReference.addValueEventListener(organizationListener); // TODO
        // [END post_value_event_listener] // TODO

        // Keep copy of organization listener so we can remove it when app stops // TODO
        mOrganizationListener = organizationListener; // TODO

//        // Listen for comments // TODO
//        mAdapter = new CommentAdapter(this, mCommentsReference); // TODO
//        mCommentsRecycler.setAdapter(mAdapter); // TODO
    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove organization value event listener // TODO
        if (mOrganizationListener != null) { // TODO
            mOrganizationReference.removeEventListener(mOrganizationListener); // TODO
        }

        // Clean up comments listener // TODO
//        mAdapter.cleanupListener(); // TODO
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
//        if (i == R.id.button_post_comment) { // TODO
//            postComment(); // TODO
//        }
    }

//    private void postComment() { // TODO
//        final String uid = getUid();
//        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        // Get user information
//                        User user = dataSnapshot.getValue(User.class);
//                        String authorName = user.username;
//
//                        // Create new comment object
//                        String commentText = mCommentField.getText().toString();
//                        Comment comment = new Comment(uid, authorName, commentText);
//
//                        // Push the comment, it will appear in the list
//                        mCommentsReference.push().setValue(comment);
//
//                        // Clear the field
//                        mCommentField.setText(null);
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//    }

//    private static class CommentViewHolder extends RecyclerView.ViewHolder {
//
//        public TextView authorView;
//        public TextView bodyView;
//
//        public CommentViewHolder(View itemView) {
//            super(itemView);
//
//            authorView = (TextView) itemView.findViewById(R.id.comment_author);
//            bodyView = (TextView) itemView.findViewById(R.id.comment_body);
//        }
//    }

//    private static class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {
//
//        private Context mContext;
//        private DatabaseReference mDatabaseReference;
//        private ChildEventListener mChildEventListener;
//
//        private List<String> mCommentIds = new ArrayList<>();
//        private List<Comment> mComments = new ArrayList<>();
//
//        public CommentAdapter(final Context context, DatabaseReference ref) {
//            mContext = context;
//            mDatabaseReference = ref;
//
//            // Create child event listener
//            // [START child_event_listener_recycler]
//            ChildEventListener childEventListener = new ChildEventListener() {
//                @Override
//                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
//                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
//
//                    // A new comment has been added, add it to the displayed list
//                    Comment comment = dataSnapshot.getValue(Comment.class);
//
//                    // [START_EXCLUDE]
//                    // Update RecyclerView
//                    mCommentIds.add(dataSnapshot.getKey());
//                    mComments.add(comment);
//                    notifyItemInserted(mComments.size() - 1);
//                    // [END_EXCLUDE]
//                }
//
//                @Override
//                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
//                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
//
//                    // A comment has changed, use the key to determine if we are displaying this
//                    // comment and if so displayed the changed comment.
//                    Comment newComment = dataSnapshot.getValue(Comment.class);
//                    String commentKey = dataSnapshot.getKey();
//
//                    // [START_EXCLUDE]
//                    int commentIndex = mCommentIds.indexOf(commentKey);
//                    if (commentIndex > -1) {
//                        // Replace with the new data
//                        mComments.set(commentIndex, newComment);
//
//                        // Update the RecyclerView
//                        notifyItemChanged(commentIndex);
//                    } else {
//                        Log.w(TAG, "onChildChanged:unknown_child:" + commentKey);
//                    }
//                    // [END_EXCLUDE]
//                }
//
//                @Override
//                public void onChildRemoved(DataSnapshot dataSnapshot) {
//                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
//
//                    // A comment has changed, use the key to determine if we are displaying this
//                    // comment and if so remove it.
//                    String commentKey = dataSnapshot.getKey();
//
//                    // [START_EXCLUDE]
//                    int commentIndex = mCommentIds.indexOf(commentKey);
//                    if (commentIndex > -1) {
//                        // Remove data from the list
//                        mCommentIds.remove(commentIndex);
//                        mComments.remove(commentIndex);
//
//                        // Update the RecyclerView
//                        notifyItemRemoved(commentIndex);
//                    } else {
//                        Log.w(TAG, "onChildRemoved:unknown_child:" + commentKey);
//                    }
//                    // [END_EXCLUDE]
//                }
//
//                @Override
//                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
//                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
//
//                    // A comment has changed position, use the key to determine if we are
//                    // displaying this comment and if so move it.
//                    Comment movedComment = dataSnapshot.getValue(Comment.class);
//                    String commentKey = dataSnapshot.getKey();
//
//                    // ...
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//                    Log.w(TAG, "postComments:onCancelled", databaseError.toException());
//                    Toast.makeText(mContext, "Failed to load comments.",
//                            Toast.LENGTH_SHORT).show();
//                }
//            };
//            ref.addChildEventListener(childEventListener);
//            // [END child_event_listener_recycler]
//
//            // Store reference to listener so it can be removed on app stop
//            mChildEventListener = childEventListener;
//        }
//
//        @Override
//        public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            LayoutInflater inflater = LayoutInflater.from(mContext);
//            View view = inflater.inflate(R.layout.item_comment, parent, false);
//            return new CommentViewHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(CommentViewHolder holder, int position) {
//            Comment comment = mComments.get(position);
//            holder.authorView.setText(comment.author);
//            holder.bodyView.setText(comment.text);
//        }
//
//        @Override
//        public int getItemCount() {
//            return mComments.size();
//        }
//
//        public void cleanupListener() {
//            if (mChildEventListener != null) {
//                mDatabaseReference.removeEventListener(mChildEventListener);
//            }
//        }
//
//    }
}

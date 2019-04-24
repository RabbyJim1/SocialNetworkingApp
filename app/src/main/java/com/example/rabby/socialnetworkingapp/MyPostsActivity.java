package com.example.rabby.socialnetworkingapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private RecyclerView myPostLists;
    private FirebaseAuth mAuth;
    private DatabaseReference postsRef, likesRef;
    private String currentUserID;
    private boolean likeChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef = FirebaseDatabase.getInstance().getReference().child("likes");

        mToolbar = findViewById(R.id.my_post_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("My Posts");
        myPostLists = findViewById(R.id.my_all_post_list);
        myPostLists.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostLists.setLayoutManager(linearLayoutManager);

        displayMyAllPosts();
    }

    private void displayMyAllPosts() {

        Query myPostsQuery = postsRef.orderByChild("uid").startAt(currentUserID).endAt(currentUserID + "\uf8ff");

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(myPostsQuery, Posts.class).build();

        FirebaseRecyclerAdapter<Posts, MyPostsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Posts, MyPostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyPostsViewHolder holder, int position, @NonNull Posts model) {

                final String myPostKey = getRef(position).getKey();

                holder.setFullName(model.getFullName());
                holder.setTime(model.getTime());
                holder.setDate(model.getDate());
                holder.setDescription(model.getDescription());
                holder.setProfileImage(getApplicationContext(), model.getProfileImage());
                holder.setPostimage(getApplicationContext(), model.getPostimage());

                holder.setLikeButtonStatus(myPostKey);

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent(MyPostsActivity.this, ClickPostActivity.class);
                        clickPostIntent.putExtra("postKey",myPostKey);
                        startActivity(clickPostIntent);
                    }
                });

                holder.commentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickCommentIntent = new Intent(MyPostsActivity.this, CommentsActivity.class);
                        clickCommentIntent.putExtra("postKey",myPostKey);
                        startActivity(clickCommentIntent);
                    }
                });

                holder.likePostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        likeChecker = true;
                        likesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(likeChecker==true){
                                    if(dataSnapshot.child(myPostKey).hasChild(currentUserID)){
                                        likesRef.child(myPostKey).child(currentUserID).removeValue();
                                        likeChecker = false;
                                    }else{
                                        likesRef.child(myPostKey).child(currentUserID).setValue(true);
                                        likeChecker = false;

                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                });

            }

            @NonNull
            @Override
            public MyPostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent,false);
                MyPostsViewHolder viewHolder = new MyPostsViewHolder(view);
                return viewHolder;
            }


        };
        myPostLists.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    public static class MyPostsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        ImageButton likePostButton, commentPostButton;
        TextView displayNoOfLikeTV, commentTV;
        int countLikes=0;
        DatabaseReference likeRef;
        String mycurrentUserID;



        public MyPostsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            likePostButton = (ImageButton) mView.findViewById(R.id.like_button);
            commentPostButton = (ImageButton) mView.findViewById(R.id.comment_button);
            displayNoOfLikeTV = (TextView) mView.findViewById(R.id.display_no_of_like);
            commentTV = (TextView) mView.findViewById(R.id.comment_text);
            likeRef = FirebaseDatabase.getInstance().getReference().child("likes");
            mycurrentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }


        public void setLikeButtonStatus(final String postKey){
            likeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(postKey).hasChild(mycurrentUserID)){
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.like);
                        displayNoOfLikeTV.setText(Integer.toString(countLikes)+" Likes");
                    }else {
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.dislike);
                        displayNoOfLikeTV.setText(Integer.toString(countLikes)+" Likes");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


        public void setFullName(String fullName){
            TextView userNameTV = (TextView) mView.findViewById(R.id.post_user_name);
            userNameTV.setText(fullName);
        }
        public void setProfileImage(Context ctx, String profileImage){
            CircleImageView imageView = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.get().load(profileImage).into(imageView);

        }
        public void setTime(String postTime){
            TextView postTimeTV = (TextView) mView.findViewById(R.id.post_time);
            postTimeTV.setText("   "+postTime);
        }
        public void setDate(String date){
            TextView postDateTV = (TextView) mView.findViewById(R.id.post_date);
            postDateTV.setText("   "+date);
        }
        public void setDescription(String description){
            TextView postDescriptionTV = (TextView) mView.findViewById(R.id.click_post_description);
            postDescriptionTV.setText(description);
        }
        public void setPostimage(Context ctx, String postimage){
            ImageView postImageIV = (ImageView) mView.findViewById(R.id.click_post_image);
            Picasso.get().load(postimage).into(postImageIV);

        }

    }

}

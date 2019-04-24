package com.example.rabby.socialnetworkingapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {

    private ImageButton postCommentB;
    private EditText commentInputET;
    private RecyclerView commentsListRV;
    private String post_key, currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef, PostsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();


        post_key = getIntent().getExtras().get("postKey").toString();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(post_key).child("comments");

        commentsListRV = findViewById(R.id.comments_list);
        commentsListRV.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentsListRV.setLayoutManager(linearLayoutManager);



        commentInputET = findViewById(R.id.comment_input);
        postCommentB = findViewById(R.id.post_comment_button);

        postCommentB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String userName = dataSnapshot.child("fullName").getValue().toString();
                            String image = dataSnapshot.child("profileImage").getValue().toString();

                            validateComment(userName, image);
                            commentInputET.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Comments> options =
                new FirebaseRecyclerOptions.Builder<Comments>()
                        .setQuery(PostsRef, Comments.class).build();

        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull Comments model) {

                holder.setUserFullName(model.getUserFullName());
                holder.setComment(model.getComment());
                holder.setDate(model.getDate());
                holder.setTime(model.getTime());
                holder.setUserProfileImage(getApplicationContext(), model.getUserProfileImage());

            }

            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_comments_layout, parent,false);
                CommentsActivity.CommentsViewHolder viewHolder = new CommentsActivity.CommentsViewHolder(view);
                return viewHolder;
            }
        };

        commentsListRV.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public CommentsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUserProfileImage(Context ctx, String userProfileImage){

            CircleImageView MyProfileImage = (CircleImageView) mView.findViewById(R.id.comment_user_profile_image);
            Picasso.get().load(userProfileImage).into(MyProfileImage);
        }

        public void setTime(String time){
            TextView timeTV = (TextView) mView.findViewById(R.id.comment_time);
            timeTV.setText("Time: "+time);

        }
        public void setDate(String date){
            TextView dateTV = (TextView) mView.findViewById(R.id.comment_date);
            dateTV.setText("Date: "+date);

        }
        public void setComment(String comment){

            TextView myComment = (TextView) mView.findViewById(R.id.comment_text);
            myComment.setText(comment);

        }
        public void setUserFullName(String userFullName){

            TextView myUserFullName = (TextView) mView.findViewById(R.id.comment_user_name);
            myUserFullName.setText(userFullName);

        }






    }



    private void validateComment(String userName, String image) {
        String commentText = commentInputET.getText().toString();
        if(TextUtils.isEmpty(commentText)){
            Toast.makeText(this, "Please, write a comment...", Toast.LENGTH_SHORT).show();
        }else {
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:SS");
            final String saveCurrentTime = currentTime.format(calFordDate.getTime());

            final String randomKey = currentUserID + saveCurrentDate + saveCurrentTime;

            HashMap commentsMap = new HashMap();
            commentsMap.put("userFullName",userName);
            commentsMap.put("uid",currentUserID);
            commentsMap.put("comment",commentText);
            commentsMap.put("date",saveCurrentDate);
            commentsMap.put("time",saveCurrentTime);
            commentsMap.put("userProfileImage",image);

            PostsRef.child(randomKey).updateChildren(commentsMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(CommentsActivity.this, "You have commented successfully", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(CommentsActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }
}

package com.example.rabby.socialnetworkingapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView myFriendList;
    private DatabaseReference friendsRef, usersRef;
    private FirebaseAuth mAuth;
    private String online_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        myFriendList = findViewById(R.id.frinds_list);
        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        displayAllfriends();

    }

    public void updateUserStatus(String state){
        String saveCurrentDate, saveCurrentTime;
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd,yyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time",saveCurrentTime);
        currentStateMap.put("date",saveCurrentDate);
        currentStateMap.put("type",state);

        usersRef.child(online_user_id).child("userSate")
                .updateChildren(currentStateMap);

    }

    //serverTime

    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("online");
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateUserStatus("offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateUserStatus("offline");
    }

    private void displayAllfriends() {

        FirebaseRecyclerOptions<Friends> options
                = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(friendsRef, Friends.class).build();

        FirebaseRecyclerAdapter<Friends, friendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Friends, friendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final friendsViewHolder holder, int position, @NonNull Friends model) {

                holder.setDate(model.getDate());
                final String usersIDs = getRef(position).getKey();
                usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            final String userFullName = dataSnapshot.child("fullName").getValue().toString();
                            final String profileImage = dataSnapshot.child("profileImage").getValue().toString();
                            final String type ;

                            if(dataSnapshot.hasChild("userSate")){
                                type = dataSnapshot.child("userSate").child("type").getValue().toString();
                                if(type.equals("online")){
                                    holder.onlineStatusView.setVisibility(View.VISIBLE);
                                }else{
                                    holder.onlineStatusView.setVisibility(View.INVISIBLE);
                                }
                            }

                            holder.setFullName(userFullName);
                            holder.setProfileImage(getApplicationContext(), profileImage);

                            holder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CharSequence options[] = new CharSequence[]
                                            {
                                                    userFullName+"'s Profile",
                                                    "Send Message"
                                            };
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                    builder.setTitle("Select Options");
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(which==0){
                                                Intent profileIntent = new Intent(FriendsActivity.this, PersonProfileActivity.class);
                                                profileIntent.putExtra("visitUserId", usersIDs);
                                                startActivity(profileIntent);
                                            }
                                            if(which==1){
                                                Intent chatIntent = new Intent(FriendsActivity.this, ChatActivity.class);
                                                chatIntent.putExtra("visitUserId", usersIDs);
                                                chatIntent.putExtra("userName",userFullName);
                                                startActivity(chatIntent);
                                            }
                                        }
                                    });
                                    builder.show();
                                }
                            });



                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @NonNull
            @Override
            public friendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_user_display_layout, parent,false);
                FriendsActivity.friendsViewHolder viewHolder = new FriendsActivity.friendsViewHolder(view);
                return viewHolder;
            }
        };
        myFriendList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class friendsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        ImageView onlineStatusView;

        public friendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            onlineStatusView = itemView.findViewById(R.id.all_user_online_icon);
        }
        public void setProfileImage (Context ctx, String profileImage){
            CircleImageView myImage = (CircleImageView) mView.findViewById(R.id.all_user_profile_image);
            Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(myImage);
        }
        public void setFullName(String fullName){
            TextView myName = (TextView) mView.findViewById(R.id.all_users_profile_full_name);
            myName.setText(fullName);
        }
        public void setDate(String date){
            TextView friendsDate = (TextView) mView.findViewById(R.id.all_users_status);
            friendsDate.setText("Friends Science: "+date);
        }
    }
}

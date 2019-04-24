package com.example.rabby.socialnetworkingapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView userNameTV, userFullnameTV, userStatusTV, userCountryTV, userGenderTV, userRelationShipStatusTV, userDOBTV;
    private CircleImageView userProfileImage;
    private DatabaseReference profileUserRef, friendsRef, postsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private Button myPosts, myFriends;
    private int countFriends = 0, countPosts = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        userProfileImage = findViewById(R.id.my_profile_pic);

        userNameTV = findViewById(R.id.my_username);
        userFullnameTV = findViewById(R.id.my_profile_full_name);
        userStatusTV = findViewById(R.id.my_profile_status);
        userCountryTV = findViewById(R.id.my_country);
        userGenderTV = findViewById(R.id.my_gender);
        userRelationShipStatusTV = findViewById(R.id.my_relationship_status);
        userDOBTV = findViewById(R.id.my_dob);
        myFriends = findViewById(R.id.my_friends_button);
        myPosts = findViewById(R.id.my_post_button);

        myFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToFriendsActivity();
            }
        });
        myPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToMyPostActivity();
            }
        });

        postsRef.orderByChild("uid").startAt(currentUserID).endAt(currentUserID + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            countPosts = (int) dataSnapshot.getChildrenCount();
                            myPosts.setText(Integer.toString(countPosts)+" Posts");
                        }else{
                            myPosts.setText("0 Post");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        friendsRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    countFriends = (int) dataSnapshot.getChildrenCount();
                    myFriends.setText(Integer.toString(countFriends)+" Friends");
                }else {
                    myFriends.setText("0 Friend");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String myProfileImage = dataSnapshot.child("profileImage").getValue().toString();
                    String myUsername = dataSnapshot.child("userName").getValue().toString();
                    String myFullname = dataSnapshot.child("fullName").getValue().toString();
                    String myDOB = dataSnapshot.child("dob").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myRelationShipStatus = dataSnapshot.child("relationShipStatus").getValue().toString();
                    String myStatus = dataSnapshot.child("status").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);
                    userNameTV.setText("Username : "+myUsername);
                    userFullnameTV.setText(myFullname);
                    userStatusTV.setText(myStatus);
                    userCountryTV.setText("Country : "+myCountry);
                    userGenderTV.setText("Gender : "+myGender);
                    userRelationShipStatusTV.setText("Relationship Status : "+myRelationShipStatus);
                    userDOBTV.setText("Date of Birth : "+ myDOB);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void sendUserToFriendsActivity() {
        Intent friendsActivityIntent = new Intent(ProfileActivity.this, FriendsActivity.class);
        startActivity(friendsActivityIntent);
    }

    private void sendUserToMyPostActivity() {
        Intent friendsActivityIntent = new Intent(ProfileActivity.this, MyPostsActivity.class);
        startActivity(friendsActivityIntent);
    }
}

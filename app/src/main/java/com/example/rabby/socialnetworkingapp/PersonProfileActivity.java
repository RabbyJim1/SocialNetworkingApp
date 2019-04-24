package com.example.rabby.socialnetworkingapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView userNameTV, userFullnameTV, userStatusTV, userCountryTV, userGenderTV, userRelationShipStatusTV, userDOBTV;
    private CircleImageView userProfileImage;
    private Button sendFriendRequestB, declineFriendRequestB;
    private FirebaseAuth mAuth;
    private DatabaseReference friendRequestRef, userRef, friendsRef;
    private String senderUserID, receverUserID;
    private String current_state, saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        receverUserID = getIntent().getExtras().get("visitUserId").toString();

        mAuth = FirebaseAuth.getInstance();
        senderUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(receverUserID);
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequest");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        initializedField();

        userRef.addValueEventListener(new ValueEventListener() {
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

                    maintainceOfButtons();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        declineFriendRequestB.setVisibility(View.INVISIBLE);
        declineFriendRequestB.setEnabled(false);

        if(!senderUserID.equals(receverUserID)){
            sendFriendRequestB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendFriendRequestB.setEnabled(false);

                    if(current_state.equals("not_friends")){
                        sendFriendRequestToThePerson();

                    }
                    if(current_state.equals("request_sent")){
                        cancelFriendRequestToThePerson();
                    }
                    if(current_state.equals("request_received")){
                        acceptFriendRequest();
                    }
                    if(current_state.equals("friends")){
                        unfriendsAnExistingFriends();
                    }
                }
            });
        }else{
            declineFriendRequestB.setVisibility(View.INVISIBLE);
            sendFriendRequestB.setVisibility(View.INVISIBLE);
        }

    }

    private void unfriendsAnExistingFriends() {

        friendsRef.child(senderUserID).child(receverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendsRef.child(receverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            sendFriendRequestB.setEnabled(true);
                                            current_state = "not_friends";
                                            sendFriendRequestB.setText("Send Friend Request");

                                            declineFriendRequestB.setVisibility(View.INVISIBLE);
                                            declineFriendRequestB.setEnabled(false);
                                        }
                                    });
                        }
                    }
                });

    }

    private void acceptFriendRequest() {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        friendsRef.child(senderUserID).child(receverUserID).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendsRef.child(receverUserID).child(senderUserID).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                friendRequestRef.child(senderUserID).child(receverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    friendRequestRef.child(receverUserID).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    sendFriendRequestB.setEnabled(true);
                                                                                    current_state = "friends";
                                                                                    sendFriendRequestB.setText("Unfriend");

                                                                                    declineFriendRequestB.setVisibility(View.INVISIBLE);
                                                                                    declineFriendRequestB.setEnabled(false);
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });

                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelFriendRequestToThePerson() {

        friendRequestRef.child(senderUserID).child(receverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendRequestRef.child(receverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            sendFriendRequestB.setEnabled(true);
                                            current_state = "not_friends";
                                            sendFriendRequestB.setText("Send Friend Request");

                                            declineFriendRequestB.setVisibility(View.INVISIBLE);
                                            declineFriendRequestB.setEnabled(false);
                                        }
                                    });
                        }
                    }
                });
    }

    private void maintainceOfButtons() {
        friendRequestRef.child(senderUserID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(receverUserID)){
                            String request_type = dataSnapshot.child(receverUserID).child("request_type").getValue().toString();
                            if(request_type.equals("sent")){
                                current_state = "request_sent";
                                sendFriendRequestB.setText("Cancel Friend Request");

                                declineFriendRequestB.setVisibility(View.INVISIBLE);
                                declineFriendRequestB.setEnabled(false);
                            }
                            else if(request_type.equals("received")){
                                current_state = "request_received";
                                sendFriendRequestB.setText("Accept Friend Request");
                                declineFriendRequestB.setVisibility(View.VISIBLE);
                                declineFriendRequestB.setEnabled(true);

                                declineFriendRequestB.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        cancelFriendRequestToThePerson();
                                    }
                                });
                            }
                        }
                        else{
                            friendsRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(receverUserID)){
                                                current_state = "friends";
                                                sendFriendRequestB.setText("Unfriend");

                                                declineFriendRequestB.setVisibility(View.INVISIBLE);
                                                declineFriendRequestB.setEnabled(false);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void sendFriendRequestToThePerson() {
        friendRequestRef.child(senderUserID).child(receverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendRequestRef.child(receverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            sendFriendRequestB.setEnabled(true);
                                            current_state = "request_sent";
                                            sendFriendRequestB.setText("Cancel Friend Request");

                                            declineFriendRequestB.setVisibility(View.INVISIBLE);
                                            declineFriendRequestB.setEnabled(false);
                                        }
                                    });
                        }
                    }
                });



    }

    private void initializedField() {
        userNameTV = findViewById(R.id.person_username);
        userFullnameTV = findViewById(R.id.person_profile_full_name);
        userStatusTV = findViewById(R.id.person_profile_status);
        userCountryTV = findViewById(R.id.person_country);
        userGenderTV = findViewById(R.id.person_gender);
        userRelationShipStatusTV = findViewById(R.id.person_relationship_status);
        userDOBTV = findViewById(R.id.person_dob);

        userProfileImage = findViewById(R.id.person_profile_pic);
        sendFriendRequestB = findViewById(R.id.person_send_friend_request_btn);
        declineFriendRequestB = findViewById(R.id.person_decline_friend_request_btn);

        current_state = "not_friends";
    }
}

package com.example.rabby.socialnetworkingapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private CircleImageView nevProfileImage;
    private TextView nevProfileUserName;
    private ImageButton addNewPostButton;

    private RecyclerView postList;
    private Toolbar mToolbar;

    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
    private ImageButton AddNewPostButton;
    private DatabaseReference userRef, postRef, likesRef;



    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef;
    private boolean likeChecker = false;

    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        currentUserID = mAuth.getCurrentUser().getUid();

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");
        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open, R.string.drawer_close );
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = findViewById(R.id.navigation_view);
        postList = findViewById(R.id.all_user_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        nevProfileImage = navView.findViewById(R.id.nav_profile_image);
        nevProfileUserName = navView.findViewById(R.id.nav_user_full_name);
        addNewPostButton = findViewById(R.id.add_new_post_button);





        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelector(item);
                return false;
            }
        });

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef = FirebaseDatabase.getInstance().getReference().child("likes");

        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){


                    if(dataSnapshot.hasChild("profileImage"))
                    {
                        String image = dataSnapshot.child("profileImage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(nevProfileImage);
                    }
                    if(dataSnapshot.hasChild("fullName"))
                    {
                        String fullName = dataSnapshot.child("fullName").getValue().toString();
                        nevProfileUserName.setText(fullName);
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "Profile name do not exists...", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        addNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                SendUserToPostActivity();
            }


        });


        dispalayAllUserPost();


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

        userRef.child(currentUserID).child("userSate")
                .updateChildren(currentStateMap);

    }



    private void dispalayAllUserPost() {
        Query sortPostInDecendingOrder = postRef.orderByChild("counter");
        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(sortPostInDecendingOrder, Posts.class).build();

        FirebaseRecyclerAdapter<Posts, PostViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostViewHolder>(options)
                {
                    @Override
                    protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull Posts model) {

                        final String postKey = getRef(position).getKey();
                        holder.setFullName(model.getFullName());
                        holder.setTime(model.getTime());
                        holder.setDate(model.getDate());
                        holder.setDescription(model.getDescription());
                        holder.setProfileImage(getApplicationContext(), model.getProfileImage());
                        holder.setPostimage(getApplicationContext(), model.getPostimage());

                        holder.setLikeButtonStatus(postKey);

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickPostIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                                clickPostIntent.putExtra("postKey",postKey);
                                startActivity(clickPostIntent);
                            }
                        });

                        holder.commentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickCommentIntent = new Intent(MainActivity.this, CommentsActivity.class);
                                clickCommentIntent.putExtra("postKey",postKey);
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
                                            if(dataSnapshot.child(postKey).hasChild(currentUserID)){
                                                likesRef.child(postKey).child(currentUserID).removeValue();
                                                likeChecker = false;
                                            }else{
                                                likesRef.child(postKey).child(currentUserID).setValue(true);
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
                    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent,false);
                        PostViewHolder viewHolder = new PostViewHolder(view);
                        return viewHolder;
                    }
                };
        postList.setAdapter(firebaseRecyclerAdapter);
        updateUserStatus("online");
        firebaseRecyclerAdapter.startListening();


    }

    public static class PostViewHolder extends RecyclerView.ViewHolder{

        View mView;
        ImageButton likePostButton, commentPostButton;
        TextView displayNoOfLikeTV, commentTV;
        int countLikes=0;
        String currentUserID;
        DatabaseReference likeRef;


        public PostViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            likePostButton = (ImageButton) mView.findViewById(R.id.like_button);
            commentPostButton = (ImageButton) mView.findViewById(R.id.comment_button);
            displayNoOfLikeTV = (TextView) mView.findViewById(R.id.display_no_of_like);
            commentTV = (TextView) mView.findViewById(R.id.comment_text);
            likeRef = FirebaseDatabase.getInstance().getReference().child("likes");
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();


        }

        public void setLikeButtonStatus(final String postKey){
            likeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(postKey).hasChild(currentUserID)){
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

    private void SendUserToPostActivity() {
        Intent addNewPostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);
    }


    @Override
    protected void onStart() {

        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            sendUserToLoginActivity();
        }else{
            checkUserExistance();
        }
    }

    private void checkUserExistance() {
        final String current_User_Id = mAuth.getCurrentUser().getUid();

        userRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(current_User_Id)){
                    sendUserToSetupActivity();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendUserToSetupActivity() {
        Intent setupActivityIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupActivityIntent);
        finish();
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item) {
        switch(item.getItemId()){
            case R.id.nav_add_new_post:
                //Toast.makeText(this, "Add New Post", Toast.LENGTH_SHORT).show();
                SendUserToPostActivity();


                break;
            case R.id.nav_profile:
                sendUserToProfileActivity();
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_friends:
                sendUserToFriendsActivity();
                break;
            case R.id.nav_find_friends:
                sendUserToFindFriendsActivity();
                Toast.makeText(this, "Find Friends List", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_messages:
                Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_settings:
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                sendUserToSettingsActivity();
                break;
            case R.id.nav_logout:

                updateUserStatus("offline");
                mAuth.signOut();
                sendUserToLoginActivity();
                break;
        }
    }

    private void sendUserToFriendsActivity() {
        Intent friendsActivityIntent = new Intent(MainActivity.this, FriendsActivity.class);
        startActivity(friendsActivityIntent);
    }

    private void sendUserToSettingsActivity() {
        Intent settingsActivityIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsActivityIntent);
    }

    private void sendUserToProfileActivity() {
        Intent profileActivityIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(profileActivityIntent);
    }
    private void sendUserToFindFriendsActivity() {
        Intent FindFriendsActivityIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(FindFriendsActivityIntent);
    }
}
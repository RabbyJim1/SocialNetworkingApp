package com.example.rabby.socialnetworkingapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar mToolbar;

    private SearchView searchFirendsSV;
    private RecyclerView searchReasultList;
    private DatabaseReference allUserDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        allUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mToolbar = findViewById(R.id.find_friends_appbar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Friends");
        searchFirendsSV = findViewById(R.id.search_box_input);
        searchReasultList = findViewById(R.id.search_result_list);
        searchReasultList.setHasFixedSize(true);
        searchReasultList.setLayoutManager(new LinearLayoutManager(this));

        searchFirendsSV.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                searchPeopleAndFriends(newText);
                return false;
            }
        });
    }

    private void searchPeopleAndFriends(String newText) {

        Query searchPeopleAndFriendsQuery = allUserDatabaseRef.orderByChild("fullName")
                .startAt(newText).endAt(newText+"\uf8ff");

        FirebaseRecyclerOptions<FindFriends> options =
                new FirebaseRecyclerOptions.Builder<FindFriends>()
                        .setQuery(searchPeopleAndFriendsQuery, FindFriends.class).build();

        FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull FindFriends model) {
                holder.setFullName(model.getFullName());
                holder.setStatus(model.getStatus());
                holder.setProfileImage(getApplicationContext(), model.getProfileImage());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visitUserId = getRef(position).getKey();
                        Intent profileIntent = new Intent(FindFriendsActivity.this, PersonProfileActivity.class);
                        profileIntent.putExtra("visitUserId",visitUserId);
                        startActivity(profileIntent);
                    }
                });



            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_user_display_layout, parent,false);
                FindFriendsActivity.FindFriendsViewHolder viewHolder = new FindFriendsActivity.FindFriendsViewHolder(view);
                return viewHolder;
            }
        };
        searchReasultList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public FindFriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setProfileImage (Context ctx, String profileImage){
            CircleImageView myImage = (CircleImageView) mView.findViewById(R.id.all_user_profile_image);
            Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(myImage);
        }
        public void setFullName(String fullName){
            TextView myName = (TextView) mView.findViewById(R.id.all_users_profile_full_name);
            myName.setText(fullName);
        }
        public void setStatus(String status){
            TextView myStatus = (TextView) mView.findViewById(R.id.all_users_status);
            myStatus.setText(status);
        }

    }
}

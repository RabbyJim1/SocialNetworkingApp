package com.example.rabby.socialnetworkingapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {

    private ImageView postImageIV;
    private TextView postDescriptionTV;
    private Button deletePostB, editPostB;
    private DatabaseReference clickPostRef;
    private FirebaseAuth mAuth;

    private String postKey, currentUserID, databaseUserID, description, image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        postKey = getIntent().getExtras().get("postKey").toString();
        clickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);

        postImageIV = findViewById(R.id.click_post_image);
        postDescriptionTV = findViewById(R.id.click_post_description);
        deletePostB = findViewById(R.id.delete_post_button);
        editPostB = findViewById(R.id.edit_post_button);

        deletePostB.setVisibility(View.INVISIBLE);
        editPostB.setVisibility(View.INVISIBLE);

        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    description = dataSnapshot.child("description").getValue().toString();
                    image = dataSnapshot.child("postimage").getValue().toString();
                    databaseUserID = dataSnapshot.child("uid").getValue().toString();
                    postDescriptionTV.setText(description);
                    Picasso.get().load(image).placeholder(R.drawable.select_image).into(postImageIV);

                    if(currentUserID.equals(databaseUserID)){
                        deletePostB.setVisibility(View.VISIBLE);
                        editPostB.setVisibility(View.VISIBLE);

                    }

                    editPostB.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditCurrentPost(description);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        deletePostB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCurrentPost();
            }
        });
    }

    private void EditCurrentPost(String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post:");
        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(description);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clickPostRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "Post Updated Sccessfully!", Toast.LENGTH_SHORT).show();
                sendUserToMainActivity();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();

    }

    private void deleteCurrentPost() {
        clickPostRef.removeValue();
        sendUserToMainActivity();
        Toast.makeText(this, "Post has been deleted.", Toast.LENGTH_SHORT).show();
    }
    private void sendUserToMainActivity() {
        Intent mainActivityIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
        finish();
    }
}

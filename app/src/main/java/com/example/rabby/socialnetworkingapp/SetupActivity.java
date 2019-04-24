package com.example.rabby.socialnetworkingapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v7.widget.*;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText username, fullNameET, countryNameET;
    private Button saveInformationButton;
    private CircleImageView profileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference usersReference;
    final private String TAG = "Image Link: ";

    String ciurrentUserID; //profilePicture="none"
    private ProgressDialog progressDialog;
    private StorageReference userProfileImageRef;
    final static int gallaryPic = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        username = (EditText) findViewById(R.id.setup_username);
        fullNameET = (EditText) findViewById(R.id.setup_fullName);
        countryNameET = (EditText) findViewById(R.id.setup_country_name);
        saveInformationButton = findViewById(R.id.save);
        profileImage = (CircleImageView) findViewById(R.id.setup_profile_image);
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        ciurrentUserID = mAuth.getCurrentUser().getUid();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(ciurrentUserID);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images Folder");
        saveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAccountSetupInformation();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallaryIntent = new Intent();
                gallaryIntent.setAction(Intent.ACTION_GET_CONTENT);
                gallaryIntent.setType("image/*");
                startActivityForResult(gallaryIntent, gallaryPic);
            }
        });

        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String image = dataSnapshot.child("profileImage").getValue().toString();
                    Log.e(TAG, "onDataChange: "+image);
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(profileImage);
                }
                else{
                    Toast.makeText(SetupActivity.this, "Please, select profile image first...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == gallaryPic && resultCode == RESULT_OK && data != null){
            Uri ImageUri = data.getData();

            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }


        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK){
                progressDialog.setTitle("Profile Image");
                progressDialog.setMessage("Please wait, For updating profile image...");
                progressDialog.setCanceledOnTouchOutside(true);
                progressDialog.show();

                Uri resultUri = result.getUri();
                StorageReference filePath = userProfileImageRef.child(ciurrentUserID+".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){

                            userProfileImageRef.child(ciurrentUserID+".jpg").getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            final String downloadUrl = uri.toString();
                                            Toast.makeText(SetupActivity.this, downloadUrl, Toast.LENGTH_LONG).show();
                                            usersReference.child("profileImage").setValue(downloadUrl)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                progressDialog.dismiss();
                                                            }else{
                                                                String massage = task.getException().getMessage();
                                                                Toast.makeText(SetupActivity.this, "Error: "+massage, Toast.LENGTH_SHORT).show();
                                                                progressDialog.dismiss();
                                                            }
                                                        }
                                                    });
                                        }
                                    });


                        }
                    }
                });
            }

            else{
                Toast.makeText(this, "Error: Image can't be cropped!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }
    }

    private void saveAccountSetupInformation() {
        String userName = username.getText().toString();
        String fullName = fullNameET.getText().toString();
        String countryName = countryNameET.getText().toString();

        if(TextUtils.isEmpty(userName)){
            Toast.makeText(this, "Enter your UserName...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(fullName)){
            Toast.makeText(this, "Enter your fullName...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(countryName)){
            Toast.makeText(this, "Enter your country name...", Toast.LENGTH_SHORT).show();
        }else{

            progressDialog.setTitle("Saving Information");
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);
            HashMap userMap = new HashMap();
            userMap.put("userName",userName);
            userMap.put("fullName",fullName);
            userMap.put("country",countryName);
            userMap.put("status","none");
            userMap.put("gender","none");
            userMap.put("dob","none");
            userMap.put("relationShipStatus","none");
           // userMap.put("profile image",profilePicture);
            usersReference.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(SetupActivity.this, "Account is created successfully!", Toast.LENGTH_LONG).show();
                        sendUserToMainActivity();
                        progressDialog.dismiss();
                    }else{
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }

    private void sendUserToMainActivity() {
        Intent mainActivityIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
        finish();
    }
}

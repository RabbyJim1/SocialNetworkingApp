package com.example.rabby.socialnetworkingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText userNameET, userFullnameET, userStatusET, userCountryET, userGenderET, userRelationShipStatusET, userDOBET;
    private Button updateAccountB;
    private CircleImageView userProfImagCIV;
    private DatabaseReference settingsUserRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    final static int gallaryPic = 1;
    private ProgressDialog progressDialog;
    private StorageReference userProfileImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        settingsUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        progressDialog = new ProgressDialog(this);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images Folder");

        mToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        userNameET = findViewById(R.id.settings_username);
        userFullnameET = findViewById(R.id.settings_profile_fullname);
        userStatusET = findViewById(R.id.settings_status);
        userCountryET = findViewById(R.id.settings_country);
        userGenderET = findViewById(R.id.settings_gender);
        userRelationShipStatusET = findViewById(R.id.settings_relationship_status);
        userDOBET = findViewById(R.id.settings_dob);
        updateAccountB = findViewById(R.id.update_account_settings_button);
        userProfImagCIV = findViewById(R.id.settings_profile_image);

        settingsUserRef.addValueEventListener(new ValueEventListener() {
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

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfImagCIV);
                    userNameET.setText(myUsername);
                    userFullnameET.setText(myFullname);
                    userStatusET.setText(myStatus);
                    userCountryET.setText(myCountry);
                    userGenderET.setText(myGender);
                    userRelationShipStatusET.setText(myRelationShipStatus);
                    userDOBET.setText(myDOB);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        updateAccountB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAccouuntInfo();
            }
        });
        userProfImagCIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallaryIntent = new Intent();
                gallaryIntent.setAction(Intent.ACTION_GET_CONTENT);
                gallaryIntent.setType("image/*");
                startActivityForResult(gallaryIntent, gallaryPic);
            }
        });
    }

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
                StorageReference filePath = userProfileImageRef.child(currentUserID+".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){

                            userProfileImageRef.child(currentUserID+".jpg").getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            final String downloadUrl = uri.toString();
                                            Toast.makeText(SettingsActivity.this, downloadUrl, Toast.LENGTH_LONG).show();
                                            settingsUserRef.child("profileImage").setValue(downloadUrl)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                progressDialog.dismiss();
                                                            }else{
                                                                String massage = task.getException().getMessage();
                                                                Toast.makeText(SettingsActivity.this, "Error: "+massage, Toast.LENGTH_SHORT).show();
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

    private void validateAccouuntInfo() {


        String userName = userNameET.getText().toString();
        String fullName = userFullnameET.getText().toString();
        String status = userStatusET.getText().toString();
        String country = userCountryET.getText().toString();
        String gender = userGenderET.getText().toString();
        String relationshipStatus = userRelationShipStatusET.getText().toString();
        String dob = userDOBET.getText().toString();

        if(TextUtils.isEmpty(userName)){
            Toast.makeText(this, "Please, write your username...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(fullName)){
            Toast.makeText(this, "Please, write your FullName...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(status)){
            Toast.makeText(this, "Please, write your status...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(country)){
            Toast.makeText(this, "Please, write your country name...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(gender)){
            Toast.makeText(this, "Please, write your gender...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(relationshipStatus)){
            Toast.makeText(this, "Please, write your relationship status...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(dob)){
            Toast.makeText(this, "Please, write your date of birth...", Toast.LENGTH_SHORT).show();
        }
        else{
            UpdateAccountInfo(userName,fullName,status,country,gender,relationshipStatus,dob);
        }
    }

    private void UpdateAccountInfo(String userName, String fullName, String status, String country, String gender, String relationshipStatus, String dob) {

        HashMap userMap = new HashMap();

        userMap.put("userName",userName);
        userMap.put("fullName",fullName);
        userMap.put("status",status);
        userMap.put("country",country);
        userMap.put("gender",gender);
        userMap.put("relationShipStatus",relationshipStatus);
        userMap.put("dob",dob);

        settingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    sendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this, "Accounts settings successfully...", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
                else {
                    String message = task.getException().getMessage();
                    Toast.makeText(SettingsActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });

    }

    private void sendUserToMainActivity() {
        Intent mainActivityIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
        finish();
    }
}

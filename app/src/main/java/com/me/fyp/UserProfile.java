package com.me.fyp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.me.fyp.databinding.ActivityUserProfileBinding;
import com.squareup.picasso.Picasso;

public class UserProfile extends BaseActivity {
    ActivityUserProfileBinding activityUserProfileBinding;
    private TextView showHello, showUsername, showEmail, showDoB, showGender, showPhone, showCountry;
    private String username, email, DOB, gender, phone, country;
    private ProgressDialog progressDialog;
    private FirebaseAuth authProfile;
    private ImageView genderView;
    private ImageView imageView;
    private Button update_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityUserProfileBinding =ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(activityUserProfileBinding.getRoot());
        allocatedActivityTitle("User Profile");

        update_button = findViewById(R.id.btn_update_profile);

        showHello = findViewById(R.id.show_hello);
        showUsername = findViewById(R.id.show_username);
        showEmail = findViewById(R.id.show_email);
        showDoB = findViewById(R.id.show_dob);
        showGender = findViewById(R.id.show_gender);
        showPhone = findViewById(R.id.show_phone);
        showCountry = findViewById(R.id.show_country);

        progressDialog = new ProgressDialog(UserProfile.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        imageView = findViewById(R.id.profile);

        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfile.this, UpdateProfile.class);
                startActivity(intent);
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfile.this, UploadProfile.class);
                startActivity(intent);
            }
        });

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if(firebaseUser == null){
            Toast.makeText(UserProfile.this, "Something went wrong! User's details are not available at the moment"
                    , Toast.LENGTH_LONG).show();
        } else{
            showUserProfile(firebaseUser);
            checkIfEmailVerified(firebaseUser);
            progressDialog.show();

        }

    }

    private void checkIfEmailVerified(FirebaseUser firebaseUser) {
        if(!firebaseUser.isEmailVerified()){
            showAlertDialog();
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfile.this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please verify your email now. You cannot login without email verification next time");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().signOut();
                //TO_DO: match with logout activity
//                Intent intent = new Intent(UserProfile.this, LoginActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showUserProfile(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();

        DocumentReference referenceProfile = FirebaseFirestore.getInstance().collection("Registered Users").document(userID);
        referenceProfile.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ReadWriteUserDetails readWriteUserDetails = document.toObject(ReadWriteUserDetails.class);
                        if (readWriteUserDetails != null) {
                            username = firebaseUser.getDisplayName();
                            email = firebaseUser.getEmail();
                            DOB = readWriteUserDetails.getDOB();
                            gender = readWriteUserDetails.getGender();
                            phone = readWriteUserDetails.getMobile();
                            country = readWriteUserDetails.getCountry();

                            showHello.setText("Hello, " + username + "!");
                            showUsername.setText(username);
                            showEmail.setText(email);
                            showDoB.setText(DOB);
                            showGender.setText(gender);
                            showPhone.setText(phone);
                            showCountry.setText(country);

                            //Load the image from the retrieved URL into the ImageView
                            String imageUrl = readWriteUserDetails.getProfile();
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Picasso.get().load(imageUrl).into(imageView);
                            } else {
                                // Set default profile picture here
                                imageView.setImageResource(R.drawable.profile);
                            }
                        } else {
                            Toast.makeText(UserProfile.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(UserProfile.this, "No such document exists!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(UserProfile.this, "Task failed with " + task.getException(), Toast.LENGTH_LONG).show();
                }
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UserProfile.this, "Error fetching document!", Toast.LENGTH_LONG).show();
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        }) ;

    }
}
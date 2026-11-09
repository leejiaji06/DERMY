package com.me.fyp;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.me.fyp.databinding.ActivityUpdateProfileBinding;

import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProfile extends BaseActivity {

    ActivityUpdateProfileBinding activityUpdateProfileBinding;
    private EditText editTextUpdateName, editTextUpdateDOB, editTextUpdateMobile, editTextUpdateCountry, editTextUpdateState;
    private RadioGroup radioGroupUpdateGender;
    private RadioButton radioButtonUpdateGenderSelected;
    private String textUsername, textDOB, textGender, textMobile, textCountry, textState;
    private FirebaseAuth authProfile;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityUpdateProfileBinding = ActivityUpdateProfileBinding.inflate(getLayoutInflater());
        setContentView(activityUpdateProfileBinding.getRoot());
        allocatedActivityTitle("Update User Profile");

        progressDialog = new ProgressDialog(UpdateProfile.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        editTextUpdateName = findViewById(R.id.editText_update_profile_name);
        editTextUpdateDOB = findViewById(R.id.editText_update_profile_dob);
        editTextUpdateMobile = findViewById(R.id.editText_update_profile_mobile);
        editTextUpdateCountry = findViewById(R.id.editText_update_profile_country);
        editTextUpdateState = findViewById(R.id.editText_update_profile_state);

        radioGroupUpdateGender = findViewById(R.id.radio_group_update_gender);
        authProfile= FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        showProfile(firebaseUser);

        Button buttonUploadProfilePic = findViewById(R.id.button_upload_profile_pic);
        Button buttonChangePassword = findViewById(R.id.button_change_password);
        buttonUploadProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateProfile.this, UploadProfile.class);
                startActivity(intent);
                finish();
            }
        });

        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateProfile.this, ChangePassword.class);
                startActivity(intent);
                finish();
            }
        });

        editTextUpdateDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                final Calendar c = Calendar.getInstance();
//
//                String textSADOB[] = textDOB.split("/");
//
//                int day = Integer.parseInt(textSADOB[0]);
//                int month = Integer.parseInt(textSADOB[1]) - 1;
//                int year = Integer.parseInt(textSADOB[2]);
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                DatePickerDialog picker =  new DatePickerDialog(
                        UpdateProfile.this,
                        new DatePickerDialog.OnDateSetListener(){
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth){
                        // Update the EditText with the selected date
                        editTextUpdateDOB.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year));
                    }
                }, year, month, day);
                picker.show();
            }
        });

        Button buttonUpdateProfile = findViewById(R.id.button_update_profile);
        buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(firebaseUser);
            }
        });
    }

    private void updateProfile(FirebaseUser firebaseUser) {
        int selectedGenderID = radioGroupUpdateGender.getCheckedRadioButtonId();
        radioButtonUpdateGenderSelected = findViewById(selectedGenderID);

//        String phoneRegex = "^(01)[0-9]-?[0-9]{7,8}$";
//        Matcher phoneMatcher;
//        Pattern phonePattern = Pattern.compile(phoneRegex);
//        phoneMatcher = phonePattern.matcher(textMobile);

        if (TextUtils.isEmpty(textUsername)) {
            Toast.makeText(UpdateProfile.this, "Please enter your username", Toast.LENGTH_LONG).show();
            editTextUpdateName.setError("Username is required");
            editTextUpdateName.requestFocus();

        }else if(TextUtils.isEmpty(textDOB)){
            Toast.makeText(UpdateProfile.this, "Please enter your date of birth", Toast.LENGTH_LONG).show();
            editTextUpdateDOB.setError("Date of Birth is required");
            editTextUpdateDOB.requestFocus();
        }else if(TextUtils.isEmpty(textCountry)){
            Toast.makeText(UpdateProfile.this, "Please enter your country", Toast.LENGTH_LONG).show();
            editTextUpdateCountry.setError("Country is required");
            editTextUpdateCountry.requestFocus();
        }else if(TextUtils.isEmpty(radioButtonUpdateGenderSelected.getText())){
            Toast.makeText(UpdateProfile.this, "Please select your gender", Toast.LENGTH_LONG).show();
            radioButtonUpdateGenderSelected.setError("Gender is required");
            radioButtonUpdateGenderSelected.requestFocus();
        }else if(TextUtils.isEmpty(textMobile)){
            Toast.makeText(UpdateProfile.this, "Please enter your phone number", Toast.LENGTH_LONG).show();
            editTextUpdateMobile.setError("Phone number is required");
            editTextUpdateMobile.requestFocus();
//        }else if(!phoneMatcher.find()){
//            Toast.makeText(UpdateProfile.this, "Please enter your valid phone number in Malaysia", Toast.LENGTH_LONG).show();
//            editTextUpdateMobile.setError("Phone no. is not valid");
//            editTextUpdateMobile.requestFocus();
//        }else if(textMobile.length() != 10 && textMobile.length() != 11){
//            Toast.makeText(UpdateProfile.this, "Please re-enter your phone number", Toast.LENGTH_LONG).show();
//            editTextUpdateMobile.setError("Phone number should be 10 or 11 digits");
//            editTextUpdateMobile.requestFocus();
        }else{
            String email = firebaseUser.getEmail();
            textGender = radioButtonUpdateGenderSelected.getText().toString();
            textUsername = editTextUpdateName.getText().toString();
            textDOB = editTextUpdateDOB.getText().toString();
            textMobile = editTextUpdateMobile.getText().toString();
            textCountry = editTextUpdateCountry.getText().toString();
            textState = editTextUpdateState.getText().toString();

            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(email, textUsername, textDOB, textGender, textMobile,textCountry,null);
            String userID = firebaseUser.getUid();
            DocumentReference referenceProfile = FirebaseFirestore.getInstance().collection("Registered Users").document(userID);
            progressDialog.show();


            referenceProfile.set(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(textUsername).build();
                        firebaseUser.updateProfile(profileUpdates);

                        Toast.makeText(UpdateProfile.this, "Update Successfully!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(UpdateProfile.this, UserProfile.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            Toast.makeText(UpdateProfile.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UpdateProfile.this, "Error updating profile!", Toast.LENGTH_LONG).show();

                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }
private void showProfile(FirebaseUser firebaseUser) {
    String userID = firebaseUser.getUid();
    DocumentReference referenceProfile = FirebaseFirestore.getInstance().collection("Registered Users").document(userID);
    progressDialog.show();

    referenceProfile.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    ReadWriteUserDetails readUserDetails = document.toObject(ReadWriteUserDetails.class);
                    if (readUserDetails != null) {
                        textUsername = firebaseUser.getDisplayName();
                        textDOB = readUserDetails.getDOB();
                        textGender = readUserDetails.getGender();
                        textMobile = readUserDetails.getMobile();
                        textCountry = readUserDetails.getCountry();

                        editTextUpdateName.setText(textUsername);
                        editTextUpdateDOB.setText(textDOB);
                        editTextUpdateMobile.setText(textMobile);
                        editTextUpdateCountry.setText(textCountry);

                        if ("Male".equals(textGender)) {
                            radioButtonUpdateGenderSelected = findViewById(R.id.radio_male);
                        } else {
                            radioButtonUpdateGenderSelected = findViewById(R.id.radio_female);
                        }

                        radioButtonUpdateGenderSelected.setChecked(true);
                    } else {
                        Toast.makeText(UpdateProfile.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(UpdateProfile.this, "No such document exists!", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(UpdateProfile.this, "Task failed with " + task.getException(), Toast.LENGTH_LONG).show();
            }

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Toast.makeText(UpdateProfile.this, "Error fetching document!", Toast.LENGTH_LONG).show();

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    });
}

}
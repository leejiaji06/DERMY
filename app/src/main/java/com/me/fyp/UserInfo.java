package com.me.fyp;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.me.fyp.databinding.ActivityUserInfoBinding;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserInfo extends BaseActivity {
    ActivityUserInfoBinding activityUserInfoBinding;
    private TextView textViewState;
    private ProgressDialog progressDialog;
    private EditText editTextDOB, editTextMobile, editTextCountry, editTextState;
    private RadioGroup radioGroupGender;
    private RadioButton radioButtonGenderSelected;
    private String textDOB, textGender, textMobile, textCountry, textState;
    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityUserInfoBinding = ActivityUserInfoBinding.inflate(getLayoutInflater());
        setContentView(activityUserInfoBinding.getRoot());
        allocatedActivityTitle("User Information");

        editTextDOB = findViewById(R.id.editText_DOB);
        editTextMobile = findViewById(R.id.editText_mobile);
        editTextCountry = findViewById(R.id.editText_country);
        editTextState = findViewById(R.id.editText_state);
        editTextCountry = findViewById(R.id.editText_country);
        textViewState = findViewById(R.id.textView_state);

        radioGroupGender = findViewById(R.id.radio_group_gender);
        authProfile= FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();


        editTextDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String textSADoB[] = textDOB.split("/");
//
//                int day = Integer.parseInt(textSADoB[0]);
//                int month = Integer.parseInt(textSADoB[1]) - 1;
//                int year = Integer.parseInt(textSADoB[2]);

                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                DatePickerDialog picker = new DatePickerDialog(UserInfo.this, new DatePickerDialog.OnDateSetListener(){
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth){
                        // Update the EditText with the selected date
                        editTextDOB.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year));
                    }
                }, year, month, day);
                picker.show();
            }
        });

        Button buttonSubmit = findViewById(R.id.button_upload_user_info);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(firebaseUser);
            }
        });
    }

    private void updateProfile(FirebaseUser firebaseUser) {
        progressDialog = new ProgressDialog(UserInfo.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        int selectedGenderID = radioGroupGender.getCheckedRadioButtonId();
        radioButtonGenderSelected = findViewById(selectedGenderID);

        textGender = radioButtonGenderSelected.getText().toString();
        textDOB = editTextDOB.getText().toString();
        textMobile = editTextMobile.getText().toString();

        textState = editTextState.getText().toString();
        textCountry = editTextCountry.getText().toString();

        Pattern pattern = Pattern.compile("^01\\d{8}$");
        Matcher phoneMatcher = pattern.matcher(textMobile);

        if(TextUtils.isEmpty(textDOB)){
            Toast.makeText(UserInfo.this, "Please enter your date of birth", Toast.LENGTH_LONG).show();
            editTextDOB.setError("Date of Birth is required");
            editTextDOB.requestFocus();
        }else if(TextUtils.isEmpty(textCountry)){
            Toast.makeText(UserInfo.this, "Please enter your country", Toast.LENGTH_LONG).show();
            editTextCountry.setError("Country is required");
            editTextCountry.requestFocus();
        }else if(TextUtils.isEmpty(radioButtonGenderSelected.getText())){
            Toast.makeText(UserInfo.this, "Please select your gender", Toast.LENGTH_LONG).show();
            radioButtonGenderSelected.setError("Gender is required");
            radioButtonGenderSelected.requestFocus();
        }else if(TextUtils.isEmpty(textMobile)){
            Toast.makeText(UserInfo.this, "Please enter your phone number", Toast.LENGTH_LONG).show();
            editTextMobile.setError("Phone number is required");
            editTextMobile.requestFocus();
        }else if(!phoneMatcher.find()){
            Toast.makeText(UserInfo.this, "Please enter your valid phone number in Malaysia", Toast.LENGTH_LONG).show();
            editTextMobile.setError("Phone no. is not valid");
            editTextMobile.requestFocus();
        }else if(textMobile.length() != 10 && textMobile.length() != 11){
            Toast.makeText(UserInfo.this, "Please re-enter your phone number", Toast.LENGTH_LONG).show();
            editTextMobile.setError("Phone number should be 10 or 11 digits");
            editTextMobile.requestFocus();
        }else{
            String email = firebaseUser.getEmail();
            String username = firebaseUser.getDisplayName();
            textGender = radioButtonGenderSelected.getText().toString();
            textDOB = editTextDOB.getText().toString();
            textMobile = editTextMobile.getText().toString();

            textState = editTextState.getText().toString();
            textCountry = editTextCountry.getText().toString();

            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(email, username, textDOB, textGender, textMobile, textCountry, textState);

            progressDialog.show();

            // create HashMap to store user data
            HashMap<String,Object> userData = new HashMap<>();
            userData.put("username", username);
            userData.put("email", email);
            userData.put("gender",textGender);
            userData.put("DOB",textDOB);
            userData.put("mobile",textMobile);
            userData.put("country", textCountry);
            userData.put("state", textState);


            // save to Firestore
            FirebaseFirestore.getInstance().collection("Registered Users").document(firebaseUser.getUid())
                    .set(userData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(UserInfo.this,"User data saved successfully.", Toast.LENGTH_SHORT).show();
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        startActivity(new Intent(UserInfo.this, UserProfile.class));
                        finish();

                    })
                    .addOnFailureListener(e -> Toast.makeText(UserInfo.this, "Failed to save user data." + e.getMessage(), Toast.LENGTH_SHORT).show());

        }
    }

}
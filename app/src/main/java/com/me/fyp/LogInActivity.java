package com.me.fyp;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LogInActivity extends AppCompatActivity {
    private FirebaseAuth authProfile;
    private EditText loginEmail, loginPassword;
    private Button loginBtn;
    private static final String TAG ="LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authProfile = FirebaseAuth.getInstance();

        loginEmail = findViewById(R.id.inputEmail);
        loginPassword = findViewById(R.id.inputPassword);
        loginBtn = findViewById(R.id.logIn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = loginEmail.getText().toString();
                String password = loginPassword.getText().toString();

                if(TextUtils.isEmpty(email))
                {
                    Toast.makeText(LogInActivity.this,"Please enter email.", Toast.LENGTH_SHORT).show();
                    loginEmail.setError("Email is required!");
                    loginEmail.requestFocus();
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    Toast.makeText(LogInActivity.this,"Please re-enter your email.", Toast.LENGTH_SHORT).show();
                    loginEmail.setError("Valid email is required!");
                    loginEmail.requestFocus();
                }
                else if(TextUtils.isEmpty(password))
                {
                    Toast.makeText(LogInActivity.this,"Please enter password.", Toast.LENGTH_SHORT).show();
                    loginPassword.setError("Password is required!");
                    loginPassword.requestFocus();
                }
                else
                {
                    loginUser(email,password);
                }
            }
        });
    }

    private void loginUser(String email, String password) {
        authProfile.signInWithEmailAndPassword(email,password).addOnCompleteListener(LogInActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    FirebaseUser firebaseUser = authProfile.getCurrentUser();

                    assert firebaseUser != null;
                    if(firebaseUser.isEmailVerified())
                    {
                        Toast.makeText(LogInActivity.this,"Welcome to DERMY!",Toast.LENGTH_SHORT).show();

                        // check for first time user
                        String uid = firebaseUser.getUid();
                        DocumentReference userRef = FirebaseFirestore.getInstance().collection("Registered Users").document(uid);
                        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.exists()){

                                    String mobile = documentSnapshot.getString("mobile");

                                    if (mobile != null && mobile.isEmpty()) {
                                        // First-time user
                                        startActivity(new Intent(LogInActivity.this, UserInfo.class));
                                        finish();
                                    } else {
                                        // Current user with non-empty mobile field
                                        Toast.makeText(LogInActivity.this, "Current user", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(LogInActivity.this, DashboardActivity.class));
                                        finish();
                                    }
//                                    Toast.makeText(LogInActivity.this,"Current user",Toast.LENGTH_SHORT).show();
//                                    startActivity(new Intent(LogInActivity.this, DashboardActivity.class));
//                                    finish();

                                }else{
                                    startActivity(new Intent(LogInActivity.this, UserInfo.class));
                                    finish();

                                }
                            }
                        });

                    }
                    else
                    {
                        firebaseUser.sendEmailVerification();
                        authProfile.signOut();
                        Toast.makeText(LogInActivity.this,"Please verify your email before login.",Toast.LENGTH_SHORT).show();
                    }

                }
                else {
                    try
                    {
                        throw task.getException();
                    }
                    catch (FirebaseAuthInvalidUserException e)
                    {
                        loginEmail.setError("User does not exists or no longer valid. Please register again.");
                        loginEmail.requestFocus();
                    }
                    catch (FirebaseAuthInvalidCredentialsException e)
                    {
                        loginEmail.setError("Invalid credentials. Kindly check and re-enter.");
                    }
                    catch (Exception e)
                    {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(LogInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void forgotPassword()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(LogInActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot, null);
        EditText emailBox = dialogView.findViewById(R.id.emailBox);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = emailBox.getText().toString();

                if(userEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches())
                {
                    Toast.makeText(LogInActivity.this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    authProfile.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(LogInActivity.this, "Please check your email", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(LogInActivity.this, "Failed to send email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        if(dialog.getWindow() != null){
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        dialog.show();
    }

    public void goSignUp(View view)
    {
        Intent goSignUp = new Intent(this, SignUpActivity.class);
        startActivity(goSignUp);
    }


    @Override
    protected void onStart() {
        super.onStart();

        // Check if the user has just signed up
        boolean justSignedUp = getIntent().getBooleanExtra("justSignedUp", false);

        if(authProfile.getCurrentUser() != null && !justSignedUp) {
            Toast.makeText(this,"Welcome back!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        }
    }

}

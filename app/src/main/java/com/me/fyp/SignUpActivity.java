package com.me.fyp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private EditText signUpUsername, signUpEmail, signUpPassword;
    private Button signUpBtn;
    private static final String TAG= "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signUpUsername = findViewById(R.id.signUpName);
        signUpEmail = findViewById(R.id.registerEmail);
        signUpPassword = findViewById(R.id.registerPassword);
        signUpBtn = findViewById(R.id.signUp);

    }

    public void goLogIn(View view)
    {
        Intent goLogin = new Intent(this, LogInActivity.class);
        startActivity(goLogin);
    }

    public void signUp(View view) {
        String username = signUpUsername.getText().toString();
        String email = signUpEmail.getText().toString().trim();
        String password = signUpPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username))
        {
            Toast.makeText(SignUpActivity.this, "Please enter username.", Toast.LENGTH_LONG).show();
            signUpUsername.setError("Username is required");
            signUpUsername.requestFocus();

        }
        else if (TextUtils.isEmpty(email))
        {
            Toast.makeText(SignUpActivity.this, "Please enter email.", Toast.LENGTH_LONG).show();
            signUpEmail.setError("Email is required");
            signUpEmail.requestFocus();

        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            Toast.makeText(SignUpActivity.this, "Please re-enter your email.", Toast.LENGTH_LONG).show();
            signUpEmail.setError("Valid email is required");
            signUpEmail.requestFocus();
        }
        else if (TextUtils.isEmpty(password))
        {
            Toast.makeText(SignUpActivity.this, "Please enter your password.", Toast.LENGTH_LONG).show();
            signUpPassword.setError("Password is required");
            signUpPassword.requestFocus();
        }
        else if (password.length() < 6)
        {
            Toast.makeText(SignUpActivity.this, "Password should be at least 6 digits", Toast.LENGTH_LONG).show();
            signUpPassword.setError("Password is too weak");
            signUpPassword.requestFocus();
        }
        else
        {
            registerUser(username, email, password);
        }
    }

    private void registerUser(String username, String email, String password) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignUpActivity.this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser firebaseUser = auth.getCurrentUser();

                            if(firebaseUser != null) {
                                UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
                                firebaseUser.updateProfile(profileChangeRequest);

                                ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(email, username, "","","","","");
                                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                                firestore.collection("Registered Users")
                                        .document(firebaseUser.getUid())
                                        .set(writeUserDetails)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful()){
                                                    firebaseUser.sendEmailVerification();
                                                    Toast.makeText(SignUpActivity.this, "User registered successfully. Please verify your email", Toast.LENGTH_LONG).show();

                                                    Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
                                                    intent.putExtra("justSignedUp", true);
                                                    //To prevent user from returning back to register activity on pressing back button after registration
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);

                                                    finish();
                                                }else{
                                                    Toast.makeText(SignUpActivity.this, "User registered failed. Please try again.", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            try {
                                throw task.getException();
                            } catch(FirebaseAuthWeakPasswordException e) {
                                signUpPassword.setError("Your password is too weak! Kindly use a mix of alphabets, numbers and special character.");
                                signUpPassword.requestFocus();
                            } catch(FirebaseAuthInvalidCredentialsException e){
                                signUpEmail.setError("Your email is invalid or already in use, kindly re-enter.");
                                signUpEmail.requestFocus();
                            } catch(FirebaseAuthUserCollisionException e){
                                signUpEmail.setError("User is already registered with this email! Kindly use another email.");
                                signUpEmail.requestFocus();
                            } catch(Exception e){
                                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                                Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }
}
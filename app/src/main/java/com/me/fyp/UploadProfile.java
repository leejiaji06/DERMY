package com.me.fyp;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.me.fyp.databinding.ActivityUploadProfileBinding;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class UploadProfile extends BaseActivity {

    ActivityUploadProfileBinding activityUploadProfileBinding;
    private ProgressDialog progressDialog;
    private ImageView imageViewUploadPic;
    private FirebaseAuth authProfile;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri uriImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityUploadProfileBinding = ActivityUploadProfileBinding.inflate(getLayoutInflater());
        setContentView(activityUploadProfileBinding.getRoot());
        allocatedActivityTitle("Upload Profile");

        Button buttonUploadPicChoose = findViewById(R.id.upload_pic_choose_button);
        Button buttonUploadPic = findViewById(R.id.upload_pic_button);
        progressDialog = new ProgressDialog(UploadProfile.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        imageViewUploadPic = findViewById(R.id.imageView_profile_dp);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference("DisplayPics");
        Uri uri = firebaseUser.getPhotoUrl();
        Picasso.get().load(uri).into(imageViewUploadPic);

        buttonUploadPicChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        buttonUploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                UploadPic();
            }
        });
    }

    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            uriImage = data.getData();
            imageViewUploadPic.setImageURI(uriImage);
        }
    }

    private void UploadPic() {
        if(uriImage != null){
            StorageReference fileReference = storageReference.child(authProfile.getUid() + "." + getFileExtension(uriImage));

            fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Retrieve the download URL of the uploaded image
                            Uri downloadUri = uri;

                            // Update the profile URL in the real-time database
                            updateProfileUrl(downloadUri.toString());

                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            Toast.makeText(UploadProfile.this, "Upload Successfully!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(UploadProfile.this, UserProfile.class);
                            startActivity(intent);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UploadProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } else {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            Toast.makeText(UploadProfile.this, "No File Selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfileUrl(String imageUrl) {
        // Get the current user's UID
        String userId = authProfile.getUid();
        //ReadWriteUserDetails userDetails = new ReadWriteUserDetails();
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("profile", imageUrl);
//        userDetails.setProfile(imageUrl);

        // Get reference to the location of the user's profile in theff real-time database
//        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Registered Users").child(userId);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("Registered Users").document(userId)
                .update(userDetails)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Profile URL updated successfully
                        Toast.makeText(UploadProfile.this, "Profile image updated successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to update profile URL
                        Toast.makeText(UploadProfile.this, "Failed to update profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Update the profile URL with the retrieved download URL of the uploaded image
//        userRef.child("profile").setValue(imageUrl)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        // Profile URL updated successfully
//                        Toast.makeText(UploadProfile.this, "Profile image updated successfully!", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        // Failed to update profile URL
//                        Toast.makeText(UploadProfile.this, "Failed to update profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
    }


    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}
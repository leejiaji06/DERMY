package com.me.fyp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private View headerView;
    private TextView user_name, user_email;


    @Override
    public void setContentView(View view) {
        drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.navigation_drawer, null);
        FrameLayout container = drawerLayout.findViewById(R.id.activity_container);
        container.addView(view);
        EdgeToEdge.enable(this);
        super.setContentView(drawerLayout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigationView = drawerLayout.findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        headerView = navigationView.getHeaderView(0);

        user_name = headerView.findViewById(R.id.user_name);
        user_email = headerView.findViewById(R.id.user_email);
        fetchUsername(user);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.menu_drawer_open, R.string.menu_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        drawerLayout.closeDrawer(GravityCompat.START);

        int itemID = menuItem.getItemId();

        if (itemID == R.id.to_dashboard) {
            startActivity(new Intent(BaseActivity.this, DashboardActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemID == R.id.to_history) {
            startActivity(new Intent(BaseActivity.this, HistoryActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemID == R.id.to_detection) {
            startActivity(new Intent(BaseActivity.this, DetectionActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemID == R.id.to_classification) {
            startActivity(new Intent(BaseActivity.this, ClassificationActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemID == R.id.to_information) {
            startActivity(new Intent(BaseActivity.this, InformationActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemID == R.id.to_location) {
            startActivity(new Intent(BaseActivity.this, LocationPermissionActivity.class));
            overridePendingTransition(0, 0);
        }  else if (itemID == R.id.to_profile) {
            startActivity(new Intent(BaseActivity.this, UserProfile.class));
            overridePendingTransition(0, 0);
        }else if (itemID == R.id.to_logout) {
            mAuth.signOut();
            startActivity(new Intent(BaseActivity.this, MainActivity.class));
            overridePendingTransition(0, 0);
        }
        return false;
    }

    private void fetchUsername(FirebaseUser user) {
        if (user != null) {
            firestore.collection("Registered Users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            String email = documentSnapshot.getString("email");

                            user_name.setText(username);
                            user_email.setText(email);

                        } else {
                            Toast.makeText(BaseActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> Toast.makeText(BaseActivity.this, "Failed to retrieve username", Toast.LENGTH_SHORT).show());
        }
    }

    protected void allocatedActivityTitle(String TitleString) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(TitleString);
        }
    }
}
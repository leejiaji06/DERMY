package com.me.fyp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.me.fyp.databinding.ActivityLocationDetailsBinding;

import java.util.Arrays;

public class LocationDetailsActivity extends BaseActivity {

    private ActivityLocationDetailsBinding binding;
    private Button back_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLocationDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        allocatedActivityTitle("Location Details");

        back_button = findViewById(R.id.back_button);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LocationDetailsActivity.this, LocationActivity.class);
                startActivity(intent);
            }
        });

        String placeId = getIntent().getStringExtra("PLACE_ID");
        fetchPlaceDetails(placeId);
    }

    private void fetchPlaceDetails(String placeId) {
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.PHONE_NUMBER,
                Place.Field.WEBSITE_URI,
                Place.Field.RATING,
                Place.Field.OPENING_HOURS,
                Place.Field.PHOTO_METADATAS
        )).build();

        Places.createClient(this).fetchPlace(request).addOnSuccessListener(response -> {
            Place place = response.getPlace();

            TextView placeName = findViewById(R.id.place_name);
            TextView placeAddress = findViewById(R.id.place_address);
            TextView placePhone = findViewById(R.id.place_phone);
            TextView placeWebsite = findViewById(R.id.place_website);
            TextView placeRating = findViewById(R.id.place_rating);
            TextView placeOpeningHours = findViewById(R.id.place_opening_hours);
            ImageView placePhoto = findViewById(R.id.place_photo);

            placeName.setText(place.getName());
            placeAddress.setText(place.getAddress());
            placePhone.setText(place.getPhoneNumber() != null ? "Phone: " + place.getPhoneNumber() : "Phone: Not available");
            placeRating.setText(place.getRating() != null ? "Rating: " + place.getRating() : "Rating: Not available");

            if (place.getWebsiteUri() != null) {
                placeWebsite.setText("Visit Website");
                placeWebsite.setOnClickListener(v -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, place.getWebsiteUri());
                    startActivity(browserIntent);
                });
            } else {
                placeWebsite.setText("Website: Not available");
            }

            if (place.getOpeningHours() != null) {
                placeOpeningHours.setText("Opening hours: " + place.getOpeningHours().getWeekdayText());
            } else {
                placeOpeningHours.setText("Opening hours: Not available");
            }

            // Fetch and display the place's photo if available
            if (place.getPhotoMetadatas() != null && !place.getPhotoMetadatas().isEmpty()) {
                PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);
                FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata).build();

                Places.createClient(this).fetchPhoto(photoRequest).addOnSuccessListener((FetchPhotoResponse photoResponse) -> {
                    Bitmap bitmap = photoResponse.getBitmap();
                    placePhoto.setImageBitmap(bitmap);
                });
            }
        });
    }
}

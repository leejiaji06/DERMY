package com.me.fyp;



import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.me.fyp.databinding.ActivityLocationBinding;
import com.skyfishjy.library.RippleBackground;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocationActivity extends BaseActivity implements OnMapReadyCallback {
    ActivityLocationBinding activityLocationBinding;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient; //Fetch current location of device
    private PlacesClient placesClient; //Loading suggestion places
    private List<AutocompletePrediction> predictionList;
    private Location mLastKnownLocation;
    private LocationCallback locationCallback;
    private MaterialSearchBar materialSearchBar;
    private View mapView;
    private Button btnFind;
    private final float DEFAULT_ZOOM = 18;
    private RippleBackground rippleBg;
    private ImageView placePhoto;  // Add ImageView for the photo
    private String placeId; // Variable to store placeId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityLocationBinding = ActivityLocationBinding.inflate(getLayoutInflater());
        setContentView(activityLocationBinding.getRoot());
        allocatedActivityTitle("Location Service");

        materialSearchBar = findViewById(R.id.searchBar);
        btnFind = findViewById(R.id.btn_find);
        rippleBg = findViewById(R.id.ripple_bg);
        placePhoto = findViewById(R.id.place_photo);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(LocationActivity.this);
        Places.initializeWithNewPlacesApiEnabled(LocationActivity.this, BuildConfig.MAPS_API_KEY);
        placesClient = Places.createClient(this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString(), true,null,true);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
                    showPopupMenu();
                }else if(buttonCode == MaterialSearchBar.BUTTON_BACK){
                    if(materialSearchBar.isSuggestionsVisible()){
                        materialSearchBar.clearSuggestions();
                    }
                    if(materialSearchBar.isSearchEnabled()){
                        materialSearchBar.disableSearch();
                    }
                }
            }
        });

        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setCountries("MY")
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build();
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if(task.isSuccessful()){
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if(predictionsResponse != null){
                                Log.d("AutoCompleteSuccess", "number of prediction" +predictionsResponse.getAutocompletePredictions().size());
                                predictionList = predictionsResponse.getAutocompletePredictions();

                                List<String> suggestionList = new ArrayList<>();
                                for (int i=0; i<predictionList.size(); i++){
                                    AutocompletePrediction prediction = predictionList.get(i);
                                    suggestionList.add(prediction.getFullText(null).toString());
                                    Log.d("AutoCompleteSuccess", "Prediction: " + prediction.getFullText(null));
                                }
                                materialSearchBar.updateLastSuggestions(suggestionList);
                                if(!materialSearchBar.isSuggestionsVisible()){
                                    materialSearchBar.showSuggestionsList();
                                }
                            }else{
                                Log.d("AutoCompleteSuccess", "No predictions found");
                            }
                        }else{
                            Exception e = task.getException();
                            if(e instanceof ApiException){
                                 ApiException apiException = (ApiException) e;
                                 int statuscode = apiException.getStatusCode();

                                 Log.d("AutoCompleteError","Error fetching predictions" + e.getMessage() + "| status code" + statuscode);
                            }else{
                                Log.e("AutoCompleteError", "Unknown error" + e.getMessage());
                            }
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        materialSearchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                // Retrieve selected place
                if(position >= predictionList.size()){
                    return;
                }
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
                materialSearchBar.setText(suggestion);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        materialSearchBar.clearSuggestions();
                    }
                },1000);

                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if(imm != null ){
                    imm.hideSoftInputFromWindow(materialSearchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
                placeId = selectedPrediction.getPlaceId();  // Store placeId

                List<Place.Field> placeFields = Arrays.asList(
                        Place.Field.ID,
                        Place.Field.LAT_LNG
                );

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();

                // Fetch place and move camera
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        Place place = fetchPlaceResponse.getPlace();
                        Log.i("Location", "Place found: " + place.getName() );
                        LatLng latLngOfPlace = place.getLatLng();
                        if(latLngOfPlace != null){
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlace,DEFAULT_ZOOM));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            int statusCode = apiException.getStatusCode();
                            Log.e("PlaceDetailsError", "Place not found: " + apiException.getMessage());
                            Log.e("PlaceDetailsError", "Status Code: " + statusCode);
                        }
                    }
                });

                // Clear suggestions after selection
                materialSearchBar.clearSuggestions();
            }
            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });
        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(materialSearchBar.isSuggestionsVisible()){
                    materialSearchBar.clearSuggestions();
                }
                if(materialSearchBar.isSearchEnabled()){
                    materialSearchBar.disableSearch();
                }
                LatLng currentMarkerLocation = mMap.getCameraPosition().target;
                rippleBg.startRippleAnimation();
                btnFind.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rippleBg.stopRippleAnimation();
                        btnFind.setEnabled(true);
                    }
                }, 4000);

            }
        });
        placePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LocationActivity.this, LocationDetailsActivity.class);
                intent.putExtra("PLACE_ID", placeId);  // Pass placeId to LocationDetailsActivity
                startActivity(intent);
            }
        });

    }



    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(LocationActivity.this, materialSearchBar);
        popupMenu.getMenuInflater().inflate(R.menu.drawer_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String selectedOption = item.getTitle().toString();
                materialSearchBar.setText(selectedOption);
                materialSearchBar.enableSearch();
                return true;
            }
        });

        // Display the PopupMenu
        popupMenu.show();
    }


    @SuppressLint("MissingPermission")
    @Override
    //called as soon as the map is ready
    public void onMapReady(@NonNull GoogleMap googleMap) {
       mMap = googleMap;
       mMap.setMyLocationEnabled(true);
       mMap.getUiSettings().setMyLocationButtonEnabled(true);

       if(mapView != null && mapView.findViewById(Integer.parseInt("1")) != null){
           View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
           RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
           layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
           layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
           layoutParams.setMargins(0,0,40,180);
       }

       //check if gps is enable
       //if not, ask user to enable it
       LocationRequest locationRequest = LocationRequest.create();
       locationRequest.setInterval(10000);
       locationRequest.setFastestInterval(5000);
       locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

       LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

       SettingsClient settingsClient = LocationServices.getSettingsClient(LocationActivity.this);
       Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

       task.addOnSuccessListener(LocationActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
           @Override
           public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
               getDeviceLocation();
           }
        });

        task.addOnFailureListener(LocationActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof ResolvableApiException){
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(LocationActivity.this,19);
                    } catch (IntentSender.SendIntentException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if(materialSearchBar.isSuggestionsVisible()){
                    materialSearchBar.clearSuggestions();
                }
                if(materialSearchBar.isSearchEnabled()){
                    materialSearchBar.disableSearch();
                }
                return false;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 19){
            if(resultCode == RESULT_OK){
                getDeviceLocation();
            }
        }
    }

    @SuppressLint("MissingPermission")
    // get the last location
    // is successful, move camera to the location
    // if not successful, location request and location callback
    private void getDeviceLocation(){
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if(task.isSuccessful()){
                            mLastKnownLocation = task.getResult();
                            if(mLastKnownLocation != null){
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }else{
                                LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(10000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
                                locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(@NonNull LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if (locationResult == null) return;

                                        mLastKnownLocation = locationResult.getLastLocation();
                                        if (mLastKnownLocation != null) {
                                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                                    new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                                            // Stop further updates once the location is found
                                            mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                        }
                                    }
                                };
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                            }
                        }else{
                            Toast.makeText(LocationActivity.this, "Unable to get last location.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
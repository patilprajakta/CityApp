package com.example.synerzip.explorecity.ui;

import android.os.Bundle;

import com.example.synerzip.explorecity.models.FoursquareJSON;
import com.example.synerzip.explorecity.models.FoursquareResponse;
import com.example.synerzip.explorecity.models.FoursquareVenue;
import com.example.synerzip.explorecity.models.GoogleResult;
import com.example.synerzip.explorecity.R;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import com.example.synerzip.explorecity.models.GoogleResponse;
import com.example.synerzip.explorecity.network.AppConstants;
import com.example.synerzip.explorecity.network.RetrofitMaps;

import android.Manifest;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import info.hoang8f.android.segmented.SegmentedGroup;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;


import static android.widget.Toast.LENGTH_SHORT;

/**
 * Created by Prajakta Patil on 9/2/17.
 * Copyright © 2017 Synerzip. All rights reserved
 */
public class MapsActivity extends FragmentActivity implements
        PlaceSelectionListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    @BindView(R.id.seach_city)
    AutoCompleteTextView mAutoCompleteCity;

    @BindView(R.id.seach_places)
    AutoCompleteTextView mAutoCompletePlace;

    private GoogleMap mMap;
    double latitude;
    double longitude;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    double googleCityLat, googleCityLng;
    double foursquareLat, foursquareLng;

    @BindView(R.id.mCategorySelector)
    SegmentedGroup mSegmentedGroup;

    String key = AppConstants.GOOGLE_PLACES_API_KEY;
    int radius = 100000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ButterKnife.bind(this);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /**
         * autocomplete city
         */
        mAutoCompleteCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getGoogleMapsAutoCity(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mAutoCompleteCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index, long id) {

                ListView lv = (ListView) arg0;
                SimpleAdapter adapter = (SimpleAdapter) arg0.getAdapter();
                HashMap<String, String> hm = (HashMap<String, String>) adapter.getItem(index);
                String ref = hm.get(getString(R.string.reference));
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(arg1.getApplicationWindowToken(), 0);
            }
        });

        /**
         *  autocomplete places in a city
         */
        mAutoCompletePlace.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getGoogleMapsAutoPlaces(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mAutoCompletePlace.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index, long id) {
                ListView lv = (ListView) arg0;
                SimpleAdapter adapter = (SimpleAdapter) arg0.getAdapter();
                HashMap<String, String> hm = (HashMap<String, String>) adapter.getItem(index);
                String ref = hm.get(getString(R.string.reference));
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(arg1.getApplicationWindowToken(), 0);
            }
        });

    }

    /**
     * Google places api call
     *
     * @param place
     */
    private void getGoogleMapsAutoCity(String place) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConstants.GOOGLE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitMaps service = retrofit.create(RetrofitMaps.class);
        service.getCityResults(place, radius, key).enqueue(new Callback<GoogleResponse>() {
            @Override
            public void onResponse(Response<GoogleResponse> response, Retrofit retrofit) {

                for (int i = 0; i < response.body().getResults().size(); i++) {
                    double lat = response.body().getResults().get(i).getGeometry().getLocation().getLat();
                    double lng = response.body().getResults().get(i).getGeometry().getLocation().getLng();
                    String placeName = response.body().getResults().get(i).getName();
                    String vicinity = response.body().getResults().get(i).getVicinity();
                    MarkerOptions markerOptions = new MarkerOptions();
                    LatLng latLng = new LatLng(lat, lng);
                    markerOptions.position(latLng);
                    markerOptions.title(placeName + " : " + vicinity);
                    Marker m = mMap.addMarker(markerOptions);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void getFoursquarePlaces(double lat, double lng, String type) {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLastLocation != null) {
                String userLL = googleCityLat + "," + googleCityLng;
                double userLLAcc = mLastLocation.getAccuracy();
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(AppConstants.FOURSQUARE_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                RetrofitMaps foursquare = retrofit.create(RetrofitMaps.class);
                Call<FoursquareJSON> stpCall = foursquare.fourSquarePlaces(
                        AppConstants.FOURSQUARE_CLIENT_ID,
                        AppConstants.FOURSQUARE_CLIENT_SECRET,
                        userLL,
                        userLLAcc);
                stpCall.enqueue(new Callback<FoursquareJSON>() {
                    @Override
                    public void onResponse(Response<FoursquareJSON> response, Retrofit retrofit) {
                        FoursquareJSON json = response.body();
                        FoursquareResponse foursquareResponse = json.getFoursquareResponse();
                        List<FoursquareVenue> foursquareVenuesList = foursquareResponse.getVenues();

                        for (FoursquareVenue foursquareVenue : foursquareVenuesList) {
                            foursquareLat = foursquareVenue.getLocation().getLat();
                            foursquareLng = foursquareVenue.getLocation().getLng();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        t.printStackTrace();
                    }

                });
            }
        }
    }

    private void getGoogleMapsAutoPlaces(final String place) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConstants.GOOGLE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitMaps service = retrofit.create(RetrofitMaps.class);
        service.getPlaceResults(place, radius, key)
                .enqueue(new Callback<GoogleResponse>() {
                    @Override
                    public void onResponse(Response<GoogleResponse> response, Retrofit retrofit) {

                        double latitude = response.body().getResults().get(0).getGeometry().getLocation().getLat();
                        double longitude = response.body().getResults().get(0).getGeometry().getLocation().getLng();

                        String placeName = response.body().getResults().get(0).getName();
                        String vicinity = response.body().getResults().get(0).getVicinity();
                        MarkerOptions markerOptions = new MarkerOptions();
                        LatLng latLng = new LatLng(latitude, longitude);
                        markerOptions.position(latLng);
                        markerOptions.title(placeName + " : " + vicinity);
                        mMap.addMarker(markerOptions);
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        t.printStackTrace();
                    }
                });

    }

    /**
     * on map ready
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        /**
         * Segmented group for categories
         */
        mSegmentedGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioBtnHotel) {
                    String restaurant = getString(R.string.restaurant);
                    mMap.clear();
                    getFoursquarePlaces(foursquareLat, foursquareLng, restaurant);
                    Object[] DataTransfer = new Object[2];
                    DataTransfer[0] = mMap;
                    Toast.makeText(MapsActivity.this, getString(R.string.Hotels), LENGTH_SHORT).show();
                }
                if (checkedId == R.id.radioBtnAtm) {
                    String atms = getString(R.string.atm);
                    mMap.clear();
                    getFoursquarePlaces(foursquareLat, foursquareLng, atms);
                    Object[] DataTransfer = new Object[2];
                    DataTransfer[0] = mMap;
                    Toast.makeText(MapsActivity.this, getString(R.string.ATMs), LENGTH_SHORT).show();

                }
                if (checkedId == R.id.radioBtnHospital) {
                    String hospital = getString(R.string.hospital);
                    mMap.clear();
                    getFoursquarePlaces(foursquareLat, foursquareLng, hospital);
                    Object[] DataTransfer = new Object[2];
                    DataTransfer[0] = mMap;
                    Toast.makeText(MapsActivity.this, getString(R.string.Hospitals), LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * check google play services
     *
     * @return
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result, 0).show();
            }
            return false;
        }
        return true;
    }

    /***
     * initialize google components
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();

            LatLng loc = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(loc).title(getString(R.string.current_pos)).
                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
            mMap.animateCamera(CameraUpdateFactory.zoomBy(11));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        latitude = location.getLatitude();
        Log.v("Lat", String.valueOf(latitude));
        longitude = location.getLongitude();

        LatLng latLng = new LatLng(latitude, longitude);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);

        mCurrLocationMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.addMarker(new MarkerOptions().
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(11));
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MapsActivity.this);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    /**
     * check location permission
     */
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * request permissions for location
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                return;
            }
        }
    }

    /**
     * create options menu
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Settings Menu
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_mapview:
                Toast.makeText(getApplicationContext(), getString(R.string.item1), Toast.LENGTH_LONG).show();
                return true;
            case R.id.menu_others:
                Toast.makeText(getApplicationContext(), getString(R.string.item2), Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPlaceSelected(Place place) {

    }

    @Override
    public void onError(Status status) {

    }
}
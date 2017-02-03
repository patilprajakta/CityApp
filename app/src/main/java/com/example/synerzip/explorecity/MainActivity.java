package com.example.synerzip.explorecity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import info.hoang8f.android.segmented.SegmentedGroup;

import static android.widget.Toast.LENGTH_SHORT;
import static com.example.synerzip.explorecity.R.id.new_map;

/**
 * Created by Prajakta Patil on 4/1/17.
 */
public
class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    final int PLACES = 0;
    final int PLACES_DETAILS = 1;
    GoogleApiClient mGoogleApiClient;
    Double latitude;
    Double longitude;
    private int PROXIMITY_RADIUS = 100000;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;

    @BindView(R.id.seach_city)
    AutoCompleteTextView mAutoCompleteCity;

    @BindView(R.id.seach_places)
    AutoCompleteTextView mAutoCompletePlace;

    DownloadTask placesDownloadTask;
    DownloadTask placeDetailsDownloadTask;
    ParserTask placesParserTask;
    ParserTask placeDetailsParserTask;
    GoogleMap mMap;

    @BindView(R.id.clear_text_city)
    Button btnClearCity;

    @BindView(R.id.clear_text_place)
    Button btnClearPlace;

    @BindView(R.id.mCategorySelector)
    SegmentedGroup mSegmentedGroup;

    Double cityLatitude;
    Double cityLongitude;
    String googleRating;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        if (!CheckGoogleServices()) {
            Log.d("onCreate", "Google Play Services are not available");
            finish();
        } else {
            Log.d("onCreate", "Google Play Services are available.");
        }

        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(new_map);
        fm.getMapAsync(this);

        mAutoCompleteCity.setThreshold(1);
        mAutoCompletePlace.setThreshold(1);

        /**
         * autocomplete city
         */
        mAutoCompleteCity.addTextChangedListener(new TextWatcher() {
            @Override
            public
            void onTextChanged(CharSequence s, int start, int before, int count) {
                placesDownloadTask = new DownloadTask(PLACES);
                String url = getAutoUrlCity(s.toString());
                placesDownloadTask.execute(url);
            }

            @Override
            public
            void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public
            void afterTextChanged(Editable s) {
            }
        });
        mAutoCompleteCity.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public
            void onItemClick(AdapterView<?> arg0, View arg1, int index, long id) {

                ListView lv = (ListView) arg0;
                SimpleAdapter adapter = (SimpleAdapter) arg0.getAdapter();
                HashMap<String, String> hm = (HashMap<String, String>) adapter.getItem(index);
                placeDetailsDownloadTask = new DownloadTask(PLACES_DETAILS);
                String url = getPlaceDetailsUrl(hm.get("reference"));

                Log.v("URL", url);
                //   Log.v("getPlacesRating", googleRating);
                placeDetailsDownloadTask.execute(url);
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(arg1.getApplicationWindowToken(), 0);
            }
        });

        /**
         *  autocomplete places in a city
         */

        mAutoCompletePlace.addTextChangedListener(new TextWatcher() {
            @Override
            public
            void onTextChanged(CharSequence s, int start, int before, int count) {
                placesDownloadTask = new DownloadTask(PLACES);
                String url = getAutoUrlPlace(s.toString());
                placesDownloadTask.execute(url);
            }

            @Override
            public
            void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public
            void afterTextChanged(Editable s) {
            }
        });
        mAutoCompletePlace.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public
            void onItemClick(AdapterView<?> arg0, View arg1, int index, long id) {
                ListView lv = (ListView) arg0;
                SimpleAdapter adapter = (SimpleAdapter) arg0.getAdapter();
                HashMap<String, String> hm = (HashMap<String, String>) adapter.getItem(index);
                placeDetailsDownloadTask = new DownloadTask(PLACES_DETAILS);
                String url = getAutoUrlPlace(hm.get("reference"));
                Log.v("URL", url);
                placeDetailsDownloadTask.execute(url);

                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(arg1.getApplicationWindowToken(), 0);
            }
        });
    }//onCreate()

    private
    boolean CheckGoogleServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
    }

    /**
     * check permissions for marshamallow
     */
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public
    boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Get autocomplete Url for google places
     *
     * @param city
     * @return
     */
    private
    String getAutoUrlCity(String city) {
        String key = "key=AIzaSyDLuFX3GsD9UGhM60SG5E9zwvib2XcX5OE";
        String country = "components=country:in";
        String input = "input=" + city;
        String types = "types=(cities)";
        String sensor = "sensor=false";
        String parameters = country + "&" + input + "&" + types + "&" + sensor + "&" + key;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/place/autocomplete/" + output + "?" + parameters;
        return url;
    }

    private
    String getAutoUrlPlace(String place) {
        String key = "key=AIzaSyDLuFX3GsD9UGhM60SG5E9zwvib2XcX5OE";
        String country = "components=country:in";
        String input = "input=" + place;
        String sensor = "sensor=false";
        String parameters = country + "&" + input + "&" + sensor + "&" + key;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/place/autocomplete/" + output + "?" + parameters;
        return url;
    }

    /**
     * Get places detail url for google places
     *
     * @param ref
     * @return
     */
    private
    String getPlaceDetailsUrl(String ref) {
        String key = "key=AIzaSyDLuFX3GsD9UGhM60SG5E9zwvib2XcX5OE";//places api key
        String reference = "reference=" + ref;
        String sensor = "sensor=false";
        String parameters = reference + "&" + sensor + "&" + key;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/place/details/" + output + "?" + parameters;
        return url;
    }

    /**
     * Method to download json data from url
     *
     * @param strUrl
     * @return
     * @throws IOException
     */
    private
    String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();// Reading data from url
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();

        } catch (Exception e) {
            Log.v("Exceptiondownloading", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    //clear text for cities
    public
    void clearTxtCity(View view) {
        mAutoCompleteCity.setText("");
    }

    //clear text for places
    public
    void clearTxtPlace(View view) {
        mAutoCompletePlace.setText("");
    }

    /**
     * on new_map ready
     *
     * @param googleMap
     */
    @Override
    public
    void onMapReady(GoogleMap googleMap) {
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
            public
            void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioBtnHotel) {
                    String restaurant = "restaurant";
                    Log.d("segment1", "Button is Clicked");
                    mMap.clear();
                    if (Double.isNaN(latitude) && Double.isNaN(longitude)) {
                        cityLatitude = latitude;
                        cityLongitude = longitude;
                    }
                    String url = getUrlSeg(cityLatitude, cityLongitude, restaurant);
                    Object[] DataTransfer = new Object[2];
                    DataTransfer[0] = mMap;
                    DataTransfer[1] = url;
                    Log.d("onClick", url);
                    GetPlacesData getPlacesData = new GetPlacesData();
                    getPlacesData.execute(DataTransfer);
                    Toast.makeText(MainActivity.this, "Hotels ", LENGTH_SHORT).show();
                }
                if (checkedId == R.id.radioBtnAtm) {
                    String atms = "atm";
                    Log.d("onClick", "Button is Clicked");
                    mMap.clear();
                    if (mCurrLocationMarker != null) {
                        mCurrLocationMarker.remove();
                    }
                    String url = getUrlSeg(cityLatitude, cityLongitude, atms);
                    Object[] DataTransfer = new Object[2];
                    DataTransfer[0] = mMap;
                    DataTransfer[1] = url;
                    Log.d("onClick", url);
                    GetPlacesData getPlacesData = new GetPlacesData();
                    getPlacesData.execute(DataTransfer);
                    Toast.makeText(MainActivity.this, "ATMs ", LENGTH_SHORT).show();

                }
                if (checkedId == R.id.radioBtnHospital) {
                    String Hospital = "hospital";
                    Log.d("onClick", "Button is Clicked");
                    mMap.clear();
                    if (Double.isNaN(cityLatitude) && Double.isNaN(cityLongitude)) {
                        cityLatitude = latitude;
                        cityLongitude = longitude;
                    }
                    String url = getUrlSeg(cityLatitude, cityLongitude, Hospital);
                    Object[] DataTransfer = new Object[2];
                    DataTransfer[0] = mMap;
                    DataTransfer[1] = url;
                    Log.d("onClick", url);
                    GetPlacesData getPlacesData = new GetPlacesData();
                    getPlacesData.execute(DataTransfer);
                    Toast.makeText(MainActivity.this, "Hospitals ", LENGTH_SHORT).show();
                }
            }
        });
    }

    private
    String getUrlSeg(double cityLatitude, double cityLongitude, String nearbyPlace) {
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");

        googlePlacesUrl.append("location=" + cityLatitude + "," + cityLongitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + "AIzaSyDLuFX3GsD9UGhM60SG5E9zwvib2XcX5OE");
        Log.d("getUrlSeg", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }

    protected synchronized
    void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public
    void onLocationChanged(Location location) {
        Log.d("onLocationChanged", "inside onLocationChanged()");
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        //Place current location marker
        latitude = location.getLatitude();
        Log.v("Lat", String.valueOf(latitude));
        longitude = location.getLongitude();

        LatLng latLng = new LatLng(latitude, longitude);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        mCurrLocationMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(11));
        Log.d("onLocationChanged", String.format("latitude:%.3f longitude:%.3f", cityLatitude, cityLongitude));
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MainActivity.this);
            Log.d("onLocationChanged", "Removing Location Updates");
        }
        Log.d("onLocationChanged", "Exit");
    }

    @Override
    public
    void onConnected(@Nullable Bundle bundle) {
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
            mMap.addMarker(new MarkerOptions().position(loc).title("My Current Location").
                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        }
    }

    @Override
    public
    void onConnectionSuspended(int i) {
    }

    @Override
    public
    void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    /**
     * Fetches data from url passed
     */
    private
    class DownloadTask extends AsyncTask<String, Void, String> {
        private int downloadType = 0;

        public
        DownloadTask(int type) {
            this.downloadType = type;
        }

        @Override
        protected
        String doInBackground(String... url) {
            String data = "";
            try {
                data = downloadUrl(url[0]);
                Log.v("DATA", data);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected
        void onPostExecute(String result) {
            super.onPostExecute(result);
            switch (downloadType) {
                case PLACES:
                    Log.v("PLACES", result);
                    placesParserTask = new ParserTask(PLACES);
                    placesParserTask.execute(result);
                    break;

                case PLACES_DETAILS:
                    Log.v("PLACES_DETAILS", result);
                    placeDetailsParserTask = new ParserTask(PLACES_DETAILS);
                    placeDetailsParserTask.execute(result);
            }
        }

    }//DownloadTask

    /**
     * A class to parse the Google Places in JSON format
     */
    private
    class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {
        int parserType = 0;

        public
        ParserTask(int type) {
            this.parserType = type;
        }

        @Override
        protected
        List<HashMap<String, String>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<HashMap<String, String>> list = null;
            try {
                jObject = new JSONObject(jsonData[0]);

                switch (parserType) {
                    case PLACES:
                        PlaceParser placeParser = new PlaceParser();
                        list = placeParser.parse(jObject);
                        break;
                    case PLACES_DETAILS:
                        PlaceDetailsParser placeDetailsParser = new PlaceDetailsParser();
                        list = placeDetailsParser.parse(jObject);
                }

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return list;
        }

        @Override
        protected
        void onPostExecute(List<HashMap<String, String>> result) {
            switch (parserType) {
                case PLACES:
                    String[] from = new String[]{"description"};
                    int[] to = new int[]{android.R.id.text1};
                    SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result,
                            android.R.layout.simple_list_item_1, from, to);
                    mAutoCompleteCity.setAdapter(adapter);
                    mAutoCompletePlace.setAdapter(adapter);
                    break;
                case PLACES_DETAILS:
                    mMap.clear();
                    HashMap<String, String> hm = result.get(0);

                    cityLatitude = Double.parseDouble(hm.get("latitude"));
                    cityLongitude = Double.parseDouble(hm.get("longitude"));
                    LatLng point = new LatLng(cityLatitude, cityLongitude);

                    CameraUpdate cameraPosition = CameraUpdateFactory.newLatLng(point);
                    CameraUpdate cameraZoom = CameraUpdateFactory.zoomBy(12);

                    mMap.moveCamera(cameraPosition);
                    mMap.animateCamera(cameraZoom);

                    MarkerOptions options = new MarkerOptions();
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
                    options.position(point);
                    options.title("Position");
                    options.snippet("Latitude:" + cityLatitude + ",Longitude:" + cityLongitude);

                    //intent to Details Activity
                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public
                        void onInfoWindowClick(Marker marker) {
                            Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                            intent.putExtra("lattitude", cityLatitude);
                            intent.putExtra("longitude", cityLongitude);
                            intent.putExtra("rating", googleRating);
                            startActivity(intent);
                        }
                    });

                    CircleOptions circleOptions = new CircleOptions();
                    circleOptions.center(point);
                    circleOptions.radius(500);
                    circleOptions.fillColor(getResources().getColor(R.color.colorMapCircle));
                    circleOptions.strokeColor(getResources().getColor(R.color.colorAccent));
                    circleOptions.strokeWidth(5);

                    mMap.addMarker(options);
                    mMap.addCircle(circleOptions);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(point));
                    break;
            }
        }
    }//end ParserTask async class

    @Override
    public
    boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public
    boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_mapview:
                Toast.makeText(getApplicationContext(), "Item 1", Toast.LENGTH_LONG).show();
                return true;
            case R.id.menu_others:
                Toast.makeText(getApplicationContext(), "Item 2", Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

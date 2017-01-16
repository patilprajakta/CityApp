package com.example.synerzip.explorecity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.location.places.AutocompleteFilter;
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
import info.hoang8f.android.segmented.SegmentedGroup;

import static android.widget.Toast.LENGTH_SHORT;
import static com.example.synerzip.explorecity.R.id.map;

/**
 * Created by Prajakta Patil on 4/1/17.
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    final int PLACES = 0;
    final int PLACES_DETAILS = 1;

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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //google map fragment
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        fm.getMapAsync(this);

        mSegmentedGroup = (SegmentedGroup) findViewById(R.id.mCategorySelector);
        mAutoCompleteCity = (AutoCompleteTextView) findViewById(R.id.seach_city);
        mAutoCompletePlace = (AutoCompleteTextView) findViewById(R.id.seach_places);
        mAutoCompleteCity.setThreshold(1);//what is setThreshold
        mAutoCompletePlace.setThreshold(1);

        /**
         * autocomplete city
         */
        mAutoCompleteCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                placesDownloadTask = new DownloadTask(PLACES);
                String url = getAutoCompleteUrl(s.toString());
                placesDownloadTask.execute(url);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mAutoCompleteCity.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index, long id) {

                ListView lv = (ListView) arg0;
                SimpleAdapter adapter = (SimpleAdapter) arg0.getAdapter();
                HashMap<String, String> hm = (HashMap<String, String>) adapter.getItem(index);
                placeDetailsDownloadTask = new DownloadTask(PLACES_DETAILS);
                String url = getPlaceDetailsUrl(hm.get("reference"));
                Log.v("URL", url);
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
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                placesDownloadTask = new DownloadTask(PLACES);
                String url = getAutoCompleteUrlPlace(s.toString());
                placesDownloadTask.execute(url);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mAutoCompletePlace.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index, long id) {
                ListView lv = (ListView) arg0;
                SimpleAdapter adapter = (SimpleAdapter) arg0.getAdapter();
                HashMap<String, String> hm = (HashMap<String, String>) adapter.getItem(index);
                placeDetailsDownloadTask = new DownloadTask(PLACES_DETAILS);
                String url = getAutoCompleteUrlPlace(hm.get("reference"));
                Log.v("URL", url);
                placeDetailsDownloadTask.execute(url);

                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(arg1.getApplicationWindowToken(), 0);
            }
        });

        /**
         * Segmented group for categories
         */
        mSegmentedGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioBtnHotel) {
                    Toast.makeText(MainActivity.this, "Hotels ", LENGTH_SHORT).show();
                }
                if (checkedId == R.id.radioBtnAtm) {
                    Toast.makeText(MainActivity.this, "ATMs ", LENGTH_SHORT).show();
                }
                if (checkedId == R.id.radioBtnHospital) {
                    Toast.makeText(MainActivity.this, "Hospitals ", LENGTH_SHORT).show();
                }
            }
        });
    }//onCreate()

    /**
     * Get autocomplete Url for google places
     *
     * @param place
     * @return
     */
    private String getAutoCompleteUrl(String place) {
        // Obtain browser key from https://code.google.com/apis/console
        String key = "key=AIzaSyDLuFX3GsD9UGhM60SG5E9zwvib2XcX5OE";
        String country = "components=country:in";
        String input = "input=" + place;
        String types = "types=(cities)";//&types=(cities)
        String sensor = "sensor=false";
        String parameters = country + "&" + input + "&" + types + "&" + sensor + "&" + key;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/place/autocomplete/" + output + "?" + parameters;
        return url;
    }

    private String getAutoCompleteUrlPlace(String place) {
        // Obtain browser key from https://code.google.com/apis/console
        String key = "key=AIzaSyDLuFX3GsD9UGhM60SG5E9zwvib2XcX5OE";
        String country = "components=country:in";
        String input = "input=" + place;
        String types = "&types=" + "hotels";//&types=(cities)
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
    private String getPlaceDetailsUrl(String ref) {
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
    private String downloadUrl(String strUrl) throws IOException {
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
    public void clearTxtCity(View view) {
        mAutoCompleteCity.setText("");
    }

    //clear text for places
    public void clearTxtPlace(View view) {
        mAutoCompletePlace.setText("");
    }

    /**
     * on map ready
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng india = new LatLng(18.5204300, 73.8567440);
        mMap.addMarker(new MarkerOptions().position(india).title("Pune, India"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(india));

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    /**
     * Fetches data from url passed
     */
    private class DownloadTask extends AsyncTask<String, Void, String> {
        private int downloadType = 0;

        public DownloadTask(int type) {
            this.downloadType = type;
        }

        @Override
        protected String doInBackground(String... url) {
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
        protected void onPostExecute(String result) {
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
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {
        int parserType = 0;

        public ParserTask(int type) {
            this.parserType = type;
        }

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<HashMap<String, String>> list = null;
            try {
                jObject = new JSONObject(jsonData[0]);

                switch (parserType) {
                    case PLACES:
                        PlaceJSONParser placeJsonParser = new PlaceJSONParser();
                        list = placeJsonParser.parse(jObject);
                        break;
                    case PLACES_DETAILS:
                        PlaceDetailsJsonParser placeDetailsJsonParser = new PlaceDetailsJsonParser();
                        list = placeDetailsJsonParser.parse(jObject);
                }

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {
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
                    final double latitude = Double.parseDouble(hm.get("latitude"));
                    final double longitude = Double.parseDouble(hm.get("longitude"));

                    LatLng point = new LatLng(latitude, longitude);

                    CameraUpdate cameraPosition = CameraUpdateFactory.newLatLng(point);
                    CameraUpdate cameraZoom = CameraUpdateFactory.zoomBy(12);

                    mMap.moveCamera(cameraPosition);
                    mMap.animateCamera(cameraZoom);

                    MarkerOptions options = new MarkerOptions();
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    options.position(point);
                    options.title("Position");
                    options.snippet("Latitude:" + latitude + ",Longitude:" + longitude);

                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                            intent.putExtra("lattitude", latitude);
                            intent.putExtra("longitude", longitude);
                            startActivity(intent);
                        }
                    });

                    CircleOptions circleOptions = new CircleOptions();
                    circleOptions.center(point);
                    //Radius in meters
                    circleOptions.radius(500);
                    circleOptions.fillColor(getResources().getColor(R.color.colorMapCircle));
                    circleOptions.strokeColor(getResources().getColor(R.color.colorAccent));
                    circleOptions.strokeWidth(5);
                    // Adding the marker in the Google Map
                    mMap.addMarker(options);
                    mMap.addCircle(circleOptions);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(point));
                    mMap.getUiSettings().setRotateGesturesEnabled(true);
                    mMap.getUiSettings().setAllGesturesEnabled(true);
                    mMap.getUiSettings().setScrollGesturesEnabled(true);
                    mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
                    mMap.getUiSettings().setMapToolbarEnabled(true);
                    mMap.getUiSettings().setZoomControlsEnabled(true);
                    mMap.getUiSettings().setZoomGesturesEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    mMap.getUiSettings().setRotateGesturesEnabled(false);
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_mapview:
                Toast.makeText(getApplicationContext(), "Item 1 Selected", Toast.LENGTH_LONG).show();
                return true;
            case R.id.menu_others:
                Toast.makeText(getApplicationContext(), "Item 2 Selected", Toast.LENGTH_LONG).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

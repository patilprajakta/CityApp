package com.example.synerzip.explorecity;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;

import static com.example.synerzip.explorecity.R.id.map;

public class DetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbarLayout;
    Double lattitude, longitude;
    GoogleMap mMap;
    private ListView lv;
    private static String url = "https://api.foursquare.com/v2/venues/search?" +
            "client_id=VVPFPMTNSXBZGIJXUNHVIWU5T5QRMVC0JBLZ1BXJE2LGWJRE&" +
            "client_secret=5FRCH0GERDOWRDZ3OFJQEWDWN1I4VO4FAWE0OECIMO3SOUPT&v=20130815&ll=18.5204300,73.8567440&query=hotels";
    ArrayList<HashMap<String, String>> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        toolbar = (Toolbar) findViewById(R.id.toolbar1);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.CollapsingToolbarLayout1);

        setSupportActionBar(toolbar);

        collapsingToolbarLayout.setTitle("Hotel Abhishek");

        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        fm.getMapAsync(this);

        Intent intent = getIntent();
        lattitude = intent.getDoubleExtra("latitude", 0);
        longitude = intent.getDoubleExtra("longitude", 0);
        list = new ArrayList<>();
        lv = (ListView) findViewById(R.id.list);
        new GetCheckins().execute();//async task
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetCheckins extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);
            Log.e("4STAG", "Response from url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray jsonArray = jsonObj.getJSONArray("stats");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject c = jsonArray.getJSONObject(i);

                        String checkin_count = c.getString("checkinsCount");
                        HashMap<String, String> single_checkin = new HashMap<>();

                        single_checkin.put("checkinsCount", checkin_count);
                        list.add(single_checkin);
                    }
                } catch (final JSONException e) {
                    Log.e("4STAG", "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Json parsing error: " +
                                    e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                }
            } else {
                Log.e("4STAG", "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            ListAdapter adapter = new SimpleAdapter(
                    DetailsActivity.this, list, R.layout.list_item, new String[]{"checkinsCount"},
                    new int[]{R.id.tv_checkin});
            lv.setAdapter(adapter);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng point = new LatLng(lattitude, longitude);
        mMap.addMarker(new MarkerOptions().position(point));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
    }
}






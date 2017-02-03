package com.example.synerzip.explorecity;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.synerzip.explorecity.R.id.new_map;

public class DetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    @BindView(R.id.toolBar)
    Toolbar toolbar;

    @BindView(R.id.CollapsingToolbarLayout1)
    CollapsingToolbarLayout collapsingToolbarLayout;

    Double lattitude, longitude, googleRating;
    GoogleMap mMap;
    ArrayList<FoursquareVenue> venuesList;
    final String CLIENT_ID = "VVPFPMTNSXBZGIJXUNHVIWU5T5QRMVC0JBLZ1BXJE2LGWJRE";
    final String CLIENT_SECRET = "5FRCH0GERDOWRDZ3OFJQEWDWN1I4VO4FAWE0OECIMO3SOUPT";

    @BindView(R.id.txtfour)
    TextView txtfour;

    @BindView(R.id.txtRating)
    TextView txtgoogle;

    @Override
    protected
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        collapsingToolbarLayout.setTitle("Hotel Aurora Towers");

        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(new_map);
        fm.getMapAsync(this);

        Intent intent = getIntent();
        lattitude = intent.getDoubleExtra("lattitude", 0);
        longitude = intent.getDoubleExtra("longitude", 0);
        googleRating = intent.getDoubleExtra("rating", 0);

        Log.d("lat", lattitude.toString());
        Log.d("lng", longitude.toString());
        Log.d("rating", googleRating.toString());
        txtgoogle.setText(googleRating.toString());
        new fourquare().execute();
    }

    @Override
    public
    void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng point = new LatLng(lattitude, longitude);
        mMap.addMarker(new MarkerOptions().position(point));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
    }

    private class fourquare extends AsyncTask<View, Void, String> {
        String temp;
        String url = "https://api.foursquare.com/v2/venues/search?client_id=" +
                CLIENT_ID + "&client_secret=" + CLIENT_SECRET + "&v=20130815&ll="+lattitude+","+longitude+ "&query=hotels";

        @Override
        protected
        String doInBackground(View... urls) {
            temp = makeCall(url);
            Log.d("4URL", url);
            Log.d("Lat", lattitude.toString());
            Log.d("Lng", longitude.toString());
            return "";
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected
        void onPostExecute(String result) {
            if (temp == null) {
            } else {
                venuesList = (ArrayList<FoursquareVenue>) parseFoursquare(temp);
                List<String> listTitle = new ArrayList<String>();
                 for (int i = 0; i < venuesList.size(); i++) {
                    listTitle.add(i, venuesList.get(i).getName() + ", " + venuesList.get(i).getCategory()
                            + "" + venuesList.get(i).getCheckins());
                    txtfour.setText(venuesList.get(i).getCheckins());
                    Log.d("name",venuesList.get(i).getName());
                }
            }
        }
    }

    public static
    String makeCall(String url) {
        StringBuffer buffer_string = new StringBuffer(url);
        Log.d("4Url", url);
        String replyString = "";
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(buffer_string.toString());

        try {
            HttpResponse response = httpclient.execute(httpget);
            InputStream is = response.getEntity().getContent();
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayBuffer baf = new ByteArrayBuffer(20);
            int current = 0;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }
            replyString = new String(baf.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return replyString.trim();
    }

    private static
    ArrayList<FoursquareVenue> parseFoursquare(final String response) {
        ArrayList<FoursquareVenue> temp = new ArrayList<FoursquareVenue>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("response")) {
                if (jsonObject.getJSONObject("response").has("venues")) {
                    JSONArray jsonArray = jsonObject.getJSONObject("response").getJSONArray("venues");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        FoursquareVenue poi = new FoursquareVenue();

                        if (jsonArray.getJSONObject(i).has("name")) {
                            poi.setName(jsonArray.getJSONObject(i).getString("name"));

                            if (jsonArray.getJSONObject(i).has("stats")) {
                                poi.setCheckins(jsonArray.getJSONObject(i).getJSONObject("stats")
                                        .getString("checkinsCount"));

                                if (jsonArray.getJSONObject(i).has("location")) {
                                    if (jsonArray.getJSONObject(i).getJSONObject("location").has("address")) {
                                        if (jsonArray.getJSONObject(i).getJSONObject("location").has("city")) {
                                            poi.setCity(jsonArray.getJSONObject(i).getJSONObject("location").getString("city"));
                                        }

                                        if (jsonArray.getJSONObject(i).has("categories")) {
                                            if (jsonArray.getJSONObject(i).getJSONArray("categories").length() > 0) {
                                                if (jsonArray.getJSONObject(i).getJSONArray("categories").getJSONObject(0)
                                                        .has("icon")) {
                                                    poi.setCategory(jsonArray.getJSONObject(i).getJSONArray("categories")
                                                            .getJSONObject(0).getString("name"));
                                                }
                                            }
                                        }

                                        temp.add(poi);

                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<FoursquareVenue>();
        }
        return temp;
    }
}
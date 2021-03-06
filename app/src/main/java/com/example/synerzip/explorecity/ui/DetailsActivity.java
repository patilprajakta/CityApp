package com.example.synerzip.explorecity.ui;

import android.content.Intent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.synerzip.explorecity.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.synerzip.explorecity.R.id.map;

/**
 * Created by Prajakta Patil on 4/1/17.
 * Copyright © 2017 Synerzip. All rights reserved
 */
public class DetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    @BindView(R.id.toolBar)
    Toolbar toolbar;

    @BindView(R.id.CollapsingToolbarLayout)
    CollapsingToolbarLayout collapsingToolbarLayout;

    Double latitude, longitude;
    GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        toolbar.setNavigationIcon(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        collapsingToolbarLayout.setTitle(getString(R.string.hotel_name));

        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        fm.getMapAsync(this);

        Intent intent = getIntent();
        latitude = intent.getDoubleExtra(getString(R.string.latitude), 0);
        longitude = intent.getDoubleExtra(getString(R.string.longitude), 0);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng point = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(point));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
    }
}
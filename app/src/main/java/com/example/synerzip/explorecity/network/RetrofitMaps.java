package com.example.synerzip.explorecity.network;

import com.example.synerzip.explorecity.models.FoursquareJSON;
import com.example.synerzip.explorecity.models.GooglePredictions;
import com.example.synerzip.explorecity.models.GoogleResponse;
import com.example.synerzip.explorecity.models.ZomatoJSON;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by Prajakta Patil on 10/2/17.
 * Copyright © 2017 Synerzip. All rights reserved
 */

public interface RetrofitMaps {

    /**
     * google autocomplete for cities
     *
     * @param input
     * @param radius
     * @param key
     * @return
     */
    @GET(AppConstants.GOOGLE_ENDPOINT)
    Call<GoogleResponse> getCityResults(@Query("input") String input,
                                           @Query("radius") Integer radius,
                                           @Query("key") String key);

    /**
     * google autocomplete for places
     *
     * @param input
     * @param radius
     * @param key
     * @return
     */
    @GET(AppConstants.GOOGLE_ENDPOINT)
    Call<GoogleResponse> getPlaceResults(@Query("input") String input,
                                         @Query("radius") Integer radius,
                                         @Query("key") String key);

    /**
     * foursquare api call
     *
     * @param clientID
     * @param clientSecret
     * @param ll
     * @param llAcc
     * @return
     */
    @GET(AppConstants.FOURSQUARE_ENDPOINT)
    Call<FoursquareJSON> fourSquarePlaces(@Query("client_id") String clientID,
                                          @Query("client_secret") String clientSecret,
                                          @Query("ll") String ll,
                                          @Query("llAcc") double llAcc);

    @GET(AppConstants.ZOMATO_ENDPOINT)
    Call<ZomatoJSON> zomatoDetails(@Query("user-key") String clientID,
                                      @Query("lat") String ll,
                                      @Query("lon") double llAcc);
}
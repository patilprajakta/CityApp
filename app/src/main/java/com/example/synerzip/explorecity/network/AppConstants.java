package com.example.synerzip.explorecity.network;

/**
 * Created by Prajakta Patil on 13/2/17.
 * Copyright © 2017 Synerzip. All rights reserved
 */

public class AppConstants {

    public static final String GOOGLE_PLACES_API_KEY = "AIzaSyDLuFX3GsD9UGhM60SG5E9zwvib2XcX5OE";

    public static final String FOURSQUARE_CLIENT_ID = "VVPFPMTNSXBZGIJXUNHVIWU5T5QRMVC0JBLZ1BXJE2LGWJRE";

    public static final String FOURSQUARE_CLIENT_SECRET = "5FRCH0GERDOWRDZ3OFJQEWDWN1I4VO4FAWE0OECIMO3SOUPT";

    public static final String ZOMATO_USER_KEY = "90b35b156ac6d4b73ccb840ced49ab3e";

    public static final String FOURSQUARE_BASE_URL = "https://api.foursquare.com/v2/";

    public static final String GOOGLE_BASE_URL = "https://maps.googleapis.com";

    //'v' parameter is a date that essentially represents the "version" of the API you expect from Foursquare
    //this date shows that application is prepared for API changes up to this date
    public static final String FOURSQUARE_ENDPOINT = "venues/search?v=20161101&limit=50";

    public static final String GOOGLE_ENDPOINT = "/maps/api/place/autocomplete/json?&types=(cities)";

    public static final String ZOMATO_BASE_URL="https://api.zomato.com/v2.1/";

    public static final String ZOMATO_ENDPOINT=
            "search.json?&count=50&radius=100000";

}

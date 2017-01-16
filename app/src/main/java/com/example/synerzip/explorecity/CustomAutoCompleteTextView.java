package com.example.synerzip.explorecity;

import java.util.HashMap;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

import com.google.android.gms.location.places.AutocompleteFilter;

/**
 * Created by Prajakta Patil on 4/1/17.
 */

public class CustomAutoCompleteTextView extends AutoCompleteTextView {

    public CustomAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    /** Returns the Place Description corresponding to the selected item */
    @Override
    protected CharSequence convertSelectionToString(Object selectedItem) {
        /** Each item in the autocompetetextview suggestion list is a hashmap object */

        AutocompleteFilter filter =
                new AutocompleteFilter.Builder().setCountry("India").build();
        HashMap<String, String> hm = (HashMap<String, String>) selectedItem;

        return hm.get("description");

    }

}
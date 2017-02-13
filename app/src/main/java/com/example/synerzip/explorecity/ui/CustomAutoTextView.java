package com.example.synerzip.explorecity.ui;

import java.util.HashMap;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

import com.example.synerzip.explorecity.R;
import com.google.android.gms.location.places.AutocompleteFilter;

/**
 * Created by Prajakta Patil on 4/1/17.
 */

public class CustomAutoTextView extends AutoCompleteTextView {

    public CustomAutoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected CharSequence convertSelectionToString(Object selectedItem) {

        AutocompleteFilter filter = new AutocompleteFilter.Builder().
                setCountry(getResources().getString(R.string.india)).build();

        HashMap<String, String> hm = (HashMap<String, String>) selectedItem;
        return hm.get(getResources().getString(R.string.description));
    }

}
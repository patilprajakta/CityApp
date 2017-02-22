package com.example.synerzip.explorecity.models;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Prajakta Patil on 16/2/17.
 * Copyright © 2017 Synerzip. All rights reserved
 */

@Getter
@Setter
public class GoogleStructuredFormatting {
    private String main_text;
    private List<GoogleMainTxtMatchSubstr> main_text_matched_substrings;
    private String secondary_text;
}

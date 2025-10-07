package com.mubashshir.blogify;

import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

public class Utility {

    // Convert timestamp to a formatted date string
    static String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        return DateFormat.format("dd-MM-yyyy", cal).toString();
    }

    // Get the current date as a formatted string
    static String getCurrentDate() {
        return getDate(System.currentTimeMillis());
    }
}

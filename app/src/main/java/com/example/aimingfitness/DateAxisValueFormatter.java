package com.example.aimingfitness;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateAxisValueFormatter extends ValueFormatter {
    private final SimpleDateFormat mFormat;

    public DateAxisValueFormatter() {
        // Define the desired date format
        mFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
    }

    @Override
    public String getFormattedValue(float value) {
        // Convert timestamp to date
        return mFormat.format(new Date((long) value));
    }
}

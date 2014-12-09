package com.example.rawand.raceme;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;


import java.util.List;

public class SettingsActivity extends BaseActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_settings, frameLayout);
        drawerList.setItemChecked(position, true);
        setTitle(listArray[position]);

        Spinner dropdown = (Spinner)findViewById(R.id.units_picker);
        String[] items = new String[]{"Meters", "Miles"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
        dropdown.setAdapter(adapter);

        Switch testModeButton = (Switch) findViewById(R.id.testmode_switch);
        testModeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SaveSharedPreference.toggleTestMode();

               //SaveSharedPreference.isTestMode().toString();
            }
        });
        Spinner unitPicker = (Spinner) findViewById(R.id.units_picker);
        unitPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedUnit = parentView.getItemAtPosition(position).toString();
                if( selectedUnit.length() > 0){
                    SaveSharedPreference.setUnits( selectedUnit);
                }



            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }

        });


    }



}


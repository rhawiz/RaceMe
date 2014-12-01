package com.example.rawand.raceme;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Set;


public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_main, frameLayout);
        drawerList.setItemChecked(position, true);
        setTitle(listArray[position]);

        TextView text = (TextView) findViewById(R.id.main_text_view);

        if(SaveSharedPreference.getUserDetails(this) != null) {
            text.setText(SaveSharedPreference.getUserDetails(this).toString());
        }
    }


}

package com.example.rawand.raceme;

import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;


public class RaceActivity extends BaseActivity {
    private TabHost tabHost;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_race, frameLayout);
        drawerList.setItemChecked(position, true);
        setTitle(listArray[position]);

        initTabs();

    }

    private void initTabs() {


        tabHost = (TabHost) findViewById(R.id.race_tabhost);
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("details");
        tabSpec.setContent(R.id.race_details_tab);
        tabSpec.setIndicator("Details");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("map");
        tabSpec.setContent(R.id.race_map_tab);
        tabSpec.setIndicator("Map");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("navigation");
        tabSpec.setContent(R.id.race_navigation_tab);
        tabSpec.setIndicator("Navigate");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("timing");
        tabSpec.setContent(R.id.race_timing_tab);
        tabSpec.setIndicator("Timings");
        tabHost.addTab(tabSpec);

        tabHost.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {
            TransitionAnimations transition = new TransitionAnimations();

            public void onSwipeLeft() {
                int nextTab = tabHost.getCurrentTab() + 1;
                if (nextTab > 3) nextTab = 0;

                tabHost.setCurrentTab(nextTab);
                tabHost.getCurrentView().setAnimation(transition.inFromRightAnimation());
            }

            public void onSwipeRight() {
                int nextTab = tabHost.getCurrentTab() - 1;
                if (nextTab < 0) nextTab = 3;

                tabHost.setCurrentTab(nextTab);
                tabHost.getCurrentView().setAnimation(transition.outToRightAnimation());
            }
        });


    }

}

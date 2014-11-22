package com.example.rawand.raceme;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;


public class ProfileActivity extends BaseActivity {
    private TabHost tabHost;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_profile, frameLayout);
        drawerList.setItemChecked(position, true);
        setTitle(listArray[position]);

        initTabs();
    }

    /**
     * Initialise tabs
     */
    private void initTabs(){


        tabHost = (TabHost) findViewById(R.id.profile_tabhost);
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("my_profile");
        tabSpec.setContent(R.id.profile_my_profile_tab);
        tabSpec.setIndicator("My Profile");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("edit_profile");
        tabSpec.setContent(R.id.profile_edit_profile_tab);
        tabSpec.setIndicator("Edit Profile");
        tabHost.addTab(tabSpec);


        tabHost.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()){
            TransitionAnimations transition = new TransitionAnimations();
            public void onSwipeLeft() {
                int nextTab = tabHost.getCurrentTab() + 1;
                if(nextTab > 1)nextTab = 0;

                tabHost.setCurrentTab(nextTab);
                tabHost.getCurrentView().setAnimation(transition.inFromRightAnimation());
            }
            public void onSwipeRight() {
                int nextTab = tabHost.getCurrentTab() - 1;
                if(nextTab < 0)nextTab = 1;

                tabHost.setCurrentTab(nextTab);
                tabHost.getCurrentView().setAnimation(transition.outToRightAnimation());
            }
        });



    }

}

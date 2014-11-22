package com.example.rawand.raceme;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;
import android.view.GestureDetector.SimpleOnGestureListener;


public class ChallengesActivity extends BaseActivity {
    private TabHost tabHost;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_challenges, frameLayout);
        drawerList.setItemChecked(position, true);
        setTitle(listArray[position]);

        initTabs();

    }


    /**
     * Initialise tabs
     */
    private void initTabs(){


        tabHost = (TabHost) findViewById(R.id.challenges_tabhost);
        tabHost.setup();

        TabSpec tabSpec = tabHost.newTabSpec("friends");
        tabSpec.setContent(R.id.challenges_friend_tab);
        tabSpec.setIndicator("Friends");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("leaderboard");
        tabSpec.setContent(R.id.challenges_leaderboard_tab);
        tabSpec.setIndicator("Leaderboard");
        tabHost.addTab(tabSpec);

        tabSpec  = tabHost.newTabSpec("achievement");
        tabSpec.setContent(R.id.challenges_achievement_tab);
        tabSpec.setIndicator("Achievements");
        tabHost.addTab(tabSpec);

        tabHost.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()){
            TransitionAnimations transition = new TransitionAnimations();
            public void onSwipeLeft() {
                int nextTab = tabHost.getCurrentTab() + 1;
                if(nextTab > 2)nextTab = 0;

                tabHost.setCurrentTab(nextTab);
                tabHost.getCurrentView().setAnimation(transition.inFromRightAnimation());
            }
            public void onSwipeRight() {
                int nextTab = tabHost.getCurrentTab() - 1;
                if(nextTab < 0)nextTab = 2;

                tabHost.setCurrentTab(nextTab);
                tabHost.getCurrentView().setAnimation(transition.outToRightAnimation());
            }
        });



    }

}

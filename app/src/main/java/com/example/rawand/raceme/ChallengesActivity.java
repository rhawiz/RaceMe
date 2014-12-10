package com.example.rawand.raceme;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;
import android.view.GestureDetector.SimpleOnGestureListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 */

public class ChallengesActivity extends BaseActivity {
    private ArrayList<ImageView> challengesIconArray;
    private ArrayList<String> challengesArrayList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_challenges, frameLayout);
        drawerList.setItemChecked(position, true);
        setTitle(listArray[position]);



        String userId = SaveSharedPreference.getUserDetails(this).getUserId();

        challengesIconArray = new ArrayList<ImageView>();
        challengesIconArray.add((ImageView) findViewById(R.id.completefirst_imageview));
        challengesIconArray.add((ImageView) findViewById(R.id.complete10mile_imageview));
        challengesIconArray.add((ImageView) findViewById(R.id.challengefriend_imageview));
        challengesIconArray.add((ImageView) findViewById(R.id.completefriend_imageview));
        challengesIconArray.add((ImageView) findViewById(R.id.rain_imageview));
        challengesIconArray.add((ImageView) findViewById(R.id.sun_imageview));
        challengesIconArray.add((ImageView) findViewById(R.id.total5miles_imageview));
        challengesIconArray.add((ImageView) findViewById(R.id.total10miles_imageview));
        challengesIconArray.add((ImageView) findViewById(R.id.complete5routes_imageview));
        challengesIconArray.add((ImageView) findViewById(R.id.complete10routes_imageview));
        challengesIconArray.add((ImageView) findViewById(R.id.complete5mile_imageview));
        challengesIconArray.add((ImageView) findViewById(R.id.complete10mile_imageview));
        challengesIconArray.add((ImageView) findViewById(R.id.beatowntime_imageview));

        // Save the new profile data
        GetDataFromDBTask getDataFromDBTask = new GetDataFromDBTask(userId );
        // Execute the Save in an Async Task
        getDataFromDBTask.execute();

    }


    /*
   ** Async Task that will save the data to the database
   */
    public class GetDataFromDBTask extends AsyncTask<Void, Void, Boolean> {
        private String id;
        private String email;
        private String firstname;
        private String surname;
        private String profileImg;
        private String gender;

        public GetDataFromDBTask(String userId
        ) {
            this.id = userId;

        }

        @Override
        protected Boolean doInBackground(Void... params) {

            challengesArrayList =  DatabaseHelper.getUserAcheivement(id);

            if(challengesArrayList == null){
                return false;
            }
            return true;

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            // Change all the acheivements

            if(success){
                for (String s : challengesArrayList){
                    //ImageView imgview = (ImageView) findViewById(s);
                    //challengesArrayList.contains();
                    for(ImageView i : challengesIconArray){
                        if(s.equals(i.getTag().toString())){
                            i.setImageResource(getResources().getIdentifier(s,"drawable",getPackageName()));
                            break;
                        }
                    }

                }
            }else{
                Utilities.showToast("Could retrieve challenges.", Toast.LENGTH_LONG,ChallengesActivity.this);
            }

        }

        @Override
        protected void onCancelled() {

        }


    }

    @Override
    protected void onStop()
    {

        try {
            unregisterReceiver(networkReceiver);
        }
        catch(IllegalArgumentException e){
            e.printStackTrace();
        }
        super.onStop();
    }





}

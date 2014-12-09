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


public class ChallengesActivity extends BaseActivity {
    private static ArrayList<String> challengesArrayList = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_challenges, frameLayout);
        drawerList.setItemChecked(position, true);
        setTitle(listArray[position]);

        String userId = SaveSharedPreference.getUserDetails(this).getUserId();
        // Save the new profile data
        //GetDataFromDBTask getDataFromDBTask = new GetDataFromDBTask(userId );
        // Execute the Save in an Async Task
        //getDataFromDBTask.execute();

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

            challengesArrayList =  DatabaseHelper.getUserAcheivement(id );


            return true;

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            // Change all the acheivements

            Boolean found = false;
            String imageSource = "";

            for (String s : challengesArrayList){
                //ImageView imgview = (ImageView) findViewById(s);
                //challengesArrayList.contains();
                Toast.makeText(getApplicationContext(),s , Toast.LENGTH_SHORT).show();

            }

            if( found)
            {

            }else
            {
                imageSource += "_bw";
            }

        }

        @Override
        protected void onCancelled() {

        }
    }




}

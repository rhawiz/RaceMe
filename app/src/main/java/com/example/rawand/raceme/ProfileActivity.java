package com.example.rawand.raceme;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;


public class ProfileActivity extends BaseActivity {
    private TabHost tabHost;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_profile, frameLayout);
        drawerList.setItemChecked(position, true);
        setTitle(listArray[position]);

        initTabs();

        // Add a click listener to the save button
        Button button = (Button) findViewById(R.id.save_button);
        button.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                saveData();
            }
        });

        // Get the username from the shared preference and populate the name field
        TextView firstNameText = (TextView) findViewById(R.id.edit_name);
        firstNameText.setText( SaveSharedPreference.getUserDetails(this).getFirstname());
        // Populate the Email field
        TextView emailText = (TextView) findViewById(R.id.edit_email);
        emailText.setText( SaveSharedPreference.getUserDetails(this).getEmail() );
        // Get the gender
        String gender = SaveSharedPreference.getUserDetails(this).getGender();
        // Need some logic to determin gender

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

        tabSpec = tabHost.newTabSpec("My Profile");
        tabSpec.setContent(R.id.profile_edit_profile_tab);
        tabSpec.setIndicator("Challenges");
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
    /*
    ** Save the new profile data
     */
    protected void saveData( ){
        SaveDatatoDBTask task = new SaveDatatoDBTask("21","aaa","bb","cc","dd","ee");
        task.execute();
        // Save the new profile data
    }

    public class SaveDatatoDBTask extends AsyncTask<Void, Void, Boolean> {
        private String id;
        private String email;
        private String firstname;
        private String surname;
        private String profileImg;
        private String gender;

        public SaveDatatoDBTask(String userId,
                                String userEmail,
                                String firstname,
                                String surname,
                                String profileImg,
                                String gender) {
            this.id = userId;
            this.email = userEmail;
            this.firstname = firstname;
            this.surname = surname;
            this.profileImg = profileImg;
            this.gender = gender;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            if(DatabaseHelper.updateUser(id,
                    email,
                    firstname,
                    surname,
                    profileImg,
                    gender))
                return true;

            return false;


        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success) {
                Toast.makeText(getApplicationContext(),
                        "Changes saved!", Toast.LENGTH_SHORT)
                        .show();
            }else{
                Toast.makeText(getApplicationContext(),
                        "Changes not saved, have you got network?", Toast.LENGTH_SHORT)
                        .show();
            }
        }

        @Override
        protected void onCancelled() {

        }


    }

}

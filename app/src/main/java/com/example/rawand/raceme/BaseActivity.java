package com.example.rawand.raceme;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;


/**
 * Main activity containing shared layouts i.e settings and navigation drawer.
 * Each activity will extend this class instead of the Activity class.
 */

public class BaseActivity extends Activity {

    protected FrameLayout frameLayout;
    protected ListView drawerList;
    protected String[] listArray;
    protected static int position;
    private static boolean isLaunch = true;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    public NetworkChangeReceiver networkReceiver;
    private IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        //Setup Navigation Drawer

        frameLayout = (FrameLayout)findViewById(R.id.content_frame);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        listArray = getResources().getStringArray(R.array.menu_items);
        drawerList = (ListView) findViewById(R.id.left_drawer);

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, listArray));


        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                openActivity(position);
            }
        });

        //If the user is not set (i.e. not logged in), call the logout method
        if(SaveSharedPreference.getUserDetails(this) == null){
            logout();
        }
        else if(SaveSharedPreference.getUserDetails(this).isEmpty()){
            logout();
        }

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);


        // ActionBarDrawerToggle ties together the the proper interactions between the sliding drawer and the action bar app icon
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,						/* host Activity */
                drawerLayout, 				/* DrawerLayout object */
                R.drawable.ic_drawer,     /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,       /* "open drawer" description for accessibility */
                R.string.drawer_close)      /* "close drawer" description for accessibility */
        {
            @Override
            public void onDrawerClosed(View drawerView) {
                getActionBar().setTitle(listArray[position]);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(getString(R.string.app_name));
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
            }
        };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);


        //Check if we need to upload any local session data
        new UploadLocalSessionDataTask(this).execute();

        //Register a network change receiver to perform any actions needed when there is a change in network

        networkReceiver = new NetworkChangeReceiver(this);
        intentFilter = new IntentFilter();
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");



        try{
            registerReceiver(networkReceiver, intentFilter);
        }catch(IllegalArgumentException e){
            e.printStackTrace();

        }


        if(isLaunch){
            isLaunch = false;
            openActivity(0);

        }
    }



    @Override
    protected void onResume() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");

        networkReceiver = new NetworkChangeReceiver(this);
        registerReceiver(networkReceiver, intentFilter);
        super.onResume();
    }


    @Override
    protected void onPause() {
        unregisterReceiver(networkReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        try {
            unregisterReceiver(networkReceiver);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }


    /**
     * @param position
     *
     * Launching activity when any list item is clicked.
     */
    protected void openActivity(int position) {

        drawerList.setItemChecked(position, true);
        setTitle(listArray[position]);
        drawerLayout.closeDrawer(drawerList);
        BaseActivity.position = position;

        switch (position) {
            case 0:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case 1:
                startActivity(new Intent(this, RaceActivity.class));
                break;
            case 2:
                startActivity(new Intent(this, ChallengesActivity.class));
                break;
            case 3:
                startActivity(new Intent(this, ProfileActivity.class));
                break;
            case 4:
                startActivity(new Intent(this, FriendsActivity.class));
                break;
            case 5:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case 6:
                logout();
                break;

            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_settings:
                openActivity(5);
                return true;
            case R.id.action_logout:
                logout();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    /* We can override onBackPressed method to toggle navigation drawer
    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(drawerList)){
            drawerLayout.closeDrawer(drawerList);
        }else {
            drawerLayout.openDrawer(drawerList);
        }
    }*/


    /**
     * Perform actions when user logs out.
     */
    private void logout(){

        drawerList.setItemChecked(0, true);
        setTitle(listArray[0]);
        drawerLayout.closeDrawer(drawerList);
        BaseActivity.position = 0;
        //this.unregisterReceiver(networkReceiver);
        SaveSharedPreference.setUserDetails(getApplicationContext(), null);
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }


    /**
     * Network change broadcast reciever.
     * Here we define what happens if the network state changes while the application is running.
     */
    public static class NetworkChangeReceiver extends BroadcastReceiver {
        Activity activity;

        public NetworkChangeReceiver(){
        }

        public NetworkChangeReceiver(Activity activity){
            this.activity = activity;
        }

        @Override
        public void onReceive(Context context,Intent intent) {

            //Get network status
            int status = NetworkUtil.getConnectivityStatusString(context);
            ConnectivityManager manager = (ConnectivityManager) activity.getSystemService(CONNECTIVITY_SERVICE);
            //Check if we have 3G.
            boolean is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
            //Check if we have WiFi.
            boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

            //If we have a network connection, attempt to load locally stored sessions.
            if (!"android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                if(status!=NetworkUtil.NETWORK_STATUS_NOT_CONNECTED && (is3g || isWifi)){
                    if(RaceUtils.getLocalRaceSessions(activity).size() > 0) {
                        UploadLocalSessionDataTask uploadTask = new UploadLocalSessionDataTask(activity);
                        uploadTask.execute();
                    }
                }

            }
        }
    }


    /**
     * Threaded task to perform upload of locally stored race sessions.
     * Will only get called if a network is found during creation of the application or during a network change event.
     */
    public static class UploadLocalSessionDataTask extends AsyncTask<Void, Void, Boolean> {
        Activity activity;


        public UploadLocalSessionDataTask(){
            super();
        }

        public UploadLocalSessionDataTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

                return Utilities.uploadLocalSessions(activity);


        }

        @Override
        protected void onPostExecute(final Boolean success) {


        }

        @Override
        protected void onCancelled() {

        }


    }



}


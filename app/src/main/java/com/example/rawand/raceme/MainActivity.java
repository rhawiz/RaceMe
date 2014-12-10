package com.example.rawand.raceme;

import android.content.Context;
//import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.view.View.OnClickListener;
import android.location.Location;
import android.location.LocationManager;

import org.json.JSONException;

/**
 * Activity class for the home page.
 */
public class MainActivity extends BaseActivity {

    private Button exerciseButton;
    private Button profileButton;
    private Button challButton;

    private TextView cityText;
    //private TextView condDesc;
    private TextView temp;
    private String iconImg;
    private Button refreshButton;
    private TextView welcomeMsg;

    private ImageView imgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_main, frameLayout);
        drawerList.setItemChecked(position, true);
        setTitle(listArray[position]);

        exerciseButton = (Button) findViewById(R.id.exerciseNowButton);
        profileButton = (Button) findViewById(R.id.profileButton);
        challButton = (Button) findViewById(R.id.challengesButton);
        refreshButton = (Button) findViewById(R.id.refreshWeather);
        welcomeMsg = (TextView) findViewById(R.id.welMsg);


        User userDetails = SaveSharedPreference.getUserDetails(this);

        if(userDetails == null){//displaying welcome message
            finish();
        }else{
            welcomeMsg.setText("Hi,\n" + SaveSharedPreference.getUserDetails(this).getFirstname());
        }

        if(userDetails.getProfileImg() != null || userDetails.getProfileImg() != ""){//setting profile button

            if( userDetails.getProfileImg().length() > 1){
                // Turn the image string into a bitmap image
                Bitmap imageBitmap = Utilities.decodeBase64( userDetails.getProfileImg());
                // Set the image view as the iamge
                profileButton.setBackground(new BitmapDrawable(imageBitmap));
                profileButton.setText("Profile");
            }
        }

        //setting listeners for all buttons
        exerciseButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                openActivity(1);

            }
        });

        profileButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                openActivity(3);

            }
        });

        challButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                openActivity(2);

            }
        });

        refreshButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                getWeather();

            }
        });

        getWeather();

    }

    protected void getWeather() {//getting weather

        cityText = (TextView) findViewById(R.id.cityText);
        //condDesc = (TextView) findViewById(R.id.condDesc);
        temp = (TextView) findViewById(R.id.temp);
        imgView = (ImageView) findViewById(R.id.condIcon);
        refreshButton = (Button) findViewById(R.id.refreshWeather);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        //checking for network if true, hide button and call weather functions
        if (networkInfo != null && networkInfo.isConnected()) {

            refreshButton.setVisibility(View.INVISIBLE);

            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if(location == null){
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }


            String lat;
            String lon;

            if(location != null){
                lat = Double.toString(location.getLatitude());
                lon = Double.toString(location.getLongitude());
            }else{
                lat = "52.7659373";
                lon = "-1.2290387";
            }



            JSONWeatherTask task = new JSONWeatherTask();
            task.execute(new String[]{lat,lon});

        } else {

            Toast.makeText(getApplicationContext(), "No Network Connection!", Toast.LENGTH_LONG).show();
        }
    }

    //used to get weather in background
    private class JSONWeatherTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... params) {

            Weather weather = null;

            String data = ( (new WeatherHttpClient()).getWeatherData(params[0], params[1]));

            try {
                weather = JSONWeatherParser.getWeather(data);

            } catch (JSONException e) {
                e.printStackTrace();
            } catch(NullPointerException e){
                e.printStackTrace();
            }
            return weather;

        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);

            //show weather with description icon and temp
            if (weather != null) {

                cityText.setText(weather.location.getCity() + "," + weather.location.getCountry());
                //condDesc.setText(weather.currentCondition.getCondition() + "(" + weather.currentCondition.getDescr() + ")");
                temp.setText("" + Math.round((weather.temperature.getTemp() - 273.15)) + "°C");

                iconImg = "icon_" + weather.currentCondition.getIcon();

                imgView.setImageResource(getResources().getIdentifier(iconImg, "drawable", getPackageName()));

            } else {

                refreshButton.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "Unable to Retrieve Weather!", Toast.LENGTH_LONG).show();

            }
        }
    }

}

package com.example.rawand.raceme;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.view.View.OnClickListener;

import org.json.JSONException;


public class MainActivity extends BaseActivity {

    private Button exerciseButton;
    private Button profileButton;
    private Button challButton;

    private TextView cityText;
    //private TextView condDesc;
    private TextView temp;
    private String iconImg;
    private Button refreshButton;

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

        exerciseButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

               // Intent intent = new Intent(v.getContext(), RaceActivity.class);
               // startActivity(intent);
                openActivity(1);

            }
        });

        profileButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                //Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                //startActivity(intent);
                openActivity(3);

            }
        });

        challButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                //Intent intent = new Intent(v.getContext(), ChallengesActivity.class);
                //startActivity(intent);
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

    protected void getWeather() {

        String city = "Las Vegas";

        cityText = (TextView) findViewById(R.id.cityText);
        //condDesc = (TextView) findViewById(R.id.condDesc);
        temp = (TextView) findViewById(R.id.temp);
        imgView = (ImageView) findViewById(R.id.condIcon);
        refreshButton = (Button) findViewById(R.id.refreshWeather);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {

            refreshButton.setVisibility(View.INVISIBLE);

            JSONWeatherTask task = new JSONWeatherTask();
            task.execute(new String[]{city});

        } else {

            Toast.makeText(getApplicationContext(), "No Network Connection!", Toast.LENGTH_LONG).show();
        }
    }

    private class JSONWeatherTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... params) {
            Weather weather = new Weather();
            String data = ( (new WeatherHttpClient()).getWeatherData(params[0]));

            try {
                weather = JSONWeatherParser.getWeather(data);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return weather;

        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);

            cityText.setText(weather.location.getCity() + "," + weather.location.getCountry());
            //condDesc.setText(weather.currentCondition.getCondition() + "(" + weather.currentCondition.getDescr() + ")");
            temp.setText("" + Math.round((weather.temperature.getTemp() - 273.15)) + "°C");

            iconImg = "icon_" + weather.currentCondition.getIcon();

            imgView.setImageResource(getResources().getIdentifier(iconImg, "drawable", getPackageName()));
        }

    }

}

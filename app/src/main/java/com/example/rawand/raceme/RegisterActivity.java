package com.example.rawand.raceme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class RegisterActivity extends Activity {
    private EditText firstnameView;
    private EditText surnameView;
    private EditText emailView;
    private EditText passwordView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firstnameView = (EditText) findViewById(R.id.firstname);
        surnameView = (EditText) findViewById(R.id.surname);
        emailView = (EditText) findViewById(R.id.email);
        passwordView = (EditText) findViewById(R.id.password);

        Button registerButton = (Button) findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void attemptRegister(){
        String firstname = firstnameView.getText().toString();
        String surname = surnameView.getText().toString();
        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();
        Boolean valid = true;

        if(!isNameValid(firstname)){
            firstnameView.setError(getString(R.string.error_invalid_name));
            valid = false;
        }
        if(!isNameValid(surname)){
            surnameView.setError(getString(R.string.error_invalid_name));
            valid = false;
        }
        if(!isEmailValid(email)){
            emailView.setError(getString(R.string.error_invalid_email));
            valid = false;
        }
        if(!isPasswordValid(password)){
            passwordView.setError(getString(R.string.error_invalid_password));
            valid = false;
        }

        if(valid && isEmailUnique(email)){
            LoginActivity.addUser(email+":"+password);

            SaveSharedPreference.setUserName(getApplicationContext(),email);
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            Toast.makeText(getApplicationContext(),email, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private boolean isEmailUnique(String email){
        //TODO:Replace this logic once database has been setup
        String[] credentials = LoginActivity.getCredentials();
        for (String credential : credentials) {
            String[] pieces = credential.split(":");

            if (pieces[0].equals(email)) {
                // Account exists, return false.

                return false;
            }
        }
        return true;
    }

    private boolean isNameValid(String name){
        //TODO: Create validation logic
        return true;
    }

    private boolean isEmailValid(String email){
        //TODO: Create validation logic

        return email.contains("@");
    }

    private boolean isPasswordValid(String password){
        //TODO: Create validation logic
        return true;
    }
}

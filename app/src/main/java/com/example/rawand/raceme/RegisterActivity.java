package com.example.rawand.raceme;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.sql.SQLException;


/**
 * Register screen.
 * Enables user to register as a new user.
 * Update database table to add new user.
 */

public class RegisterActivity extends Activity {
    private EditText firstnameView;
    private EditText surnameView;
    private EditText emailView;
    private EditText usernameView;
    private EditText passwordView;
    private RadioGroup radioGroup;
    private RadioButton genderRadio;
    private UserRegisterTask registerTask;
    private View registerFormView;
    private View progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);



        firstnameView = (EditText) findViewById(R.id.firstname);
        surnameView = (EditText) findViewById(R.id.surname);
        emailView = (EditText) findViewById(R.id.email);
        usernameView = (EditText) findViewById(R.id.username);
        passwordView = (EditText) findViewById(R.id.password);
        radioGroup = (RadioGroup) findViewById(R.id.gender_group);
        registerFormView = findViewById(R.id.register_form);
        progressView = findViewById(R.id.register_progress);

        genderRadio =  (RadioButton) findViewById(radioGroup.getCheckedRadioButtonId());

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


    /**
     * Attempt to register the user.
     */
    private void attemptRegister(){


        String firstname = firstnameView.getText().toString();
        String surname = surnameView.getText().toString();
        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();
        String username = usernameView.getText().toString();
        String gender = genderRadio.getText().toString();
        if(gender.equals("Male"))
            gender = "m";
        else gender = "f";


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

        //TODO: check whether username and email already exist.

        if(valid){
            showProgress(true);
            registerTask = new UserRegisterTask(firstname,surname,email,username,password,gender);
            registerTask.execute((Void) null);


        }
    }

    /**
     * Represents an asynchronous registration task used to authenticate
     * the user.
     */
    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String firstname;
        private final String surname;
        private final String email;
        private final String username;
        private final String password;
        private final String gender;
        private DatabaseConnection dbConnection = null;


        public UserRegisterTask(String firstname, String surname, String email, String username, String password, String gender) {
            this.firstname = firstname;
            this.surname = surname;
            this.email = email;
            this.username = username;
            this.password = password;
            this.gender = gender;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if(!RegisterUtils.isUsernameUnique(username)){

                usernameView.post(new Runnable() {
                    @Override
                    public void run() {
                        usernameView.setError(getString((R.string.error_username_taken)));
                    }
                });

                return false;
            }
            if(!RegisterUtils.isEmailUnique(email)){

                emailView.post(new Runnable() {
                    @Override
                    public void run() {
                        emailView.setError(getString((R.string.error_email_taken)));
                    }
                });
                return false;
            }

            return RegisterUtils.registerUser(firstname, surname, email, username, password, gender); //Attempted to create new user. If it fails, it will return false.
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            registerTask = null;
            showProgress(false);
            if (success) {
                //If login was successful set SharedPreference to store login credentials.
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
            registerTask = null;
        }


    }

    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(RegisterActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Check if the name is valid
     * @param name Input name
     * @return True if valid, false if invalid.
     */
    private boolean isNameValid(String name){
        //TODO: Create validation logic
        return true;
    }


    /**
     * Check if the email inputted is valid
     *
     * @param email Input email
     * @return True if valid, false if invalid.
     */
    private boolean isEmailValid(String email){
        //TODO: Create validation logic

        return email.contains("@");
    }

    /**
     * Check if the password inputted is valid
     *
     * @param password Input password
     * @return True if valid, false if invalid.
     */

    private boolean isPasswordValid(String password){
        //TODO: Create validation logic
        return password.length() > 4;
    }


    /**
     * Show or hide the progress animation to let the user know that something is happening in the background.
     * @param show True to show, false to hide.
     */
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            registerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            registerFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    registerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            registerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}

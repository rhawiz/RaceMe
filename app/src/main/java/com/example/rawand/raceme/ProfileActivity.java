package com.example.rawand.raceme;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Made by Ian Wong.
 */

public class ProfileActivity extends BaseActivity {
    private TabHost tabHost;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final int COMPRESS_QUALITY = 100;

    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "Raceme";

    private Uri fileUri; // file url to store image/video

    private ImageView imgPreview;
    private Button btnCapturePicture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_profile, frameLayout);
        drawerList.setItemChecked(position, true);
        setTitle(listArray[position]);

        initTabs();

        // Add a click listener to the save button
        Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });

        // Add click listener to Edit Profile Button
        Button editProfileButton = (Button) findViewById(R.id.edit_profile_button);
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableEditProfile();
            }
        });

        // Add click listener to Edit Profile Button
        Button cancelEditButton = (Button) findViewById(R.id.cancel_edit_button);
        cancelEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelEditProfile();
            }
        });

        // Populate all textviews with the user's data
        populateData();

        // For the camera
        imgPreview = (ImageView) findViewById(R.id.imgPreview);

        btnCapturePicture = (Button) findViewById(R.id.btnCapturePicture);
        btnCapturePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // capture the picture
                captureImage();
            }
        });

        // Check if the user's device supports a camera
        if (!isDeviceSupportCamera()) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support the camera",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Initialise tabs
     */
    private void initTabs() {


        tabHost = (TabHost) findViewById(R.id.profile_tabhost);
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("my_profile");
        tabSpec.setContent(R.id.profile_my_profile_tab);
        tabSpec.setIndicator("My Profile");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("My Profile");
        tabSpec.setContent(R.id.profile_challenges_tab);
        tabSpec.setIndicator("Challenges");
        tabHost.addTab(tabSpec);


        tabHost.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {
            TransitionAnimations transition = new TransitionAnimations();

            public void onSwipeLeft() {
                int nextTab = tabHost.getCurrentTab() + 1;
                if (nextTab > 1) nextTab = 0;

                tabHost.setCurrentTab(nextTab);
                tabHost.getCurrentView().setAnimation(transition.inFromRightAnimation());
            }

            public void onSwipeRight() {
                int nextTab = tabHost.getCurrentTab() - 1;
                if (nextTab < 0) nextTab = 1;

                tabHost.setCurrentTab(nextTab);
                tabHost.getCurrentView().setAnimation(transition.outToRightAnimation());
            }
        });
    }

    /*
    ** Make the Email Address not editable
   */
    protected void disableEmailEditText() {
        TextView emailText = (TextView) findViewById(R.id.edit_email);
        emailText.setKeyListener(null);
        // Set the color to transparent so it looks like a normal text view
        emailText.setBackgroundColor(Color.TRANSPARENT);
    }

    /*
     ** Populate all the text views with data from the user object
    */
    protected void populateData() {
        // Get the username from the shared preference and populate the name field
        TextView firstNameText = (TextView) findViewById(R.id.edit_name);
        firstNameText.setText(SaveSharedPreference.getUserDetails(this).getFirstname());
        // Get the surname and populate the surname field
        TextView surnameText = (TextView) findViewById(R.id.edit_surname);
        surnameText.setText(SaveSharedPreference.getUserDetails(this).getSurname());
        // Populate the Email field
        TextView emailText = (TextView) findViewById(R.id.edit_email);
        emailText.setText(SaveSharedPreference.getUserDetails(this).getEmail());
        // Get the gender
        String gender = SaveSharedPreference.getUserDetails(this).getGender();
        RadioButton maleRadioButton = (RadioButton) findViewById(R.id.male_radio);
        RadioButton femaleRadioButton = (RadioButton) findViewById(R.id.female_radio);
        // Disable the gender radio buttons so the user can't accidentally click it
        disableGenderButtons();
        // Make sure the correct Radio Button is checked
        if (gender.toLowerCase() == "m" || gender.toLowerCase() == "male") {
            maleRadioButton.setChecked(true);
            femaleRadioButton.setChecked(false);

        } else {
            maleRadioButton.setChecked(false);
            femaleRadioButton.setChecked(true);
        }

        // Populate the user's profile picture if they have one
        ImageView profileImageView = (ImageView) findViewById(R.id.imgPreview);
        String profileImageString = SaveSharedPreference.getUserDetails(this).getProfileImg();

        if( profileImageString.length() > 1){
            // Turn the image string into a bitmap image
            Bitmap imageBitmap = decodeBase64(profileImageString);
            // Set the image view as the iamge
            profileImageView.setImageBitmap(imageBitmap);
        }

    }

    /*
     ** Make the name not editable
    */
    protected void disableNameEditText() {
        TextView nameText = (TextView) findViewById(R.id.edit_name);
        nameText.setKeyListener(null);
        nameText.setBackgroundColor(Color.TRANSPARENT);
        // hide the surname edit text
        TextView surnameText = (TextView) findViewById(R.id.edit_surname);
        surnameText.setKeyListener(null);
        surnameText.setBackgroundColor(Color.TRANSPARENT);
    }

    /*
    ** Hide the save button
   */
    protected void hideSaveButton() {
        Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setVisibility(View.GONE);
    }

    /*
    ** Disable the gender buttons
   */
    protected void disableGenderButtons() {
        Button maleRadioButton = (Button) findViewById(R.id.male_radio);
        Button femaleRadioButton = (Button) findViewById(R.id.female_radio);
        maleRadioButton.setEnabled(false);
        femaleRadioButton.setEnabled(false);
    }

    /*
    ** Enable the gender radio buttons
   */
    protected void enableGenderButtons() {
        Button maleRadioButton = (Button) findViewById(R.id.male_radio);
        Button femaleRadioButton = (Button) findViewById(R.id.female_radio);
        maleRadioButton.setEnabled(true);
        femaleRadioButton.setEnabled(true);
    }

    /*
   ** Enable the save button and all text to be editable
    */
    protected void enableEditProfile() {

        Button cancelEditButton = (Button) findViewById(R.id.cancel_edit_button);
        cancelEditButton.setVisibility(View.VISIBLE);

        Button editProfileButton = (Button) findViewById(R.id.edit_profile_button);
        editProfileButton.setVisibility(View.GONE);

        enableGenderButtons();

        TextView nameText = (TextView) findViewById(R.id.edit_name);
        TextView surnameText = (TextView) findViewById(R.id.edit_surname);
        TextView emailText = (TextView) findViewById(R.id.edit_email);
        TextView hiddenEditText = (TextView) findViewById(R.id.hidden_edit_text);
        Drawable defaultBackground = hiddenEditText.getBackground();

        // Enable the edit text for the email address. Apply the default style so
        // it looks like it is editable
        emailText.setClickable(true);
        emailText.setCursorVisible(true);
        emailText.setFocusable(true);
        emailText.setFocusableInTouchMode(true);
        emailText.setBackground(defaultBackground);

        // Unhide the first name field
        nameText.setClickable(true);
        nameText.setCursorVisible(true);
        nameText.setFocusable(true);
        nameText.setFocusableInTouchMode(true);
        nameText.setBackground(defaultBackground);

        // Unhide the surname field
        surnameText.setClickable(true);
        surnameText.setCursorVisible(true);
        surnameText.setFocusable(true);
        surnameText.setFocusableInTouchMode(true);
        surnameText.setBackground(defaultBackground);

        // Unhide the save button
        Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setVisibility(View.VISIBLE);

        // Unhide the take photo button
        Button captureButton = (Button) findViewById(R.id.btnCapturePicture);
        captureButton.setVisibility(View.VISIBLE);

    }

    /*
    ** Save the new profile data
     */
    protected void saveData() {

        Button captureButton = (Button) findViewById(R.id.btnCapturePicture);
        captureButton.setVisibility(View.GONE);

        Button cancelEditButton = (Button) findViewById(R.id.cancel_edit_button);
        cancelEditButton.setVisibility(View.GONE);

        disableGenderButtons();

        Button editProfileButton = (Button) findViewById(R.id.edit_profile_button);
        editProfileButton.setVisibility(View.VISIBLE);

        disableNameEditText();
        disableEmailEditText();

        Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setVisibility(View.GONE);

        EditText emailEditText = (EditText) findViewById(R.id.edit_email);
        EditText firstNameEditText = (EditText) findViewById(R.id.edit_name);
        EditText surnameEditText = (EditText) findViewById(R.id.edit_surname);
        ImageView profileImageView = (ImageView) findViewById(R.id.imgPreview);
        Bitmap profileImageBitmap = ((BitmapDrawable) profileImageView.getDrawable()).getBitmap();

        RadioButton femaleRadioButton = (RadioButton) findViewById(R.id.female_radio);

        String userId = SaveSharedPreference.getUserDetails(this).getUserId();
        String userEmail = emailEditText.getText().toString();
        String firstname = firstNameEditText.getText().toString();
        String surname = surnameEditText.getText().toString();
        String profileImg = encodeBase64(profileImageBitmap);


        // Set the gender to m by default. If female radio button is checked then set it to f
        String gender = "m";

        if( femaleRadioButton.isChecked() == true )
        {
            gender = "f";
        }



        // Save the new profile data
        SaveDatatoDBTask saveDatatoDTask = new SaveDatatoDBTask(userId,
                userEmail,
                firstname,
                surname,
                profileImg,
                gender);
        // Execute the Save in an Async Task
        saveDatatoDTask.execute();


    }

    /*
    ** Cancel editing the profile and repopulate all textviews with User's details from User object
    */
    protected void cancelEditProfile() {
        // Repopulate the Text views with Data from User object
        populateData();

        // Make every field not editable
        disableEmailEditText();
        disableNameEditText();

        hideSaveButton();

        Button cancelEditButton = (Button) findViewById(R.id.cancel_edit_button);
        cancelEditButton.setVisibility(View.GONE);

        disableGenderButtons();

        Button editProfileButton = (Button) findViewById(R.id.edit_profile_button);
        editProfileButton.setVisibility(View.VISIBLE);

        Button captureButton = (Button) findViewById(R.id.btnCapturePicture);
        captureButton.setVisibility(View.GONE);
    }

    /**
     * Checking device has camera hardware or not
     */
    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * Capturing Camera Image will launch camera app request image capture
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on scren orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }


    /**
     * Receiving activity result method will be called after closing the camera
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image
                // display it in image view
                previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "You decided to not take a photo", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture photo", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /**
     * Display image from a path to ImageView
     */
    private void previewCapturedImage() {
        try {


            imgPreview.setVisibility(View.VISIBLE);

            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                    options);

            imgPreview.setImageBitmap(bitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
    /*
    ** Encode the bitmap image into a string
     */
    public static String encodeBase64(Bitmap bitmap) {

        ByteArrayOutputStream baostream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, baostream);
        byte[] imageBytes = baostream.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        return (encodedImage);
    }

    /*
    ** Decode the string back into a bitmap image
     */
    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    /*
    ** Async Task that will save the data to the database
    */
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

            if (DatabaseHelper.updateUser(id,
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
            if (success) {
                Toast.makeText(getApplicationContext(),
                        "Changes saved!", Toast.LENGTH_SHORT)
                        .show();
            } else {
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

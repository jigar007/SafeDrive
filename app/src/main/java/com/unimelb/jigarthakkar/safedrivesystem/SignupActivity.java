package com.unimelb.jigarthakkar.safedrivesystem;

/**
 * Created by Tang on 9/28/17.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    Button sign_up;
    Button sign_cancel;

    private EditText User_name;
    private EditText User_email;
    private EditText password;

    private ImageView backgroundRotate;




    String name;
    String email;
    String pwd;

    /**
     * Mobile Service Client reference
     */
    private MobileServiceClient mClient;

    /**
     * Mobile Service Table used to access data
     */
    private MobileServiceTable<User> mUser;

    /**
     * Progress spinner to use for table operations
     */
    private ProgressBar mProgressBar;

    private SharedPreferences shared;
    private Editor editor;

    /**
     * Initializes the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        sign_up = (Button) findViewById(R.id.sign_signup);
        sign_cancel = (Button) findViewById(R.id.singin_cancel);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar_s);

        shared = getSharedPreferences("userdata", Activity.MODE_PRIVATE);
        editor = shared.edit();
        /*
        backgroundRotate = (ImageView)findViewById(R.id.id_back);
        //backgroundRotate.setScaleType(ImageView.ScaleType.FIT_XY);
        Animation animation = AnimationUtils.loadAnimation(SignupActivity.this, R.anim.background);
        animation.setFillAfter(true);
        backgroundRotate.startAnimation(animation);
        */
        RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.signup);
        AnimationDrawable animationDrawable = (AnimationDrawable) relativeLayout.getBackground();
        animationDrawable.setEnterFadeDuration(1250);
        animationDrawable.setExitFadeDuration(2500);

        animationDrawable.start();

        try {
            // Create the Mobile Service Client instance
            // Use the provided Mobile Service URL and key
            mClient = new MobileServiceClient(
                    "https://lengr.azurewebsites.net",
                    this);

            // Get the Mobile Service Table instance to use
            mUser = mClient.getTable(User.class);

        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. " +
                    "Verify the URL"), "Error");
        }

        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Make sure all EditText filled out
                // and valid
                if(EditTextValid()) {
                    // Bind entered data to item
                    // and sent to mobile service table User
                    insertItem();
                }
            }
        });

        sign_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginSignupActivity.class);
                intent.putExtra("finish", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    public void insertItem() {

        // Start progress bar
        mProgressBar.setVisibility(ProgressBar.VISIBLE);

        // Create a new item
        final User item = new User();

        name = User_name.getText().toString();
        email = User_email.getText().toString();
        pwd = password.getText().toString();

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                try {

                    final MobileServiceList<User> result =
                            mUser.where().field("email").eq(email).execute().get();

                    Log.d("try", "got the result");

                    if(result.size() == 0) {

                        // Add value to item
                        item.setText(name);
                        item.setEmail(email);
                        item.setPassword(pwd);

                        // Insert new item to User Table
                        mUser.insert(item).get();

                        runOnUiThread(new Runnable() {

                            public void run() {

                                Toast.makeText(getApplicationContext(), "SUCCESS", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("finish", true);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });

                        // Save data
                        editor.putString("name", name);
                        editor.putString("email", email);
                        editor.putBoolean("isLogin", true);
                        editor.commit();

                    } else {

                        runOnUiThread(new Runnable() {

                            public void run() {

                                Toast toast = Toast.makeText(getApplicationContext(), "Email is Already Taken", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                            }
                        });
                    }
                } catch (Exception exception) {
                    createAndShowDialog(exception, "Error");
                }
                return null;
            }
        }.execute();
    }

    public Boolean EditTextValid() {

        // Make sure all EditText filled out
        // Make sure all EditText valid
        return isNameValid() && isEmailValid() && isPwdValid();
    }

    public Boolean isNameValid() {
        User_name = (EditText) findViewById(R.id.Name);

        if(User_name.getText().toString().trim().equals("")) {
            User_name.setError("Name is required!");
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            return false;
        }

        if(User_name.getText().toString().trim().length() > 25) {
            User_name.setError("Name is too long!");
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            return false;
        }
        return true;
    }

    public Boolean isEmailValid() {
        User_email = (EditText) findViewById(R.id.edit_email);

        if(User_email.getText().toString().trim().equals("")) {
            User_email.setError("Email is required!");
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            return false;
        }

        if(!checkEmail(User_email.getText().toString().trim())) {
            User_email.setError("Invalid Email!");
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            return false;
        }
        return true;
    }

    // Check if email is valid
    private static boolean checkEmail(String email){
        String RULE_EMAIL =
                "^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$";
        Pattern p = Pattern.compile(RULE_EMAIL);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    public Boolean isPwdValid() {
        password = (EditText) findViewById(R.id.edit_password);

        if(password.getText().toString().trim().equals("")) {
            password.setError("Password is required!");
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            return false;
        }

        if(password.getText().toString().trim().length() > 0
                && password.getText().toString().trim().length() < 6) {
            password.setError("Password is too short!");
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            return false;
        }
        return true;
    }

    /**
     * Creates a dialog and shows it
     *
     * @param exception
     *            The exception to show in the dialog
     * @param title
     *            The dialog title
     */
    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if(exception.getCause() != null){
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }

    /**
     * Creates a dialog and shows it
     *
     * @param message
     *            The dialog message
     * @param title
     *            The dialog title
     */
    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

}

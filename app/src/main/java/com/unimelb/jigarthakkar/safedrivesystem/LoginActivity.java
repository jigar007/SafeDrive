package com.unimelb.jigarthakkar.safedrivesystem;

/**
 * Created by Tang on 9/28/17.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.net.MalformedURLException;

public class LoginActivity extends AppCompatActivity {

    Button bt_login;
    Button bt_cancel;
    EditText lEmail;
    EditText lPass;

    String name;
    String email;
    String pwd;
    String date;

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
    SignInButton button;
    FirebaseAuth mAuth;
    private final static int RC_SIGN_IN = 2;
    GoogleApiClient mGoogleApiClient;
    FirebaseAuth.AuthStateListener mAuthListener;
    private CallbackManager mCallbackManager;
    private static final String TAG = "FacebookLogin";


    /**
     * Initializes the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        bt_login = (Button) findViewById(R.id.btlogin);
        bt_cancel = (Button) findViewById(R.id.cancel);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        shared = getSharedPreferences("userdata", Activity.MODE_PRIVATE);
        editor = shared.edit();

        // firebase
        button = (SignInButton)findViewById(R.id.googleBtn);

        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser()!=null){
                    startActivity(new Intent(LoginActivity.this,MainActivity.class));
                }
            }
        };

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
//
        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this,new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Toast.makeText(LoginActivity.this, "Something is wrong", Toast.LENGTH_SHORT);

            }
        }).addApi(Auth.GOOGLE_SIGN_IN_API,gso).build();

        try {
            // Create the Mobile Service Client instance
            // Use the provided Mobile Service URL and key
            mClient = new MobileServiceClient(
                    "https://lengr.azurewebsites.net",
                    this);

            // Get the Mobile Service Table instance to use
            mUser = mClient.getTable(User.class);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        bt_login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // Make sure all EditText filled out
                if(EditTextComplete()) {
                    // If user has signed up before,
                    // then login allowed
                    validateUser();
                }
            }
        });

        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginSignupActivity.class);
                intent.putExtra("finish", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        // Fb login
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.fbBtn);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("TAG", "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("TAG", "facebook:onCancel");
                // [START_EXCLUDE]
//                updateUI(null);
                // [END_EXCLUDE]
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("TAG", "facebook:onError", error);
                // [START_EXCLUDE]
//                updateUI(null);
                // [END_EXCLUDE]
            }
        });
        // [END initialize_fblogin]
    }

    // [START auth_with_facebook]
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

                    }
                });
    }
    // [END auth_with_facebook]

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Toast.makeText(LoginActivity.this,"Something is wrong",Toast.LENGTH_SHORT);
            }
        }else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("TAG", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    public void validateUser() {

        // Start progress bar
        mProgressBar.setVisibility(ProgressBar.VISIBLE);

        email = lEmail.getText().toString();
        pwd = lPass.getText().toString();
        date = TimeUtil.getTime();

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final MobileServiceList<User> result =
                            mUser.where().field("email").eq(email).and(mUser.where().field("password").eq(pwd)).execute().get();

                    Log.d("try", "got the result");

                    if(result.size() == 1) {

                        for(User item : result) {
                            // Get column data
                            name = item.getText();
                        }

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
                        editor.putString("date", date);
                        editor.putBoolean("isLogin", true);
                        editor.commit();

                    } else {

                        runOnUiThread(new Runnable() {

                            public void run() {

                                Toast toast = Toast.makeText(getApplicationContext(), "Email or Password is Wrong", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                            }
                        });
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Log.d("Error", "catching the error");
                }
                return null;
            }
        }.execute();
    }

    public Boolean EditTextComplete() {

        lEmail = (EditText) findViewById(R.id.log_email);
        lPass = (EditText) findViewById(R.id.log_pwd);

        if(lEmail.getText().toString().trim().equals("")) {
            lEmail.setError("Email is required!");
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            return false;
        }

        if(lPass.getText().toString().trim().equals("")) {
            lPass.setError("Password is required!");
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            return false;
        }
        return true;
    }
}